package id.co.bfi.dmsuploadscheduler.query.action.dctm_rest;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.response.DctmDqlPropertiesResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmDqlResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmUploadResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmUploadVersionResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.QueryConfig;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestService;

@Component
public class DctmRestDocumentAction {

	@Autowired
	private DctmRestConfig dctmRestConfig;

	@Autowired
	private QueryConfig queryConfig;

	@Autowired
	private DctmRestService dctmRestService;

	@Autowired
	private ObjectMapper objectMapper;

	Logger logger = LoggerFactory.getLogger(DctmRestDocumentAction.class);

	public DctmUploadResponse uploadDocument(String properties, String documentByte, String mimeType, String folderId,
			List<String> msg) throws JsonMappingException, JsonProcessingException {
		DctmUploadResponse dctmUploadResponse = null;
		MultiValueMap<String, Object> parameters = getUploadParameters(properties, mimeType, documentByte);
		HttpHeaders headers = getHttpHeaders();
		ResponseEntity<String> responseEntity = dctmRestService
				.getDataFromDql(queryConfig.getContentTypeDql() + "'" + mimeType + "'");
		if (responseEntity.getStatusCodeValue() == 200) {
			var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(), DctmDqlResponse.class);
			String contentType = null;
			if (dctmDqlResponse.getDctmDqlEntriesResponse() != null) {
				contentType = dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle();
				
				responseEntity = dctmRestService.uploadDocument(folderId, contentType, parameters, headers);
				
				if (responseEntity.getStatusCodeValue() == 200 || responseEntity.getStatusCodeValue() == 201) {
					dctmUploadResponse = objectMapper.readValue(responseEntity.getBody().toString(),
							DctmUploadResponse.class);
					msg.add(responseEntity.getStatusCode().getReasonPhrase());
					logger.info("upload document response : " + responseEntity.getStatusCode().getReasonPhrase());
				} else {
					msg.add(responseEntity.getBody().toString());
					logger.info("upload document response : " + responseEntity.getBody().toString());
				}
			}
		} else {
			msg.add(responseEntity.getBody().toString());
		}
		return dctmUploadResponse;
	}

	public DctmUploadVersionResponse uploadVersionDocument(String chronicleId, String properties, String objectType,
			String documentByte, String mimeType, List<String> msg)
			throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		DctmUploadVersionResponse dctmUploadVersionResponse = null;
		ResponseEntity<String> responseEntity = unlockDocument(objectType, chronicleId, msg);
		if (responseEntity.getStatusCodeValue() == 200 || responseEntity.getStatusCodeValue() == 204) {
			responseEntity = dctmRestService.checkoutDocument("/objects/" + chronicleId + "/lock");
			if (responseEntity.getStatusCodeValue() != 200) {
				msg.add(responseEntity.getBody().toString());
			} else {
				MultiValueMap<String, Object> parameters = getUploadParameters(properties, mimeType, documentByte);
				HttpHeaders headers = getHttpHeaders();
				responseEntity = dctmRestService.getDataFromDql(queryConfig.getContentTypeDql() + "'" + mimeType + "'");
				if (responseEntity.getStatusCodeValue() == 200) {
					var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
							DctmDqlResponse.class);
					String contentType = null;
					if (dctmDqlResponse.getDctmDqlEntriesResponse() != null) {
						contentType = dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle();
						
						responseEntity = dctmRestService.uploadVersionDocument(chronicleId, contentType, parameters,
								headers);
						
						if (responseEntity.getStatusCodeValue() == 200 || responseEntity.getStatusCodeValue() == 201) {
							dctmUploadVersionResponse = objectMapper.readValue(responseEntity.getBody().toString(),
									DctmUploadVersionResponse.class);
							msg.add(responseEntity.getStatusCode().getReasonPhrase());
							logger.info("upload version document response : " + responseEntity.getBody().toString());
						} else {
							msg.add(responseEntity.getBody().toString());
							logger.info("upload version document response : " + responseEntity.getBody().toString());
						}
					}
				} else {
					msg.add(responseEntity.getBody().toString());
				}
			}
		}
		return dctmUploadVersionResponse;
	}

	public MultiValueMap<String, Object> getUploadParameters(String properties, String mimeType, String documentByte) {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();

		HttpHeaders metadataHeader = new HttpHeaders();
		metadataHeader.setContentType(MediaType.valueOf(dctmRestConfig.getContentType()));
		parameters.add("metadata", new HttpEntity<>(properties, metadataHeader));

		HttpHeaders binaryHeader = new HttpHeaders();
		binaryHeader.setContentType(MediaType.valueOf(mimeType));
		parameters.add("binary",
				new HttpEntity<>(Base64.getDecoder().decode(documentByte.replaceAll(" ", "+")), binaryHeader));

		return parameters;
	}

	public HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		return headers;
	}

	public ResponseEntity<String> unlockDocument(String objectType, String chronicleId, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		String isDocumentCheckoutDql = queryConfig.getIsDocumentCheckoutDql().replace("objectType", objectType)
				.replace("chronicleId", chronicleId);
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(isDocumentCheckoutDql);
		if (responseEntity.getStatusCodeValue() == 200) {
			var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(), DctmDqlResponse.class);
			String lockOwner = (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
					? dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle()
					: "";
			if (!lockOwner.equals("")) {
				responseEntity = dctmRestService.cancelCheckoutDocument("/objects/" + chronicleId + "/lock");
				if (responseEntity.getStatusCodeValue() != 204)
					msg.add(responseEntity.getBody().toString());
			}
		} else {
			msg.add(responseEntity.getBody().toString());
		}
		return responseEntity;
	}
	
	public String getFullFileSystemPath(String objectId, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		String fullFileSystemPath = null;
		String docbaseParamDql = queryConfig.getDocbaseParamDql();
		ResponseEntity<String> responseEntity = dctmRestService
				.getDataFromDql(docbaseParamDql.replace("parentId", objectId));
		DctmDqlPropertiesResponse dctmDqlPropertiesResponse = null;
		if (responseEntity.getStatusCodeValue() != 200) {
			msg.add(responseEntity.getBody().toString());
		} else {
			var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(), DctmDqlResponse.class);
			dctmDqlPropertiesResponse = dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getDctmDqlContentResponse().getDctmDqlPropertiesResponse();
		}
		if (dctmDqlPropertiesResponse != null) {
			int dataTicket = dctmDqlPropertiesResponse.getDataTicket();
			String dosExtension = dctmDqlPropertiesResponse.getDosExtension();
			String fileSystemPath = dctmDqlPropertiesResponse.getFileSystemPath();
			int docbaseId = dctmDqlPropertiesResponse.getDocbaseId();
			
			String dbId = new String().valueOf(docbaseId);
			while (dbId.length() < 8) {
				dbId = "0" + dbId;
			}
			StringBuilder sb = new StringBuilder(fileSystemPath + "\\" + dbId);
//			String dbId = dctmConfigProp.getProperty("specific_docbase_id");
//			StringBuilder sb = new StringBuilder(fileSystemPath+"\\"+dbId);
			double dataTicketTemp = dataTicket + Math.pow(2.00, 32.00);
			String hexStr = Long.toHexString((long) dataTicketTemp);
			String[] hexStrArr = hexStr.replaceAll("..(?!$)", "$0 ").split(" ");
			if (hexStrArr.length > 0) {
				int count = 1;
				for (int i = 0; i < hexStrArr.length; i++) {
					if (count < hexStrArr.length)
						sb.append("\\" + hexStrArr[i]);
					else
						sb.append(hexStrArr[i]);
				}
				sb.append("." + dosExtension);
				fullFileSystemPath = sb.toString();
			}
		}
		return fullFileSystemPath.toString();
	}

}

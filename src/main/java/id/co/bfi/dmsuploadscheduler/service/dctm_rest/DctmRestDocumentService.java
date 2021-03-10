package id.co.bfi.dmsuploadscheduler.service.dctm_rest;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.response.DctmRestDqlResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmRestResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.QueryConfig;

@Service
public class DctmRestDocumentService {

	@Autowired
	private DctmRestConfig dctmRestConfig;

	@Autowired
	private QueryConfig queryConfig;

	@Autowired
	private DctmRestService dctmRestService;

	@Autowired
	private ObjectMapper objectMapper;

	Logger logger = LoggerFactory.getLogger(DctmRestDocumentService.class);

	public DctmRestResponse uploadDocument(String properties, String documentByte, String mimeType, String folderId,
			List<String> msg) throws JsonMappingException, JsonProcessingException {
		DctmRestResponse dctmRestResponse = null;
		DctmRestDqlResponse dctmRestDqlResponse = null;
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();

		HttpHeaders metadataHeader = new HttpHeaders();
		metadataHeader.setContentType(MediaType.valueOf(dctmRestConfig.getContentType()));
		parameters.add("metadata", new HttpEntity<>(properties, metadataHeader));

		HttpHeaders binaryHeader = new HttpHeaders();
		binaryHeader.setContentType(MediaType.valueOf(mimeType));
		parameters.add("binary",
				new HttpEntity<>(Base64.getDecoder().decode(documentByte.replaceAll(" ", "+")), binaryHeader));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		ResponseEntity<String> responseEntity = dctmRestService
				.getDataFromDql(queryConfig.getContentTypeDql() + "'" + mimeType + "'");
		String contentType = null;
		if (responseEntity.getStatusCodeValue() == 200) {
			dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
					DctmRestDqlResponse.class);
			if (dctmRestDqlResponse.getEntries() != null)
				contentType = dctmRestDqlResponse.getEntries().get(0).get("title").toString();
			if (contentType != null) {
				responseEntity = dctmRestService.uploadDocument(folderId, contentType, parameters, headers);
				if (responseEntity.getStatusCodeValue() == 200 || responseEntity.getStatusCodeValue() == 201) {
					dctmRestResponse = objectMapper.readValue(responseEntity.getBody().toString(),
							DctmRestResponse.class);
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
		return dctmRestResponse;
	}

	public DctmRestResponse uploadVersionDocument(String chronicleId, String properties, String objectType,
			String documentByte, String mimeType, List<String> msg)
			throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		DctmRestDqlResponse dctmRestDqlResponse = null;
		DctmRestResponse dctmRestResponse = null;
		String isDocumentCheckoutDql = queryConfig.getIsDocumentCheckoutDql().replace("objectType", objectType)
				.replace("chronicleId", chronicleId);
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(isDocumentCheckoutDql);
		String lockOwner = "";
		if (responseEntity.getStatusCodeValue() == 200) {
			dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
					DctmRestDqlResponse.class);
			lockOwner = dctmRestDqlResponse.getEntries().get(0).get("title").toString();
			if (!lockOwner.equals("")) {
				responseEntity = dctmRestService.cancelCheckoutDocument("/objects/" + chronicleId + "/lock");
				if (responseEntity.getStatusCodeValue() != 204)
					msg.add(responseEntity.getBody().toString());
			}
		} else {
			msg.add(responseEntity.getBody().toString());
		}
		if (responseEntity.getStatusCodeValue() == 200 || responseEntity.getStatusCodeValue() == 204) {
			responseEntity = dctmRestService.checkoutDocument("/objects/" + chronicleId + "/lock");
			if (responseEntity.getStatusCodeValue() != 200) {
				msg.add(responseEntity.getBody().toString());
			} else {
				MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();

				HttpHeaders metadataHeader = new HttpHeaders();
				metadataHeader.setContentType(MediaType.valueOf(dctmRestConfig.getContentType()));
				parameters.add("metadata", new HttpEntity<>(properties, metadataHeader));

				HttpHeaders binaryHeader = new HttpHeaders();
				binaryHeader.setContentType(MediaType.valueOf(mimeType));
				parameters.add("binary",
						new HttpEntity<>(Base64.getDecoder().decode(documentByte.replaceAll(" ", "+")), binaryHeader));

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);

				String contentType = null;
				responseEntity = dctmRestService.getDataFromDql(queryConfig.getContentTypeDql() + "'" + mimeType + "'");
				if (responseEntity.getStatusCodeValue() == 200) {
					dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
							DctmRestDqlResponse.class);
					contentType = dctmRestDqlResponse.getEntries().get(0).get("title").toString();
					responseEntity = dctmRestService.uploadVersionDocument(chronicleId, contentType, parameters,
							headers);
					if (responseEntity.getStatusCodeValue() == 200 || responseEntity.getStatusCodeValue() == 201) {
						dctmRestResponse = objectMapper.readValue(responseEntity.getBody().toString(),
								DctmRestResponse.class);
						msg.add(responseEntity.getStatusCode().getReasonPhrase());
						logger.info("upload version document response : " + responseEntity.getBody().toString());
					} else {
						msg.add(responseEntity.getBody().toString());
						logger.info("upload version document response : " + responseEntity.getBody().toString());
					}
				} else {
					msg.add(responseEntity.getBody().toString());
				}
			}
		}
		return dctmRestResponse;
	}

	public String getFullFileSystemPath(String objectId, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		String fullFileSystemPath = null;
		DctmRestDqlResponse dctmRestDqlResponse = null;
		Map<String, Object> paramsMap = null;
		String docbaseParamDql = queryConfig.getDocbaseParamDql();
		ResponseEntity<String> responseEntity = dctmRestService
				.getDataFromDql(docbaseParamDql.replace("parentId", objectId));
		if (responseEntity.getStatusCodeValue() != 200) {
			msg.add(responseEntity.getBody().toString());
		} else {
			dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
					DctmRestDqlResponse.class);
			if (dctmRestDqlResponse.getEntries() != null) {
				Map<String, Object> contentMap = (Map<String, Object>) dctmRestDqlResponse.getEntries().get(0)
						.get("content");
				paramsMap = (Map<String, Object>) contentMap.get("properties");
			}
		}
		if (paramsMap != null) {
			int dataTicket = (int) paramsMap.get("data_ticket");
			String dosExtension = paramsMap.get("dos_extension").toString();
			String fileSystemPath = paramsMap.get("file_system_path").toString();
			int docbaseId = (int) paramsMap.get("r_docbase_id");
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

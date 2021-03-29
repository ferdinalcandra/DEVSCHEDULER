package id.co.bfi.dmsuploadscheduler.query.action.dctm_rest;

import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
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

	private Logger logger = LoggerFactory.getLogger(DctmRestDocumentAction.class);
	private Marker marker;

	public DctmUploadResponse uploadDocument(String properties, String documentByte, String mimeType, String folderId,
			List<String> msg) throws JsonProcessingException {
		DctmUploadResponse dctmUploadResponse = null;
		MultiValueMap<String, Object> parameters = getUploadParameters(properties, mimeType, documentByte);
		HttpHeaders headers = getHttpHeaders();
		ResponseEntity<String> responseEntity = dctmRestService
				.getDataFromDql(queryConfig.getContentTypeDql() + "'" + mimeType + "'");
		var responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
		var dctmDqlResponse = objectMapper.readValue(responseBody, DctmDqlResponse.class);
		String contentType = (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
				? dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle()
				: null;

		responseEntity = dctmRestService.uploadDocument(folderId, contentType, parameters, headers);
		responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
		logger.info(marker, "upload document response : {}", responseBody);
		if (responseEntity.getStatusCodeValue() == 201) {
			dctmUploadResponse = objectMapper.readValue(responseBody, DctmUploadResponse.class);
			msg.add(responseEntity.getStatusCode().getReasonPhrase());
		} else {
			msg.add(responseBody);
		}
		return dctmUploadResponse;
	}

	public DctmUploadVersionResponse uploadVersionDocument(String chronicleId, String properties, String objectType,
			String documentByte, String mimeType, List<String> msg) throws JsonProcessingException {
		DctmUploadVersionResponse dctmUploadVersionResponse = null;

		unlockDocument(objectType, chronicleId);
		dctmRestService.checkoutDocument("/objects/" + chronicleId + "/lock");

		MultiValueMap<String, Object> parameters = getUploadParameters(properties, mimeType, documentByte);
		HttpHeaders headers = getHttpHeaders();

		var responseEntity = dctmRestService.getDataFromDql(queryConfig.getContentTypeDql() + "'" + mimeType + "'");
		var responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
		var dctmDqlResponse = objectMapper.readValue(responseBody, DctmDqlResponse.class);

		String contentType = (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
				? dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle()
				: null;

		responseEntity = dctmRestService.uploadVersionDocument(chronicleId, contentType, parameters, headers);
		responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
		logger.info(marker, "upload version document response : {}", responseBody);
		if (responseEntity.getStatusCodeValue() == 201) {
			dctmUploadVersionResponse = objectMapper.readValue(responseBody, DctmUploadVersionResponse.class);
			msg.add(responseEntity.getStatusCode().getReasonPhrase());
		} else {
			msg.add(responseBody);
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
				new HttpEntity<>(Base64.getDecoder().decode(documentByte.replace(" ", "+")), binaryHeader));
		return parameters;
	}

	public HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		return headers;
	}

	public void unlockDocument(String objectType, String chronicleId) throws JsonProcessingException {
		String isDocumentCheckoutDql = queryConfig.getIsDocumentCheckoutDql().replace("objectType", objectType)
				.replace("chronicleId", chronicleId);
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(isDocumentCheckoutDql);
		var responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
		var dctmDqlResponse = objectMapper.readValue(responseBody, DctmDqlResponse.class);
		String lockOwner = (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
				? dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle()
				: "";
		if (!lockOwner.equals("")) {
			dctmRestService.cancelCheckoutDocument("/objects/" + chronicleId + "/lock");
		}
	}

	public String getFullFileSystemPath(String objectId) throws JsonProcessingException {
		String fullFileSystemPath = null;
		String docbaseParamDql = queryConfig.getDocbaseParamDql();
		ResponseEntity<String> responseEntity = dctmRestService
				.getDataFromDql(docbaseParamDql.replace("parentId", objectId));
		var responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
		var dctmDqlResponse = objectMapper.readValue(responseBody, DctmDqlResponse.class);
		DctmDqlPropertiesResponse dctmDqlPropertiesResponse = dctmDqlResponse.getDctmDqlEntriesResponse().get(0)
				.getDctmDqlContentResponse().getDctmDqlPropertiesResponse();

		int dataTicket = dctmDqlPropertiesResponse.getDataTicket();
		String dosExtension = dctmDqlPropertiesResponse.getDosExtension();
		String fileSystemPath = dctmDqlPropertiesResponse.getFileSystemPath();
		int docbaseId = dctmDqlPropertiesResponse.getDocbaseId();

		String dbId = String.valueOf(docbaseId);
		while (dbId.length() < 8) {
			dbId = "0" + dbId;
		}
		StringBuilder sb = new StringBuilder(fileSystemPath + "\\" + dbId);
		double dataTicketTemp = dataTicket + Math.pow(2.00, 32.00);
		String hexStr = Long.toHexString((long) dataTicketTemp);
		String[] hexStrArr = hexStr.replaceAll("..(?!$)", "$0 ").split(" ");
		if (hexStrArr.length > 0) {
			int count = 1;
			for (int i = 0; i < hexStrArr.length; i++) {
				sb = (count < hexStrArr.length) ? sb.append("\\" + hexStrArr[i]) : sb.append(hexStrArr[i]);
			}
			sb.append("." + dosExtension);
			fullFileSystemPath = sb.toString();
		}
		return fullFileSystemPath;
	}

}

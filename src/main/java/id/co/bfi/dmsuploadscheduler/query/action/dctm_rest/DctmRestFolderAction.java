package id.co.bfi.dmsuploadscheduler.query.action.dctm_rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmCreateObjectPropertiesRequest;
import id.co.bfi.dmsuploadscheduler.api.request.DctmCreateObjectRequest;
import id.co.bfi.dmsuploadscheduler.api.request.DctmUploadRequest;
import id.co.bfi.dmsuploadscheduler.api.response.DctmCreateObjectResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmDqlResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.QueryConfig;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeEntity;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestService;

@Component
public class DctmRestFolderAction {

	@Autowired
	private DctmRestService dctmRestService;

	@Autowired
	private DctmRestConfig dctmRestConfig;

	@Autowired
	private QueryConfig queryConfig;

	@Autowired
	private ObjectMapper objectMapper;

	public String createFolderByPath(String path) throws JsonProcessingException {
		String cabinetId = createCabinet(path);
		return createFolder(path, cabinetId);
	}

	public String createCabinet(String path) throws JsonProcessingException {
		String cabinetId = null;
		String[] folders = path.split("/");
		String checkedPath = "/" + folders[1];
		String cabinetCheckDql = queryConfig.getCabinetCheckDql().replace("objectName", folders[1])
				.replace("folderPath", checkedPath);
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(cabinetCheckDql);
		var responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
		var dctmDqlResponse = objectMapper.readValue(responseBody, DctmDqlResponse.class);
		String existingCabinetId = (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
				? dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle()
				: null;
		if (existingCabinetId == null) {
			responseEntity = createObject(folders[1], "dm_cabinet", "/cabinets");
			responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
			var dctmCreateObjectResponse = objectMapper.readValue(responseBody, DctmCreateObjectResponse.class);
			cabinetId = dctmCreateObjectResponse.getDctmCreateObjectPropertiesResponse().getObjectId();
		} else {
			cabinetId = existingCabinetId;
		}
		return cabinetId;
	}

	public String createFolder(String path, String cabinetId) throws JsonProcessingException {
		String folderId = null;
		String[] folders = path.split("/");
		String checkedPath = "/" + folders[1];
		for (int i = 2; i < folders.length; ++i) {
			checkedPath = checkedPath + "/" + folders[i];
			String folderCheckDql = queryConfig.getFolderCheckDql().replace("objectName", folders[i])
					.replace("folderPath", checkedPath);
			ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(folderCheckDql);
			var responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
			var dctmDqlResponse = objectMapper.readValue(responseBody, DctmDqlResponse.class);
			String existingFolderId = (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
					? dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle()
					: null;
			if (existingFolderId == null) {
				responseEntity = createObject(folders[i], "dm_folder", "/folders/" + cabinetId + "/objects");
				responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
				var dctmCreateObjectResponse = objectMapper.readValue(responseBody, DctmCreateObjectResponse.class);
				folderId = dctmCreateObjectResponse.getDctmCreateObjectPropertiesResponse().getObjectId();
				cabinetId = folderId;
			} else {
				folderId = existingFolderId;
				cabinetId = existingFolderId;
			}
		}
		return folderId;
	}

	public ResponseEntity<String> createObject(String objectName, String objectType, String url) {
		DctmCreateObjectPropertiesRequest dctmCreateObjectPropertiesRequest = new DctmCreateObjectPropertiesRequest();
		dctmCreateObjectPropertiesRequest.setObjectName(objectName);
		dctmCreateObjectPropertiesRequest.setObjectType(objectType);

		var dctmCreateObjectRequest = new DctmCreateObjectRequest();
		dctmCreateObjectRequest.setDctmCreateObjectPropertiesRequest(dctmCreateObjectPropertiesRequest);

		return dctmRestService.createObject(url, dctmCreateObjectRequest);
	}

	public String generateFolderPath(DctmUploadRequest dctmUploadRequest, DocumentTypeEntity dmsDocType) {
		String folderPath = null;
		String customerId = null;
		if (dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmCustomerId() != null)
			customerId = dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmCustomerId();

		if (dmsDocType.getFlagUploadType().equalsIgnoreCase("KONSUMEN")) {
			folderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/" + customerId + "/"
					+ dmsDocType.getDocType();
		} else if (dmsDocType.getFlagUploadType().equalsIgnoreCase("INTERNAL")
				|| dmsDocType.getFlagUploadType().equalsIgnoreCase("COMPANY")) {
			folderPath = "/" + dctmRestConfig.getInternalDocCabinetName() + "/" + dmsDocType.getDocTypeDesc() + "/"
					+ dctmRestConfig.getDmsActiveDocFolderName();
		} else {
			String branchId = null;
			String leadsId = null;
			if (dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmBranchId() != null)
				branchId = dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmBranchId();
			if (dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmLeadsId() != null) {
				leadsId = dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmLeadsId();
			}
			folderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/" + customerId + "/" + branchId + "/"
					+ leadsId + "/" + dmsDocType.getDocType();
		}
		return folderPath;
	}

}

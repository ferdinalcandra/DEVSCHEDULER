package id.co.bfi.dmsuploadscheduler.query.action.dctm_rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

	public String createFolderByPath(String path, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		String folderId = null;
		DctmCreateObjectResponse dctmCreateObjectResponse = null;
		String[] folders = path.split("/");
		String checkedPath = "/" + folders[1];
		String cabinetId = null;
		String existingCabinetId = null;
		String cabinetCheckDql = queryConfig.getCabinetCheckDql().replace("objectName", folders[1])
				.replace("folderPath", checkedPath);
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(cabinetCheckDql);
		if (responseEntity.getStatusCodeValue() == 200) {
			var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(), DctmDqlResponse.class);
			if (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
				existingCabinetId = dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle();
			
			if (existingCabinetId == null) {
				responseEntity = createObject(folders[1], "dm_cabinet", "/cabinets");
				if (responseEntity.getStatusCodeValue() == 200 || responseEntity.getStatusCodeValue() == 201) {
					dctmCreateObjectResponse = objectMapper.readValue(responseEntity.getBody().toString(),
							DctmCreateObjectResponse.class);
					cabinetId = dctmCreateObjectResponse.getDctmCreateObjectPropertiesResponse().getObjectId();
				} else {
					msg.add(responseEntity.getBody().toString());
				}
			} else {
				cabinetId = existingCabinetId;
			}

			for (int i = 2; i < folders.length; ++i) {
				checkedPath = checkedPath + "/" + folders[i];
				String folderCheckDql = queryConfig.getFolderCheckDql().replace("objectName", folders[i])
						.replace("folderPath", checkedPath);
				responseEntity = dctmRestService.getDataFromDql(folderCheckDql);
				if (responseEntity.getStatusCodeValue() != 200) {
					msg.add(responseEntity.getBody().toString());
				} else {
					String existingFolderId = null;
					dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(), DctmDqlResponse.class);
					if (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
						existingFolderId = dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle();
					
					if (existingFolderId == null) {
						responseEntity = createObject(folders[i], "dm_folder",
								"/folders/" + cabinetId + "/objects");
						if (responseEntity.getStatusCodeValue() == 200
								|| responseEntity.getStatusCodeValue() == 201) {
							dctmCreateObjectResponse = objectMapper.readValue(responseEntity.getBody().toString(),
									DctmCreateObjectResponse.class);
							folderId = dctmCreateObjectResponse.getDctmCreateObjectPropertiesResponse().getObjectId();
							cabinetId = folderId;
						} else {
							msg.add(responseEntity.getBody().toString());
						}
					} else {
						folderId = existingFolderId;
						cabinetId = existingFolderId;
					}
				}
			}
		}
		return folderId;
	}
	
	public ResponseEntity<String> createObject(String objectName, String objectType, String url)
			throws JsonMappingException, JsonProcessingException {
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

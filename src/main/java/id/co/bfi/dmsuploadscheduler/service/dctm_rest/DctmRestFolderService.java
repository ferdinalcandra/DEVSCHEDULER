package id.co.bfi.dmsuploadscheduler.service.dctm_rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmRestRequest;
import id.co.bfi.dmsuploadscheduler.api.response.DctmRestDqlResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmRestResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.QueryConfig;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeMasterEntity;

@Service
public class DctmRestFolderService {

	@Autowired
	private DctmRestService dctmRestService;

	@Autowired
	private DctmRestConfig dctmRestConfig;

	@Autowired
	private QueryConfig queryConfig;

	private ObjectMapper objectMapper = new ObjectMapper();

	public String createFolderByPath(String path, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		String folderId = null;
		if (path.substring(0, 1).equals("/")) {

			DctmRestResponse dctmRestResponse = null;
			DctmRestDqlResponse dctmRestDqlResponse = null;
			String[] folders = path.split("/");
			String checkedPath = "/" + folders[1];
			String cabinetId = null;
			String existingCabinetId = null;
			String cabinetCheckDql = queryConfig.getCabinetCheckDql().replace("objectName", folders[1])
					.replace("folderPath", checkedPath);
			ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(cabinetCheckDql);
			if (responseEntity.getStatusCodeValue() == 200) {
				dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
						DctmRestDqlResponse.class);

				if (dctmRestDqlResponse.getEntries() != null)
					existingCabinetId = dctmRestDqlResponse.getEntries().get(0).get("title").toString();

				if (existingCabinetId == null) {
					responseEntity = createObject(folders[1], "dm_cabinet", "/cabinets");
					if (responseEntity.getStatusCodeValue() == 200 || responseEntity.getStatusCodeValue() == 201) {
						dctmRestResponse = objectMapper.readValue(responseEntity.getBody().toString(),
								DctmRestResponse.class);
						cabinetId = dctmRestResponse.getProperties().get("r_object_id").toString();
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
						dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
								DctmRestDqlResponse.class);
						if (dctmRestDqlResponse.getEntries() != null)
							existingFolderId = dctmRestDqlResponse.getEntries().get(0).get("title").toString();
						if (existingFolderId == null) {
							responseEntity = createObject(folders[i], "dm_folder",
									"/folders/" + cabinetId + "/objects");
							if (responseEntity.getStatusCodeValue() == 200
									|| responseEntity.getStatusCodeValue() == 201) {
								dctmRestResponse = objectMapper.readValue(responseEntity.getBody().toString(),
										DctmRestResponse.class);
								folderId = dctmRestResponse.getProperties().get("r_object_id").toString();
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
		}
		return folderId;
	}

	public ResponseEntity<String> createObject(String objectName, String objectType, String url)
			throws JsonMappingException, JsonProcessingException {
		DctmRestRequest dctmRestRequest = new DctmRestRequest();
		Map<String, Object> propertiesMap = new HashMap<>();
		propertiesMap.put("object_name", objectName);
		propertiesMap.put("r_object_type", objectType);
		dctmRestRequest.setProperties(propertiesMap);

		return dctmRestService.createObject(url, objectMapper.writeValueAsString(dctmRestRequest));
	}

	public String generateFolderPath(DctmRestRequest dctmRestRequest, DocumentTypeMasterEntity dmsDocType) {
		String folderPath = null;
		String customerId = null;
		if (dctmRestRequest.getProperties().containsKey("dctm_customer_id"))
			customerId = dctmRestRequest.getProperties().get("dctm_customer_id").toString().trim();
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
			if (dctmRestRequest.getProperties().containsKey("dctm_branch_id"))
				branchId = dctmRestRequest.getProperties().get("dctm_branch_id").toString().trim();
			if (dctmRestRequest.getProperties().containsKey("dctm_leads_id")) {
				leadsId = dctmRestRequest.getProperties().get("dctm_leads_id").toString().trim();
			}
			folderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/" + customerId + "/" + branchId + "/"
					+ leadsId + "/" + dmsDocType.getDocType();
		}
		return folderPath;
	}

}

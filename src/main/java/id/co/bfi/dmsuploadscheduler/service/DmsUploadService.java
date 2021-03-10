package id.co.bfi.dmsuploadscheduler.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmRestRequest;
import id.co.bfi.dmsuploadscheduler.api.response.DctmRestDqlResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.QueryConfig;
import id.co.bfi.dmsuploadscheduler.entity.DctmDocumentsEntity;
import id.co.bfi.dmsuploadscheduler.entity.DctmUploadDocumentHistoryEntity;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeMasterEntity;
import id.co.bfi.dmsuploadscheduler.repository.DctmDocumentsRepository;
import id.co.bfi.dmsuploadscheduler.repository.DctmUploadDocumentHistoryRepository;
import id.co.bfi.dmsuploadscheduler.repository.DocumentTypeMasterRepository;
import id.co.bfi.dmsuploadscheduler.service.dctm_rest.DctmRestDocumentService;
import id.co.bfi.dmsuploadscheduler.service.dctm_rest.DctmRestFolderService;
import id.co.bfi.dmsuploadscheduler.service.dctm_rest.DctmRestService;
import id.co.bfi.dmsuploadscheduler.service.share_folder.ShareFolderService;

@Service
public class DmsUploadService {

	@Autowired
	private DctmDocumentsRepository documentsRepository;

	@Autowired
	private DctmUploadDocumentHistoryRepository uploadDocHistoryRepository;

	@Autowired
	private DocumentTypeMasterRepository dmsDocTypeRepository;

	@Autowired
	private ShareFolderService shareFolderService;

	@Autowired
	private DctmRestService dctmRestService;

	@Autowired
	private DctmRestDocumentService dctmDocumentService;

	@Autowired
	private DctmRestFolderService dctmFolderService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DctmDocumentsService dctmDocumentsService;

	@Autowired
	private QueryConfig queryConfig;

	@Autowired
	private DctmUploadDocumentHistoryService dctmUploadDocumentHistoryService;

	public void processDmsReqUpload() throws SQLException, Exception {
		List<DctmDocumentsEntity> documentsList = dctmDocumentsService.getDctmDocumentsList();
		if (documentsList.size() > 0) {
			for (int i = 0; i < documentsList.size(); i++) {
				String dctmDocumentsId = documentsList.get(i).getDctmDocumentsId();
				List<DctmUploadDocumentHistoryEntity> uploadDocHistoryList = dctmUploadDocumentHistoryService
						.getDctmUploadDocumentHistoryList(dctmDocumentsId);
				if (uploadDocHistoryList.size() > 0) {
					boolean success = true;
					for (int j = 0; j < uploadDocHistoryList.size(); j++) {
						if (success) {
							String objectId = null;
							List<String> msg = new ArrayList<>();

							String properties = uploadDocHistoryList.get(j).getMetadataDctm();
							var dctmRestRequest = objectMapper.readValue(properties, DctmRestRequest.class);
							int docTypeId = (int) dctmRestRequest.getProperties().get("dctm_doc_type_id");
							DocumentTypeMasterEntity dmsDocType = dmsDocTypeRepository
									.findBySqDocumentTypeMst(new Long(docTypeId));

							objectId = doUpload(documentsList, i, uploadDocHistoryList, j, msg, objectId,
									dctmRestRequest, dmsDocType);
							if (objectId == null && dmsDocType.getIsAllowVersioning() == 1)
								success = false;
						}
					}
				}
			}
		}
	}

	public String doUpload(List<DctmDocumentsEntity> documentsList, int i,
			List<DctmUploadDocumentHistoryEntity> uploadDocHistoryList, int j, List<String> msg, String objectId,
			DctmRestRequest dctmRestRequest, DocumentTypeMasterEntity dmsDocType) throws Exception {
		String folderId = null;
		String chronicleId = null;
		String dctmDocNumber = dctmRestRequest.getProperties().get("dctm_doc_number").toString().trim();
		String objectType = dctmRestRequest.getProperties().get("r_object_type").toString().trim();

		ResponseEntity<String> responseEntity = null;
		DctmRestDqlResponse dctmRestDqlResponse = null;
		if (objectType != null && dctmDocNumber != null) {
			String chronicleIdCheckDql = queryConfig.getChronicleIdCheckDql().replace("objectType", objectType)
					.replace("dctmDocNumber", dctmDocNumber.toLowerCase());
			responseEntity = dctmRestService.getDataFromDql(chronicleIdCheckDql);
			if (responseEntity.getStatusCodeValue() == 200) {
				dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
						DctmRestDqlResponse.class);
				if (dctmRestDqlResponse.getEntries() != null)
					chronicleId = dctmRestDqlResponse.getEntries().get(0).get("title").toString();
			} else {
				msg.add(responseEntity.getBody().toString());
			}
		}

		if (responseEntity.getStatusCodeValue() == 200) {
			String pathFileName = uploadDocHistoryList.get(j).getPathFileName();
			byte[] byteArrayDoc = shareFolderService.getFileOverSharedFolder(pathFileName, msg).toByteArray();
			String documentByte = Base64.getEncoder().encodeToString(byteArrayDoc);
			String mimeType = uploadDocHistoryList.get(j).getDmsMimeType();
			boolean fileIsValid = shareFolderService.validateFile(byteArrayDoc, mimeType, pathFileName, msg);
			if (chronicleId != null && dmsDocType.getIsAllowVersioning() == 1 && byteArrayDoc.length > 0
					&& fileIsValid) {
				// versioning
				var dctmRestResponse = dctmDocumentService.uploadVersionDocument(chronicleId,
						objectMapper.writeValueAsString(dctmRestRequest), objectType, documentByte, mimeType, msg);
				if (dctmRestResponse != null)
					objectId = dctmRestResponse.getProperties().get("r_object_id").toString();
			} else if (byteArrayDoc.length > 0 && fileIsValid) {
				// new doc or multiple doc
				folderId = dctmFolderService
						.createFolderByPath(dctmFolderService.generateFolderPath(dctmRestRequest, dmsDocType), msg);
				var dctmRestResponse = dctmDocumentService.uploadDocument(
						objectMapper.writeValueAsString(dctmRestRequest), documentByte, mimeType, folderId, msg);
				if (dctmRestResponse != null)
					objectId = dctmRestResponse.getProperties().get("r_object_id").toString();
			}
		}
		updatingData(objectId, uploadDocHistoryList, j, documentsList, i, msg);
		return objectId;
	}

	public void updatingData(String objectId, List<DctmUploadDocumentHistoryEntity> uploadDocHistoryList, int j,
			List<DctmDocumentsEntity> documentsList, int i, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		Date now = new Date();
		String fullSystemPath = null;

		if (objectId != null) {
			uploadDocHistoryList.get(j).setDctmId(objectId);
			uploadDocHistoryList.get(j).setStatusUpload("done");
			uploadDocHistoryList.get(j).setMsg(msg.toString());
			fullSystemPath = dctmDocumentService.getFullFileSystemPath(objectId, msg);
			uploadDocHistoryList.get(j).setPathFileName(Base64.getEncoder().encodeToString(fullSystemPath.getBytes()));

			documentsList.get(i).setPathFileName(Base64.getEncoder().encodeToString(fullSystemPath.getBytes()));
			documentsList.get(i).setDctmId(objectId);
			
		} else {
			uploadDocHistoryList.get(j).setStatusUpload("failed");
			uploadDocHistoryList.get(j).setMsg(msg.toString());
		}
		documentsList.get(i).setLastModifiedDate(now);
		documentsList.get(i).setLastModifiedBy("dms scheduler");
		documentsRepository.save(documentsList.get(i));
		
		uploadDocHistoryList.get(j).setLastModifiedDate(now);
		uploadDocHistoryList.get(j).setLastModifiedBy("dms scheduler");
		uploadDocHistoryRepository.save(uploadDocHistoryList.get(j));
	}

}

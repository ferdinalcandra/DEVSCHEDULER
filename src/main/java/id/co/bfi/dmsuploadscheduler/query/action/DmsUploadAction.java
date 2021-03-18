package id.co.bfi.dmsuploadscheduler.query.action;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmUploadRequest;
import id.co.bfi.dmsuploadscheduler.api.response.DctmDqlResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.QueryConfig;
import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;
import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeEntity;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestDocumentService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestFolderService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestService;
import id.co.bfi.dmsuploadscheduler.query.service.share_folder.ShareFolderService;
import id.co.bfi.dmsuploadscheduler.repository.DocumentRepository;
import id.co.bfi.dmsuploadscheduler.repository.UploadHistoryRepository;

@Component
public class DmsUploadAction {

	@Autowired
	private QueryConfig queryConfig;

	@Autowired
	private DctmRestService dctmRestService;

	@Autowired
	private ShareFolderService shareFolderService;

	@Autowired
	private DctmRestDocumentService dctmRestDocumentService;

	@Autowired
	private DctmRestFolderService dctmRestFolderService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentRepository documentsRepository;

	@Autowired
	private UploadHistoryRepository uploadHistoryRepository;

	public String doUpload(List<DocumentEntity> documentsList, int i, List<UploadHistoryEntity> uploadHistoryList,
			int j, List<String> msg, String objectId, DctmUploadRequest dctmUploadRequest,
			DocumentTypeEntity dmsDocType, String properties) throws Exception {

		String dctmDocNumber = dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmDocNumber();
		String objectType = dctmUploadRequest.getDctmUploadPropertiesRequest().getObjectType();
		
		String chronicleId = getExistingChronicleId(dctmDocNumber, objectType, msg);

		if (chronicleId != null) {
			String pathFileName = uploadHistoryList.get(j).getPathFileName();
			byte[] byteArrayDoc = shareFolderService.getFileOverSharedFolder(pathFileName, msg).toByteArray();
			String documentByte = Base64.getEncoder().encodeToString(byteArrayDoc);
			String mimeType = uploadHistoryList.get(j).getDmsMimeType();
			boolean fileIsValid = shareFolderService.validateFile(byteArrayDoc, mimeType, pathFileName, msg);
			if (!chronicleId.equals("") && dmsDocType.getIsAllowVersioning() == 1 && byteArrayDoc.length > 0
					&& fileIsValid) {
				// versioning
				var dctmUploadVersionResponse = dctmRestDocumentService.uploadVersionDocument(chronicleId, properties,
						objectType, documentByte, mimeType, msg);
				if (dctmUploadVersionResponse != null)
					objectId = dctmUploadVersionResponse.getDctmUploadVersionPropertiesResponse().getObjectId();
			} else if (byteArrayDoc.length > 0 && fileIsValid) {
				// new doc or multiple doc
				String folderPath = dctmRestFolderService.generateFolderPath(dctmUploadRequest, dmsDocType);
				String folderId = dctmRestFolderService.createFolderByPath(folderPath, msg);
				var dctmUploadResponse = dctmRestDocumentService.uploadDocument(properties, documentByte, mimeType,
						folderId, msg);
				if (dctmUploadResponse != null)
					objectId = dctmUploadResponse.getDctmUploadPropertiesResponse().getObjectId();
			}
		}
		updatingData(objectId, uploadHistoryList, j, documentsList, i, msg);
		return objectId;
	}

	public String getExistingChronicleId(String dctmDocNumber, String objectType, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		String chronicleId = null;
		String chronicleIdCheckDql = queryConfig.getChronicleIdCheckDql().replace("objectType", objectType)
				.replace("dctmDocNumber", dctmDocNumber.toLowerCase());
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(chronicleIdCheckDql);
		if (responseEntity.getStatusCodeValue() == 200) {
			var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(), DctmDqlResponse.class);
			chronicleId = (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
					? dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle()
					: "";
		} else {
			msg.add(responseEntity.getBody().toString());
		}
		return chronicleId;
	}

	public void updatingData(String objectId, List<UploadHistoryEntity> uploadHistoryList, int j,
			List<DocumentEntity> documentsList, int i, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		Date now = new Date();
		String fullSystemPath = null;
		if (objectId != null) {
			uploadHistoryList.get(j).setDctmId(objectId);
			uploadHistoryList.get(j).setStatusUpload("done");
			uploadHistoryList.get(j).setMsg(msg.toString());
			fullSystemPath = dctmRestDocumentService.getFullFileSystemPath(objectId, msg);
			uploadHistoryList.get(j).setPathFileName(Base64.getEncoder().encodeToString(fullSystemPath.getBytes()));

			documentsList.get(i).setPathFileName(Base64.getEncoder().encodeToString(fullSystemPath.getBytes()));
			documentsList.get(i).setDctmId(objectId);
		} else {
			uploadHistoryList.get(j).setStatusUpload("failed");
			uploadHistoryList.get(j).setMsg(msg.toString());
		}
		documentsList.get(i).setLastModifiedDate(now);
		documentsList.get(i).setLastModifiedBy("dms scheduler");
		documentsRepository.save(documentsList.get(i));

		uploadHistoryList.get(j).setLastModifiedDate(now);
		uploadHistoryList.get(j).setLastModifiedBy("dms scheduler");
		uploadHistoryRepository.save(uploadHistoryList.get(j));
	}

}

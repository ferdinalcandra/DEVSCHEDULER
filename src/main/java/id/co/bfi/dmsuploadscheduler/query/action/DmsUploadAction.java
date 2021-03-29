package id.co.bfi.dmsuploadscheduler.query.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DmsDoUploadRequest;
import id.co.bfi.dmsuploadscheduler.api.response.DctmDqlResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.QueryConfig;
import id.co.bfi.dmsuploadscheduler.query.service.DocumentService;
import id.co.bfi.dmsuploadscheduler.query.service.UploadHistoryService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestDocumentService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestFolderService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestService;
import id.co.bfi.dmsuploadscheduler.query.service.share_folder.ShareFolderService;

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
	private DocumentService documentService;

	@Autowired
	private UploadHistoryService uploadHistoryService;

	public String doUpload(DmsDoUploadRequest doUploadRequest) throws IOException {

		List<String> msg = new ArrayList<>();
		String objectId = null;
		String dctmDocNumber = doUploadRequest.getDctmUploadRequest().getDctmUploadPropertiesRequest()
				.getDctmDocNumber();
		String objectType = doUploadRequest.getDctmUploadRequest().getDctmUploadPropertiesRequest().getObjectType();
		String chronicleId = getExistingChronicleId(dctmDocNumber, objectType, msg);
		int uploadHistoryListIndex = doUploadRequest.getUploadHistoryListIndex();

		if (chronicleId != null) {
			String pathFileName = doUploadRequest.getUploadHistoryList().get(uploadHistoryListIndex).getPathFileName();
			byte[] byteArrayDoc = shareFolderService.getFileOverSharedFolder(pathFileName).toByteArray();
			String documentByte = Base64.getEncoder().encodeToString(byteArrayDoc);
			String mimeType = doUploadRequest.getUploadHistoryList().get(uploadHistoryListIndex).getDmsMimeType();
			boolean fileIsValid = shareFolderService.validateFile(byteArrayDoc, mimeType, pathFileName, msg);
			if (!chronicleId.equals("") && doUploadRequest.getDmsDocType().getIsAllowVersioning() == 1
					&& byteArrayDoc.length > 0 && fileIsValid) {
				// versioning
				var dctmUploadVersionResponse = dctmRestDocumentService.uploadVersionDocument(chronicleId,
						doUploadRequest.getProperties(), objectType, documentByte, mimeType, msg);
				if (dctmUploadVersionResponse != null)
					objectId = dctmUploadVersionResponse.getDctmUploadVersionPropertiesResponse().getObjectId();
			} else if (byteArrayDoc.length > 0 && fileIsValid) {
				// new doc or multiple doc
				String folderPath = dctmRestFolderService.generateFolderPath(doUploadRequest.getDctmUploadRequest(),
						doUploadRequest.getDmsDocType());
				String folderId = dctmRestFolderService.createFolderByPath(folderPath);
				var dctmUploadResponse = dctmRestDocumentService.uploadDocument(doUploadRequest.getProperties(),
						documentByte, mimeType, folderId, msg);
				if (dctmUploadResponse != null)
					objectId = dctmUploadResponse.getDctmUploadPropertiesResponse().getObjectId();
			}
		}
		updatingData(objectId, doUploadRequest, msg);
		return objectId;
	}

	public String getExistingChronicleId(String dctmDocNumber, String objectType, List<String> msg)
			throws JsonProcessingException {
		String chronicleId = null;
		String chronicleIdCheckDql = queryConfig.getChronicleIdCheckDql().replace("objectType", objectType)
				.replace("dctmDocNumber", dctmDocNumber.toLowerCase());
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(chronicleIdCheckDql);
		var responseBody = (responseEntity.getBody() != null) ? responseEntity.getBody() : null;
		if (responseEntity.getStatusCodeValue() == 200) {
			var dctmDqlResponse = objectMapper.readValue(responseBody, DctmDqlResponse.class);
			chronicleId = (dctmDqlResponse.getDctmDqlEntriesResponse() != null)
					? dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle()
					: "";
		} else {
			msg.add(responseBody);
		}
		return chronicleId;
	}

	public void updatingData(String objectId, DmsDoUploadRequest doUploadRequest, List<String> msg)
			throws JsonProcessingException {
		String uploadStatus = "failed";
		String fullSystemPath = null;
		if (objectId != null) {
			uploadStatus = "done";
			fullSystemPath = dctmRestDocumentService.getFullFileSystemPath(objectId);
		}
		doUploadRequest.setObjectId(objectId);
		doUploadRequest.setFullSystemPath(fullSystemPath);
		doUploadRequest.setUploadStatus(uploadStatus);

		uploadHistoryService.updateData(doUploadRequest, msg);
		documentService.updateData(doUploadRequest);
	}

}

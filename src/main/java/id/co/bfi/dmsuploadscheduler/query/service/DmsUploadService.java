package id.co.bfi.dmsuploadscheduler.query.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmUploadRequest;
import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;
import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeEntity;
import id.co.bfi.dmsuploadscheduler.query.action.DmsUploadAction;
import id.co.bfi.dmsuploadscheduler.repository.DocumentTypeRepository;

@Service
public class DmsUploadService {

	@Autowired
	private DocumentTypeRepository docTypeRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private UploadHistoryService uploadHistoryService;
	
	@Autowired
	private DmsUploadAction dmsUploadAction;

	public void processDmsUpload() throws SQLException, Exception {
		List<DocumentEntity> documentsList = documentService.getDocumentList();
		if (documentsList.size() > 0) {
			for (int i = 0; i < documentsList.size(); i++) {
				String dctmDocumentsId = documentsList.get(i).getDctmDocumentsId();
				List<UploadHistoryEntity> uploadHistoryList = uploadHistoryService.getUploadHistoryList(dctmDocumentsId);
				if (uploadHistoryList.size() > 0) {
					boolean success = true;
					for (int j = 0; j < uploadHistoryList.size(); j++) {
						if (success) {
							String objectId = null;
							List<String> msg = new ArrayList<>();

							String properties = uploadHistoryList.get(j).getMetadataDctm();
							var dctmUploadRequest = objectMapper.readValue(properties, DctmUploadRequest.class);
							
							int docTypeId = dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmDocTypeId();
							DocumentTypeEntity dmsDocType = docTypeRepository.findByDocTypeId(new Long(docTypeId));

							objectId = dmsUploadAction.doUpload(documentsList, i, uploadHistoryList, j, msg, objectId,
									dctmUploadRequest, dmsDocType, properties);
							if (objectId == null && dmsDocType.getIsAllowVersioning() == 1)
								success = false;
						}
					}
				}
			}
		}
	}

}

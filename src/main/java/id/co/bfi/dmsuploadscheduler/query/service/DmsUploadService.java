package id.co.bfi.dmsuploadscheduler.query.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmUploadRequest;
import id.co.bfi.dmsuploadscheduler.api.request.DmsDoUploadRequest;
import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;
import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeEntity;
import id.co.bfi.dmsuploadscheduler.query.action.DmsUploadAction;
import id.co.bfi.dmsuploadscheduler.repository.DocumentRepository;
import id.co.bfi.dmsuploadscheduler.repository.DocumentTypeRepository;
import id.co.bfi.dmsuploadscheduler.repository.UploadHistoryRepository;

@Service
public class DmsUploadService {

	@Autowired
	private DocumentTypeRepository docTypeRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DmsUploadAction dmsUploadAction;
	
	@Autowired
	private DocumentRepository documentRepository;
	
	@Autowired
	private UploadHistoryRepository uploadHistoryRepository;

	public void processDocument() throws IOException {
		List<DocumentEntity> documentList = documentRepository.findAllDocument();
		for (int i = 0; i < documentList.size(); i++) {
			String dctmDocumentsId = documentList.get(i).getDctmDocumentsId();
			processUploadHistory(documentList, i, dctmDocumentsId);
		}
	}
	
	public void processUploadHistory(List<DocumentEntity> documentList, int i, String dctmDocumentsId) throws IOException {
		List<UploadHistoryEntity> uploadHistoryList = uploadHistoryRepository.findUploadHistoryByDctmId(dctmDocumentsId);
		boolean success = true;
		for (int j = 0; j < uploadHistoryList.size(); j++) {
			if (success) {
				String properties = uploadHistoryList.get(j).getMetadataDctm();
				var dctmUploadRequest = objectMapper.readValue(properties, DctmUploadRequest.class);

				Long docTypeId = dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmDocTypeId();
				DocumentTypeEntity dmsDocType = docTypeRepository.findByDocTypeId(docTypeId);

				DmsDoUploadRequest doUploadRequest = new DmsDoUploadRequest();
				doUploadRequest.setDctmUploadRequest(dctmUploadRequest);
				doUploadRequest.setDmsDocType(dmsDocType);
				doUploadRequest.setDocumentList(documentList);
				doUploadRequest.setDocumentListIndex(i);
				doUploadRequest.setUploadHistoryList(uploadHistoryList);
				doUploadRequest.setUploadHistoryListIndex(j);
				doUploadRequest.setProperties(properties);
				
				String objectId = dmsUploadAction.doUpload(doUploadRequest);
				if (objectId == null && dmsDocType.getIsAllowVersioning() == 1)
					success = false;
			}
		}
	}

}

package id.co.bfi.dmsuploadscheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmUploadPropertiesRequest;
import id.co.bfi.dmsuploadscheduler.api.request.DctmUploadRequest;
import id.co.bfi.dmsuploadscheduler.api.request.DmsDoUploadRequest;
import id.co.bfi.dmsuploadscheduler.api.response.DctmDqlResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;
import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeEntity;
import id.co.bfi.dmsuploadscheduler.query.action.DmsUploadAction;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestDocumentService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestFolderService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestService;
import id.co.bfi.dmsuploadscheduler.repository.DocumentRepository;
import id.co.bfi.dmsuploadscheduler.repository.DocumentTypeRepository;
import id.co.bfi.dmsuploadscheduler.repository.UploadHistoryRepository;

@SpringBootTest
class DmsUploadSchedulerApplicationTests {

	@Autowired
	private DctmRestService dctmRestService;

	@Autowired
	private DctmRestFolderService dctmFolderService;

	@Autowired
	private DctmRestDocumentService dctmRestDocumentService;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private DmsUploadAction dmsUploadAction;

	@Autowired
	private UploadHistoryRepository uploadHistoryRepository;

	@Autowired
	private DocumentTypeRepository docTypeRepository;

	@Autowired
	private DctmRestConfig dctmRestConfig;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void getDataFromDqlTest() throws JsonProcessingException {
		String expectedAttribute = "Temp";
		String dql = "select object_name from dm_folder where object_name = 'Temp'";
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(dql);
		assertEquals(200, responseEntity.getStatusCodeValue());
		var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
				DctmDqlResponse.class);

		assertEquals(expectedAttribute, dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle());
	}

	@Test
	void generateFolderPathTest() {
		DctmUploadPropertiesRequest dctmUploadPropertiesRequest = new DctmUploadPropertiesRequest();
		dctmUploadPropertiesRequest.setDctmCustomerId("customer01");
		dctmUploadPropertiesRequest.setDctmBranchId("401");
		dctmUploadPropertiesRequest.setDctmLeadsId("leads01");
		
		DctmUploadRequest dctmUploadRequest = new DctmUploadRequest();
		dctmUploadRequest.setDctmUploadPropertiesRequest(dctmUploadPropertiesRequest);

		// case 1 - consumer doc
		DocumentTypeEntity dmsDocType = docTypeRepository.findByDocTypeId(Long.valueOf(1));
		String expectedFolderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/"
				+ dctmUploadPropertiesRequest.getDctmCustomerId() + "/" + dmsDocType.getDocType();
		String actualFolderPath = dctmFolderService.generateFolderPath(dctmUploadRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);

		// case 2 - internal doc
		dmsDocType = docTypeRepository.findByDocTypeId(Long.valueOf(10100));
		expectedFolderPath = "/" + dctmRestConfig.getInternalDocCabinetName() + "/" + dmsDocType.getDocTypeDesc() + "/"
				+ dctmRestConfig.getDmsActiveDocFolderName();
		actualFolderPath = dctmFolderService.generateFolderPath(dctmUploadRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);

		// case 3 - non consumer doc
		dmsDocType = docTypeRepository.findByDocTypeId(Long.valueOf(2));
		expectedFolderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/"
				+ dctmUploadPropertiesRequest.getDctmCustomerId() + "/"
				+ dctmUploadPropertiesRequest.getDctmBranchId() + "/"
				+ dctmUploadPropertiesRequest.getDctmLeadsId() + "/" + dmsDocType.getDocType();
		actualFolderPath = dctmFolderService.generateFolderPath(dctmUploadRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);
	}

	@Test
	void uploadHistoryTest() throws Exception {
		List<DocumentEntity> documentList = documentRepository.findAllDocument();
		boolean success = false;
		if (documentList.size() > 0) {
			String dctmDocumentsId = documentList.get(0).getDctmDocumentsId();
			List<UploadHistoryEntity> uploadHistoryList = uploadHistoryRepository.findUploadHistoryByDctmId(dctmDocumentsId);
			if (uploadHistoryList.size() > 0) {
				for (int j = 0; j < uploadHistoryList.size(); j++) {
					String expectedObjectId = null;
					String properties = uploadHistoryList.get(j).getMetadataDctm();
					var dctmUploadRequest = objectMapper.readValue(properties, DctmUploadRequest.class);
					
					Long docTypeId = dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmDocTypeId();
					DocumentTypeEntity dmsDocType = docTypeRepository.findByDocTypeId(docTypeId);
					
					DmsDoUploadRequest doUploadRequest = new DmsDoUploadRequest();
					doUploadRequest.setDctmUploadRequest(dctmUploadRequest);
					doUploadRequest.setDmsDocType(dmsDocType);
					doUploadRequest.setDocumentList(documentList);
					doUploadRequest.setDocumentListIndex(0);
					doUploadRequest.setUploadHistoryList(uploadHistoryList);
					doUploadRequest.setUploadHistoryListIndex(j);
					doUploadRequest.setProperties(properties);
					
					expectedObjectId = dmsUploadAction.doUpload(doUploadRequest);
					if (expectedObjectId != null)
						success = true;
					
					final String expectedStatusUpload = "done";
					assertEquals(expectedStatusUpload, uploadHistoryList.get(j).getStatusUpload());
					assertEquals(expectedObjectId, uploadHistoryList.get(j).getDctmId());
					assertTrue(success);
				}
			}
		}
	}

	@Test
	void getFullFileSystemPathTest() throws JsonProcessingException {
		String documentCheckDql = "select r_object_id from dm_document order by r_creation_date desc enable (return_top 1)";
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(documentCheckDql);
		assertEquals(200, responseEntity.getStatusCodeValue());

		var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
				DctmDqlResponse.class);
		String objectId = dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle();

		String fullFileSystemPath = dctmRestDocumentService.getFullFileSystemPath(objectId);
		assertNotNull(fullFileSystemPath);

		boolean pathIsCorrect = false;
		if (fullFileSystemPath.contains("Documentum\\data"))
			pathIsCorrect = true;

		assertTrue(pathIsCorrect);
	}

}

package id.co.bfi.dmsuploadscheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmUploadPropertiesRequest;
import id.co.bfi.dmsuploadscheduler.api.request.DctmUploadRequest;
import id.co.bfi.dmsuploadscheduler.api.response.DctmDqlResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;
import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeEntity;
import id.co.bfi.dmsuploadscheduler.query.service.DocumentService;
import id.co.bfi.dmsuploadscheduler.query.service.UploadHistoryService;
import id.co.bfi.dmsuploadscheduler.query.action.DmsUploadAction;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestDocumentService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestFolderService;
import id.co.bfi.dmsuploadscheduler.query.service.dctm_rest.DctmRestService;
import id.co.bfi.dmsuploadscheduler.repository.DocumentTypeRepository;

@SpringBootTest
class DmsUploadSchedulerApplicationTests {

	@Test
	void contextLoads() {

	}

	@Autowired
	private DctmRestService dctmRestService;

	@Autowired
	private DctmRestFolderService dctmFolderService;

	@Autowired
	private DctmRestDocumentService dctmRestDocumentService;

	@Autowired
	private DocumentService documentService;

	@Autowired
	private DmsUploadAction dmsUploadAction;

	@Autowired
	private UploadHistoryService uploadHistoryService;

	@Autowired
	private DocumentTypeRepository docTypeRepository;

	@Autowired
	private DctmRestConfig dctmRestConfig;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void getDataFromDqlTest() throws JsonMappingException, JsonProcessingException {
		String expectedAttribute = "Temp";
		String dql = "select object_name from dm_folder where object_name = 'Temp'";
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(dql);
		assertEquals(200, responseEntity.getStatusCodeValue());
		var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
				DctmDqlResponse.class);

		assertEquals(expectedAttribute, dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle());
	}

	@Test
	public void generateFolderPathTest() {
		DctmUploadPropertiesRequest dctmUploadPropertiesRequest = new DctmUploadPropertiesRequest();
		dctmUploadPropertiesRequest.setDctmCustomerId("customer01");
		dctmUploadPropertiesRequest.setDctmBranchId("401");
		dctmUploadPropertiesRequest.setDctmLeadsId("leads01");
		
		DctmUploadRequest dctmUploadRequest = new DctmUploadRequest();
		dctmUploadRequest.setDctmUploadPropertiesRequest(dctmUploadPropertiesRequest);

		// case 1 - consumer doc
		DocumentTypeEntity dmsDocType = docTypeRepository.findByDocTypeId(new Long(1));
		String expectedFolderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/"
				+ dctmUploadPropertiesRequest.getDctmCustomerId() + "/" + dmsDocType.getDocType();
		String actualFolderPath = dctmFolderService.generateFolderPath(dctmUploadRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);

		// case 2 - internal doc
		dmsDocType = docTypeRepository.findByDocTypeId(new Long(10100));
		expectedFolderPath = "/" + dctmRestConfig.getInternalDocCabinetName() + "/" + dmsDocType.getDocTypeDesc() + "/"
				+ dctmRestConfig.getDmsActiveDocFolderName();
		actualFolderPath = dctmFolderService.generateFolderPath(dctmUploadRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);

		// case 3 - non consumer doc
		dmsDocType = docTypeRepository.findByDocTypeId(new Long(2));
		expectedFolderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/"
				+ dctmUploadPropertiesRequest.getDctmCustomerId() + "/"
				+ dctmUploadPropertiesRequest.getDctmBranchId() + "/"
				+ dctmUploadPropertiesRequest.getDctmLeadsId() + "/" + dmsDocType.getDocType();
		actualFolderPath = dctmFolderService.generateFolderPath(dctmUploadRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);
	}

	@Test
	public void uploadHistoryTest() throws Exception {
		List<DocumentEntity> documentsList = documentService.getDocumentList();
		boolean success = false;
		if (documentsList.size() > 0) {
			String docId = documentsList.get(0).getDctmDocumentsId();
			List<UploadHistoryEntity> uploadHistoryList = uploadHistoryService
					.getUploadHistoryList(docId);
			if (uploadHistoryList.size() > 0) {
				for (int j = 0; j < uploadHistoryList.size(); j++) {
					List<String> msg = new ArrayList<>();
					String expectedObjectId = null;
					String properties = uploadHistoryList.get(j).getMetadataDctm();
					var dctmUploadRequest = objectMapper.readValue(properties, DctmUploadRequest.class);
					
					int docTypeId = dctmUploadRequest.getDctmUploadPropertiesRequest().getDctmDocTypeId();
					DocumentTypeEntity dmsDocType = docTypeRepository.findByDocTypeId(new Long(docTypeId));
					expectedObjectId = dmsUploadAction.doUpload(documentsList, 0, uploadHistoryList, j, msg, expectedObjectId, dctmUploadRequest, dmsDocType, properties);
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
	public void getFullFileSystemPathTest() throws JsonMappingException, JsonProcessingException {
		String documentCheckDql = "select r_object_id from dm_document order by r_creation_date desc enable (return_top 1)";
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(documentCheckDql);
		assertEquals(200, responseEntity.getStatusCodeValue());

		var dctmDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
				DctmDqlResponse.class);
		String objectId = dctmDqlResponse.getDctmDqlEntriesResponse().get(0).getTitle();

		List<String> msg = new ArrayList<>();
		String fullFileSystemPath = dctmRestDocumentService.getFullFileSystemPath(objectId, msg);
		assertNotNull(fullFileSystemPath);

		boolean pathIsCorrect = false;
		if (fullFileSystemPath.contains("Documentum\\data"))
			pathIsCorrect = true;

		assertTrue(pathIsCorrect);
	}

}

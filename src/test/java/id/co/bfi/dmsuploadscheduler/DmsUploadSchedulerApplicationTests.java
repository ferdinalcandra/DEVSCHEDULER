package id.co.bfi.dmsuploadscheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import id.co.bfi.dmsuploadscheduler.api.request.DctmRestRequest;
import id.co.bfi.dmsuploadscheduler.api.response.DctmRestDqlResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmRestResponse;
import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.entity.DctmDocumentsEntity;
import id.co.bfi.dmsuploadscheduler.entity.DctmUploadDocumentHistoryEntity;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeMasterEntity;
import id.co.bfi.dmsuploadscheduler.repository.DocumentTypeMasterRepository;
import id.co.bfi.dmsuploadscheduler.service.DctmDocumentsService;
import id.co.bfi.dmsuploadscheduler.service.DctmUploadDocumentHistoryService;
import id.co.bfi.dmsuploadscheduler.service.DmsUploadService;
import id.co.bfi.dmsuploadscheduler.service.dctm_rest.DctmRestDocumentService;
import id.co.bfi.dmsuploadscheduler.service.dctm_rest.DctmRestFolderService;
import id.co.bfi.dmsuploadscheduler.service.dctm_rest.DctmRestService;

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
	private DctmDocumentsService dctmDocumentsService;

	@Autowired
	private DmsUploadService dmsUploadService;

	@Autowired
	private DctmUploadDocumentHistoryService dctmUploadDocumentHistoryService;

	@Autowired
	private DocumentTypeMasterRepository dmsDocTypeRepository;

	@Autowired
	private DctmRestConfig dctmRestConfig;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void getAttributeFromDqlTest() throws JsonMappingException, JsonProcessingException {
		String expectedAttribute = "Temp";
		String dql = "select object_name from dm_folder where object_name = 'Temp'";
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(dql);
		assertEquals(200, responseEntity.getStatusCodeValue());
		DctmRestDqlResponse dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
				DctmRestDqlResponse.class);

		assertEquals(expectedAttribute, dctmRestDqlResponse.getEntries().get(0).get("title"));
	}

	@Test
	public void generateFolderPathTest() {
		Map<String, Object> propertiesMap = new HashMap<>();
		propertiesMap.put("dctm_customer_id", "customer01");
		propertiesMap.put("dctm_branch_id", "401");
		propertiesMap.put("dctm_leads_id", "leads01");

		DctmRestRequest dctmRestRequest = new DctmRestRequest();
		dctmRestRequest.setProperties(propertiesMap);

		// case 1 - consumer doc
		DocumentTypeMasterEntity dmsDocType = dmsDocTypeRepository.findBySqDocumentTypeMst(new Long(1));
		String expectedFolderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/"
				+ dctmRestRequest.getProperties().get("dctm_customer_id").toString() + "/" + dmsDocType.getDocType();
		String actualFolderPath = dctmFolderService.generateFolderPath(dctmRestRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);

		// case 2 - internal doc
		dmsDocType = dmsDocTypeRepository.findBySqDocumentTypeMst(new Long(10100));
		expectedFolderPath = "/" + dctmRestConfig.getInternalDocCabinetName() + "/" + dmsDocType.getDocTypeDesc() + "/"
				+ dctmRestConfig.getDmsActiveDocFolderName();
		actualFolderPath = dctmFolderService.generateFolderPath(dctmRestRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);

		// case 3 - non consumer doc
		dmsDocType = dmsDocTypeRepository.findBySqDocumentTypeMst(new Long(2));
		expectedFolderPath = "/" + dctmRestConfig.getConsumerDocCabinetName() + "/"
				+ dctmRestRequest.getProperties().get("dctm_customer_id").toString() + "/"
				+ dctmRestRequest.getProperties().get("dctm_branch_id").toString() + "/"
				+ dctmRestRequest.getProperties().get("dctm_leads_id").toString() + "/" + dmsDocType.getDocType();
		actualFolderPath = dctmFolderService.generateFolderPath(dctmRestRequest, dmsDocType);

		assertEquals(expectedFolderPath, actualFolderPath);
	}

	@Test
	public void uploadDocumentHistoryTest() throws Exception {
		List<DctmDocumentsEntity> documentsList = dctmDocumentsService.getDctmDocumentsList();
		String expectedObjectId = null;
		boolean success = false;
		if (documentsList.size() > 0) {
			String docId = documentsList.get(0).getDctmDocumentsId();
			List<DctmUploadDocumentHistoryEntity> uploadDocHistoryList = dctmUploadDocumentHistoryService
					.getDctmUploadDocumentHistoryList(docId);
			if (uploadDocHistoryList.size() > 0) {
				List<String> msg = new ArrayList<>();
				String properties = uploadDocHistoryList.get(0).getMetadataDctm();
				var dctmRestRequest = objectMapper.readValue(properties, DctmRestRequest.class);
				int docTypeId = (int) dctmRestRequest.getProperties().get("dctm_doc_type_id");
				DocumentTypeMasterEntity dmsDocType = dmsDocTypeRepository.findBySqDocumentTypeMst(new Long(docTypeId));

				expectedObjectId = dmsUploadService.doUpload(documentsList, 0, uploadDocHistoryList, 0,
						msg, expectedObjectId, dctmRestRequest, dmsDocType);
				if (expectedObjectId != null)
					success = true;
			}
			final String expectedStatusUpload = "done";
			assertEquals(expectedStatusUpload, uploadDocHistoryList.get(0).getStatusUpload());
			assertEquals(expectedObjectId, uploadDocHistoryList.get(0).getDctmId());
		}
		assertTrue(success);
	}

	@Test
	public void uploadDocumentTest() throws IOException {
		Map<String, Object> propertiesMap = new HashMap<>();
		propertiesMap.put("r_object_type", "dctm_consumer_doc");
		propertiesMap.put("object_name", "sample_doc");
		propertiesMap.put("dctm_doc_number", "BAST_customer01");
		propertiesMap.put("dctm_customer_id", "customer01");
		propertiesMap.put("dctm_doc_type_id", 1);
		propertiesMap.put("dctm_source", "testing script");

		DctmRestRequest dctmRestRequest = new DctmRestRequest();
		dctmRestRequest.setProperties(propertiesMap);

		PrintWriter writer = new PrintWriter("sample_doc.txt", "UTF-8");
		writer.println("Hello");
		writer.println("World");
		writer.close();

		InputStream inputStream = new FileInputStream(new File("sample_doc.txt"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buf = new byte[16 * 1024 * 1024];
		int len;
		while ((len = inputStream.read(buf)) > 0) {
			baos.write(buf, 0, len);
		}
		String documentByte = Base64.getEncoder().encodeToString(baos.toByteArray());

		List<String> msg = new ArrayList<>();
		DocumentTypeMasterEntity dmsDocType = dmsDocTypeRepository
				.findBySqDocumentTypeMst(new Long(dctmRestRequest.getProperties().get("dctm_doc_type_id").toString()));
		String folderId = dctmFolderService
				.createFolderByPath(dctmFolderService.generateFolderPath(dctmRestRequest, dmsDocType), msg);

		boolean success = false;
		DctmRestResponse dctmRestResponse = dctmRestDocumentService.uploadDocument(
				objectMapper.writeValueAsString(dctmRestRequest), documentByte, "text/plain", folderId, msg);
		if (dctmRestResponse.getProperties().get("r_object_id") != null)
			success = true;

		assertTrue(success);
		inputStream.close();
		baos.close();
		if (new File("sample_doc.txt").exists())
			new File("sample_doc.txt").delete();
	}

	@Test
	public void checkoutDocument() throws JsonMappingException, JsonProcessingException {
		String chronicleIdCheckDql = "select i_chronicle_id from dctm_consumer_doc(all) where dctm_doc_number = 'BAST_customer01'";

		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(chronicleIdCheckDql);
		assertEquals(200, responseEntity.getStatusCodeValue());

		DctmRestDqlResponse dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
				DctmRestDqlResponse.class);
		responseEntity = dctmRestService.checkoutDocument(
				"/objects/" + dctmRestDqlResponse.getEntries().get(0).get("title").toString() + "/lock");

		assertEquals(200, responseEntity.getStatusCodeValue());
	}

	@Test
	public void uploadVersionDocumentTest() throws IOException {
		Map<String, Object> propertiesMap = new HashMap<>();
		propertiesMap.put("r_object_type", "dctm_consumer_doc");
		propertiesMap.put("object_name", "sample_doc_v2");
		propertiesMap.put("dctm_doc_number", "BAST_customer01");
		propertiesMap.put("dctm_customer_id", "customer01_v2");
		propertiesMap.put("dctm_doc_type_id", 1);
		propertiesMap.put("dctm_source", "testing script v2");

		DctmRestRequest dctmRestRequest = new DctmRestRequest();
		dctmRestRequest.setProperties(propertiesMap);

		PrintWriter writer = new PrintWriter("sample_doc_v2.txt", "UTF-8");
		writer.println("Hello");
		writer.println("World");
		writer.close();

		InputStream inputStream = new FileInputStream(new File("sample_doc_v2.txt"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buf = new byte[16 * 1024 * 1024];
		int len;
		while ((len = inputStream.read(buf)) > 0) {
			baos.write(buf, 0, len);
		}
		String documentByte = Base64.getEncoder().encodeToString(baos.toByteArray());

		List<String> msg = new ArrayList<>();

		boolean success = false;

		String queryDql = "select i_chronicle_id from "
				+ dctmRestRequest.getProperties().get("r_object_type").toString() + "(all) where dctm_doc_number = '"
				+ dctmRestRequest.getProperties().get("dctm_doc_number").toString() + "'";

		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(queryDql);
		assertEquals(200, responseEntity.getStatusCodeValue());

		DctmRestDqlResponse dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
				DctmRestDqlResponse.class);
		String chronicleId = dctmRestDqlResponse.getEntries().get(0).get("title").toString();

		DctmRestResponse dctmRestResponse = dctmRestDocumentService.uploadVersionDocument(chronicleId,
				objectMapper.writeValueAsString(dctmRestRequest),
				dctmRestRequest.getProperties().get("r_object_type").toString(), documentByte, "text/plain", msg);

		if (dctmRestResponse.getProperties().get("r_object_id") != null)
			success = true;

		assertTrue(success);
		inputStream.close();
		baos.close();
		if (new File("sample_doc_v2.txt").exists())
			new File("sample_doc_v2.txt").delete();
	}

	@Test
	public void getFullFileSystemPathTest() throws JsonMappingException, JsonProcessingException {
		String documentCheckDql = "select r_object_id from dctm_consumer_doc where dctm_doc_number = 'BAST_customer01' "
				+ "and dctm_source = 'testing script'";
		ResponseEntity<String> responseEntity = dctmRestService.getDataFromDql(documentCheckDql);
		assertEquals(200, responseEntity.getStatusCodeValue());

		DctmRestDqlResponse dctmRestDqlResponse = objectMapper.readValue(responseEntity.getBody().toString(),
				DctmRestDqlResponse.class);
		String objectId = dctmRestDqlResponse.getEntries().get(0).get("title").toString();

		List<String> msg = new ArrayList<>();
		String fullFileSystemPath = dctmRestDocumentService.getFullFileSystemPath(objectId, msg);
		assertEquals(0, msg.size());

		boolean pathIsCorrect = false;
		if (fullFileSystemPath.contains("Documentum\\data") && fullFileSystemPath.endsWith(".txt"))
			pathIsCorrect = true;

		assertTrue(pathIsCorrect);
	}

}

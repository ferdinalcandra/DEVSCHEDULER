package id.co.bfi.dmsuploadscheduler.service.temp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.YamlPropertySourceFactory;
import id.co.bfi.dmsuploadscheduler.model.ms.DmsDocType;
import id.co.bfi.dmsuploadscheduler.model.temp.Documents;
import id.co.bfi.dmsuploadscheduler.model.temp.UploadDocHistory;
import id.co.bfi.dmsuploadscheduler.repository.ms.DmsDocTypeRepository;
import id.co.bfi.dmsuploadscheduler.repository.temp.DocumentsRepository;
import id.co.bfi.dmsuploadscheduler.repository.temp.UploadDocHistoryRepository;
import id.co.bfi.dmsuploadscheduler.service.share_folder.ShareFolderService;
import id.co.bfi.dmsuploadscheduler.service.dctm.DctmDocumentService;
import id.co.bfi.dmsuploadscheduler.service.dctm.DctmFolderService;
import id.co.bfi.dmsuploadscheduler.service.dctm.DctmRestService;

@Component
@Configuration
@PropertySource(value = "classpath:id/co/bfi/dmsuploadscheduler/conf/application.yml", factory = YamlPropertySourceFactory.class)
public class UploadDocHistoryService {
	@Autowired
	DocumentsRepository documentsRepository;
	
	@Autowired
	UploadDocHistoryRepository uploadDocHistoryRepository;
	
	@Autowired
	DmsDocTypeRepository dmsDocTypeRepository;
	
	@Autowired
	ShareFolderService shareFolderService;
	
	@Autowired
	DctmRestService dctmRestService;
	
	@Autowired
	DctmRestConfig dctmRestConfig;
	
	@Autowired
	DctmDocumentService dctmDocumentService;
	
	@Autowired
	DctmFolderService dctmFolderService;
	
	@Scheduled(cron = "${scheduler.cronExpression}", zone = "Asia/Jakarta")
	public void processDmsReqUpload() throws SQLException, IOException, JSONException {
		List<Documents> documentsList = documentsRepository.listDocuments();
		if (documentsList != null) {
			if (documentsList.size() > 0) {
				for(int i=0; i<documentsList.size(); i++) {
					String docId = documentsList.get(i).getDocId();
					List<UploadDocHistory> uploadDocHistoryList = uploadDocHistoryRepository.listUploadDocHistoryByDocId(docId);
					if (uploadDocHistoryList != null) {
						if (uploadDocHistoryList.size() > 0) {
							boolean success = true;
							for (int j=0; j<uploadDocHistoryList.size(); j++) {
								if (success == true) {
									String pathFileName = uploadDocHistoryList.get(j).getPathFileName();
									ByteArrayOutputStream baos = shareFolderService.getFileOverSharedFolder(pathFileName);
									String documentByte = Base64.getEncoder().encodeToString(baos.toByteArray());
									String mimeType = uploadDocHistoryList.get(j).getDmsMimeType();
									String properties = uploadDocHistoryList.get(j).getMetaDataDctm();
									JSONObject json = new JSONObject(properties);
									String dctmDocNumber = null;
									String objectType = null;
									String chronicleId = null;
									String docTypeId = null;
									if (json != null) {
										if (json.has("dctm_doc_number")) {
											if (json.get("dctm_doc_number") != null) {
												if (!json.get("dctm_doc_number").toString().isEmpty())
													dctmDocNumber = json.get("dctm_doc_number").toString().trim();
											}
										}
										if (json.has("r_object_type")) {
											if (json.get("r_object_type") != null) {
												if (!json.get("r_object_type").toString().isEmpty())
													objectType = json.get("r_object_type").toString().trim();
											}
										}
										if (json.has("dctm_doc_type_id")) {
											if (json.get("dctm_doc_type_id") != null) {
												if (!json.get("dctm_doc_type_id").toString().isEmpty())
													docTypeId = json.get("dctm_doc_type_id").toString().trim();
											}
										}
									}
									if (objectType != null && dctmDocNumber != null) {
										String queryDql = "select i_chronicle_id from "+objectType+" where lower(dctm_doc_number) = '"+dctmDocNumber.toLowerCase()+"'";
									    chronicleId = dctmRestService.getAttributeFromDql(queryDql);
									}
									String objectId = null;
									JSONObject uploadDocResponse = null;
									if (chronicleId != null) {
										// versioning
										uploadDocResponse = dctmDocumentService.uploadVersionDocument(chronicleId, properties, documentByte, mimeType);
										objectId = uploadDocResponse.getJSONObject("properties").getString("r_object_id");
									} else {
										// new doc
										String folderId = dctmFolderService.createFolderByPath(generateFolderPath(docTypeId, json));
										uploadDocResponse = dctmDocumentService.uploadDocument(properties, documentByte, mimeType, folderId);
										objectId = uploadDocResponse.getJSONObject("properties").getString("r_object_id");
									}
									Date now = new Date();
									String fullSystemPath = null;
									if (objectId != null) {
										uploadDocHistoryList.get(j).setDctmId(objectId);
										uploadDocHistoryList.get(j).setStatusUpload(2);
										uploadDocHistoryList.get(j).setMsg("OK");
										fullSystemPath = dctmDocumentService.getFullFileSystemPath(objectId);
										uploadDocHistoryList.get(j).setPathFileName(fullSystemPath);
										
										documentsList.get(i).setPathFileName(fullSystemPath);
										documentsList.get(i).setDctmId(objectId);
										documentsList.get(i).setModifiedDate(now);
										documentsRepository.save(documentsList.get(i));
									} else {
										success = false;
										uploadDocHistoryList.get(j).setStatusUpload(3);
										uploadDocHistoryList.get(j).setMsg("Failed");
									}
									uploadDocHistoryList.get(j).setModifiedDate(now);
									uploadDocHistoryRepository.save(uploadDocHistoryList.get(j));
								}
							}
						}
					}
				}
			}
		}
	}
	
	public String generateFolderPath(String docTypeId, JSONObject json) throws JSONException {
		String folderPath = null;
		DmsDocType dmsDocType = dmsDocTypeRepository.findByDmsDocTypeId(docTypeId);
		String customerId = null;
		if (json.has("dctm_customer_id")) {
			if (json.get("dctm_customer_id") != null) {
				if (!json.get("dctm_customer_id").toString().isEmpty())
					customerId = json.get("dctm_customer_id").toString().trim();
			}
		}
		if (dmsDocType.getDmsGroupDocId().equals("DG002")) {
			folderPath = "/"+dctmRestConfig.getConsumerDocCabinetName()+"/"+customerId+"/"+dmsDocType.getDmsDocType();
		} else {
			if (dmsDocType.getDmsProcessName().equals(dctmRestConfig.getDmsProcessNameConsumer())) {
				String branchId = null;
				if (json.has("dctm_branch_id")) {
					if (json.get("dctm_branch_id") != null) {
						if (!json.get("dctm_branch_id").toString().isEmpty())
							branchId = json.get("dctm_branch_id").toString().trim();
					}
				}
				String leadsId = null;
				if (json.has("dctm_branch_id")) {
					if (json.get("dctm_branch_id") != null) {
						if (!json.get("dctm_branch_id").toString().isEmpty())
							branchId = json.get("dctm_branch_id").toString().trim();
					}
				}
				folderPath = "/"+dctmRestConfig.getConsumerDocCabinetName()+"/"+customerId+"/"+branchId
    			+"/"+leadsId+"/"+dmsDocType.getDmsDocType();
			} else if (dmsDocType.getDmsProcessName().equals(dctmRestConfig.getDmsProcessNameInternal())) {
				folderPath = "/"+dctmRestConfig.getInternalDocCabinetName()+"/"+
							dmsDocType.getDmsDocTypeName()+"/"+dctmRestConfig.getDmsActiveDocFolderName();
			}
		}
		return folderPath;
	}
	
}

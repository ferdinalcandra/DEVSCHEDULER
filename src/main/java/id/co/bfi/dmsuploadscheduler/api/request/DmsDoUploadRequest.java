package id.co.bfi.dmsuploadscheduler.api.request;

import java.util.List;

import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;
import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeEntity;
import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;

public class DmsDoUploadRequest {
	
	private List<DocumentEntity> documentList;
	
	private int documentListIndex;
	
	private List<UploadHistoryEntity> uploadHistoryList;
	
	private int uploadHistoryListIndex;
	
	private DctmUploadRequest dctmUploadRequest;
	
	private DocumentTypeEntity dmsDocType;
	
	private String properties;
	
	private String objectId;
	
	private String fullSystemPath;
	
	private String uploadStatus;

	public List<DocumentEntity> getDocumentList() {
		return documentList;
	}

	public void setDocumentList(List<DocumentEntity> documentList) {
		this.documentList = documentList;
	}

	public int getDocumentListIndex() {
		return documentListIndex;
	}

	public void setDocumentListIndex(int documentListIndex) {
		this.documentListIndex = documentListIndex;
	}

	public List<UploadHistoryEntity> getUploadHistoryList() {
		return uploadHistoryList;
	}

	public void setUploadHistoryList(List<UploadHistoryEntity> uploadHistoryList) {
		this.uploadHistoryList = uploadHistoryList;
	}

	public int getUploadHistoryListIndex() {
		return uploadHistoryListIndex;
	}

	public void setUploadHistoryListIndex(int uploadHistoryListIndex) {
		this.uploadHistoryListIndex = uploadHistoryListIndex;
	}

	public DctmUploadRequest getDctmUploadRequest() {
		return dctmUploadRequest;
	}

	public void setDctmUploadRequest(DctmUploadRequest dctmUploadRequest) {
		this.dctmUploadRequest = dctmUploadRequest;
	}

	public DocumentTypeEntity getDmsDocType() {
		return dmsDocType;
	}

	public void setDmsDocType(DocumentTypeEntity dmsDocType) {
		this.dmsDocType = dmsDocType;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getFullSystemPath() {
		return fullSystemPath;
	}

	public void setFullSystemPath(String fullSystemPath) {
		this.fullSystemPath = fullSystemPath;
	}

	public String getUploadStatus() {
		return uploadStatus;
	}

	public void setUploadStatus(String uploadStatus) {
		this.uploadStatus = uploadStatus;
	}
	
}

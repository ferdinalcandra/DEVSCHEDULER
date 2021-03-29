package id.co.bfi.dmsuploadscheduler.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dctm_upload_document_history")
public class UploadHistoryEntity {

	@Id
	@Column(name = "dctm_upload_document_history_id", unique = true, nullable = false)
	private String dctmUploadDocumentHistoryId;

	@Column(name = "path_file_name")
	private String pathFileName;

	@Column(name = "status_upload")
	private String statusUpload;

	@Column(name = "metadata_dctm")
	private String metadataDctm;

	@Column(name = "created_by")
	private String uploadHistoryCreatedBy;

	@Column(name = "created_date")
	private Date uploadHistoryCreatedDate;

	@Column(name = "last_modified_by")
	private String uploadHistoryLastModifiedBy;

	@Column(name = "last_modified_date")
	private Date uploadHistoryLastModifiedDate;

	@Column(name = "dms_mime_type")
	private String dmsMimeType;

	@Column(name = "msg")
	private String msg;

	@Column(name = "dctm_id")
	private String dctmId;

	@Column(name = "dctm_documents_id")
	private String dctmDocumentsId;

	@Column(name = "dctm_doc_type_id")
	private long dctmDocTypeId;

	public String getDctmUploadDocumentHistoryId() {
		return dctmUploadDocumentHistoryId;
	}

	public void setDctmUploadDocumentHistoryId(String dctmUploadDocumentHistoryId) {
		this.dctmUploadDocumentHistoryId = dctmUploadDocumentHistoryId;
	}

	public String getPathFileName() {
		return pathFileName;
	}

	public void setPathFileName(String pathFileName) {
		this.pathFileName = pathFileName;
	}

	public String getStatusUpload() {
		return statusUpload;
	}

	public void setStatusUpload(String statusUpload) {
		this.statusUpload = statusUpload;
	}

	public String getMetadataDctm() {
		return metadataDctm;
	}

	public void setMetadataDctm(String metadataDctm) {
		this.metadataDctm = metadataDctm;
	}
	
	public String getUploadHistoryCreatedBy() {
		return uploadHistoryCreatedBy;
	}

	public void setUploadHistoryCreatedBy(String uploadHistoryCreatedBy) {
		this.uploadHistoryCreatedBy = uploadHistoryCreatedBy;
	}

	public Date getUploadHistoryCreatedDate() {
		return uploadHistoryCreatedDate;
	}

	public void setUploadHistoryCreatedDate(Date uploadHistoryCreatedDate) {
		this.uploadHistoryCreatedDate = uploadHistoryCreatedDate;
	}

	public String getUploadHistoryLastModifiedBy() {
		return uploadHistoryLastModifiedBy;
	}

	public void setUploadHistoryLastModifiedBy(String uploadHistoryLastModifiedBy) {
		this.uploadHistoryLastModifiedBy = uploadHistoryLastModifiedBy;
	}

	public Date getUploadHistoryLastModifiedDate() {
		return uploadHistoryLastModifiedDate;
	}

	public void setUploadHistoryLastModifiedDate(Date uploadHistoryLastModifiedDate) {
		this.uploadHistoryLastModifiedDate = uploadHistoryLastModifiedDate;
	}

	public String getDmsMimeType() {
		return dmsMimeType;
	}

	public void setDmsMimeType(String dmsMimeType) {
		this.dmsMimeType = dmsMimeType;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getDctmId() {
		return dctmId;
	}

	public void setDctmId(String dctmId) {
		this.dctmId = dctmId;
	}

	public String getDctmDocumentsId() {
		return dctmDocumentsId;
	}

	public void setDctmDocumentsId(String dctmDocumentsId) {
		this.dctmDocumentsId = dctmDocumentsId;
	}

	public long getDctmDocTypeId() {
		return dctmDocTypeId;
	}

	public void setDctmDocTypeId(long dctmDocTypeId) {
		this.dctmDocTypeId = dctmDocTypeId;
	}

}

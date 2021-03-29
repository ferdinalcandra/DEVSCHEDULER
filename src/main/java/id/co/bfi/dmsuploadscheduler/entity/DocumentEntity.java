package id.co.bfi.dmsuploadscheduler.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dctm_documents")
public class DocumentEntity {

	@Id
	@Column(name = "dctm_documents_id", unique = true, nullable = false)
	private String dctmDocumentsId;

	@Column(name = "dctm_id")
	private String dctmId;

	@Column(name = "original_file_name")
	private String originalFileName;

	@Column(name = "path_file_name")
	private String pathFileName;

	@Column(name = "created_by")
	private String documentCreatedBy;

	@Column(name = "created_date")
	private Date documentCreatedDate;

	@Column(name = "last_modified_by")
	private String documentLastModifiedBy;

	@Column(name = "last_modified_date")
	private Date documentLastModifiedDate;

	@Column(name = "source")
	private String source;

	@Column(name = "doc_type")
	private String docType;

	@Column(name = "doc_key")
	private String docKey;

	@Column(name = "doc_type_id")
	private String docTypeId;

	public String getDctmDocumentsId() {
		return dctmDocumentsId;
	}

	public void setDctmDocumentsId(String dctmDocumentsId) {
		this.dctmDocumentsId = dctmDocumentsId;
	}

	public String getDctmId() {
		return dctmId;
	}

	public void setDctmId(String dctmId) {
		this.dctmId = dctmId;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public String getPathFileName() {
		return pathFileName;
	}

	public void setPathFileName(String pathFileName) {
		this.pathFileName = pathFileName;
	}
	
	public String getDocumentCreatedBy() {
		return documentCreatedBy;
	}

	public void setDocumentCreatedBy(String documentCreatedBy) {
		this.documentCreatedBy = documentCreatedBy;
	}

	public Date getDocumentCreatedDate() {
		return documentCreatedDate;
	}

	public void setDocumentCreatedDate(Date documentCreatedDate) {
		this.documentCreatedDate = documentCreatedDate;
	}

	public String getDocumentLastModifiedBy() {
		return documentLastModifiedBy;
	}

	public void setDocumentLastModifiedBy(String documentLastModifiedBy) {
		this.documentLastModifiedBy = documentLastModifiedBy;
	}

	public Date getDocumentLastModifiedDate() {
		return documentLastModifiedDate;
	}

	public void setDocumentLastModifiedDate(Date documentLastModifiedDate) {
		this.documentLastModifiedDate = documentLastModifiedDate;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getDocKey() {
		return docKey;
	}

	public void setDocKey(String docKey) {
		this.docKey = docKey;
	}

	public String getDocTypeId() {
		return docTypeId;
	}

	public void setDocTypeId(String docTypeId) {
		this.docTypeId = docTypeId;
	}
	
}

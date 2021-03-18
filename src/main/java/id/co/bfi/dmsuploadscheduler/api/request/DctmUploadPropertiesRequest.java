package id.co.bfi.dmsuploadscheduler.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class DctmUploadPropertiesRequest {
	
	@JsonProperty(value = "r_object_type", required = true)
	private String objectType;
	
	@JsonProperty(value = "object_name", required = true)
	private String objectName;
	
	@JsonProperty(value = "dctm_doc_type_id", required = true)
	private int dctmDocTypeId;
	
	@JsonProperty(value = "dctm_doc_number", required = true)
	private String dctmDocNumber;
	
	@JsonProperty(value = "dctm_doc_name", required = false)
	private String dctmDocName;
	
	@JsonProperty(value = "dctm_department", required = false)
	private String dctmDepartment;
	
	@JsonProperty(value = "dctm_source", required = false)
	private String dctmSource;
	
	@JsonProperty(value = "dctm_is_revision", required = false)
	private int dctmIsRevision;
	
	@JsonProperty(value = "dctm_customer_id", required = false)
	private String dctmCustomerId;
	
	@JsonProperty(value = "dctm_branch_id", required = false)
	private String dctmBranchId;
	
	@JsonProperty(value = "dctm_leads_id", required = false)
	private String dctmLeadsId;

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public int getDctmDocTypeId() {
		return dctmDocTypeId;
	}

	public void setDctmDocTypeId(int dctmDocTypeId) {
		this.dctmDocTypeId = dctmDocTypeId;
	}

	public String getDctmDocNumber() {
		return dctmDocNumber;
	}

	public void setDctmDocNumber(String dctmDocNumber) {
		this.dctmDocNumber = dctmDocNumber;
	}

	public String getDctmDocName() {
		return dctmDocName;
	}

	public void setDctmDocName(String dctmDocName) {
		this.dctmDocName = dctmDocName;
	}

	public String getDctmDepartment() {
		return dctmDepartment;
	}

	public void setDctmDepartment(String dctmDepartment) {
		this.dctmDepartment = dctmDepartment;
	}

	public String getDctmSource() {
		return dctmSource;
	}

	public void setDctmSource(String dctmSource) {
		this.dctmSource = dctmSource;
	}

	public int getDctmIsRevision() {
		return dctmIsRevision;
	}

	public void setDctmIsRevision(int dctmIsRevision) {
		this.dctmIsRevision = dctmIsRevision;
	}

	public String getDctmCustomerId() {
		return dctmCustomerId;
	}

	public void setDctmCustomerId(String dctmCustomerId) {
		this.dctmCustomerId = dctmCustomerId;
	}

	public String getDctmBranchId() {
		return dctmBranchId;
	}

	public void setDctmBranchId(String dctmBranchId) {
		this.dctmBranchId = dctmBranchId;
	}

	public String getDctmLeadsId() {
		return dctmLeadsId;
	}

	public void setDctmLeadsId(String dctmLeadsId) {
		this.dctmLeadsId = dctmLeadsId;
	}
	
}

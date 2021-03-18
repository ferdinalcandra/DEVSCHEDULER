package id.co.bfi.dmsuploadscheduler.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DctmCreateObjectPropertiesRequest {
	
	@JsonProperty("object_name")
	private String objectName;
	
	@JsonProperty("r_object_type")
	private String objectType;

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	
}

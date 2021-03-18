package id.co.bfi.dmsuploadscheduler.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DctmUploadRequest {
	
	@JsonProperty("properties")
	private DctmUploadPropertiesRequest dctmUploadPropertiesRequest;

	public DctmUploadPropertiesRequest getDctmUploadPropertiesRequest() {
		return dctmUploadPropertiesRequest;
	}

	public void setDctmUploadPropertiesRequest(DctmUploadPropertiesRequest dctmUploadPropertiesRequest) {
		this.dctmUploadPropertiesRequest = dctmUploadPropertiesRequest;
	}
	
}

package id.co.bfi.dmsuploadscheduler.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DctmUploadResponse {

	@JsonProperty("properties")
	private DctmUploadPropertiesResponse dctmUploadPropertiesResponse;

	public DctmUploadPropertiesResponse getDctmUploadPropertiesResponse() {
		return dctmUploadPropertiesResponse;
	}

	public void setDctmUploadPropertiesResponse(DctmUploadPropertiesResponse dctmUploadPropertiesResponse) {
		this.dctmUploadPropertiesResponse = dctmUploadPropertiesResponse;
	}
	
}

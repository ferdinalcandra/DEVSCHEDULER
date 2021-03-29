package id.co.bfi.dmsuploadscheduler.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DctmUploadVersionResponse {

	@JsonProperty("properties")
	private DctmUploadVersionPropertiesResponse dctmUploadVersionPropertiesResponse;

	public DctmUploadVersionPropertiesResponse getDctmUploadVersionPropertiesResponse() {
		return dctmUploadVersionPropertiesResponse;
	}

	public void setDctmUploadVersionPropertiesResponse(
			DctmUploadVersionPropertiesResponse dctmUploadVersionPropertiesResponse) {
		this.dctmUploadVersionPropertiesResponse = dctmUploadVersionPropertiesResponse;
	}

}

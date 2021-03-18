package id.co.bfi.dmsuploadscheduler.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DctmDqlContentResponse {
	
	@JsonProperty("properties")
	private DctmDqlPropertiesResponse dctmDqlPropertiesResponse;

	public DctmDqlPropertiesResponse getDctmDqlPropertiesResponse() {
		return dctmDqlPropertiesResponse;
	}

	public void setDctmDqlPropertiesResponse(DctmDqlPropertiesResponse dctmDqlPropertiesResponse) {
		this.dctmDqlPropertiesResponse = dctmDqlPropertiesResponse;
	}
	
}

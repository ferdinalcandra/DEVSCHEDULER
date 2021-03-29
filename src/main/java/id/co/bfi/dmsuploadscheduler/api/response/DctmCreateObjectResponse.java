package id.co.bfi.dmsuploadscheduler.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DctmCreateObjectResponse {

	@JsonProperty("properties")
	private DctmCreateObjectPropertiesResponse dctmCreateObjectPropertiesResponse;

	public DctmCreateObjectPropertiesResponse getDctmCreateObjectPropertiesResponse() {
		return dctmCreateObjectPropertiesResponse;
	}

	public void setDctmCreateObjectPropertiesResponse(
			DctmCreateObjectPropertiesResponse dctmCreateObjectPropertiesResponse) {
		this.dctmCreateObjectPropertiesResponse = dctmCreateObjectPropertiesResponse;
	}

}

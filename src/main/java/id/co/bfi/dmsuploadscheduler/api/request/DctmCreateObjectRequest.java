package id.co.bfi.dmsuploadscheduler.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DctmCreateObjectRequest {

	@JsonProperty("properties")
	private DctmCreateObjectPropertiesRequest dctmCreateObjectPropertiesRequest;

	public DctmCreateObjectPropertiesRequest getDctmCreateObjectPropertiesRequest() {
		return dctmCreateObjectPropertiesRequest;
	}

	public void setDctmCreateObjectPropertiesRequest(
			DctmCreateObjectPropertiesRequest dctmCreateObjectPropertiesRequest) {
		this.dctmCreateObjectPropertiesRequest = dctmCreateObjectPropertiesRequest;
	}

}

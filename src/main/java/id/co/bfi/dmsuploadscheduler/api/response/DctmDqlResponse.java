package id.co.bfi.dmsuploadscheduler.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DctmDqlResponse {

	@JsonProperty(value = "entries", required = false)
	private List<DctmDqlEntriesResponse> dctmDqlEntriesResponse;

	public List<DctmDqlEntriesResponse> getDctmDqlEntriesResponse() {
		return dctmDqlEntriesResponse;
	}

	public void setDctmDqlEntriesResponse(List<DctmDqlEntriesResponse> dctmDqlEntriesResponse) {
		this.dctmDqlEntriesResponse = dctmDqlEntriesResponse;
	}

}
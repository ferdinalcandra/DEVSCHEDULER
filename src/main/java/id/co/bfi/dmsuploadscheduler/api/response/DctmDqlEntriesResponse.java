package id.co.bfi.dmsuploadscheduler.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DctmDqlEntriesResponse {
	
	@JsonProperty("title")
	private String title;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@JsonProperty("content")
	private DctmDqlContentResponse dctmDqlContentResponse;

	public DctmDqlContentResponse getDctmDqlContentResponse() {
		return dctmDqlContentResponse;
	}

	public void setDctmDqlContentResponse(DctmDqlContentResponse dctmDqlContentResponse) {
		this.dctmDqlContentResponse = dctmDqlContentResponse;
	}
	
}

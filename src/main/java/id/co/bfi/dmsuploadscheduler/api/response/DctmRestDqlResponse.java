package id.co.bfi.dmsuploadscheduler.api.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DctmRestDqlResponse {
	
	private List<Map<String, Object>> entries;

	public List<Map<String, Object>> getEntries() {
		return entries;
	}

	public void setEntries(List<Map<String, Object>> entries) {
		this.entries = entries;
	}
	
}

package id.co.bfi.dmsuploadscheduler.service.dctm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;

@Service
public class DctmRestService {
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	DctmRestConfig dctmRestConfig; 
	
	public JSONObject makeRequest(String url, HttpMethod method) throws JSONException {
		HttpHeaders headers = new HttpHeaders();
	    headers.set("Accept", dctmRestConfig.getContentType());
	    HttpEntity<String> entity = new HttpEntity<String>(headers);
	    ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
	    JSONObject jsonObject = new JSONObject(response.getBody());
	    return jsonObject;
	}
	
	public String getAttributeFromDql(String dql) throws JSONException {
	    JSONObject theObject = makeRequest(dctmRestConfig.getUrl()+"/repositories/"+ 
	    		dctmRestConfig.getRepositoryName()+"?dql="+dql, HttpMethod.GET);
	    String attribute = null;
	    if (theObject != null) {
	    	if (theObject.has("entries")) {
	    		JSONArray jsonArray = theObject.getJSONArray("entries");
		    	for (int j = 0; j < jsonArray.length(); j++) {
		    		JSONObject ja = (JSONObject)jsonArray.get(j);
		    		for (int i = 0; i < ja.length(); i++)
		    			attribute = (String)ja.get("title"); 
		    	} 
	    	}
	    } 
	    return attribute;
	}
	
}

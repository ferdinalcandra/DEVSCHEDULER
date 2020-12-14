package id.co.bfi.dmsuploadscheduler.service.dctm;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.QueryConfig;

@Service
public class DctmDocumentService {
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	DctmRestConfig dctmRestConfig; 
	
	@Autowired
	QueryConfig queryConfig;
	
	@Autowired
	DctmRestService dctmRestService;
	
	public JSONObject checkoutDocument(String url) throws JSONException {
		return dctmRestService.makeRequest(url, HttpMethod.PUT);
	}
	
	public JSONObject cancelCheckoutDocument(String url) throws JSONException {
		return dctmRestService.makeRequest(url, HttpMethod.DELETE);
	}
	
	public JSONObject uploadDocument(String properties, String documentByte, String mimeType, String folderId) throws JSONException {
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		parameters.add("metadata", new JSONObject().put("properties", new JSONObject(properties)).toString());
		parameters.add("content", Base64.getDecoder().decode(documentByte.replaceAll(" ", "+")));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		String contentType = dctmRestService.getAttributeFromDql(queryConfig.getContentTypeDql()+"'"+mimeType+"'");
		String result = restTemplate.postForObject(dctmRestConfig.getUrl()+"/repositories/"+dctmRestConfig.getRepositoryName()+
				"/folders/"+folderId+"/documents?format="+contentType, 
		    new HttpEntity<MultiValueMap<String, Object>>(parameters, headers), String.class);
	    return new JSONObject(result);
	}
	
	public JSONObject uploadVersionDocument(String chronicleId, String properties, String documentByte, String mimeType) throws JSONException, UnsupportedEncodingException {
		String isDocumentCheckoutDql = "select r_lock_owner from "+new JSONObject(properties).get("r_object_type").toString()+"(all) where r_object_id = '"+chronicleId+"'";
		if (!dctmRestService.getAttributeFromDql(isDocumentCheckoutDql).equals("")) {
			cancelCheckoutDocument(dctmRestConfig.getUrl()+"/repositories/"+dctmRestConfig.getRepositoryName()+"/objects/"+chronicleId+"/lock");
		}
		checkoutDocument(dctmRestConfig.getUrl()+"/repositories/"+dctmRestConfig.getRepositoryName()+"/objects/"+chronicleId+"/lock");
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		parameters.add("content", Base64.getDecoder().decode(documentByte.replaceAll(" ", "+")));
		parameters.add("metadata", new JSONObject().put("properties", new JSONObject(properties)).toString());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		String contentType = dctmRestService.getAttributeFromDql(queryConfig.getContentTypeDql()+"'"+mimeType+"'");
		String result = restTemplate.postForObject(dctmRestConfig.getUrl()+"/repositories/"+dctmRestConfig.getRepositoryName()+"/objects/"+chronicleId+
				"/versions?object-id="+chronicleId+"&version-policy=next-major&format="+contentType, 
		    new HttpEntity<MultiValueMap<String, Object>>(parameters, headers), String.class);
		return new JSONObject(result);
	}
	
	public String getFullFileSystemPath(String objectId) throws JSONException {
		String fullFileSystemPath = null;
		String docbaseParamDql = queryConfig.getDocbaseParamDql();
		JSONObject resultObject = dctmRestService.makeRequest(dctmRestConfig.getUrl()+"/repositories/"+ 
	    		dctmRestConfig.getRepositoryName()+"?dql="+docbaseParamDql.replace("parentId", objectId), HttpMethod.GET);
		JSONObject paramObject = null;
		if (resultObject != null) {
	    	if (resultObject.has("entries")) {
	    		JSONArray jsonArray = resultObject.getJSONArray("entries");
		    	for (int j = 0; j < jsonArray.length(); j++) {
		    		JSONObject jsonObject = (JSONObject)jsonArray.get(j);
		    		for (int i = 0; i < jsonObject.length(); i++)
		    			paramObject = jsonObject.getJSONObject("content").getJSONObject("properties"); 
		    	} 
	    	}
	    }
		if (paramObject != null) {
			int dataTicket = paramObject.getInt("data_ticket");
			String dosExtension = paramObject.getString("dos_extension");
			String fileSystemPath = paramObject.getString("file_system_path");
			int docbaseId = paramObject.getInt("r_docbase_id");
			String dbId = new String().valueOf(docbaseId);
			while (dbId.length() < 8) {
				dbId = "0"+dbId;
			}
			StringBuilder sb = new StringBuilder(fileSystemPath+"\\"+dbId);
//			String dbId = dctmConfigProp.getProperty("specific_docbase_id");
//			StringBuilder sb = new StringBuilder(fileSystemPath+"\\"+dbId);
			double dataTicketTemp = dataTicket + Math.pow(2.00, 32.00);
			String hexStr = Long.toHexString((long)dataTicketTemp);
			String[] hexStrArr = hexStr.replaceAll("..(?!$)", "$0 ").split(" ");
			if (hexStrArr.length > 0) {
				int count = 1;
				for(int i=0; i<hexStrArr.length; i++) {
					if (count < hexStrArr.length)
						sb.append("\\"+hexStrArr[i]);
					else
						sb.append(hexStrArr[i]);
				}
				sb.append("."+dosExtension);
				fullFileSystemPath = sb.toString();
			}
		}
		return fullFileSystemPath.toString();
	}
	
}

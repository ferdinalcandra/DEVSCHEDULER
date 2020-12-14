package id.co.bfi.dmsuploadscheduler.service.dctm;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;

@Service
public class DctmFolderService {
	
	@Autowired
	DctmRestService dctmRestService;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	DctmRestConfig dctmRestConfig;
	
	public String createFolderByPath(String path) throws JSONException {
		String folderId = null;
		if (path.substring(0, 1).equals("/")) {
			String[] folders = path.split("/");
			String checkedPath = "/" + folders[1];
			String cabinetId = null;
			String dqlCabinetCheck = "select r_object_id from dm_cabinet where object_name = '"+folders[1]+"' and "
					+ "any r_folder_path = '"+checkedPath+"'";
			if (dctmRestService.getAttributeFromDql(dqlCabinetCheck) == null)
				cabinetId = createCabinet(folders[1]);
			for (int i=2; i<folders.length; ++i) {
				checkedPath = checkedPath + "/" + folders[i];
				String dqlFolderCheck = "select r_object_id from dm_folder where object_name = '"+folders[i]+"' and "
						+ "any r_folder_path = '"+checkedPath+"'";
				if (dctmRestService.getAttributeFromDql(dqlFolderCheck) == null) {
					folderId = createFolder(folders[i], cabinetId);
					if (folderId != null)
						cabinetId = folderId;
				}
			}
		}
		return folderId;
	}
	
	public String createCabinet(String cabinetName) throws JSONException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(dctmRestConfig.getContentType()));
		HttpEntity<String> entity = new HttpEntity<String>(new JSONObject().put("properties", 
				new JSONObject().put("object_name", cabinetName)).toString(), headers);
		String result = restTemplate.postForObject(dctmRestConfig.getUrl()+"/repositories/"+dctmRestConfig.getRepositoryName()+
				"/cabinets", entity, String.class);
	    String objectId = null;
		if (new JSONObject(result) != null) {
	    	if (new JSONObject(result).has("properties")) 
	    		objectId = new JSONObject(result).getJSONObject("properties").getString("r_object_id");
	    }
		return objectId;
	}
	
	public String createFolder(String folderName, String folderParentId) throws JSONException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.valueOf(dctmRestConfig.getContentType()));
		HttpEntity<String> entity = new HttpEntity<String>(new JSONObject().put("properties", 
				new JSONObject().put("object_name", folderName).put("r_object_type", "dm_folder")).toString(), headers);
		String result = restTemplate.postForObject(dctmRestConfig.getUrl()+"/repositories/"+dctmRestConfig.getRepositoryName()+
				"/folders/"+folderParentId+"/objects", entity, String.class);
	    String objectId = null;
		if (new JSONObject(result) != null) {
	    	if (new JSONObject(result).has("properties")) 
	    		objectId = new JSONObject(result).getJSONObject("properties").getString("r_object_id");
	    }
		return objectId;
	}
}

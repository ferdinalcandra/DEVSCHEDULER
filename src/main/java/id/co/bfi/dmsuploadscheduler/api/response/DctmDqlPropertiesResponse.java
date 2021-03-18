package id.co.bfi.dmsuploadscheduler.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DctmDqlPropertiesResponse {
	
	@JsonProperty("r_object_id")
	private String objectId;
	
	@JsonProperty("data_ticket")
	private int dataTicket;
    
	@JsonProperty("dos_extension")
	private String dosExtension;
    
	@JsonProperty("file_system_path")
	private String fileSystemPath;
    
	@JsonProperty("r_docbase_id")
	private int docbaseId;
	
	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public int getDataTicket() {
		return dataTicket;
	}

	public void setDataTicket(int dataTicket) {
		this.dataTicket = dataTicket;
	}

	public String getDosExtension() {
		return dosExtension;
	}

	public void setDosExtension(String dosExtension) {
		this.dosExtension = dosExtension;
	}

	public String getFileSystemPath() {
		return fileSystemPath;
	}

	public void setFileSystemPath(String fileSystemPath) {
		this.fileSystemPath = fileSystemPath;
	}

	public int getDocbaseId() {
		return docbaseId;
	}

	public void setDocbaseId(int docbaseId) {
		this.docbaseId = docbaseId;
	}
	
}

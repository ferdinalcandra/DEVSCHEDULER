package id.co.bfi.dmsuploadscheduler.config.yaml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "query")
@PropertySource(value = "file:conf/query.yml", factory = YamlPropertySourceFactory.class)
public class QueryConfig {

	private String docbaseParamDql;

	private String contentTypeDql;

	private String chronicleIdCheckDql;

	private String isDocumentCheckoutDql;

	private String cabinetCheckDql;

	private String folderCheckDql;

	public String getDocbaseParamDql() {
		return docbaseParamDql;
	}

	public void setDocbaseParamDql(String docbaseParamDql) {
		this.docbaseParamDql = docbaseParamDql;
	}

	public String getContentTypeDql() {
		return contentTypeDql;
	}

	public void setContentTypeDql(String contentTypeDql) {
		this.contentTypeDql = contentTypeDql;
	}

	public String getChronicleIdCheckDql() {
		return chronicleIdCheckDql;
	}

	public void setChronicleIdCheckDql(String chronicleIdCheckDql) {
		this.chronicleIdCheckDql = chronicleIdCheckDql;
	}

	public String getIsDocumentCheckoutDql() {
		return isDocumentCheckoutDql;
	}

	public void setIsDocumentCheckoutDql(String isDocumentCheckoutDql) {
		this.isDocumentCheckoutDql = isDocumentCheckoutDql;
	}

	public String getCabinetCheckDql() {
		return cabinetCheckDql;
	}

	public void setCabinetCheckDql(String cabinetCheckDql) {
		this.cabinetCheckDql = cabinetCheckDql;
	}

	public String getFolderCheckDql() {
		return folderCheckDql;
	}

	public void setFolderCheckDql(String folderCheckDql) {
		this.folderCheckDql = folderCheckDql;
	}

}

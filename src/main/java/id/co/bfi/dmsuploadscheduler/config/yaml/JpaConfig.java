package id.co.bfi.dmsuploadscheduler.config.yaml;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "dms.jpa")
@PropertySource(value = "file:conf/dms.yml", factory = YamlPropertySourceFactory.class)
public class JpaConfig {

	private String databasePlatform;

	private String showSql;

	public String getDatabasePlatform() {
		return databasePlatform;
	}

	public void setDatabasePlatform(String databasePlatform) {
		this.databasePlatform = databasePlatform;
	}

	public String getShowSql() {
		return showSql;
	}

	public void setShowSql(String showSql) {
		this.showSql = showSql;
	}

}

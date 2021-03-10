package id.co.bfi.dmsuploadscheduler.repository;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.JpaRepository;
import id.co.bfi.dmsuploadscheduler.config.yaml.YamlPropertySourceFactory;
import id.co.bfi.dmsuploadscheduler.entity.DctmUploadDocumentHistoryEntity;

@Configuration
@PropertySource(value = "file:conf/query.yml", factory = YamlPropertySourceFactory.class)
public interface DctmUploadDocumentHistoryRepository extends JpaRepository<DctmUploadDocumentHistoryEntity, String> {
	
	DctmUploadDocumentHistoryEntity findByDctmUploadDocumentHistoryId(String dctmUploadDocumentHistoryId);

}

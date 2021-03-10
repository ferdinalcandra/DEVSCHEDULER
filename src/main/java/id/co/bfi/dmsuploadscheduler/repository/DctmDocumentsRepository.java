package id.co.bfi.dmsuploadscheduler.repository;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.JpaRepository;

import id.co.bfi.dmsuploadscheduler.config.yaml.YamlPropertySourceFactory;
import id.co.bfi.dmsuploadscheduler.entity.DctmDocumentsEntity;

@Configuration
@PropertySource(value = "file:conf/query.yml", factory = YamlPropertySourceFactory.class)
public interface DctmDocumentsRepository extends JpaRepository<DctmDocumentsEntity, String> {

	DctmDocumentsEntity findByDctmDocumentsId(String dctmDocumentsId);

}

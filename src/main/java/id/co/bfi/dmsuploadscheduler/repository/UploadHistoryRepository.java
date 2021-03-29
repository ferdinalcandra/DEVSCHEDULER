package id.co.bfi.dmsuploadscheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;

public interface UploadHistoryRepository extends JpaRepository<UploadHistoryEntity, String> {

	List<UploadHistoryEntity> findUploadHistoryByDctmId(@Param("dctmDocumentsId") String dctmDocumentsId);
	
}

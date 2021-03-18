package id.co.bfi.dmsuploadscheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;

public interface UploadHistoryRepository extends JpaRepository<UploadHistoryEntity, String> {
}

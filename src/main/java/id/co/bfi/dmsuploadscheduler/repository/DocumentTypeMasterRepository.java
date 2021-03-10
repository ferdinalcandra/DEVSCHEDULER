package id.co.bfi.dmsuploadscheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeMasterEntity;

@Repository
public interface DocumentTypeMasterRepository extends JpaRepository<DocumentTypeMasterEntity, Long> {
	
	DocumentTypeMasterEntity findBySqDocumentTypeMst(long sqDocumentTypeMst);

}

package id.co.bfi.dmsuploadscheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.co.bfi.dmsuploadscheduler.entity.DocumentTypeEntity;

@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, Long> {
	
	DocumentTypeEntity findByDocTypeId(long docTypeId);

}

package id.co.bfi.dmsuploadscheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {
}

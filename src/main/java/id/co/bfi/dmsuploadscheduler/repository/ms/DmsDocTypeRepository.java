package id.co.bfi.dmsuploadscheduler.repository.ms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.co.bfi.dmsuploadscheduler.model.ms.DmsDocType;

@Repository
public interface DmsDocTypeRepository extends JpaRepository<DmsDocType, String>{
	DmsDocType findByDmsDocTypeId(String dmsDocTypeId);
}

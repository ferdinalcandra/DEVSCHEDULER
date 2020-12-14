package id.co.bfi.dmsuploadscheduler.repository.ms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.co.bfi.dmsuploadscheduler.model.ms.DmsGroupDoc;

@Repository
public interface DmsGroupDocRepository extends JpaRepository<DmsGroupDoc, String> {

}

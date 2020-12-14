package id.co.bfi.dmsuploadscheduler.repository.ms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.co.bfi.dmsuploadscheduler.model.ms.DmsBranch;

@Repository
public interface DmsBranchRepository extends JpaRepository<DmsBranch, String>{
	DmsBranch findByDmsBranchId(String dmsBranchId);
}

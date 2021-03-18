package id.co.bfi.dmsuploadscheduler.query.action;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;

@Component
public class UploadHistoryAction {
	
	@Autowired
	private EntityManager entityManager;
	
	public List<UploadHistoryEntity> getUploadHistoryList(String dctmDocumentsId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<UploadHistoryEntity> criteriaQuery = criteriaBuilder.createQuery(UploadHistoryEntity.class);
		
		Root<UploadHistoryEntity> root = criteriaQuery.from(UploadHistoryEntity.class);
		
		Predicate dctmDocumentsIdPredicate = criteriaBuilder.equal(root.get("dctmDocumentsId"), dctmDocumentsId);
		
		Predicate statusReadyPredicate = criteriaBuilder.equal(root.get("statusUpload"), "ready");
		Predicate statusFailedPredicate = criteriaBuilder.equal(root.get("statusUpload"), "failed");
		Predicate statusUploadPredicate = criteriaBuilder.or(statusReadyPredicate, statusFailedPredicate);
		
		Predicate dctmIdNullPredicate = criteriaBuilder.isNull(root.get("dctmId"));
		Predicate predicate = criteriaBuilder.and(statusUploadPredicate, dctmIdNullPredicate);
		
		Predicate finalPredicate = criteriaBuilder.and(predicate, dctmDocumentsIdPredicate);
		
		criteriaQuery.where(finalPredicate);
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createdDate")));
		
		return entityManager.createQuery(criteriaQuery).getResultList();
	}
	
}

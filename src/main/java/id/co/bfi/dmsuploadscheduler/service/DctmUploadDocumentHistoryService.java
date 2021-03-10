package id.co.bfi.dmsuploadscheduler.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.co.bfi.dmsuploadscheduler.entity.DctmUploadDocumentHistoryEntity;

@Service
public class DctmUploadDocumentHistoryService {
	
	@Autowired
	EntityManager entityManager;
	
	public List<DctmUploadDocumentHistoryEntity> getDctmUploadDocumentHistoryList(String dctmDocumentsId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DctmUploadDocumentHistoryEntity> criteriaQuery = criteriaBuilder.createQuery(DctmUploadDocumentHistoryEntity.class);
		Root<DctmUploadDocumentHistoryEntity> root = criteriaQuery.from(DctmUploadDocumentHistoryEntity.class);
		
		Predicate dctmDocumentsIdPredicate = criteriaBuilder.equal(root.get("dctmDocumentsId"), dctmDocumentsId);
		
		Predicate statusUploadReadyPredicate = criteriaBuilder.equal(root.get("statusUpload"), "ready");
		Predicate statusUploadFailedPredicate = criteriaBuilder.equal(root.get("statusUpload"), "failed");
		Predicate statusUploadPredicate = criteriaBuilder.or(statusUploadReadyPredicate, statusUploadFailedPredicate);
		
		Predicate dctmIdNullPredicate = criteriaBuilder.isNull(root.get("dctmId"));
		Predicate predicate = criteriaBuilder.and(statusUploadPredicate, dctmIdNullPredicate);
		
		Predicate finalPredicate = criteriaBuilder.and(predicate, dctmDocumentsIdPredicate);
		
		criteriaQuery.where(finalPredicate);
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createdDate")));
		List<DctmUploadDocumentHistoryEntity> dctmUploadDocumentHistoryList = entityManager.createQuery(criteriaQuery).getResultList();
		return dctmUploadDocumentHistoryList;
	}
	
}

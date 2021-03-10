package id.co.bfi.dmsuploadscheduler.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.co.bfi.dmsuploadscheduler.entity.DctmDocumentsEntity;

@Service
public class DctmDocumentsService {

	@Autowired
	EntityManager entityManager;
	
	public List<DctmDocumentsEntity> getDctmDocumentsList() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DctmDocumentsEntity> criteriaQuery = criteriaBuilder.createQuery(DctmDocumentsEntity.class);
		Root<DctmDocumentsEntity> itemRoot = criteriaQuery.from(DctmDocumentsEntity.class);
		
		criteriaQuery.orderBy(criteriaBuilder.asc(itemRoot.get("createdDate")));
		List<DctmDocumentsEntity> dctmDocumentsList = entityManager.createQuery(criteriaQuery).getResultList();
		return dctmDocumentsList;
	}
	
}

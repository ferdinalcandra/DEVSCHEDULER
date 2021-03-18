package id.co.bfi.dmsuploadscheduler.query.action;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;

@Component
public class DocumentAction {
	
	@Autowired
	private EntityManager entityManager;
	
	public List<DocumentEntity> getDocumentList() {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DocumentEntity> criteriaQuery = criteriaBuilder.createQuery(DocumentEntity.class);
		
		Root<DocumentEntity> root = criteriaQuery.from(DocumentEntity.class);
		
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createdDate")));
		
		return entityManager.createQuery(criteriaQuery).getResultList();
	}
	
}

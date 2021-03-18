package id.co.bfi.dmsuploadscheduler.query.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.co.bfi.dmsuploadscheduler.entity.DocumentEntity;
import id.co.bfi.dmsuploadscheduler.query.action.DocumentAction;

@Service
public class DocumentService {

	@Autowired
	private DocumentAction documentAction;
	
	public List<DocumentEntity> getDocumentList() {
		return documentAction.getDocumentList();
	}
	
}

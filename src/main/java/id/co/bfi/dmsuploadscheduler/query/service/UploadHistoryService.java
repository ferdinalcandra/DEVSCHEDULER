package id.co.bfi.dmsuploadscheduler.query.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.co.bfi.dmsuploadscheduler.entity.UploadHistoryEntity;
import id.co.bfi.dmsuploadscheduler.query.action.UploadHistoryAction;

@Service
public class UploadHistoryService {
	
	@Autowired
	private UploadHistoryAction uploadHistoryAction;
	
	public List<UploadHistoryEntity> getUploadHistoryList(String dctmDocumentsId) {
		return uploadHistoryAction.getUploadHistoryList(dctmDocumentsId);
	}
	
}

package id.co.bfi.dmsuploadscheduler.query.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.co.bfi.dmsuploadscheduler.api.request.DmsDoUploadRequest;
import id.co.bfi.dmsuploadscheduler.query.action.DocumentAction;

@Service
public class DocumentService {

	@Autowired
	private DocumentAction documentAction;

	public void updateData(DmsDoUploadRequest doUploadRequest) {
		documentAction.updateData(doUploadRequest);
	}

}

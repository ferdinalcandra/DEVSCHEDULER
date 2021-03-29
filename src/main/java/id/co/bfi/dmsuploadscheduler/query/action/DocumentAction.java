package id.co.bfi.dmsuploadscheduler.query.action;

import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import id.co.bfi.dmsuploadscheduler.api.request.DmsDoUploadRequest;
import id.co.bfi.dmsuploadscheduler.repository.DocumentRepository;

@Component
public class DocumentAction {

	@Autowired
	private DocumentRepository documentRepository;

	public void updateData(DmsDoUploadRequest doUploadRequest) {
		int index = doUploadRequest.getDocumentListIndex();
		String fullSystemPath = doUploadRequest.getFullSystemPath();
		if (fullSystemPath != null) {
			doUploadRequest.getDocumentList().get(index)
			.setPathFileName(Base64.getEncoder().encodeToString(fullSystemPath.getBytes()));
		}
		doUploadRequest.getDocumentList().get(index).setDctmId(doUploadRequest.getObjectId());
		doUploadRequest.getDocumentList().get(index).setDocumentLastModifiedDate(new Date());
		doUploadRequest.getDocumentList().get(index).setDocumentLastModifiedBy("dms scheduler");

		documentRepository.save(doUploadRequest.getDocumentList().get(index));
	}

}

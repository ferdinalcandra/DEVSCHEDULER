package id.co.bfi.dmsuploadscheduler.query.action;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import id.co.bfi.dmsuploadscheduler.api.request.DmsDoUploadRequest;
import id.co.bfi.dmsuploadscheduler.repository.UploadHistoryRepository;

@Component
public class UploadHistoryAction {

	@Autowired
	private UploadHistoryRepository uploadHistoryRepository;

	public void updateData(DmsDoUploadRequest doUploadRequest, List<String> msg)
			throws JsonProcessingException {
		int index = doUploadRequest.getUploadHistoryListIndex();
		doUploadRequest.getUploadHistoryList().get(index).setDctmId(doUploadRequest.getObjectId());
		doUploadRequest.getUploadHistoryList().get(index).setStatusUpload(doUploadRequest.getUploadStatus());
		doUploadRequest.getUploadHistoryList().get(index).setMsg(msg.toString());

		String fullSystemPath = doUploadRequest.getFullSystemPath();
		if (fullSystemPath != null) {
			doUploadRequest.getUploadHistoryList().get(index)
			.setPathFileName(Base64.getEncoder().encodeToString(fullSystemPath.getBytes()));
		}
		doUploadRequest.getUploadHistoryList().get(index).setUploadHistoryLastModifiedDate(new Date());
		doUploadRequest.getUploadHistoryList().get(index).setUploadHistoryLastModifiedBy("dms scheduler");

		uploadHistoryRepository.save(doUploadRequest.getUploadHistoryList().get(index));
	}

}

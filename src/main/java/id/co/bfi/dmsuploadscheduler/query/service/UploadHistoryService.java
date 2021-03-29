package id.co.bfi.dmsuploadscheduler.query.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import id.co.bfi.dmsuploadscheduler.api.request.DmsDoUploadRequest;
import id.co.bfi.dmsuploadscheduler.query.action.UploadHistoryAction;

@Service
public class UploadHistoryService {

	@Autowired
	private UploadHistoryAction uploadHistoryAction;

	public void updateData(DmsDoUploadRequest doUploadRequest, List<String> msg) throws JsonProcessingException {
		uploadHistoryAction.updateData(doUploadRequest, msg);
	}

}

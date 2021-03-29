package id.co.bfi.dmsuploadscheduler.query.service.dctm_rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import id.co.bfi.dmsuploadscheduler.api.response.DctmUploadResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmUploadVersionResponse;
import id.co.bfi.dmsuploadscheduler.query.action.dctm_rest.DctmRestDocumentAction;

@Service
public class DctmRestDocumentService {

	@Autowired
	private DctmRestDocumentAction dctmRestDocumentAction;

	public DctmUploadResponse uploadDocument(String properties, String documentByte, String mimeType, String folderId,
			List<String> msg) throws JsonProcessingException {
		return dctmRestDocumentAction.uploadDocument(properties, documentByte, mimeType, folderId, msg);
	}

	public DctmUploadVersionResponse uploadVersionDocument(String chronicleId, String properties, String objectType,
			String documentByte, String mimeType, List<String> msg)
			throws JsonProcessingException {
		return dctmRestDocumentAction.uploadVersionDocument(chronicleId, properties, objectType, documentByte, mimeType,
				msg);
	}

	public String getFullFileSystemPath(String objectId)
			throws JsonProcessingException {
		return dctmRestDocumentAction.getFullFileSystemPath(objectId);
	}

}

package id.co.bfi.dmsuploadscheduler.query.service.dctm_rest;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import id.co.bfi.dmsuploadscheduler.api.response.DctmUploadResponse;
import id.co.bfi.dmsuploadscheduler.api.response.DctmUploadVersionResponse;
import id.co.bfi.dmsuploadscheduler.query.action.dctm_rest.DctmRestDocumentAction;

@Service
public class DctmRestDocumentService {

	@Autowired
	private DctmRestDocumentAction dctmRestDocumentAction;

	Logger logger = LoggerFactory.getLogger(DctmRestDocumentService.class);

	public DctmUploadResponse uploadDocument(String properties, String documentByte, String mimeType, String folderId,
			List<String> msg) throws JsonMappingException, JsonProcessingException {
		return dctmRestDocumentAction.uploadDocument(properties, documentByte, mimeType, folderId, msg);
	}

	public DctmUploadVersionResponse uploadVersionDocument(String chronicleId, String properties, String objectType,
			String documentByte, String mimeType, List<String> msg)
			throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		return dctmRestDocumentAction.uploadVersionDocument(chronicleId, properties, objectType, documentByte, mimeType, msg);
	}

	public String getFullFileSystemPath(String objectId, List<String> msg)
			throws JsonMappingException, JsonProcessingException {
		return dctmRestDocumentAction.getFullFileSystemPath(objectId, msg);
	}

}

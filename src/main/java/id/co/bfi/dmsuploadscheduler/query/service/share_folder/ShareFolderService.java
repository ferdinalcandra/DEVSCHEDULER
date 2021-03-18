package id.co.bfi.dmsuploadscheduler.query.service.share_folder;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.co.bfi.dmsuploadscheduler.config.yaml.JasyptConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.ShareFolderConfig;
import id.co.bfi.dmsuploadscheduler.query.action.share_folder.ShareFolderAction;

@Service
public class ShareFolderService {

	@Autowired
	private ShareFolderConfig shareFolderConfig;

	@Autowired
	private JasyptConfig jasyptConfig;

	@Autowired
	private ShareFolderAction shareFolderAction;

	public ByteArrayOutputStream getFileOverSharedFolder(String filePath, List<String> msg) {
		final String host = shareFolderConfig.getHost();
		final String domain = shareFolderConfig.getDomain();
		final String username = shareFolderConfig.getUser();
		final String password = jasyptConfig.decryptPassword(shareFolderConfig.getPass());

		return shareFolderAction.getFileOverSharedFolder(host, domain, username, password, filePath, msg);
	}

	public boolean validateFile(byte[] file, String mimeType, String fileName, List<String> msg) {
		boolean fileIsValid = false;
		String originalMimeType = shareFolderAction.getMimeTypeFromFile(file, mimeType, fileName, msg);
		if (mimeType.equals(originalMimeType))
			fileIsValid = true;
		else
			msg.add("mime type does not match with the file.");
		return fileIsValid;
	}

}

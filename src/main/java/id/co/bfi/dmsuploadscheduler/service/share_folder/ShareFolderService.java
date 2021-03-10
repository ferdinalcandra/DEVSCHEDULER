package id.co.bfi.dmsuploadscheduler.service.share_folder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;

import id.co.bfi.dmsuploadscheduler.config.yaml.JasyptConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.ShareFolderConfig;

@Service
public class ShareFolderService {

	@Autowired
	private ShareFolderConfig shareFolderConfig;

	@Autowired
	private JasyptConfig jasyptConfig;

	public ByteArrayOutputStream getFileOverSharedFolder(String filePath, List<String> msg) {
		final String username = shareFolderConfig.getUser();
		final String password = jasyptConfig.decryptPassword(shareFolderConfig.getPass());
		final String domain = shareFolderConfig.getDomain();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			filePath = new String(Base64.getDecoder().decode(filePath));
			filePath = filePath.replace(filePath.substring(0, 3), "//" + domain).replace("\\\\", "/").replace("\\", "/");
			
			StaticUserAuthenticator auth = new StaticUserAuthenticator(domain, username, password);
			FileSystemOptions opts = new FileSystemOptions();
			DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
			FileObject fo = VFS.getManager().resolveFile(filePath, opts);

			final InputStream inputStream = fo.getContent().getInputStream();
			
			final byte[] buf = new byte[16 * 1024 * 1024];
			int len;
			while ((len = inputStream.read(buf)) > 0) {
				baos.write(buf, 0, len);
			}
			inputStream.close();
		} catch (Exception e) {
			msg.add(e.getMessage());
		}
		return baos;
	}
	
	public boolean validateFile(byte[] file, String mimeType, String fileName, List<String> msg) {
		boolean fileIsValid = false;
		try {
			String name = new String(Base64.getDecoder().decode(fileName));
			ContentHandler contenthandler = new BodyContentHandler();
		    Metadata metadata = new Metadata();
		    metadata.set(Metadata.RESOURCE_NAME_KEY, name);
		    Parser parser = new AutoDetectParser();
		    InputStream is = new ByteArrayInputStream(file);
		    ParseContext context = new ParseContext();
		    parser.parse(is, contenthandler, metadata, context);
			String mimeTypeFromFile = metadata.get("Content-Type");
		    if (mimeType.equals(mimeTypeFromFile))
		    	fileIsValid = true;
		    else
		    	msg.add("mime type does not match with the file.");
		} catch (Exception e) {
			msg.add(e.getMessage());
		}
		return fileIsValid;
	}
	
}

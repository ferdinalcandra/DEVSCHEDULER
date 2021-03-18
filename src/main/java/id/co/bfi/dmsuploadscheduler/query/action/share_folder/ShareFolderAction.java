package id.co.bfi.dmsuploadscheduler.query.action.share_folder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

@Component
public class ShareFolderAction {

	public ByteArrayOutputStream getFileOverSharedFolder(String host, String domain, String username, String password,
			String filePath, List<String> msg) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			filePath = new String(Base64.getDecoder().decode(filePath));
			filePath = filePath.replace(filePath.substring(0, 3), "smb://" + host).replace("\\\\", "/").replace("\\",
					"/");

			final NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(domain, username, password);
			final CIFSContext context = SingletonContext.getInstance().withCredentials(auth);
			final SmbFile sFile = new SmbFile(filePath, context);
			final SmbFileInputStream inputStream = new SmbFileInputStream(sFile);

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

	public String getMimeTypeFromFile(byte[] file, String mimeType, String fileName, List<String> msg) {
		String originalMimeType = null;
		try {
			String name = new String(Base64.getDecoder().decode(fileName));
			ContentHandler contenthandler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, name);
			Parser parser = new AutoDetectParser();
			InputStream is = new ByteArrayInputStream(file);
			ParseContext context = new ParseContext();
			parser.parse(is, contenthandler, metadata, context);
			originalMimeType = metadata.get("Content-Type");
		} catch (Exception e) {
			msg.add(e.getMessage());
		}
		return originalMimeType;
	}

}

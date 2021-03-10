package id.co.bfi.dmsuploadscheduler.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;

import id.co.bfi.dmsuploadscheduler.config.yaml.YamlPropertySourceFactory;
import id.co.bfi.dmsuploadscheduler.service.DmsUploadService;

@Configuration
@PropertySource(value = "file:conf/application.yml", factory = YamlPropertySourceFactory.class)
public class DmsUploadScheduler {

	@Autowired
	DmsUploadService dmsUploadService;
	
	@Scheduled(cron = "${scheduler.cronExpression}", zone = "Asia/Jakarta")
	private void runDmsUploadScheduler() throws Exception {
		dmsUploadService.processDmsReqUpload();
	}
	
}

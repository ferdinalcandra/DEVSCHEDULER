package id.co.bfi.dmsuploadscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DmsUploadSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DmsUploadSchedulerApplication.class, args);
	}

}

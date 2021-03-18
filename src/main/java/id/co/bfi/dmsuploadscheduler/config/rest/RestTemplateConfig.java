package id.co.bfi.dmsuploadscheduler.config.rest;

import java.nio.charset.Charset;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.JasyptConfig;

@Configuration
public class RestTemplateConfig {
	
	@Autowired
	private DctmRestConfig dctmRestConfig;
	
	@Autowired
	private JasyptConfig jasyptConfig;
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
	    return builder
	        .setConnectTimeout(Duration.ofSeconds(Integer.parseInt(dctmRestConfig.getConnectTimeout())))
	        .setReadTimeout(Duration.ofSeconds(Integer.parseInt(dctmRestConfig.getReadTimeout())))
	        .basicAuthentication(dctmRestConfig.getUser(), jasyptConfig.decryptPassword(dctmRestConfig.getPass()), Charset.forName("UTF-8"))
	        .build();
	}
	
}

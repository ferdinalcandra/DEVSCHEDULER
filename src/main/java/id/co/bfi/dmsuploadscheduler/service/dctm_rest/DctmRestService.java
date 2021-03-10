package id.co.bfi.dmsuploadscheduler.service.dctm_rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import id.co.bfi.dmsuploadscheduler.api.client.DctmRestClient;
import id.co.bfi.dmsuploadscheduler.config.yaml.DctmRestConfig;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class DctmRestService {

	@Autowired
	private DctmRestClient dctmRestClient;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DctmRestConfig dctmRestConfig;

	Logger logger = LoggerFactory.getLogger(DctmRestService.class);

	@Retry(name = "dctmRestService", fallbackMethod = "fallbackRetry")
	public ResponseEntity<String> getDataFromDql(String dql) {
		return dctmRestClient.getDataFromDql(dql);
	}

	@Retry(name = "dctmRestService", fallbackMethod = "fallbackRetry")
	public ResponseEntity<String> createObject(String url, String properties) {
		return dctmRestClient.createObject(url, properties);
	}

	@Retry(name = "dctmRestService", fallbackMethod = "fallbackRetry")
	public ResponseEntity<String> checkoutDocument(String url) {
		return dctmRestClient.checkoutDocument(url);
	}

	@Retry(name = "dctmRestService", fallbackMethod = "fallbackRetry")
	public ResponseEntity<String> cancelCheckoutDocument(String url) {
		return dctmRestClient.cancelCheckoutDocument(url);
	}

	@Retry(name = "dctmRestService", fallbackMethod = "fallbackRetry")
	public ResponseEntity<String> uploadDocument(String folderId, String contentType,
			MultiValueMap<String, Object> parameters, HttpHeaders headers) {
		return restTemplate.exchange(
				dctmRestConfig.getUrl() + "/repositories/" + dctmRestConfig.getRepositoryName() + "/folders/" + folderId
						+ "/documents?format=" + contentType,
				HttpMethod.POST, new HttpEntity<MultiValueMap<String, Object>>(parameters, headers), String.class);
	}

	@Retry(name = "dctmRestService", fallbackMethod = "fallbackRetry")
	public ResponseEntity<String> uploadVersionDocument(String chronicleId, String contentType,
			MultiValueMap<String, Object> parameters, HttpHeaders headers) {
		return restTemplate.exchange(
				dctmRestConfig.getUrl() + "/repositories/" + dctmRestConfig.getRepositoryName() + "/objects/"
						+ chronicleId + "/versions?object-id=" + chronicleId + "&version-policy=next-major&format="
						+ contentType,
				HttpMethod.POST, new HttpEntity<MultiValueMap<String, Object>>(parameters, headers), String.class);
	}

	public ResponseEntity<String> fallbackRetry(Exception e) {
		logger.info("dctmRestService fallback : " + e.getMessage());
		return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}

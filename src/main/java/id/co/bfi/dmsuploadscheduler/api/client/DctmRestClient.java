package id.co.bfi.dmsuploadscheduler.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import id.co.bfi.dmsuploadscheduler.api.request.DctmCreateObjectRequest;
import id.co.bfi.dmsuploadscheduler.config.feign.FeignConfig;
import id.co.bfi.dmsuploadscheduler.config.yaml.YamlPropertySourceFactory;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@PropertySource(value = "file:conf/dctm-rest.yml", factory = YamlPropertySourceFactory.class)
@FeignClient(name = "dctmRestClient", url = "${dctm-rest.url}/repositories/${dctm-rest.repositoryName}", configuration = FeignConfig.class)
public interface DctmRestClient {

	@RequestLine("GET ?dql={dql}")
	ResponseEntity<String> getDataFromDql(@Param(value = "dql") String dql);

	@RequestLine("PUT {url}")
	ResponseEntity<String> checkoutDocument(@Param(value = "url") String url);

	@RequestLine("DELETE {url}")
	ResponseEntity<String> cancelCheckoutDocument(@Param(value = "url") String url);

	@RequestLine("POST {url}")
	@Headers("Content-Type: application/vnd.emc.documentum+json")
	ResponseEntity<String> createObject(@Param(value = "url") String url, @RequestBody DctmCreateObjectRequest dctmCreateObjectRequest);
	
}

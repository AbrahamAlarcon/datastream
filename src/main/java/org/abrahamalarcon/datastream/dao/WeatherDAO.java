package org.abrahamalarcon.datastream.dao;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.abrahamalarcon.datastream.dom.response.BaseError;
import org.abrahamalarcon.datastream.dom.response.DatastoreResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by AbrahamAlarcon on 3/29/2017.
 */
@Repository
public class WeatherDAO
{
    @Autowired RetryTemplate retryRestTemplate;
    @Autowired RestTemplate restTemplate;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Cacheable("stores")
    @HystrixCommand(commandKey = "weather", fallbackMethod = "fallback")
    public DatastoreResponse get(String url) throws URISyntaxException {
        url = this.url + url;
        ResponseEntity<DatastoreResponse> response = callIt(url);
        return response.getBody();
    }

    public DatastoreResponse fallback(String url) throws URISyntaxException {
        DatastoreResponse response = new DatastoreResponse();
        response.setError(new BaseError());
        response.getError().setMessage("Not available!, going thru fallback");
        return response;
    }

    public ResponseEntity<DatastoreResponse> callIt(String url) throws URISyntaxException {
        URI uri = new URI(url);

        return retryRestTemplate.execute(
                new RetryCallback<ResponseEntity<DatastoreResponse>, RuntimeException>()
                {
                    @Override
                    public ResponseEntity<DatastoreResponse> doWithRetry(RetryContext context)
                    {
                        return restTemplate.getForEntity(uri, DatastoreResponse.class);
                    }
                }
        );
    }
}

package io.pivotal.bboe.httpproxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProxyController {
    private static Log logger = LogFactory.getLog(ProxyController.class);

    private String serverUrl;
    RestTemplate restTemplate = new RestTemplate();

    @Value("${server.url}")
    public void setServerUrl(String serverUrl) {
        logger.info("Server URL: " + serverUrl);
        this.serverUrl = serverUrl;
    }


    @RequestMapping("/{input}")
    public String handleRequest(@PathVariable("input") String input) {
        logger.debug("handleRequest(" + input+")");
        String result = doProxyRequest(input);
        logger.debug("handleRequest() -> " + result);
        return result;
    }

    private String doProxyRequest(String input) {
        String url = serverUrl + "/" + input;
        logger.debug("doProxyRequest calling " + url);
        return restTemplate.getForObject(url, String.class);
    }
}

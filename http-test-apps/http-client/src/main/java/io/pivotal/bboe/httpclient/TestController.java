package io.pivotal.bboe.httpclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {
    private static Log logger = LogFactory.getLog(TestController.class);

    private String serverUrl;

    @Value("${server.url}")
    public void setServerUrl(String serverUrl) {
        logger.info("Server URL: " + serverUrl);
        this.serverUrl = serverUrl;
    }

    @RequestMapping("/")
    public String usageString() {
        return "Usage: <test-app-url>/runtest/<iterations>";
    }

    @RequestMapping("/runtest/{iterations}")
    public String runTest(@PathVariable("iterations") int iterations) {
        logger.info("Starting test with " + iterations + " iterations");

        String requestURL = serverUrl + "/ping";
        logger.info("Test URL: " + requestURL);

        RestTemplate restTemplate = new RestTemplate();
        SimpleTimer t = new SimpleTimer();

        for(int i = 0; i < iterations; i++) {
            restTemplate.getForObject(requestURL, String.class);
        }

        String result = generateResultString(iterations, t);

        logger.info(result);
        return result;
    }

    private String generateResultString(int iterations, SimpleTimer timer) {
        double time = timer.getTime();
        String iterationsString = String.format("%,d", iterations);
        String totalTimeString = String.format("%1$,.2f", time);
        String avgLatencyString = String.format("%1$,.4f", time / iterations);
        String txPerSecondString = String.format("%,d", (int)(iterations / time * 1000));


        return "Iterations: " + iterationsString + ", Total Time: " + totalTimeString + "ms, Avg latency: " + avgLatencyString + "ms, TX pr second: " + txPerSecondString;
    }

}

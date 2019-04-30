package io.pivotal.bboe.httpclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        return "Usage: curl <test-app-url>/runtest \\" +
                "\n-d iterations=1000 \\" +
                "\n-d payloadsize=1 \\" +
                "\n-d bigtest=false \\" +
                "\n-d mintestruns=1";
    }

    @RequestMapping("/runtest")
    public String runTest(@RequestParam(value="iterations", defaultValue="100") int iterations,
                          @RequestParam(value="payloadsize", defaultValue="1") int payloadsize,
                          @RequestParam(value="bigtest", defaultValue="false") boolean bigtest,
                          @RequestParam(value="mintestruns", defaultValue="1") int mintestruns) {
        logger.info("Run test. Iterations: " + iterations + " Payload size: " + payloadsize + " Big test: " + bigtest + " Minimum test runs: " + mintestruns);

        String requestURL = serverUrl + "/" + generateTestString(payloadsize);
        logger.info("Test URL: " + requestURL);

        double bestResult = Double.MAX_VALUE;
        int testCounter = 0;
        while(true) {
            logger.info("Run test #" + testCounter);
            double newResult = doRunTest(iterations, requestURL);
            logger.info("Test time: " + String.format("%1$,.2f", newResult) + "ms");

            testCounter++;
            if(testCounter < mintestruns) {
                if(newResult < bestResult) {
                    bestResult = newResult;
                }
            } else {
                if(newResult < bestResult) {
                    bestResult = newResult;
                    if(!bigtest) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        String result = generateResultString(iterations, bestResult);

        logger.info(result);
        return result;
    }

    public double doRunTest(int iterations, String requestURL) {
        RestTemplate restTemplate = new RestTemplate();
        SimpleTimer t = new SimpleTimer();

        for(int i = 0; i < iterations; i++) {
            restTemplate.getForObject(requestURL, String.class);
        }
        return t.getTime();
    }

    private String generateTestString(int length) {
        String result = "";
        for(int i = 0; i < length; i++) {
            result = result + "A";
        }
        return result;
    }

    private String generateResultString(int iterations, double time) {
        String iterationsString = String.format("%,d", iterations);
        String totalTimeString = String.format("%1$,.2f", time);
        String avgLatencyString = String.format("%1$,.4f", time / iterations);
        String txPerSecondString = String.format("%,d", (int)(iterations / time * 1000));

        return "Iterations: " + iterationsString + ", Total Time: " + totalTimeString + "ms, Avg latency: " + avgLatencyString + "ms, TX pr second: " + txPerSecondString;
    }
}

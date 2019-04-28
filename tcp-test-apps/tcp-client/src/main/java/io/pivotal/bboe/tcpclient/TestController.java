package io.pivotal.bboe.tcpclient;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {
    private static Log logger = LogFactory.getLog(TestController.class);

    private String serverAddress;
    private int serverPort;

    private Counter metricCounter;
    private Timer metricTimer;


    @Value("${remote.server.address}")
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
        logger.info("TestExecutor - setting remote server address to " + serverAddress);
    }

    @Value("${remote.server.port}")
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
        logger.info("TestExecutor - setting remote server port to " + serverPort);
    }

    @Autowired
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.metricCounter = meterRegistry.counter("testcontroller.counter");
        this.metricTimer = meterRegistry.timer("testcontroller.latency");
    }

    private Socket getNewClientSocket() throws IOException {
        return new Socket(serverAddress, serverPort);
    }

    @RequestMapping("/")
    public String usageString() {
        return "Usage: <test-app-url>/runtest/<iterations>";
    }

    @RequestMapping("/runtest/{iterations}")
    public String runTest(@PathVariable("iterations") int iterations) {
        logger.info("Starting test with " + iterations + " iterations");

        String result = null;

        try{
            logger.info("Connecting to " + serverAddress + " on port " + serverPort);
            Socket clientSocket = getNewClientSocket();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            SimpleTimer t = new SimpleTimer();
            logger.info("Starting");
            for(int i = 0; i < iterations; i++) {
                doRunTest(out, in);
            }
            result = generateResultString(iterations, t);
            out.close();
            in.close();
            clientSocket.close();
            logger.info(result);
        } catch (Exception e) {
            result = "Test failed - " + e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    private void doRunTest(PrintWriter out, BufferedReader in) throws IOException {
        logger.debug("Run...");
        long startTime = System.nanoTime();
        try {
            out.println("Ping");
            out.flush();
            in.readLine();
        } finally {
            metricCounter.increment();
            metricTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
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
package io.pivotal.bboe.httpclient;

/**
 * A simple timing utility.
 *
 * @author Bjorn Boe (bboe@pivotal.io)
 */
public class SimpleTimer {
    private long startTime;
    private long stopTime;

    public SimpleTimer() {
        start();
    }

    public void start() {
        startTime = System.nanoTime();
        stopTime = -1;
    }

    public void stop() {
        stopTime = System.nanoTime();
    }

    public double getTime() {
        long sTime = stopTime > 0 ? stopTime : System.nanoTime();
        return (double)(sTime - startTime)/1000000d;
    }

    public String toString() {
        return String.format("%1$,.2f", getTime());
    }
}
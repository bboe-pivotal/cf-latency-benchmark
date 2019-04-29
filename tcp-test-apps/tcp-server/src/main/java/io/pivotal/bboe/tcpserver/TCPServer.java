package io.pivotal.bboe.tcpserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;


@Component
public class TCPServer implements Lifecycle, Runnable {
    private static Log logger = LogFactory.getLog(TCPServer.class);
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private int listenPort;

    @Value("${tcp.server.port}")
    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public TCPServer() {
        logger.info("Server.construct");
    }

    @Override
    public void run() {
        logger.info("Start TCP Server");
        while(isRunning.get()) {
            try {
                doRun();
            } catch (Exception e) {
                logger.error("DoRun failed, retrying", e);
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    logger.error("Another thread exception", ee);
                }
            }
        }
        logger.debug("run stopping");
    }

    private void doRun() {
        try {
            logger.debug("doRun - setting up new socket on port " + listenPort);
            ServerSocket serverSocket = new ServerSocket(listenPort);
            while(isRunning.get()) {
                logger.debug("doRun - listening for client");
                Socket clientSocket = serverSocket.accept();
                logger.debug("doRun - new client");
                ClientManager m = new ClientManager(clientSocket);
                m.start();
            }

            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @PostConstruct
    public void start() {
        logger.debug("start");
        isRunning.set(true);
        new Thread(this).start();
    }

    @Override
    @PreDestroy
    public void stop() {
        logger.info("stop");
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }


    private class ClientManager implements Runnable {
        private Log logger = LogFactory.getLog(ClientManager.class);

        private Socket clientSocket;

        public ClientManager(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void start() {
            new Thread(this).run();
        }

        @Override
        public void run() {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);;
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));;
                while(isRunning.get()) {
                    logger.debug("Listening for message");
                    String input = in.readLine();
                    if(input == null) {
                        logger.debug("run - End of line, disconnecting");
                        break;
                    }
                    logger.debug("run - Received message \"" + input + "\", returning same message");
                    out.println(input);
                    out.flush();
                }
                in.close();
                out.close();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

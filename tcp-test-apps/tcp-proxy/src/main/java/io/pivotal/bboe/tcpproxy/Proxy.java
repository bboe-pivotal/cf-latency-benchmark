package io.pivotal.bboe.tcpproxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class Proxy implements Lifecycle, Runnable{
    private static Log logger = LogFactory.getLog(Proxy.class);
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private int listenPort;

    private String serverAddress;
    private int serverPort;

    @Value("${tcp.server.port}")
    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
        logger.info("Proxy - listen port to " + listenPort);
    }

    @Value("${remote.server.address}")
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
        logger.info("Proxy - setting remote server address to " + serverAddress);
    }

    @Value("${remote.server.port}")
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
        logger.info("Proxy - setting remote server port to " + serverPort);
    }

    @Override
    public void run() {
        logger.info("Start Proxy");
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
        ServerSocket serverSocket = null;
        try {
            logger.debug("doRun - setting up new socket on port " + listenPort);
             serverSocket = new ServerSocket(listenPort);
            while(isRunning.get()) {
                logger.debug("doRun - listening for client");
                Socket clientSocket = serverSocket.accept();
                logger.debug("doRun - new client");
                ClientManager m = new ClientManager(clientSocket);
                m.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

        //incoming sockets
        private Socket incomingSocket = null;
        private PrintWriter incomingWriter = null;
        private BufferedReader incomingReader = null;

        //outgoing sockets
        private Socket outgoingSocket = null;
        private PrintWriter outgoingWriter = null;
        private BufferedReader outgoingReader = null;


        public ClientManager(Socket incomingSocket) {
            this.incomingSocket = incomingSocket;
        }

        public void start() {
            new Thread(this).run();
        }

        @Override
        public void run() {
            try {
                incomingWriter = new PrintWriter(incomingSocket.getOutputStream(), true);;
                incomingReader = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));;
                while(isRunning.get()) {
                    logger.debug("Listening for message");
                    String input = incomingReader.readLine();
                    if(input == null) {
                        logger.debug("run - End of line, disconnecting");
                        break;
                    }
                    logger.debug("run - Received message \"" + input + "\"");
                    String response = callServer(input);
                    logger.debug("run - Sending message back: \""+response+"\"");
                    incomingWriter.println(response);
                    incomingWriter.flush();
                }
                cleanup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String callServer(String input) {
            try {
                return doCallServer(input);
            } catch (IOException e) {
                logger.info("Back-end connection failed, reconnecting");
                cleanupOutgoingConnection();
                try {
                    return doCallServer(input);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        private String doCallServer(String input) throws IOException {
            if(outgoingSocket == null) {
                logger.debug("Setting up new outgoing connection");
                outgoingSocket = new Socket(serverAddress, serverPort);
                outgoingWriter = new PrintWriter(outgoingSocket.getOutputStream(), true);
                outgoingReader = new BufferedReader(new InputStreamReader(outgoingSocket.getInputStream()));
            }

            outgoingWriter.println(input);
            outgoingWriter.flush();
            return outgoingReader.readLine();
        }

        private void cleanup() {
            cleanupOutgoingConnection();
            try {
                if(incomingSocket != null) {
                    incomingReader.close();
                    incomingWriter.close();
                    incomingSocket.close();
                }
                incomingReader = null;
                incomingWriter = null;
                incomingSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void cleanupOutgoingConnection() {
            try {
                if(outgoingSocket != null) {
                    outgoingReader.close();
                    outgoingWriter.close();
                    outgoingSocket.close();
                    outgoingReader = null;
                    outgoingWriter = null;
                    outgoingSocket = null;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

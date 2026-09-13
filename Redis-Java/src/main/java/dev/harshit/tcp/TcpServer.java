package dev.harshit.tcp;

import dev.harshit.core.RedisEngine;
import dev.harshit.execution.RequestExecutor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements Runnable{

    private final int port;
    private final RedisEngine engine;
    private final RequestExecutor executor;

    private volatile boolean running = true;
    private ServerSocket serverSocket;


    public TcpServer(int port, RedisEngine engine, RequestExecutor executor) {

        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port");
        }

        this.port = port;
        this.engine = engine;
        this.executor = executor;
    }

    @Override
    public void run() {

        try(ServerSocket server = new ServerSocket(port)){

            this.serverSocket = server;

            while (running){
                Socket socket = server.accept();

                executor.execute(new ConnectionHandler(socket, engine));
            }

        } catch (IOException e) {
            if(running) throw new IllegalStateException("Server Failed");
        }
    }

    public void stop(){
        running = false;
        if(serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
        }
    }
}

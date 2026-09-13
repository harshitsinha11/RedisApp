package dev.harshit.tcp;

import dev.harshit.core.RedisEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectionHandler implements Runnable{

    private final Socket socket;
    private final RedisEngine engine;

    public ConnectionHandler(Socket socket, RedisEngine engine) {
        this.socket = socket;
        this.engine = engine;
    }

    @Override
    public void run() {

        try(socket;
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream()
        ){

            CommandParser parser = new CommandParser(input);

            while (true){
                String[] cmd = parser.parse();

                if(cmd == null) break;

                String res = execute(cmd);

                output.write(
                        (res+"/r/n").getBytes(StandardCharsets.UTF_8)
                );

                output.flush();
            }


        } catch (IOException e){
            //Client Disconnected
        }
    }

    private String execute(String[] cmd){
        String operation = cmd[0].toUpperCase();

        return switch (operation){
            case "GET" -> handleGet(cmd);

            case "SET" -> handleSet(cmd);

            case "TTL" -> handleTtl(cmd);

            default -> "ERR Unknown Command";
        };
    }

    private String handleGet(String[] cmd) {
        if(cmd.length != 2) return "ERR wrong number of arguments GET";

        String value = engine.get(cmd[1]);

        return value == null ? "NULL" : value;
    }

    private String handleSet(String[] cmd) {
        if(cmd.length == 3){

            engine.set(cmd[1],cmd[2]);

            return "OK";

        } else if(cmd.length == 4){

            try {
                long ttl = Long.parseLong(cmd[3]);

                engine.set(cmd[1], cmd[2], ttl);

                return "OK";
            } catch (IllegalArgumentException e) {
                return "Invalid TTL in SET";
            }
        } else{

            return "ERR wrong number of arguments SET";
        }
    }

    private String handleTtl(String[] cmd) {
        if(cmd.length != 2) return "ERR wrong number of arguments TTL";

        return String.valueOf(engine.ttl(cmd[1]));
    }
}

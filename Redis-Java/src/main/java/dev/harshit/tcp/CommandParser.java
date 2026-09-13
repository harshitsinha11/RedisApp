package dev.harshit.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CommandParser {

    private final BufferedReader reader;

    public CommandParser(InputStream input){
        this.reader = new BufferedReader(
                new InputStreamReader(
                        input,
                        StandardCharsets.UTF_8
                )
        );
    }

    public String[] parse() throws IOException{
        String line = reader.readLine();
        if(line == null) return null;
        return line.split(" ");
    }
}

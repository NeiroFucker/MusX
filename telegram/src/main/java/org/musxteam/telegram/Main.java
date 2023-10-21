package org.musxteam.telegram;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

import org.musxteam.core.RequestHandler;
import org.musxteam.core.command.EchoCommand;
import org.musxteam.core.command.HelpCommand;
import org.musxteam.core.requests.ConcreteRequest;

public class Main {
    private static final RequestHandler requestHandler = new RequestHandler();
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {
        while (true) {
            String input = br.readLine();
            ConcreteRequest request = new ConcreteRequest(input);

            if (Objects.equals(input, "/help"))
                requestHandler.startNewCommand(request, new HelpCommand());

            if (Objects.equals(input, "/echo"))
                requestHandler.startNewCommand(request, new EchoCommand());

            if (Objects.equals(input, "/break")) break;

            System.out.println(requestHandler.handleRequest(request));
        }
    }
}
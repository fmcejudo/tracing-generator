package com.github.fmcejudo.tracing.generator.exporter;

import io.vavr.control.Try;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SocketExporter implements Exporter {


    private final List<Socket> boundSocket;

    @SneakyThrows
    public SocketExporter(final int port) {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("server socket initialized in " + port + " port");

        this.boundSocket = new ArrayList<>();
        var socketBinder = new SocketBinder(serverSocket, boundSocket);
        new Thread(socketBinder).start();
    }

    @Override
    public synchronized void write(byte[] message) {

        boundSocket.removeIf(Socket::isClosed);
        boundSocket.stream().map(s -> {
            try {
                return new PrintWriter(s.getOutputStream(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).forEach(w ->
                Try.run(() -> {
                    System.out.println(new String(message, UTF_8));
                    w.println(new String(message, UTF_8));
                }).onFailure(e -> System.out.println(e.getMessage()))
        );

    }

}

class SocketBinder implements Runnable {

    private final ServerSocket serverSocket;
    private final List<Socket> boundSocket;

    SocketBinder(final ServerSocket serverSocket, List<Socket> boundSocket) {
        this.serverSocket = serverSocket;
        this.boundSocket = boundSocket;
    }

    @Override
    @SneakyThrows
    public void run() {
        while (true) {
            var socket = serverSocket.accept();
            System.out.println("A new client coming in");
            this.boundSocket.add(socket);
        }
    }
}

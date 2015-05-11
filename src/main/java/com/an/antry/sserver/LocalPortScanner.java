package com.an.antry.sserver;

import java.io.IOException;
import java.net.ServerSocket;

public class LocalPortScanner {
    public static void main(String[] args) {
        for (int port = 1; port <= 65535; port++) {
            try {
                new ServerSocket(port);
            } catch (IOException e) {
                System.out.println("There is a server on port " + port + ".");
            }
        }
    }
}

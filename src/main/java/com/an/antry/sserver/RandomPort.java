package com.an.antry.sserver;

import java.io.IOException;
import java.net.ServerSocket;

public class RandomPort {
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(0);
            System.out.println("This server runs on port " + server.getLocalPort());
            System.out.println(server);
            System.out.println("SoTimeout: " + server.getSoTimeout());
            System.out.println("ReuseAddress: " + server.getReuseAddress());
            System.out.println("ReceiveBufferSize: " + server.getReceiveBufferSize());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

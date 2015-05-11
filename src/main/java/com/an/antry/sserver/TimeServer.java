package com.an.antry.sserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TimeServer {
    public static final int port = 13;

    public static void main(String[] args) {
        System.out.println("Time Sever.");
        try (ServerSocket server = new ServerSocket(port);) {
            while (true) {
                try (Socket conn = server.accept();) {
                    System.out.println("Accept.");
                    System.out.println("Handle conn " + conn.getInetAddress().getHostName() + ", "
                            + conn.getInetAddress().getHostAddress() + ":" + conn.getPort());
                    Writer out = new OutputStreamWriter(conn.getOutputStream());
                    out.write(new Date().toString() + "\r\n");
                    out.flush();
                } catch (IOException e) {
                    System.err.println("Error while handling conn.");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error while creating server.");
            e.printStackTrace();
        }
    }
}

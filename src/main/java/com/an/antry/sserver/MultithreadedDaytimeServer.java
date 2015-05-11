package com.an.antry.sserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MultithreadedDaytimeServer {
    public static final int port = 13;

    public static void main(String[] args) {
        System.out.println("Multithreaded daytime server.");
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket conn = server.accept();
                    System.out.println("Accept.");
                    System.out.println("Handle conn " + conn.getInetAddress().getHostName() + ", "
                            + conn.getInetAddress().getHostAddress() + ":" + conn.getPort());
                    new DaytimeThread(conn).start();
                    System.out.println("Finished handling.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DaytimeThread extends Thread {
    private Socket conn;

    public DaytimeThread(Socket conn) {
        this.conn = conn;
    }

    @Override
    public void run() {
        try {
            Writer out = new OutputStreamWriter(conn.getOutputStream());
            out.write(new Date().toString() + "\r\n");
            System.out.println("Write date.");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

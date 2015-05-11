package com.an.antry.sserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PooledDaytimeServer {
    public static final int port = 13;

    public static void main(String[] args) {
        System.out.println("Pooled daytime server.");
        ExecutorService pool = Executors.newFixedThreadPool(50);
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket conn = server.accept();
                    System.out.println("Accept.");
                    System.out.println("Handle conn " + conn.getInetAddress().getHostName() + ", "
                            + conn.getInetAddress().getHostAddress() + ":" + conn.getPort());
                    pool.submit(new DaytimeTask(conn));
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

class DaytimeTask implements Callable<Void> {
    private Socket conn;

    public DaytimeTask(Socket conn) {
        this.conn = conn;
    }

    @Override
    public Void call() {
        Writer out;
        try {
            out = new OutputStreamWriter(conn.getOutputStream());
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
        return null;
    }
}
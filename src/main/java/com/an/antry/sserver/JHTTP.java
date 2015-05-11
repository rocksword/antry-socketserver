package com.an.antry.sserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JHTTP {
    private static final Logger logger = Logger.getLogger(JHTTP.class.getCanonicalName());
    private static final int NUM_THREADS = 50;
    private static final String INDEX_FILE = "index.html";

    private final File rootDir;
    private final int port;

    public JHTTP(File rootDir, int port) throws IOException {
        if (!rootDir.isDirectory()) {
            throw new IOException(rootDir + " does not exist as a directory.");
        }
        this.rootDir = rootDir;
        this.port = port;
    }

    public void start() throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("Accepting connections on port " + server.getLocalPort());
            logger.info("Document root: " + rootDir);
            while (true) {
                Socket request = server.accept();
                new RequestProcessor(rootDir, INDEX_FILE, request);
            }
        }
    }

    public static void main(String[] args) {
        // get the Document root
        File docroot;
        try {
            docroot = new File(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Usage: java JHTTP docroot port");
            return;
        }

        // set the port to listen on
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 0 || port > 65535) {
                port = 80;
            }
        } catch (RuntimeException ex) {
            port = 80;
        }

        try {
            new JHTTP(docroot, port).start();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Server could not start", ex);
        }
    }
}

class RequestProcessor implements Runnable {
    private static final Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
    private File rootDir = null;
    private String indexFileName = "index.html";
    private Socket conn;

    public RequestProcessor(File rootDir, String indexFileName, Socket conn) {
        if (rootDir.isFile()) {
            throw new IllegalArgumentException("rootDir must be a directory, not a file.");
        }

        try {
            rootDir = rootDir.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.rootDir = rootDir;

        if (indexFileName != null) {
            this.indexFileName = indexFileName;
        }
        this.conn = conn;
    }

    @Override
    public void run() {
        String root = rootDir.getPath();
        OutputStream raw = null;
        try {
            raw = new BufferedOutputStream(conn.getOutputStream());
            Writer out = new OutputStreamWriter(raw);
            Reader in = new InputStreamReader(conn.getInputStream(), "US-ASCII");
            StringBuilder requestLine = new StringBuilder();
            while (true) {
                int c = in.read();
                if (c == '\r' || c == '\n') {
                    break;
                }
                requestLine.append((char) c);
            }

            String get = requestLine.toString();
            logger.info(conn.getRemoteSocketAddress() + " " + get);

            String[] tokens = get.split("\\s+");
            String method = tokens[0];
            String version = "";
            if (method.equals("GET")) {
                String fileName = tokens[1];
                if (fileName.endsWith("/")) {
                    fileName += indexFileName;
                }
                String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
                if (tokens.length > 2) {
                    version = tokens[2];
                }

                File theFile = new File(rootDir, fileName.substring(1, fileName.length()));

                if (theFile.canRead()
                // Don't let clients outside the document root
                        && theFile.getCanonicalPath().startsWith(root)) {
                    byte[] theData = Files.readAllBytes(theFile.toPath());
                    if (version.startsWith("HTTP/")) { // send a MIME header
                        sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
                    }
                    // send the file; it may be an image or other binary data
                    // so use the underlying output stream
                    // instead of the writer
                    raw.write(theData);
                    raw.flush();
                } else {// can't find the file
                    String body = new StringBuilder("<HTML>\r\n").append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
                            .append("</HEAD>\r\n").append("<BODY>")
                            .append("<H1>HTTP Error 404: File Not Found</H1>\r\n").append("</BODY></HTML>\r\n")
                            .toString();
                    if (version.startsWith("HTTP/")) { // send a MIME header
                        sendHeader(out, "HTTP/1.0 404 File Not Found", "text/html; charset=utf-8", body.length());
                    }
                    out.write(body);
                    out.flush();
                }
            } else {// method does not equal "GET"
                String body = new StringBuilder("<HTML>\r\n").append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
                        .append("</HEAD>\r\n").append("<BODY>").append("<H1>HTTP Error 501: Not Implemented</H1>\r\n")
                        .append("</BODY></HTML>\r\n").toString();
                if (version.startsWith("HTTP/")) { // send a MIME header
                    sendHeader(out, "HTTP/1.0 501 Not Implemented", "text/html; charset=utf-8", body.length());
                }
                out.write(body);
                out.flush();
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error talking to " + conn.getRemoteSocketAddress(), ex);
        } finally {
            try {
                conn.close();
            } catch (IOException ex) {
            }
        }
    }

    private void sendHeader(Writer out, String responseCode, String contentType, int length) throws IOException {
        out.write(responseCode + "\r\n");
        Date now = new Date();
        out.write("Date: " + now + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-length: " + length + "\r\n");
        out.write("Content-type: " + contentType + "\r\n\r\n");
        out.flush();
    }
}

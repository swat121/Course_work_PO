package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

public class Server {
    public static IndexService indexService = new IndexService();
    public static String inputWords = "";

    public static void main(String[] args) throws IOException, InterruptedException {
        indexService.runBuild();
        runServer();
    }
    private static void runServer() throws IOException {
        try (var listener = new ServerSocket(59090)) {
            System.out.println("The date server is running...");
            while (true) {
                try {
                    var socket = listener.accept();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("New client has been accepted.");
                            try (var in = new Scanner(socket.getInputStream());
                                 var out = new PrintWriter(socket.getOutputStream(), true);) {
                                if (in.hasNext()) {
                                    System.out.println("New message.");
                                    inputWords = in.nextLine();
                                    out.println(indexService.searchFiles(Arrays.asList(inputWords.replaceAll("<.*?>", "")
                                            .replaceAll("[^A-Za-z\\s]", "")
                                            .replaceAll(" +", " ").split("\\W+"))));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }




}


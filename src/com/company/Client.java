package com.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static final int PORT = 59090;
    public static final String HOST = "localhost";
    public static void main(String[] args) {
        Socket socket = null;
        try{
            socket = new Socket(HOST,PORT);
            try (var in = new Scanner(socket.getInputStream());
                 var out = new PrintWriter(socket.getOutputStream(), true)) {
                Scanner sc = new Scanner(System.in);
                System.out.print("Print your massage: ");
                String line = sc.nextLine();
                out.println(line);
                while (in.hasNext()) {
                    System.out.println(in.nextLine());
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

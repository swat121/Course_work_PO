package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Main {
    public static List<File> filesInFolder = new ArrayList<File>();
    public static Hashtable<String,ArrayList<Integer>> index = new Hashtable<String,ArrayList<Integer>>();
    public static String findWords = new String();
    public static ArrayList<List<File>> getResult;
    public static ArrayList<List<File>> Result;
    private static final int NUMBER_THREADS = 1;
    public static void main(String[] args) throws IOException, InterruptedException {
        filesInFolder();
//        Index index = new Index(filesInFolder,1,10);
//        index.run();
        indexThread();
        Server();
//        String findWords = "today we met him";
//        searchFiles(Arrays.asList(findWords.split("\\W+")));
    }
    public static void Server() throws IOException {
        try (var listener = new ServerSocket(59090)) {
            System.out.println("The date server is running...");
            while (true) {
                try{
                    var socket = listener.accept();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("New client has been accepted.");
                            try(var in = new Scanner(socket.getInputStream());
                            var out = new PrintWriter(socket.getOutputStream(), true);) {
                                if (in.hasNext()) {
                                    System.out.println("New massage.");
                                    findWords = in.nextLine();
                                    searchFiles(Arrays.asList(findWords.split("\\W+")));
                                    out.println(Result.get(0));
                                }
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                           finally {
                                try {
                                    socket.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public static void indexThread() throws InterruptedException{
        Index[] indexThread = new Index[NUMBER_THREADS];
        for (int i = 0; i < NUMBER_THREADS; i++) {
            indexThread[i] = new Index(filesInFolder, filesInFolder.size() / NUMBER_THREADS * i,
                    i == (NUMBER_THREADS - 1) ? filesInFolder.size() : filesInFolder.size() / NUMBER_THREADS * (i + 1));
            indexThread[i].start();
        }
        for (int i = 0; i < NUMBER_THREADS; i++) {
            indexThread[i].join();
        }
    }
    public static void searchFiles(List<String> words) {
        getResult = new ArrayList<>();
        Result = new ArrayList<>();
        for (String wordHigh : words) {
            List<File> filesForWord = new ArrayList<>();
            String word = wordHigh.toLowerCase();
            List<Integer> filesWithWord = index.get(word);
            System.out.print(word+":");
            if (filesWithWord != null) {
                for (Integer t : filesWithWord) {
                    filesForWord.add(filesInFolder.get(t));
                }
            }
            getResult.add(filesForWord);
            System.out.println("");
        }
        System.out.println("size "+getResult.size());
        Result.add(getResult.get(0));
        for(int i=1;i<getResult.size();i++){
            Result.get(0).retainAll(getResult.get(i));
            //System.out.println("--"+i);
        }


    }
    private static void filesInFolder() throws IOException {
        filesInFolder = Files.walk(Paths.get("dataset"))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}
 class Index extends Thread{
     public static ArrayList<Integer> indexData;
     int startIndex;
     int endIndex;
     List<File> filesInFolder;
    Index(List<File> filesInFolder, int startIndex, int endIndex){
        this.startIndex = startIndex;
        this.endIndex =endIndex;
        this.filesInFolder = filesInFolder;
    }
        public void run(){
            for (int i=startIndex;i<endIndex;i++){
                int fileNumber = filesInFolder.indexOf(filesInFolder.get(i));
                int wordCounter=0;
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(filesInFolder.get(i)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        line = line.replaceAll("<br /><br />","");
                        for (String wordHigh : line.split("\\W+")) {
                            wordCounter++;
                            String word = wordHigh.toLowerCase();
                            indexData = Main.index.get(word);
                            //System.out.println("------1 --"+indexData);
                            if (/*indexData == null*/!Main.index.containsKey(word)) {
                                indexData = new ArrayList<>();
                                //System.out.println("------2 --"+indexData);
                                Main.index.put(word, indexData);
                               // System.out.println("------3 --"+Main.index.size());
                            }
                            indexData.add(fileNumber);
                           // System.out.println("------4 --"+indexData);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("filename " + filesInFolder.get(i) + " " + wordCounter + " words");
            }
        }

}

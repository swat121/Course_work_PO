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
    public static Hashtable<String,ArrayList<Data>> index = new Hashtable<String,ArrayList<Data>>();
    public static String findWords = new String();
    public static HashMap<String,List<File>> getResult = new HashMap<>();
    private static final int NUMBER_THREADS = 4;
    public static void main(String[] args) throws IOException, InterruptedException {
        filesInFolder();
        indexThread();
        Server();
    }
    public static void Server() throws IOException {
        try (var listener = new ServerSocket(59090)) {
            System.out.println("The date server is running...");
            while (true) {
                try (var socket = listener.accept()) {
                    System.out.println("New client has been accepted.");
                    var in = new Scanner(socket.getInputStream());
                    var out = new PrintWriter(socket.getOutputStream(), true);
                    if (in.hasNext()) {
                        System.out.println("New massage.");
                        findWords = in.nextLine();
                        searchFiles(Arrays.asList(findWords.split(",")));
                        out.println(getResult);
                    }
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
        List<File> filesForWord = new ArrayList<>();
        for (String wordHigh : words) {
            String word = wordHigh.toLowerCase();
            List<Data> filesWithWord = index.get(word);
            System.out.print(word+":");
            if (filesWithWord != null) {
                for (Data t : filesWithWord) {
                    filesForWord.add(filesInFolder.get(t.fileNumber));
                }
            }
            getResult.put(word,filesForWord);
            System.out.println("");
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
     public static List<Data> indexData;
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
                if (fileNumber == -1) {
                    fileNumber = filesInFolder.size() - 1;
                }
                int wordCounter=0;
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(filesInFolder.get(i)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        for (String wordHigh : line.split("\\W+")) {
                            wordCounter++;
                            String word = wordHigh.toLowerCase();
                            word.replaceAll("<br /><br />","");
                            ArrayList<Data> indexData = Main.index.get(word);
                            if (indexData == null) {
                                indexData = new ArrayList<>();
                                Main.index.put(word, indexData);
                            }
                            indexData.add(new Data(fileNumber, wordCounter));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("indexed " + filesInFolder.get(i) + " " + wordCounter + " words");
            }
        }

}
class Data{
     int fileNumber;
     int positionWord;
    public Data(int fileNumber, int positionWord) {
        this.fileNumber = fileNumber;
        this.positionWord = positionWord;
    }
}

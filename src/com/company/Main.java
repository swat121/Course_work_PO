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
    public static List<File> filesInFolder = new ArrayList<>();
    public static Hashtable<String,List<Integer>> index = new Hashtable<>();
    public static String findWords = new String();
    public static ArrayList<List<File>> Result;
    public static List<String> stopWords =  stopWords();
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
    public static List<String> stopWords(){
        List<String> words = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("stop_words_list.txt"));
            for (String line = reader.readLine(); line != null; line = reader.readLine()){
                for (String word : line.split("\\W+")){
                    words.add(word);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return words;
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
                    i == (NUMBER_THREADS - 1) ? filesInFolder.size() : filesInFolder.size() / NUMBER_THREADS * (i + 1), stopWords);
            indexThread[i].start();
        }
        for (int i = 0; i < NUMBER_THREADS; i++) {
            indexThread[i].join();
        }
    }
    public static void searchFiles(List<String> words) {
        ArrayList<List<File>> getResult = new ArrayList<>();
        Result = new ArrayList<>();
        for (String wordHigh : words) {
            List<File> filesForWord = new ArrayList<>();
            String word = wordHigh.toLowerCase();
            if (stopWords.contains(word))
                continue;
            List<Integer> filesWithWord = index.get(word);
            if (filesWithWord != null) {
                for (Integer t : filesWithWord) {
                    filesForWord.add(filesInFolder.get(t));
                }
            }
            getResult.add(filesForWord);
        }
        Result.add(getResult.get(0));
        for(int i=1;i<getResult.size();i++){
            Result.get(0).retainAll(getResult.get(i));
        }
        System.out.println(Result);
        System.out.println(getResult);

    }
    private static void filesInFolder() throws IOException {
        filesInFolder = Files.walk(Paths.get("dataset"))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}
 class Index extends Thread{
     List<Integer> indexData;

     int startIndex;
     int endIndex;
     List<File> filesInFolder;
     List<String> stopWords;
    Index(List<File> filesInFolder, int startIndex, int endIndex, List<String> stopWords){
        this.startIndex = startIndex;
        this.endIndex =endIndex;
        this.filesInFolder = filesInFolder;
        this.stopWords = stopWords;
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
                            if (stopWords.contains(word))
                                continue;
                            if (!Main.index.containsKey(word)) {
                                indexData = new LinkedList<>();
                                //Слово с соответствующим пустым списком файлов
                                Main.index.put(word, indexData);
                            }else{
                                indexData = Main.index.get(word);
                            }
                            //добавляем текущий файл к слову word
                            indexData.add(fileNumber);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("filename " + filesInFolder.get(i) + " " + wordCounter + " words");
            }
        }


}

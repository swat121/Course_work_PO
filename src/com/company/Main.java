package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Main {
    public static ArrayList<File> allfiles = new ArrayList<File>();
    private static final int NUMBER_THREADS = 4;
    public static void main(String[] args) throws IOException, InterruptedException {
        File[] paths = {new File("test//neg"),new File("test//pos"),new File("train//neg"),new File("train//pos"),new File("train//unsup") };
        initAllFiles(paths);
        Index[] indexThread = new Index[NUMBER_THREADS];
        for (int i = 0; i < NUMBER_THREADS; i++) {//разбиваем на потоки
            indexThread[i] = new Index(allfiles, allfiles.size() / NUMBER_THREADS * i,
                    i == (NUMBER_THREADS - 1) ? allfiles.size() : allfiles.size() / NUMBER_THREADS * (i + 1), paths);
            indexThread[i].start();
        }
        //waiting for finish of threads
        for (int i = 0; i < NUMBER_THREADS; i++) {
            indexThread[i].join();
        }
        String findWords = "this,horse,cat,dog";
        Index index = new Index();
        index.searchFiles(Arrays.asList(findWords.split(",")));
    }
    private static void initAllFiles(File[] paths) {
        for (File path : paths) {
            if (path.isDirectory()) {
                File[] files = path.listFiles();
                allfiles.addAll(Arrays.asList(files));
            }
        }
    }
}
 class Index extends Thread{
     Hashtable<String,List<Data>> index = new Hashtable<String,List<Data>>();
     int startIndex;
     int endIndex;
     ArrayList<File> allfiles;
     File[] paths;
    Index(ArrayList<File> allfiles, int startIndex, int endIndex,File[] paths){
        this.startIndex = startIndex;
        this.endIndex =endIndex;
        this.allfiles = allfiles;
        this.paths = paths;
    }
    Index(){

     }
        public void run(){

            int fileNumber = allfiles.indexOf(paths);
            if (fileNumber == -1) {
                fileNumber = allfiles.size() - 1;
            }
            for (int i=startIndex;i<endIndex;i++){
                int wordCounter=0;
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(allfiles.get(i)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        for (String wordHigh : line.split("\\W+")) {
                            wordCounter++;
                            String word = wordHigh.toLowerCase();
                            List<Data> indexData = index.get(word);
                            if (indexData == null) {
                                indexData = new LinkedList<Data>();
                                index.put(word, indexData);
                            }
                            indexData.add(new Data(fileNumber, wordCounter));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("indexed " + allfiles.get(i) + " " + wordCounter + " words");
            }
        }
     public void searchFiles(List<String> words) {
         for (String wordHigh : words) {
             String word = wordHigh.toLowerCase();
             List<Data> filesWithWord = index.get(word);
             System.out.print(word);
             if (filesWithWord != null) {
                 for (Data t : filesWithWord) {
                     System.out.print(" позиция "+ t.positionWord+" в файле: "+allfiles.get(t.fileNumber));
                 }
             }
            System.out.println("");
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

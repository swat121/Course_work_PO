package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Index index = new Index();
        String findWords = "this,horse,cat,dog";
        index.buildIndex(1,10);
        index.searchFiles(Arrays.asList(findWords.split(",")));
    }
}
 class Index{
    public static ArrayList<File> allfiles = new ArrayList<File>();
    Hashtable<String,List<Data>> index= new Hashtable<>();
        public void buildIndex(int startIndex, int endIndex) throws IOException {
            File[] paths = {new File("test//neg"),new File("test//pos"),new File("train//neg"),new File("train//pos"),new File("train//unsup") };
            initAllFiles(paths);
            int fileNumber = allfiles.indexOf(paths);
            if (fileNumber == -1) {
                fileNumber = allfiles.size() - 1;
            }
            for (int i=startIndex;i<endIndex;i++){
                int wordCounter=0;
                BufferedReader reader = new BufferedReader(new FileReader(allfiles.get(i)));
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
     private static void initAllFiles(File[] paths) {
         for (File path : paths) {
             if (path.isDirectory()) {
                 File[] files = path.listFiles();
                 allfiles.addAll(Arrays.asList(files));
             }
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

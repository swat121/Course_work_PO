package com.company;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Index index = new Index();
        String findWords = "spider,horse,cat,dog";
        String[] nameFiles = {"testfile1.txt","testfile2.txt"};
        for(int i=0;i<nameFiles.length;i++){
            index.buildIndex(new File(nameFiles[i]));
        }
        index.searchFiles(Arrays.asList(findWords.split(",")));
    }
}
 class Index{
    Hashtable<String,List<Data>> index;
    ArrayList<String> files;
        Index(){
            index = new Hashtable<>();
            files = new ArrayList<>();
        }
        public void buildIndex(File file) throws IOException {
            int fileNumber = files.indexOf(file.getPath());
            if (fileNumber == -1) {
                files.add(file.getPath());
                fileNumber = files.size() - 1;
            }
            int wordCounter=0;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for (String line = reader.readLine(); line != null; line = reader.readLine()){
                for (String wordHigh : line.split("\\W+")){
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
            System.out.println("indexed " + file.getName() + " " + wordCounter + " words");
        }
     public void searchFiles(List<String> words) {
         for (String wordHigh : words) {
             String word = wordHigh.toLowerCase();
             List<Data> filesWithWord = index.get(word);
             System.out.print(word);
             if (filesWithWord != null) {
                 for (Data t : filesWithWord) {
                     System.out.print(" позиция "+ t.positionWord+" в файле: "+files.get(t.fileNumber));
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

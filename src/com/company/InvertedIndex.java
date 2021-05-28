package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InvertedIndex extends Thread{
    int startFileIndex;
    int endFileIndex;
    List<Integer> wordFiles;
    List<File> filePath;
    List<String> stopWords;

    InvertedIndex(List<File> filePath, int startFileIndex, int endFileIndex, List<String> stopWords) {
        this.startFileIndex = startFileIndex;
        this.endFileIndex = endFileIndex;
        this.filePath = filePath;
        this.stopWords = stopWords;
    }

    public void run() {

        for (int i = startFileIndex; i < endFileIndex; i++) {
            int fileNumber = filePath.indexOf(filePath.get(i));
            int wordCounter = 0;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(filePath.get(i)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    line = line.replaceAll("<.*?>", "")
                            .replaceAll("[^A-Za-z\\s]", "")
                            .replaceAll(" +", " ");
                    for (String rawWords : line.split("\\W+")) {
                        wordCounter++;
                        String word = rawWords.toLowerCase();
                        if (stopWords.contains(word))
                            continue;
                        if (!Server.index.containsKey(word)) {
                            wordFiles = new ArrayList<>();
                            //Слово с соответствующим пустым списком файлов
                            Server.index.put(word, wordFiles);
                        } else {
                            wordFiles = Server.index.get(word);
                        }
                        //добавляем текущий файл к слову word
                        wordFiles.add(fileNumber);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("filename " + filePath.get(i) + " " + wordCounter + " words");
        }
    }
}

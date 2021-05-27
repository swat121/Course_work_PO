package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InvertedIndex extends Thread{
    List<Integer> indexData;
    int startIndex;
    int endIndex;
    List<File> filesInFolder;
    List<String> stopWords;

    InvertedIndex(List<File> filesInFolder, int startIndex, int endIndex, List<String> stopWords) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.filesInFolder = filesInFolder;
        this.stopWords = stopWords;
    }

    public void run() {

        for (int i = startIndex; i < endIndex; i++) {
            int fileNumber = filesInFolder.indexOf(filesInFolder.get(i));
            int wordCounter = 0;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(filesInFolder.get(i)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    line = line.replaceAll("<.*?>", "")
                            .replaceAll("[^A-Za-z\\s]", "")
                            .replaceAll(" +", " ");
                    for (String wordHigh : line.split("\\W+")) {
                        wordCounter++;
                        String word = wordHigh.toLowerCase();
                        if (stopWords.contains(word))
                            continue;
                        if (!Server.index.containsKey(word)) {
                            indexData = new ArrayList<>();
                            //Слово с соответствующим пустым списком файлов
                            Server.index.put(word, indexData);
                        } else {
                            indexData = Server.index.get(word);
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

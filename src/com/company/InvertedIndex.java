package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InvertedIndex{

    List<Integer> wordFiles;
//    File filePath;
//    List<String> stopWords;
//
//    InvertedIndex(File filePath, List<String> stopWords) {
//        this.filePath = filePath;
//        this.stopWords = stopWords;
//    }

    public void buildIndex(File filePath, List<String> stopWords, int fileNumber,ConcurrentHashMap<String, List<Integer>> index) {
            //int fileNumber = filePath.indexOf(filePath.get(i));
            int wordCounter = 0;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(filePath));
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
                        if (!index.containsKey(word)) {
                            wordFiles = new ArrayList<>();
                            //Слово с соответствующим пустым списком файлов
                            index.put(word, wordFiles);
                        } else {
                            wordFiles = index.get(word);
                        }
                        //добавляем текущий файл к слову word
                        wordFiles.add(fileNumber);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println("filename " + filePath + " " + wordCounter + " words");

        }

    }


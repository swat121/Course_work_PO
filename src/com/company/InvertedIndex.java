package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InvertedIndex{
    private List<Integer> wordFiles;

    public void buildIndex(File filePath, List<String> stopWords, int fileNumber,ConcurrentHashMap<String, List<Integer>> index) {
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
                            index.put(word, wordFiles);
                        } else {
                            wordFiles = index.get(word);
                        }
                        wordFiles.add(fileNumber);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


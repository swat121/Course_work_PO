package com.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IndexService {
    private static final List<String> stopWords = readStopWords();
    private static ConcurrentHashMap<String, List<Integer>> index = new ConcurrentHashMap<>();
    private static final String stopWordsPath = "stop_words_list.txt";
    private static final String datasetDirectoryPath = "dataset";
    private static final int NUMBER_THREADS =1;
    private static List<File> filePath = new ArrayList<>();

    public void runBuild() throws IOException, InterruptedException {
        readFilesInFolder();
        Instant start = Instant.now();
        buildParallelIndex();
        Instant finish = Instant.now();
        System.out.println("Time: " + Duration.between(start, finish).toMillis() + " ms");
    }
    public Object searchFiles(List<String> words) {
        ArrayList<List<File>> listOfFoundFiles = new ArrayList<>();
        ArrayList<List<File>> Result = new ArrayList<>();
        for (String rawWords : words) {
            List<File> filesForWord = new ArrayList<>();
            String word = rawWords.toLowerCase();
                List<Integer> filesWithWord = index.get(word);
                if (filesWithWord != null) {
                    for (Integer fileNumber : filesWithWord) {
                        filesForWord.add(filePath.get(fileNumber));
                        listOfFoundFiles.add(filesForWord);
                    }
                }
        }
        if(listOfFoundFiles.size()!=0){
            Result.add(listOfFoundFiles.get(0));
            for (int i = 1; i < listOfFoundFiles.size(); i++) {
                Result.get(0).retainAll(listOfFoundFiles.get(i));
            }
            return Result.get(0);
        }else {
            System.out.println("No files found");
            return "No files found";
        }
    }

    private static void readFilesInFolder() throws IOException {
        filePath = Files.walk(Paths.get(datasetDirectoryPath))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
    private static List<String> readStopWords() {
        List<String> words = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("stop_words_list.txt"));
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                words.addAll(Arrays.asList(line.split("\\W+")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }
    private static void buildParallelIndex() throws InterruptedException {
        ParallelIndex[] parallelIndices = new ParallelIndex[NUMBER_THREADS];
        for (int i = 0; i < NUMBER_THREADS; i++) {
            parallelIndices[i] = new ParallelIndex(filePath, filePath.size() / NUMBER_THREADS * i,
                    i == (NUMBER_THREADS - 1) ? filePath.size() : filePath.size() / NUMBER_THREADS * (i + 1), stopWords,index);
            parallelIndices[i].start();
        }
        for (int i = 0; i < NUMBER_THREADS; i++) {
            parallelIndices[i].join();
        }
    }
}

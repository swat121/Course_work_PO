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
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IndexService {
    public static List<String> stopWords = readStopWords();
    public static ConcurrentHashMap<String, List<Integer>> index = new ConcurrentHashMap<>();
    public static String stopWordsPath = "stop_words_list.txt";
    public static String datasetDirectoryPath = "dataset";
    public static int NUMBER_THREADS =4;
    public static List<File> filePath = new ArrayList<>();

    public void runBuild() throws IOException, InterruptedException {
        readFilesInFolder();
        Instant start = Instant.now();
        buildParallelIndex();
        Instant finish = Instant.now();
        System.out.println("Time: " + Duration.between(start, finish).toMillis() + " ms");
    }
    public List<File> searchFiles(List<String> words) {
        ArrayList<List<File>> listOfFoundFiles = new ArrayList<>();
        ArrayList<List<File>> Result = new ArrayList<>();
        for (String rawWords : words) {
            List<File> filesForWord = new ArrayList<>();
            String word = rawWords.toLowerCase();
            if (stopWords.contains(word))
                continue;
            List<Integer> filesWithWord = index.get(word);
            if (filesWithWord != null) {
                for (Integer fileNumber : filesWithWord) {
                    filesForWord.add(filePath.get(fileNumber));
                }
            }
            listOfFoundFiles.add(filesForWord);
        }
        Result.add(listOfFoundFiles.get(0));
        for (int i = 1; i < listOfFoundFiles.size(); i++) {
            Result.get(0).retainAll(listOfFoundFiles.get(i));
        }
        return Result.get(0);
    }

    private static void readFilesInFolder() throws IOException {
        filePath = Files.walk(Paths.get(datasetDirectoryPath))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
    public static List<String> readStopWords() {
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
    public static void buildParallelIndex() throws InterruptedException {
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

package com.company;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ParallelIndex extends Thread {
    ConcurrentHashMap<String, List<Integer>> index;
    private int startFileIndex;
    private int endFileIndex;
    private List<File> filePath;
    private List<String> stopWords;
    private InvertedIndex invertedIndex = new InvertedIndex();

    ParallelIndex(List<File> filePath, int startFileIndex, int endFileIndex, List<String> stopWords,ConcurrentHashMap<String, List<Integer>> index) {
        this.filePath = filePath;
        this.startFileIndex = startFileIndex;
        this.endFileIndex = endFileIndex;
        this.stopWords = stopWords;
        this.index = index;
    }

    @Override
    public void run() {
        for(int i=startFileIndex;i<endFileIndex;i++){
            invertedIndex.buildIndex(filePath.get(i),stopWords,i,index);
        }
    }
}

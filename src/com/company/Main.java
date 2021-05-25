package com.company;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static List<File> filesInFolder = new ArrayList<File>();
    public static Hashtable<String,List<Data>> index = new Hashtable<String,List<Data>>();
    private static final int NUMBER_THREADS = 4;
    public static void main(String[] args) throws IOException, InterruptedException {
        filesInFolder();
        Index[] indexThread = new Index[NUMBER_THREADS];
        for (int i = 0; i < NUMBER_THREADS; i++) {
            indexThread[i] = new Index(filesInFolder, filesInFolder.size() / NUMBER_THREADS * i,
                    i == (NUMBER_THREADS - 1) ? filesInFolder.size() : filesInFolder.size() / NUMBER_THREADS * (i + 1));
            indexThread[i].start();
        }
        for (int i = 0; i < NUMBER_THREADS; i++) {
            indexThread[i].join();
        }
        String findWords = "horse,cat,dog,gtrsdf";
        searchFiles(Arrays.asList(findWords.split(",")));

    }
    public static void searchFiles(List<String> words) {
        for (String wordHigh : words) {
            String word = wordHigh.toLowerCase();
            List<Data> filesWithWord = index.get(word);
            System.out.print(word+":");
            if (filesWithWord != null) {
                for (Data t : filesWithWord) {
                    System.out.print(/*" позиция "+ t.positionWord+" в файле: "+*/" " +filesInFolder.get(t.fileNumber)+"; ");
                }
            }
            System.out.println("");
        }
    }
    private static void filesInFolder() throws IOException {
        filesInFolder = Files.walk(Paths.get("dataset"))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        System.out.println(filesInFolder);
    }
}
 class Index extends Thread{
     int startIndex;
     int endIndex;
     List<File> filesInFolder;
    Index(List<File> allfiles, int startIndex, int endIndex){
        this.startIndex = startIndex;
        this.endIndex =endIndex;
        this.filesInFolder = allfiles;
    }
        public void run(){
            for (int i=startIndex;i<endIndex;i++){
                int fileNumber = filesInFolder.indexOf(filesInFolder.get(i));
                //int fileNumber = i;
                if (fileNumber == -1) {
                    fileNumber = filesInFolder.size() - 1;
                }
                int wordCounter=0;
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(filesInFolder.get(i)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        for (String wordHigh : line.split("\\W+")) {
                            wordCounter++;
                            String word = wordHigh.toLowerCase();
                            List<Data> indexData = Main.index.get(word);
                            if (indexData == null) {
                                indexData = new LinkedList<Data>();
                                Main.index.put(word, indexData);
                            }
                            indexData.add(new Data(fileNumber, wordCounter));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("indexed " + filesInFolder.get(i) + " " + wordCounter + " words");
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
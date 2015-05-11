package experiments;

import Tokenizer.Stemmer;
import Tokenizer.StopWordRemover;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import myungha.utils.DirectoryManager;
import myungha.utils.SimpleFileReader;
import myungha.utils.SimpleFileWriter;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.Parameters;
import simple.io.myungha.DirectoryReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mhjang on 5/7/2015.
 */
public class ExperimentHarness {
    // Experiments options

    Stemmer stemmer;
    WikiRetrieval wr;
    DocumentQueryGenerator queryGen;
    Parameters p;
    String paramFileDir;
    public ExperimentHarness(String[] args) throws IOException {
        System.out.println(args);
        p = Parameters.parseArgs(args);

        paramFileDir = args[0];
        stemmer = new Stemmer();
        wr = new WikiRetrieval();
        queryGen = new DocumentQueryGenerator();

        // this is probably the worst design decision ever
        Tile.sr = new StopWordRemover();
    }

    public void runRankingExperiments() throws IOException {
        // make directtory for this experiment
        // files should be contained: wiki.rel, rankings, param file
        String listDir = p.get("querylist", "./");
        SimpleFileReader sr = new SimpleFileReader(listDir);
        LinkedList<String> docList = new LinkedList<String>();
        while(sr.hasMoreLines()) {
            docList.add(sr.readLine());
        }
        String baseDir = ""; //clueweb directory
        ArrayList<Parameters> expParam = (ArrayList<Parameters>) p.getList("experiment");
        ArrayList<Parameters> tiledParam = (ArrayList<Parameters>)expParam.get(0).get("tiled");
        Parameters tp = tiledParam.get(0);
        boolean useTFTile = tp.get("tf_tile", false);
        int querySize = (int) p.get("querySize", 10);
        int tfInclude = (int) tp.get("tf_include", 0);
        boolean useWeightedTile = tp.get("weighted", false);
        String signature1;
        if(useTFTile)
            signature1 = "tftile";
        else
            signature1 = "";



        String expOutputDir = p.get("expDir", "./");
        expOutputDir = expOutputDir + "/" + "tiled" + "_"+signature1 + "_"+tfInclude+"_"+(querySize - tfInclude);
        DirectoryManager.makeDir(expOutputDir);

        int retrieved = (int)p.get("retrieved", 20);
        String dataDir = p.get("data", "");

        SimpleFileWriter outputWriter = new SimpleFileWriter(expOutputDir + "/" + "tiled_ranking.run");
        Path filePathA = (new File(paramFileDir)).toPath();
        Path filePathB = (new File(expOutputDir + "/" + paramFileDir)).toPath();
        Files.copy(filePathA, filePathB, StandardCopyOption.REPLACE_EXISTING);
        for(String doc : docList) {
            LinkedList<Tile> tiles = readTiles(dataDir+ "\\" + doc);
            String fullText = joinTiles(tiles);
           // if(p.get("experiments").includes("tf")
                    // run TFExperiment(fullText)
           if(useWeightedTile)
               computeTileImportance(tiles);
           if(useTFTile)
               tiles.add(new Tile(fullText));
           Multiset<String> weightedRankedList = runTileExperiment(tiles);
            ImmutableMultiset<String> entryList = Multisets.copyHighestCountFirst(weightedRankedList);


            int rank = 1;
            for (Multiset.Entry<String> e : entryList.entrySet()) {
                String documentName = e.getElement().replaceAll(".html", "");
                outputWriter.writeLine(doc + "\t 0 \t" + documentName + "\t" + rank + "\t" + e.getCount() + "\t" + "tile");
                if (rank == retrieved) break;
                rank++;
            }
        }
        outputWriter.close();
        Evaluation.queryEval(false, expOutputDir);

    }
/*
    private void generateRankOutput(String qid, Multiset<String> weightedDocs, String output) throws IOException {
        ImmutableMultiset<String> entryList = Multisets.copyHighestCountFirst(weightedDocs);
        HashSet<String> topKDocsInTile = new HashSet<String>();

        int rank = 1;
        SimpleFileWriter tiled = new SimpleFileWriter(output);
        for (Multiset.Entry<String> e : entryList.entrySet()) {
            String documentName = e.getElement().replaceAll(".html", "");
            tiled.writeLine(filename + "\t 0 \t" + documentName + "\t" + rank + "\t" + e.getCount() + "\t" + "tile");
            //        topKDocsInTile.add(e.getElement());
            if (rank == topK) break;
            rankedSet.add(e.getElement());
            rank++;
        }
    }

    }
    */
    private Multiset<String> runTileExperiment(LinkedList<Tile> tiles) {
        Multiset<String> weightedDocs = HashMultiset.create();
        for (Tile t : tiles) {
            List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(t.text, 10));
            int rankWeight = 1000;
            for (ScoredDocument sd : docs) {
                weightedDocs.add(sd.documentName, (int) ((rankWeight--) * t.importance));
            }
        }
        return weightedDocs;
    }

    private void writeParamFile(String outputDir) {

    }
    private void computeTileImportance(LinkedList<Tile> tiles) {
        double denorm = 0.0;
        for (Tile t : tiles) {
            denorm += (1.0 - t.stopwordContainment);
        }
        for (Tile t : tiles) {
            t.importance = (1.0 - t.stopwordContainment) / denorm;
        }
    }

    private String joinTiles(LinkedList<Tile> tiles) {
        StringBuilder fullText = new StringBuilder();
        for(Tile t : tiles) {
            fullText.append(t.text + "\n");
        }
        return fullText.toString();
    }
    private LinkedList<Tile> readTiles(String dir) throws IOException {
        System.out.println(dir);
        SimpleFileReader sr = new SimpleFileReader(dir);
        boolean tileOpened = false;
        StringBuilder tileBuilder = new StringBuilder();
        LinkedList<Tile> tiles = new LinkedList<Tile>();

        while (sr.hasMoreLines()) {
            String line = sr.readLine();
            if (!tileOpened && line.contains("<TILE>")) {
                tileOpened = true;
                tileBuilder.append(line.replace("<TILE>", ""));
            } else if (line.contains("</TILE>")) {
                tileOpened = false;
                tileBuilder.append(line.replace("</TILE>", ""));
                String stemmedText = stemmer.stemString(tileBuilder.toString(), false);
         //       System.out.println(stemmedText);
                Tile t = new Tile(stemmedText);
                tiles.add(t);
                tileBuilder = new StringBuilder();
                //
            } else if (tileOpened) {
                tileBuilder.append(line);
            }
        }
        return tiles;
    }

    public static void main(String[] args) throws IOException {
        ExperimentHarness eh = new ExperimentHarness(args);
        eh.runRankingExperiments();






    }
}

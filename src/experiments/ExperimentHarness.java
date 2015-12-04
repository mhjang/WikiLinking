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
import java.util.*;

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
    public static int RANKING_ADDITIVE = 0;
    public static int RANKING_RECIPROCAL = 1;
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
        while (sr.hasMoreLines()) {
            docList.add(sr.readLine());
        }
        String baseDir = ""; //clueweb directory
        ArrayList<Parameters> expParam = (ArrayList<Parameters>) p.getList("experiment");
        ArrayList<Parameters> tiledParam = (ArrayList<Parameters>) expParam.get(0).get("tiled");
        Parameters tp = tiledParam.get(0);
        boolean useTFTile = tp.get("tf_tile", false);
        String qsize = p.get("querysize", "10");
        int querySize;
        if(qsize.equals("all"))
                querySize = Integer.MAX_VALUE;
        else
            querySize = Integer.parseInt(qsize);
        System.out.println("Query Size: " + querySize);
 //       int querySize = (int) p.get("querySize", 10);
        boolean useWeightedTile = tp.get("weighted", false);
        int rankingMethod;
        String rankingMethodSig = p.get("aggregation", "additive");
        if (rankingMethodSig.equals("additive"))
            rankingMethod = RANKING_ADDITIVE;
        else
            rankingMethod = RANKING_RECIPROCAL;
        String signature1;
        if (useTFTile)
            signature1 = "300_tftile_new";
        else
            signature1 = "300_no_tftile_new";

        boolean runTileExp = p.get("runTile", true);
        boolean runTFExp = p.get("runTFBaseline", false);
        boolean runManual = p.get("runManual", false);
        SimpleFileWriter tileWriter = null;
        SimpleFileWriter TFWriter = null;
        SimpleFileWriter manualWriter = null;

        int tfInclude = (int)tp.get("tf_include", 0);
   //     for (int tfInclude = 1; tfInclude < 10; tfInclude++) {
            String expOutputDir = p.get("expDir", "./");
            expOutputDir = expOutputDir + "/" + "exp" + "_" + signature1 + "_" + rankingMethodSig + "_" + tfInclude + "_" + (querySize - tfInclude);
            DirectoryManager.makeDir(expOutputDir);

            int retrieved = (int) p.get("retrieved", 20);
            String dataDir = p.get("data", "");


            if (runTileExp)
                tileWriter = new SimpleFileWriter(expOutputDir + "/" + "tile_ranking.run");
            if (runTFExp)
                TFWriter = new SimpleFileWriter(expOutputDir + "/" + "tf_ranking.run");
            if (runManual)
                manualWriter = new SimpleFileWriter(expOutputDir + "/" + "manual.run");

            Path filePathA = (new File(paramFileDir)).toPath();
            Path filePathB = (new File(expOutputDir + "/" + paramFileDir)).toPath();
            Files.copy(filePathA, filePathB, StandardCopyOption.REPLACE_EXISTING);
            for (String doc : docList) {
                LinkedList<Tile> tiles = readTiles(dataDir + "\\" + doc);
                String fullText = joinTiles(tiles);
                if (runTFExp) {
                    List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(fullText, querySize));
                    for (ScoredDocument sd : docs) {
                        TFWriter.writeLine(sd.toTRECformat(doc).replaceAll(".html", ""));
                    }
                }
                if (runTileExp) {
                    if (useWeightedTile)
                        computeTileImportance(tiles);
                    if (useTFTile) {
                        tiles.add(new Tile(fullText));
                    }

                    HashMap<String, Double> weightedRankedList = runTileExperiment(tiles, tfInclude, querySize, rankingMethod);
                    List<Map.Entry<String, Double>> entries = new LinkedList(weightedRankedList.entrySet());
                    Collections.sort((List) entries, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            Map.Entry<String, Double> m1 = (Map.Entry<String, Double>) (o1);
                            Map.Entry<String, Double> m2 = (Map.Entry<String, Double>) (o2);

                            return Double.compare(m2.getValue(), m1.getValue());
                        }
                    });

                    int rank = 1;
                    for (Map.Entry<String, Double> m : entries) {
                        String documentName = m.getKey().replaceAll(".html", "");
                        tileWriter.writeLine(doc + "\t 0 \t" + documentName + "\t" + rank + "\t" + m.getValue() + "\t" + "tile");
                        if (rank == retrieved) break;
                        rank++;
                    }
                }
                  }

            if (tileWriter != null)
                tileWriter.close();
            if (TFWriter != null)
                TFWriter.close();
            Evaluation.queryEval(false, expOutputDir);
        }
 //   }

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
    private HashMap<String, Double> runTileExperiment(LinkedList<Tile> tiles, int tfInclude, int querySize, int rankingMethod) {
        HashMap<String, Double> weightedDocs = new HashMap<String, Double>();
        String fullText = joinTiles(tiles);
        String globalQuery = queryGen.generateQuerybyFrequency(fullText, tfInclude);
        for (Tile t : tiles) {
            List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(t.text, (querySize - tfInclude)) + " " + globalQuery);
            int rank = 1;
            int n = docs.size()+1;
            for (ScoredDocument sd : docs) {
                if(!weightedDocs.containsKey(sd.documentName))
                    weightedDocs.put(sd.documentName, 0.0);
                if (rankingMethod == RANKING_ADDITIVE)
                    weightedDocs.put(sd.documentName, weightedDocs.get(sd.documentName) + (double)(n - rank) * t.importance);
                else if(rankingMethod == RANKING_RECIPROCAL)
                    weightedDocs.put(sd.documentName, (double)1.0/(double)rank);
                rank++;
            }

        }

        return weightedDocs;
    }


    private void writeParamFile(String outputDir) {

    }
    private void computeTileImportance(LinkedList<Tile> tiles) {
      /*  double denorm = 0.0;
        for (Tile t : tiles) {
            denorm += (1.0 - t.stopwordContainment);
        }
        for (Tile t : tiles) {
            t.importance = (1.0 - t.stopwordContainment) / denorm;
        }
       */
        TiledDocument td = new TiledDocument(tiles);

        for(Tile t: tiles) {
            double kld = 0.0;


            for(String term : t.tokens) {
                kld += t.getTermProb(term) * Math.log(t.getTermProb(term) / td.getTermProb(term));
            }
            t.importance = kld;
        }
        double denorm = 0.0;
        for (Tile t : tiles) {
            denorm += t.importance;
        }
        for (Tile t : tiles) {
            t.importance = (1.0 - (t.importance / denorm));
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
        long startTime = System.nanoTime();
        ExperimentHarness eh = new ExperimentHarness(args);
        eh.runRankingExperiments();
        long endTime = System.nanoTime();
        double durationInSec = (endTime - startTime)/1000000000.0;
        System.out.println("execution time: " + durationInSec);





    }
}

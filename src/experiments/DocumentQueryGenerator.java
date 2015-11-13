package experiments;

import Clustering.Document;
import Clustering.DocumentCollection;
import TeachingDocParser.Tokenizer;
import TermScoring.TFIDF.TFIDFCalculator;
import Tokenizer.HTMLParser;
import Tokenizer.Stemmer;
import Tokenizer.StopWordRemover;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;

import annotation.GenAnnotation;
import com.google.common.collect.*;
import db.DBConnector;
import myungha.utils.DirectoryManager;
import myungha.utils.SimpleFileReader;
import myungha.utils.SimpleFileWriter;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.ScoredDocument;

/*
 * Created by mhjang on 1/27/15.
 * Generates a query from a given document */

public class DocumentQueryGenerator {
    Stemmer stemmer = new Stemmer();
    TagTokenizer tagTokenizer = new TagTokenizer();
    StopWordRemover sr = new StopWordRemover();

    public DocumentQueryGenerator() {
        Tile.sr = this.sr;
    }

    static class Tile {
        String text;
        static StopWordRemover sr;
        double stopwordContainment = 0.0;
        int numOfTokens = 0;
        double importance = 0.0;

        public Tile(String t) {
            text = t;
            String[] tokens = text.split(" ");
            String[] newTokens = sr.removeStopWords(tokens);
            numOfTokens = tokens.length;
            if (tokens.length > 0)
                stopwordContainment = (double) (tokens.length - newTokens.length) / (double) tokens.length;
            else
                stopwordContainment = 0.0;
        }
    }


    public static void generateDropDownMenu() {
        String dir = "/Users/mhjang/Desktop/Research/WikiLinking/data/clueweb_plaintext/tiled/";
        DirectoryManager dr = new DirectoryManager(dir);
        DocumentQueryGenerator queryGen = new DocumentQueryGenerator();

        for (String filename : dr.getFileNameList()) {
            try {
                SimpleFileReader sr = new SimpleFileReader(dir + filename);
                StringBuilder sb = new StringBuilder();
                while (sr.hasMoreLines()) {
                    sb.append(sr.readLine());
                }
                String keyword = queryGen.generateQuerybyFrequency(sb.toString(), 1);
                System.out.println("<li role=\"presentation\"><a role=\"menuitem\" tabindex=\"-1\" href" +
                        "=\"" + filename + ".html>" + keyword + "</a></li>");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void showQuery(String documentName) throws IOException {
        LinkedList<Tile> tiles = new LinkedList<Tile>();
        String baseDir = "C:\\Users\\mhjang\\Research\\WikiLinking\\tiled_bprm\\";

    //    SimpleFileReader sr = new SimpleFileReader(baseDir + documentName);
        SimpleFileReader sr = new SimpleFileReader(documentName);
        WikiRetrieval wr = new WikiRetrieval();


        boolean tileOpened = false;
        StringBuilder tileBuilder = new StringBuilder();
        StringBuilder fullTextBulider = new StringBuilder();

        System.out.println("Tiled Query");
        while (sr.hasMoreLines()) {
            String line = sr.readLine();
            if (!tileOpened && line.contains("<TILE>")) {
                tileOpened = true;
                tileBuilder.append(line.replace("<TILE>", "") + "\n");
            } else if (line.contains("</TILE>")) {
                tileOpened = false;
                tileBuilder.append(line.replace("</TILE>", "") + "\n");
                 System.out.println("TILE: " + tileBuilder.toString());
                 List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(generateQuerybyFrequency(tileBuilder.toString(), 10));
                 for (ScoredDocument sd : docs) {
                      System.out.println(sd.documentName);
                       }
                fullTextBulider.append(tileBuilder.toString());
                tileBuilder = new StringBuilder();
                //
            } else if (tileOpened) {
                tileBuilder.append(line + "\n");
            }
        }
        System.out.println("Full Query");
        List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(generateQuerybyFrequency(fullTextBulider.toString(), 10));
        for (ScoredDocument sd : docs) {
            System.out.println(sd.documentName);
        }

    }

    /**
     * Comebine all different generated rankings to get the expanded polling
     */
    public static void mergeRankedListforPolling() {
        GenAnnotation gen = new GenAnnotation("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\annotation\\");
        try {
            SimpleFileReader sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\expnotes\\tf vs tiling (large_scale)\\202 queries\\original_query_ranking.txt");
            HashMap<String, HashSet<String>> querySet = new HashMap<String, HashSet<String>>();
            while (sr.hasMoreLines()) {
                String line = sr.readLine();
                String[] tokens = line.split("\t");
                if (!querySet.containsKey(tokens[0]))
                    querySet.put(tokens[0], new HashSet<String>());
                querySet.get(tokens[0]).add(tokens[2]);
            }

            sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\expnotes\\tf vs tiling (large_scale)\\202 queries\\tiled_query_ranking.txt");
            while (sr.hasMoreLines()) {
                String line = sr.readLine();
                String[] tokens = line.split("\t");
                if (!querySet.containsKey(tokens[0]))
                    querySet.put(tokens[0], new HashSet<String>());
                querySet.get(tokens[0]).add(tokens[2]);
            }

            sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\expnotes\\tf vs tiling (large_scale)\\202 queries\\tile_weight_7_tf3");
            while (sr.hasMoreLines()) {
                String line = sr.readLine();
                String[] tokens = line.split("\t");
                if (!querySet.containsKey(tokens[0]))
                    querySet.put(tokens[0], new HashSet<String>());
                querySet.get(tokens[0]).add(tokens[2]);
            }

            sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\expnotes\\3methods\\34 queries\\manual_query_ranking.txt");
            while (sr.hasMoreLines()) {
                String line = sr.readLine();
                String[] tokens = line.split("\t");
                if (!querySet.containsKey(tokens[0]))
                    querySet.put(tokens[0], new HashSet<String>());
                querySet.get(tokens[0]).add(tokens[2]);
            }
            DBConnector db = new DBConnector("jdbc:mysql://localhost/", "wikilinking");

            for (String query : querySet.keySet()) {
                gen.generateAnnotationPage(query, querySet.get(query));
                db.sendQuery("INSERT INTO annotation_pages values ('" + query + "')");
            }

            db.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    public static void main(String[] args) throws IOException {
        DocumentQueryGenerator gen = new DocumentQueryGenerator();
        gen.showQuery("C:\\Users\\mhjang\\Downloads\\test_crawl\\vaccine.txt");
        //   generateDropDownMenu();
        // mergeRankedListforPolling();

/*
        String dir = "C://Users/mhjang/Research/WikiLinking/tiled_bprm/";
        // a list of clueweb documents that are judged
        LinkedList<String> cluewebJudged = new LinkedList<String>();

        try {
            DBConnector db = new DBConnector("jdbc:mysql://localhost/", "wikilinking");
            ResultSet rs = db.getQueryResult("select distinct clueweb_id from rating group by clueweb_id, wiki_title");
            while (rs.next())
                cluewebJudged.add(rs.getString("clueweb_id"));

        } catch (Exception e) {
            e.printStackTrace();
        }


        DirectoryManager dr = new DirectoryManager(dir);
        DocumentQueryGenerator queryGen = new DocumentQueryGenerator();
        Tile.sr = queryGen.sr;
        WikiRetrieval wr = new WikiRetrieval();

        HashMap<String, String> manualQuery = new HashMap<String, String>();

        int k = 10; // # of query words
        int topK = 20;
        double avgFraction = 0.0;

        // read manual query
        /*
        try {
            SimpleFileReader sr2 = new SimpleFileReader("manual_query.txt");
            String line;
            while (sr2.hasMoreLines()) {
                line = sr2.readLine();
                String[] tokens = line.split("\t");
                String cluewebId = tokens[0];
                String query = tokens[1];
                System.out.println(cluewebId + ":" + query);
                manualQuery.put(cluewebId, query);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

/*
        int tileNumSum = 0;
        int documentJudgedNum = 0;
        try {
            SimpleFileWriter tiled = new SimpleFileWriter("tiled_query_ranking.txt");
            //        SimpleFileWriter original = new SimpleFileWriter("original_query_ranking.txt");
            SimpleFileWriter tileNumWriter = new SimpleFileWriter("tile_query.txt");
            //        SimpleFileWriter manual = new SimpleFileWriter("manual_query_ranking.txt");
            for (String filename : dr.getFileNameList()) {
                filename = "clueweb09-en0003-25-20700";
                // sometimes there is an error, you don't want to start the whole generation over.
                // If the annotation file exists, it skips.
                // If there is any change in polling, you need to clear up the annotation directory, otherwise it'll skip everything.

                 /*if (dr2.getFileNameList().contains(filename + ".html")) {
                        System.out.println("skipping " + filename);
                        continue;
                        }
                    */
/*
                if (!cluewebJudged.contains(filename.replace(".html", ""))) continue;
                //         if (!manualQuery.containsKey(filename)) continue;

                Multiset<String> weightedDocs = HashMultiset.create();
                SimpleFileReader sr = new SimpleFileReader(dir + filename);
                StringBuilder tileBuilder = new StringBuilder();
                StringBuilder fullTextBulider = new StringBuilder();
                HashSet<String> rankedSet = new HashSet<String>();

                boolean tileOpened = false;
             //   System.out.println("******************* " + filename + " *****************");

                LinkedList<Tile> tiles = new LinkedList<Tile>();
                Stemmer stem = new Stemmer();
                while (sr.hasMoreLines()) {
                    String line = sr.readLine();
                    if (!tileOpened && line.contains("<TILE>")) {
                        tileOpened = true;
                        tileBuilder.append(line.replace("<TILE>", ""));
                    } else if (line.contains("</TILE>")) {
                        tileOpened = false;
                        tileBuilder.append(line.replace("</TILE>", ""));
                        String stemmedText = stem.stemString(tileBuilder.toString(), false);
                        Tile t = new Tile(stemmedText);
                        tiles.add(t);
                        //       System.out.println("TILE: " + tileBuilder.toString());
                        //    List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(tileBuilder.toString(), k));
                        //     int rankWeight = topK;
                        //     for (ScoredDocument sd : docs) {
                        //        weightedDocs.add(sd.documentName, rankWeight--);
                        //     }
                        fullTextBulider.append(tileBuilder.toString());
                        tileBuilder = new StringBuilder();
                        //
                    } else if (tileOpened) {
                        tileBuilder.append(line);
                    }
                }
                tiles.add(new Tile(stem.stemString(fullTextBulider.toString(), false)));

                if (tiles.size() == 1) continue;
                tileNumWriter.writeLine(filename + "\t" + tiles.size());
                tileNumSum += tiles.size();
                documentJudgedNum++;


                int idx = 1;
                tileNumWriter.writeLine(filename);
                // varying query size in tile
                /*
                for (Tile t : tiles) {
                    int querySize = t.numOfTokens / 10;
                    List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(t.text, querySize));
                    int rankWeight = topK;
                    for (ScoredDocument sd : docs) {
                        weightedDocs.add(sd.documentName, rankWeight--);
                    }
                    tileNumWriter.writeLine("tile " + idx++ + ": " + querySize);
                }
                */

                // weighing tile importance
                // normalize
                /*
                double denorm = 0.0;
                for (Tile t : tiles) {
                    denorm += (1.0 - t.stopwordContainment);
                }
                for (Tile t : tiles) {
                    t.importance = (1.0 - t.stopwordContainment) / denorm;
                }
                */
                //   String topGlobalQuery = queryGen.generateQuerybyFrequency(fullTextBulider.toString(), 3);
 /*               for (Tile t : tiles) {
                    List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(t.text, 10));
                    int rankWeight = 1000;
                    for (ScoredDocument sd : docs) {
                        weightedDocs.add(sd.documentName, (int) ((rankWeight--) * t.importance));
                    }
                }


                int rank = 1;

     /*           System.out.println("Original Rank");
                List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(fullTextBulider.toString(), k));

                for (ScoredDocument sd : docs) {
                    String documentName = sd.documentName.replaceAll(".html", "");
                    original.writeLine(filename + "\t 0 \t" + documentName + "\t" + sd.rank + "\t" + sd.score + "\t" + "original");
                    if (rank == topK) break;
                    rank++;
                }
     */

            //    System.out.println("Tiled Rank");
   /*             ImmutableMultiset<String> entryList = Multisets.copyHighestCountFirst(weightedDocs);
                HashSet<String> topKDocsInTile = new HashSet<String>();

                rank = 1;
                for (Multiset.Entry<String> e : entryList.entrySet()) {
                    String documentName = e.getElement().replaceAll(".html", "");
                    tiled.writeLine(filename + "\t 0 \t" + documentName + "\t" + rank + "\t" + e.getCount() + "\t" + "tile");
                    System.out.println(filename + "\t 0 \t" + documentName + "\t" + rank + "\t" + e.getCount() + "\t" + "tile");
                    //        topKDocsInTile.add(e.getElement());
                    if (rank == topK) break;
                    rankedSet.add(e.getElement());
                    rank++;
                }
            }
                /*
                System.out.println("Manual Query");
                rank = 1;
                List<ScoredDocument> docs2 = (List<ScoredDocument>) wr.runQuery(manualQuery.get(filename));
                for (ScoredDocument sd : docs2) {
  //                  System.out.println(rank++ + ": " + sd.documentName);
                    manual.writeLine(filename.replace(".html","") + "\t 0 \t" + sd.documentName + "\t" + sd.rank + "\t" + sd.score + "\t" + "manual");
                    if (rank == topK) break;
                    rank++;
            //        rankedSet.add(sd.documentName);
                }
                */
            //     }

            /*
            int intersection = 0;
            int rank = 1;
            for (ScoredDocument sd : docs) {
                System.out.println(rank++ + ": " + sd.documentName);
                if (topKDocsInTile.contains(sd.documentName)) intersection++;
                rankedSet.add(sd.documentName);
            }
            double fraction = (double) (intersection) / (double) (k);
            avgFraction += fraction;
            System.out.println("list fraction: " + fraction);
            */
            //     manual.close();
   /*         tiled.close();
            //            original.close();
            tileNumWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //   gen.generateAnnotationPage(filename, rankedSet);

        System.out.println("# of documents queried: " + documentJudgedNum + "\t");
        System.out.println("# of document tiles: " + (double) tileNumSum / (double) documentJudgedNum);


                //     avgFraction = avgFraction / (double) (dr.getFileNameList().size());
                //       queryGen.generateQuerybyTFIDF(dir, filename, 10);

                //}
                */
        }







    // using top K frequency
    public String generateQuerybyTFIDF(String dir, String filename, int k) {
        try {
            HTMLParser parser = new HTMLParser();
            String parsedString = parser.parse(dir + filename);
            parsedString = parsedString.replace("\t", " ");
            parsedString = parsedString.replace("\n", " ");
            Stemmer stemmer = new Stemmer();
            StopWordRemover sr = new StopWordRemover();


            TFIDFCalculator tfidf = new TFIDFCalculator(false);
            tfidf.calulateTFIDF(TFIDFCalculator.LOGTFIDF, dir, Tokenizer.UNIGRAM, false);
            DocumentCollection dc = tfidf.getDocumentCollection();

            HashMap<String, Document> documentSet = dc.getDocumentSet();
            for (String docName : documentSet.keySet()) {
                Document doc = documentSet.get(docName);
                LinkedList<Map.Entry<String, Double>> topRankedTerms = doc.getTopTermsTFIDF(k);
                System.out.print(doc.getName() + ": ");
                for(Map.Entry<String, Double> e: topRankedTerms) {
                    System.out.print(e.getKey() + " ");
                }
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    // using top K frequency
    public String generateQuerybyFrequency(String text, int k) {
        try {
            text = text.replace("\t", " ");
            text = text.replace("\n", " ");

            org.lemurproject.galago.core.parse.Document queryDoc = tagTokenizer.tokenize(text);
            Multiset<String> docTermBag = HashMultiset.create();

            for (String t : queryDoc.terms) {
                if (!sr.stopwords.contains(t)) {
                    docTermBag.add(t);
                }
            }
            int count = 0;
            StringBuilder query = new StringBuilder();

            for (String term : Multisets.copyHighestCountFirst(docTermBag).elementSet()) {
                if(term.length() > 1) {
                    query.append(term + " ");
                    if (count++ == k) break;
                }
            }
            //        System.out.println();
            return query.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    // using top K frequency
    public String generateQuerybyFrequency(String dir, String filename, int k) {
        try {
            HTMLParser parser = new HTMLParser();
            String parsedString = parser.parse(dir + filename);

            parsedString = parsedString.replace("\t", " ");
            parsedString = parsedString.replace("\n", " ");
            Stemmer stemmer = new Stemmer();
            StopWordRemover sr = new StopWordRemover();

            String stemmedString = stemmer.stemString(parsedString, true);
            Multiset<String> docTermBag = HashMultiset.create();
            String[] terms = stemmedString.split("\\s");
            for (String t : terms) {
                if (!sr.stopwords.contains(t)) {
                    docTermBag.add(t.trim().replace(".", " "));
                }
            }
            int count = 0;
            StringBuilder query = new StringBuilder();

            for (String term : Multisets.copyHighestCountFirst(docTermBag).elementSet()) {
                if (term.length() > 1) {
                    //              System.out.print(term + "( " + docTermBag.count(term) + " )");
                    query.append(term + " ");
                    if (count++ == k) break;
                }
            }
            //        System.out.println();
            System.out.println(filename + ": " + query.toString());
            return query.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

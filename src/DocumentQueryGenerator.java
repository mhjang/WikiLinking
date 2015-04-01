import Clustering.Document;
import Clustering.DocumentCollection;
import TeachingDocParser.Tokenizer;
import TermScoring.TFIDF.TFIDFCalculator;
import Tokenizer.HTMLParser;
import Tokenizer.Stemmer;
import Tokenizer.StopWordRemover;

import java.io.IOException;
import java.util.*;

import annotation.GenAnnotation;
import com.google.common.collect.*;
import myungha.DirectoryReader;
import myungha.SimpleFileReader;
import myungha.SimpleFileWriter;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.utility.lists.Scored;

/*
 * Created by mhjang on 1/27/15.
 * Generates a query from a given document */

public class DocumentQueryGenerator {
    Stemmer stemmer = new Stemmer();
    StopWordRemover sr = new StopWordRemover();

    public static void generateDropDownMenu() {
        String dir = "/Users/mhjang/Desktop/Research/WikiLinking/data/clueweb_plaintext/tiled/";
        DirectoryReader dr = new DirectoryReader(dir);
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

    public static void main(String[] args) {
     //   generateDropDownMenu();

        String dir = "/Users/mhjang/Desktop/Research/WikiLinking/data/clueweb_plaintext/tiled/more/";
        String annotationDir = "/Users/mhjang/Desktop/Research/WikiLinking/data/annotation/";

        DirectoryReader dr = new DirectoryReader(dir);
        DocumentQueryGenerator queryGen = new DocumentQueryGenerator();
        WikiRetrieval wr = new WikiRetrieval();
        GenAnnotation gen = new GenAnnotation(annotationDir);
        DirectoryReader dr2 = new DirectoryReader(annotationDir);

        HashMap<String, String> manualQuery = new HashMap<String, String>();


        int k = 10; // # of query words
        int topK = 10; 
        double avgFraction = 0.0;

        // read manual query
        try {
            SimpleFileReader sr2 = new SimpleFileReader("/Users/mhjang/Desktop/manual_query.txt");
            String line;
            while(sr2.hasMoreLines()) {
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



        try {
            SimpleFileWriter tiled = new SimpleFileWriter("tiled_query_ranking.txt");
            SimpleFileWriter original = new SimpleFileWriter("original_query_ranking.txt");
            SimpleFileWriter manual = new SimpleFileWriter("manual_query_ranking.txt");
            for(String filename : dr.getFileNameList()) {

                // sometimes there is an error, you don't want to start the whole generation over.
                // If the annotation file exists, it skips.
                // If there is any change in polling, you need to clear up the annotation directory, otherwise it'll skip everything.

                /*if (dr2.getFileNameList().contains(filename + ".html")) {
                    System.out.println("skipping " + filename);
                    continue;
                }
                */

                Multiset<String> weightedDocs = HashMultiset.create();
                SimpleFileReader sr = new SimpleFileReader(dir + filename);
                StringBuilder tileBuilder = new StringBuilder();
                StringBuilder fullTextBulider = new StringBuilder();
                HashSet<String> rankedSet = new HashSet<String>();

                boolean tileOpened = false;
                System.out.println("******************* " + filename + " *****************");

                while (sr.hasMoreLines()) {
                    String line = sr.readLine();
                    if (!tileOpened && line.contains("<TILE>")) {
                        tileOpened = true;
                        tileBuilder.append(line.replace("<TILE>", ""));
                    } else if (line.contains("</TILE>")) {
                        tileOpened = false;
                        tileBuilder.append(line.replace("</TILE>", ""));
                        //       System.out.println("TILE: " + tileBuilder.toString());
                        List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(tileBuilder.toString(), k));
                        int rankWeight = k;
                        for (ScoredDocument sd : docs) {
                            weightedDocs.add(sd.documentName, rankWeight--);
                        }
                        fullTextBulider.append(tileBuilder.toString());
                        tileBuilder = new StringBuilder();
                        //
                    } else if (tileOpened) {
                        tileBuilder.append(line);
                    }
                }


                System.out.println("Tiled Rank");
                ImmutableMultiset<String> entryList = Multisets.copyHighestCountFirst(weightedDocs);
                int rank = 1;
                HashSet<String> topKDocsInTile = new HashSet<String>();

                for (Multiset.Entry<String> e : entryList.entrySet()) {
                    System.out.println(rank + ": " + e.getElement() + "(" + e.getCount());
                    tiled.writeLine(filename + "\t 0 \t" + e.getElement() + "\t" + rank + "\t" + e.getCount() + "\t" + "manual");

                    topKDocsInTile.add(e.getElement());
                    if (rank == k) break;
                    rankedSet.add(e.getElement());
                    rank++;
                }


                System.out.println("Original Rank");
                List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(fullTextBulider.toString(), k));

                for (ScoredDocument sd : docs) {
                    original.writeLine(filename + "\t 0 \t" + sd.documentName + "\t" + sd.rank + "\t" + sd.score + "\t" + "original");
                }

                System.out.println("Manual Query");

                filename = filename.replace(".html", "");
                if (manualQuery.containsKey(filename)) {
                    List<ScoredDocument> docs2 = (List<ScoredDocument>) wr.runQuery(manualQuery.get(filename));
                    for (ScoredDocument sd : docs2) {
                        System.out.println(rank++ + ": " + sd.documentName);
                        manual.writeLine(filename + "\t 0 \t" + sd.documentName + "\t" + sd.rank + "\t" + sd.score + "\t" + "manual");
                        rankedSet.add(sd.documentName);
                    }
                }


                int intersection = 0;
                rank = 0;
                for (ScoredDocument sd : docs) {
                    System.out.println(rank++ + ": " + sd.documentName);
                    if (topKDocsInTile.contains(sd.documentName)) intersection++;
                    rankedSet.add(sd.documentName);
                }
                double fraction = (double) (intersection) / (double) (k);
                avgFraction += fraction;
                System.out.println("list fraction: " + fraction);


                gen.generateAnnotationPage(filename, rankedSet);

            }

            manual.close();
            tiled.close();
            original.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            avgFraction = avgFraction / (double)(dr.getFileNameList().size());
     //       queryGen.generateQuerybyTFIDF(dir, filename, 10);
        }






    public DocumentQueryGenerator() {

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

            String stemmedString = stemmer.stemString(text, true);
            Multiset<String> docTermBag = HashMultiset.create();
            String[] terms = stemmedString.split("\\s");
            for (String t : terms) {
                if (!sr.stopwords.contains(t)) {
                    docTermBag.add(t.trim().replace("."," "));
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
                    docTermBag.add(t.trim().replace("."," "));
                }
            }
            int count = 0;
            StringBuilder query = new StringBuilder();

            for (String term : Multisets.copyHighestCountFirst(docTermBag).elementSet()) {
                if(term.length() > 1) {
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

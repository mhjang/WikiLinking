import Clustering.Document;
import Clustering.DocumentCollection;
import TeachingDocParser.Tokenizer;
import TermScoring.TFIDF.TFIDFCalculator;
import Tokenizer.HTMLParser;
import Tokenizer.Stemmer;
import Tokenizer.StopWordRemover;

import java.util.List;

import com.google.common.collect.*;
import myungha.DirectoryReader;
import myungha.SimpleFileReader;
import org.lemurproject.galago.core.retrieval.ScoredDocument;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by mhjang on 1/27/15.
 * Generates a query from a given document

public class DocumentQueryGenerator {
    public static void main(String[] args) {
        String dir = "/Users/mhjang/Desktop/Research/WikiLinking/data/documentsplit/homeopathy/";
        DirectoryReader dr = new DirectoryReader(dir);
        DocumentQueryGenerator queryGen = new DocumentQueryGenerator();
        WikiRetrieval wr = new WikiRetrieval();
        Multiset<String> weightedDocs = HashMultiset.create();
        int k = 10;
        for(String filename : dr.getFileNameList()) {
            List<ScoredDocument> docs = (List<ScoredDocument>) wr.runQuery(queryGen.generateQuerybyFrequency(dir, filename, k));
            int rankWeight = k;
            for(ScoredDocument sd : docs) {
                weightedDocs.add(sd.documentName, rankWeight--);
            }
     //       queryGen.generateQuerybyTFIDF(dir, filename, 10);
        }


        ImmutableMultiset<String> entryList = Multisets.copyHighestCountFirst(weightedDocs);
        int rank = 1;
        for(Multiset.Entry<String> e: entryList.entrySet()) {
            System.out.println(rank++ + ": " + e.getElement() + "(" + e.getCount());
        }
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
*/
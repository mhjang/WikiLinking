import Tokenizer.HTMLParser;
import Tokenizer.Stemmer;
import Tokenizer.StopWordRemover;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import myungha.utils.DirectoryReader;
import myungha.utils.SimpleFileWriter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mhjang on 1/23/15.
 * takes an input of directory of document files and convert them to a LDA format files.
 * [M] [term_1]:[count] [term_2]:[count] ...  [term_N]:[count]
 * where [M] is the number of unique terms in the document and the
 [count] associated with each term is how many times that term appeared
 in the document.  Note that [term_1] is an integer which indexes the
 term; it is not a string.

 */
public class LdaFormatConvert {
    HashMap<String, Integer> wordMap;
    HashMap<Integer, String> inverseMap;
    int idx = 1;
    public static void main(String[] args) {
        // initializing the wordmap
        LdaFormatConvert ldaFormatter = new LdaFormatConvert("/Users/mhjang/Desktop/Research/WikiLinking/data/wikipages", "/Users/mhjang/Desktop/Research/WikiLinking/data/lda/wikipages");
    }

    public LdaFormatConvert(String inputDir, String outDir) {
        wordMap = new HashMap<String, Integer>();
        inverseMap = new HashMap<Integer, String>();

        try {
            DirectoryReader dr = new DirectoryReader(inputDir);
            ArrayList<String> docNameList = dr.getFileNameList();
            StopWordRemover sr = new StopWordRemover();

            HTMLParser wp = new HTMLParser();
            Stemmer stemmer = new Stemmer();
            for (String docName : docNameList) {
                    SimpleFileWriter sw = new SimpleFileWriter(outDir + "/" + docName.replace(".html", ".txt"));
                    String parsedString = wp.parse(inputDir + "/" + docName);
                    parsedString = parsedString.replace("\t", " ");
                    parsedString = parsedString.replace("\n", " ");

                    String stemmedString = stemmer.stemString(parsedString, true);
                    Multiset<String> docTermBag = HashMultiset.create();
                    String[] terms = stemmedString.split("\\s");
                    for (String t : terms) {
                        if (!sr.stopwords.contains(t)) {
                            docTermBag.add(t);
                            if (!wordMap.containsKey(t)) {
                                wordMap.put(t, idx);
                                inverseMap.put(idx, t);
                                idx++;
                            }
                        }
                    }
                /// take top 10 terms
                    sw.write(docTermBag.size() + "\t");
                    for (String t : docTermBag.elementSet()) {
                        sw.write(wordMap.get(t) + ":" + docTermBag.count(t) + "\t");
                    }
                    sw.write("\n");
                    sw.close();
            }



            // dictionary dump
            SimpleFileWriter sw = new SimpleFileWriter("term_map.dic");
            for(int i= 1; i<idx; i++) {
                sw.write(i + ":" + inverseMap.get(i) + "\n");
            }

            sw.close();
            System.out.println(idx + " terms in the collection");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

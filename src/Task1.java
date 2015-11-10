import org.lemurproject.galago.core.eval.Eval;
import org.lemurproject.galago.core.eval.QuerySetJudgments;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.prf.RelevanceModel1;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import static java.lang.System.out;

/**
 * Created by mhjang on 3/19/14.
 */
public class Task1 {

    public static void main(String[] args) throws Exception {
        String queryFile = "robust-class.xml";
        String output = "robustclass.txt";

        //     String queryFile = "/Users/mhjang/Documents/teaching_documents/extracted/dataset/applydataset/experiments/c2/queries.xml";
        //     String output = "/Users/mhjang/Documents/teaching_documents/extracted/dataset/applydataset/experiments/c2/noise_output";
        runQueries(queryFile, output);
        //     compareTwoModels(queryFile);
        //    queryEval();
    }


    public static void queryEval() throws IOException {
        QuerySetJudgments qset = new QuerySetJudgments("robust-class.qrels", true, false);
        Parameters p = new Parameters();
        ///Users/mhjang/Downloads/
        //      p.set("baseline", "./robust.community.desc.mhjang.tr100.txt");
        //      p.set("baseline", "/Users/mhjang/Downloads/books.title.mhjang.tr100.txt");
        p.set("baseline", "robustclass.txt");
        //      p.set("baseline", "/Users/mhjang/Documents/teaching_documents/extracted/dataset/applydataset/experiments/c2/noise_output");
        //   p.set("noise", "/Users/mhjang/Documents/teaching_documents/extracted/dataset/applydataset/experiments/c2/noise_output");
        p.set("details", true);
        p.set("map", true);
        PrintStream ps = new PrintStream((OutputStream)(System.out));
        Eval evaluator = new Eval();
        //   evaluator.comparisonEvaluation(p, qset, ps);
        evaluator.singleEvaluation(p, qset, ps);
    }
    public static void runQueries(String queryFile, String output) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(new File(queryFile)));
        String line;
        String jsonConfigFile = "search.params";
        Parameters globalParams = Parameters.parseFile(jsonConfigFile);
        // globalParams.set("index", "your/index/here"); // an alternative way to select the index
        Retrieval retrieval = RetrievalFactory.instance(globalParams);

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
        while ((line = br.readLine()) != null) {
            String query = null;
            String id = null;
            if (line.contains("<topic>")) {
                id = br.readLine().replace("<num>", "").replace("</num>", "");
                //     br.readLine();
                //     query = br.readLine().replace("<desc>", "").replace("</desc>", "").replace(".", "");
                query = br.readLine().replace("<title>", "").replace("</title>", "").replace(".", "");
            } else {
                continue;
            }

//            String weigthedQLquery = "#sdm( " + query + " )";
            //  out.println(weigthedQLquery);
            //String query = " bacteria growth";
            Parameters p = new Parameters();
            p.set("startAt", 0);
            p.set("requested", 100);    // ask for 50 results
            p.set("metrics", "map");

            Node root = StructuredQuery.parse(query);       // turn the query string into a query tree
            Node transformed = retrieval.transformQuery(root, p);  // apply traversals

            Document.DocumentComponents dc = new Document.DocumentComponents(true, false, true);

   /*     Document d = retrieval.getDocument("membenfrank02frankrich-407", dc);
        for (int j = 0; j < d.terms.size(); j++) {
            System.out.print(d.terms.get(j) + " ");
            if(j%10 == 0) System.out.println();
        }
*/




            List<ScoredDocument> results;
            results= (List<ScoredDocument>)retrieval.executeQuery(transformed, p).scoredDocuments; // issue the query!
            for (ScoredDocument sd : results) {
                System.out.println(sd.toTRECformat(id));
                bw.write(sd.toTRECformat(id) + "\n");
            }

            //     if(results.size() < 100)
            //         System.out.println(query);

            //  }


        }
        bw.close();

    }

    //       bw.close();



    public static void compareTwoModels(String queryFile) throws Exception {
      /*
        BufferedReader br = new BufferedReader(new FileReader(new File(queryFile)));
        String line;
        String jsonConfigFile = "search.params";
        Parameters globalParams = Parameters.parseFile(jsonConfigFile);
        // globalParams.set("index", "your/index/here"); // an alternative way to select the index
        Retrieval retrieval = RetrievalFactory.instance(globalParams);
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./model_result.txt")));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("./model_result2.txt")));

        while((line = br.readLine())!=null) {
            StringTokenizer st = new StringTokenizer(line, "\t");
            String id = st.nextToken();
            String query = st.nextToken();
            String sdmQuery = "#rm (#sdm ( "+query+ " ) )";
            String rm3Query = "#combine ( " + query + " ) ";

            PrintStream ps = new PrintStream((OutputStream)(System.out));Parameters sdmRmParams = new Parameters();
            sdmRmParams.set("startAt", 0);
            sdmRmParams.set("requested", 10);    // ask for 50 results
            // param setting for relevance model
            sdmRmParams.set("fbOrigWt", 0.8);
            sdmRmParams.set("fbDocs", 20);
            sdmRmParams.set("fbTerms", 10);
            // param setting for sdm
            sdmRmParams.set("uniw", 0.8);
            sdmRmParams.set("odw", 0.15);
            sdmRmParams.set("uww", 0.05);


            Parameters p = new Parameters();
            p.set("startAt", 0);
            p.set("requested", 20);    // ask for 50 results

            Node root = StructuredQuery.parse(sdmQuery);       // turn the query string into a query tree
            Node transformed = retrieval.transformQuery(root, sdmRmParams);  // apply traversals
            List<ScoredDocument> results1 = retrieval.executeQuery(transformed, sdmRmParams).scoredDocuments; // issue the query!

            for(ScoredDocument sd : results1) {
                bw.write(sd.toTRECformat(id) + "\n");
            }

            Node root2 = StructuredQuery.parse(rm3Query);       // turn the query string into a query tree
            Node transformed2 = retrieval.transformQuery(root2, p);  // apply traversals
            List<ScoredDocument> results2 = retrieval.executeQuery(transformed2, p).scoredDocuments; // issue the query!

            for(ScoredDocument sd : results2) {
                bw2.write(sd.toTRECformat(id) + "\n");
            }

            out.println(query);
            out.println(results1.size() + ", " + results2.size());
            int minLen = (Math.min(results1.size(), results2.size()));
            for(int i=0; i<Math.min(minLen, 10); i++) {
                out.println("rank " + i +" : "+ ((ScoredDocument)results1.get(i)).documentName + "\t" +
                ((ScoredDocument)results2.get(i)).documentName);
            }

        }
        bw.close();
        bw2.close();
        */

        QuerySetJudgments qset = new QuerySetJudgments("/Users/mhjang/Desktop/relevance.txt", false, false);
        Parameters p = new Parameters();
        p.set("baseline", "/Users/mhjang/Documents/teaching_documents/extracted/dataset/applydataset/experiments/c2/output");
        p.set("treatment", "/Users/mhjang/Documents/teaching_documents/extracted/dataset/applydataset/experiments/c2/noise_output");

        PrintStream ps = new PrintStream((OutputStream)(System.out));
        Eval evaluator = new Eval();
        evaluator.comparisonEvaluation(p, qset, ps);
    }




    public static void evaluate(String resultFile) throws IOException {
        QuerySetJudgments qset = new QuerySetJudgments("/Users/mhjang/Desktop/relevance.txt", true, false);
        Eval evavluator = new Eval();
    }
}



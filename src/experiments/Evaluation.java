package experiments;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import db.DBConnector;
import myungha.utils.SimpleFileReader;
import myungha.utils.SimpleFileWriter;
import org.lemurproject.galago.core.eval.Eval;
import org.lemurproject.galago.core.eval.QueryJudgments;
import org.lemurproject.galago.core.eval.QuerySetJudgments;
import org.lemurproject.galago.tupleflow.Parameters;
import simple.io.myungha.DirectoryReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by mhjang on 3/26/15.
 */
public class Evaluation {

    public Evaluation() {
        loadCollectedJudgments(2);
    }

    public static void main(String[] args) throws IOException {
    //    filter();
       // loadCollectedJudgments();
    //    try {
     //       for(int i=0; i<=9; i++) {
  //          int i=0;
  //              queryEval(true, "C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\exp\\exp_all_no_tftile_additive_" + i + "_" + (10-i));
      //      }
            filter("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\exp\\allquery\\tf_ranking.run");
            queryEval(false, "C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\exp\\allquery");
      //     countJudgedItems();

     //       queryEval();
 //       } catch (IOException e) {
 //           e.printStackTrace();
 //       }

    }

    public static void filter(String dir) throws IOException {
        SimpleFileReader sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\query_list_10");
        HashSet<String> doc = new HashSet<String>();
        while(sr.hasMoreLines()) {
            doc.add(sr.readLine());
        }
        SimpleFileWriter sw = new SimpleFileWriter(dir + ".filtered");
        SimpleFileReader sr2 = new SimpleFileReader(dir);
        while(sr2.hasMoreLines()) {
            String line = sr2.readLine();
            String docId = line.split("\t")[0];
            if(doc.contains(docId))
                sw.writeLine(line);
        }

    }

    /**
     * Generate a relevance file from the database tables
     */
    private static void loadCollectedJudgments(int relevantThreshold) {
        String binaryOutput = null;
        String gradedOutput = null;
        try {
            DBConnector db = new DBConnector("jdbc:mysql://localhost/", "wikilinking");
            binaryOutput = "wiki_"+relevantThreshold + ".binary.qrel";
            gradedOutput = "wiki_"+relevantThreshold + ".graded.qrel";

            SimpleFileWriter binaryRel = new SimpleFileWriter(binaryOutput);
            SimpleFileWriter gradedRel = new SimpleFileWriter(gradedOutput);

            ResultSet rs = db.getQueryResult("select *, avg(rating) r from rating group by clueweb_id, wiki_title");
            while (rs.next()) {
                String cluewebId = rs.getString("clueweb_id");
                String wikiTitle = rs.getString("wiki_title");
                int rating = Math.round((float) rs.getDouble("r"));
            /*    if(rating == 1 || rating == 2)
                    rating = 1;
                else
                    rating = 0;
*/
                int binaryRating = 0;
                int gradedRating = 0;



                if (rating == 0)
                    binaryRating = 0;
                else {
                    if (rating <= relevantThreshold)
                        binaryRating = 1;
                    else
                        binaryRating = 0;
                }

                if (rating == 1) gradedRating = 3;
                else if (rating == 2) gradedRating = 2;
                else if (rating == 3) gradedRating = 1;
                else gradedRating = 0;
                binaryRel.writeLine(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + binaryRating);
                gradedRel.writeLine(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + gradedRating);

            }




           //     System.out.println(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + rating);


            binaryRel.close();
            gradedRel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void queryEval(String file) throws IOException {
        QuerySetJudgments qset = new QuerySetJudgments("wiki3.qrel", true, true);
        Parameters p = new Parameters();
        p.set("baseline", file);
        p.set("details", false);
//        p.set("metrics", "map");
        PrintStream ps = new PrintStream((OutputStream)(System.out));

        Eval evaluator = new Eval();
        evaluator.singleEvaluation(p, qset, ps);

    }


    public static void queryEval(boolean refreshJudgment, String rankingFileDir) throws IOException {
        int relevantThreshold = 2;
        String binaryjudgementFile = "wiki_"+relevantThreshold + ".binary.qrel";
        String gradedjudgementFile = "wiki_"+relevantThreshold + ".graded.qrel";

        if(refreshJudgment)
            loadCollectedJudgments(2);
        QuerySetJudgments binaryQset = new QuerySetJudgments(binaryjudgementFile, true, true);
        QuerySetJudgments gradedQset = new QuerySetJudgments(gradedjudgementFile, true, true);

        Path filePathA = (new File(binaryjudgementFile)).toPath();
        Path filePathB = (new File(rankingFileDir+"\\" + binaryjudgementFile)).toPath();

        Files.copy(filePathA, filePathB, StandardCopyOption.REPLACE_EXISTING);
        Parameters p = new Parameters();
        p.set("details", true);
 //       p.set("metrics", "map");

        Parameters p2 = new Parameters();
        p2.set("details", true);
  //      p2.set("metrics", "map");

        DirectoryReader dr = new DirectoryReader(rankingFileDir);
        for(String file : dr.getFileNameList()) {
            if(file.endsWith("ranking.run") || file.endsWith("run.filtered")) {
                System.out.println(file);
                p.set("baseline", rankingFileDir + "\\" + file);
                p2.set("baseline", rankingFileDir + "\\" + file);

                PrintStream ps = new PrintStream((OutputStream)(new FileOutputStream(new File(rankingFileDir + "\\" + file + ".map.binary.eval"))));
                PrintStream ps2 = new PrintStream((OutputStream)(new FileOutputStream(new File(rankingFileDir + "\\" + file + ".map.graded.eval"))));

            //    PrintStream ps = new PrintStream((OutputStream)(System.out));

                Eval evaluator = new Eval();
                evaluator.singleEvaluation(p, binaryQset, ps);
                evaluator.singleEvaluation(p2, gradedQset, ps2);

            }
        }



    }



    public static void countJudgedItems() throws IOException {
        // how many relevant articles are there for each query?
        QuerySetJudgments qset = new QuerySetJudgments("wiki3.qrel", false, true);
        HashMap<String, String> manualQuery = new HashMap<String, String>();
        try {
            SimpleFileReader sr2 = new SimpleFileReader("manual_query.txt");
            String line;
            int qsSizeSum = 0;
            int numOfQueries = 0;
            while (sr2.hasMoreLines()) {
                line = sr2.readLine();
                String[] tokens = line.split("\t");
                String cluewebId = tokens[0];
                String query = tokens[1];
                System.out.println(cluewebId + ":" + query);
                manualQuery.put(cluewebId, query);

                org.lemurproject.galago.core.eval.QueryJudgments qs = qset.get(cluewebId);
                if(qs == null) continue;
                qsSizeSum += qs.size();
                numOfQueries++;

            }
          //  System.out.println("manual query set: " + (double)qsSizeSum / (double)numOfQueries);
            qsSizeSum = 0;
      /*      for(String query : qset.keySet()) {

             //   if(manualQuery.containsKey(query)) {
                    QueryJudgments qs = qset.get(query);
                    System.out.println(query + "\t" + qs.size());
                    qsSizeSum += qs.size();
            //    }
            }
           */
          //  System.out.println("entire query set: " + (double)qsSizeSum / (double)qset.size());


            // how many items in the retrieved list are judged
         //   SimpleFileWriter sw = new SimpleFileWriter("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\expnotes\\tf vs tiling (large_scale)\\202 queries\\tile_weight_7_tf3_testset");
            SimpleFileReader sr3 = new SimpleFileReader("query_ranking_list");
            LinkedList<String> queryList = new LinkedList<String>();
            while(sr3.hasMoreLines()) {
                queryList.add(sr3.readLine());
            }

            SimpleFileReader sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\expnotes\\tf vs tiling (large_scale)\\202 queries\\tile_weight_7_tf3");
            Multiset<String> OriginaljudgedItems = HashMultiset.create();
            while(sr.hasMoreLines()) {
                line = sr.readLine();
                String[] tokens = line.split("\t");
                // if(.containsKey(tokens[0]) && qset.get(tokens[0]).size() >= 10)
                QueryJudgments qs = qset.get(tokens[0]);
            //    if(qs.containsKey(tokens[2]))
            //        OriginaljudgedItems.add(tokens[0]);
            }

            sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\expnotes\\tf vs tiling (large_scale)\\202 queries\\tiled_query_ranking.txt");
            Multiset<String> tiledjudgedItems = HashMultiset.create();
            while(sr.hasMoreLines()) {
                line = sr.readLine();
                String[] tokens = line.split("\t");
                org.lemurproject.galago.core.eval.QueryJudgments qs = qset.get(tokens[0]);
           //    if(qs.containsKey(tokens[2]))
           //         tiledjudgedItems.add(tokens[0]);
            }

            System.out.println("TF Query Judged List");
            for(String query : queryList) {
                System.out.println(query + "\t" + OriginaljudgedItems.count(query));
            }
            System.out.println("Tiled Query Judged List");

            for(String query : queryList) {
                System.out.println(query + "\t" + tiledjudgedItems.count(query));
            }
     //       sw.close();



        } catch (IOException e) {
            e.printStackTrace();
        }



    }


}

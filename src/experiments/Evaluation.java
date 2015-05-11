package experiments;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import db.DBConnector;
import myungha.utils.SimpleFileReader;
import myungha.utils.SimpleFileWriter;
import org.lemurproject.galago.core.eval.*;
import org.lemurproject.galago.tupleflow.Parameters;
import simple.io.myungha.DirectoryReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by mhjang on 3/26/15.
 */
public class Evaluation {

    public Evaluation() {
        loadCollectedJudgments();
    }

    public static void main(String[] args) {
       // loadCollectedJudgments();
        try {
           countJudgedItems();

     //       queryEval();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Generate a relevance file from the database tables
     */
    private static void loadCollectedJudgments() {
        try {
            DBConnector db = new DBConnector("jdbc:mysql://localhost/", "wikilinking");
            SimpleFileWriter sw = new SimpleFileWriter("wiki3.qrel");
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
                if(rating == 0)
                    rating = 0;
                else
                    rating = 5 - rating;

           //     System.out.println(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + rating);
                sw.writeLine(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + rating);
            }
            sw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void queryEval(boolean refreshJudgment, String rankingFileDir) throws IOException {
        if(refreshJudgment) loadCollectedJudgments();
        QuerySetJudgments qset = new QuerySetJudgments("wiki3.qrel", false, true);
        Path filePathA = (new File("wiki3.qrel")).toPath();
        Path filePathB = (new File(rankingFileDir+"\\" + "wiki.qrel")).toPath();

        Files.copy(filePathA, filePathB, StandardCopyOption.REPLACE_EXISTING);
        Parameters p = new Parameters();
        p.set("details", true);
        p.set("metrics", "map");
        DirectoryReader dr = new DirectoryReader(rankingFileDir);
        for(String file : dr.getFileNameList()) {
            if(file.endsWith("ranking.run")) {
                p.set("baseline", rankingFileDir + "\\" + file);
                PrintStream ps = new PrintStream((OutputStream)(new FileOutputStream(new File(rankingFileDir + "\\" + file + ".eval"))));
                //        PrintStream ps = new PrintStream((OutputStream)(System.out));

                Eval evaluator = new Eval();
                evaluator.singleEvaluation(p, qset, ps);
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
                if(!qset.containsKey(cluewebId)) continue;
                QueryJudgments qs = qset.get(cluewebId);
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
                if(qs.containsKey(tokens[2]))
                    OriginaljudgedItems.add(tokens[0]);
            }

            sr = new SimpleFileReader("C:\\Users\\mhjang\\IdeaProjects\\WikiLinking2\\expnotes\\tf vs tiling (large_scale)\\202 queries\\tiled_query_ranking.txt");
            Multiset<String> tiledjudgedItems = HashMultiset.create();
            while(sr.hasMoreLines()) {
                line = sr.readLine();
                String[] tokens = line.split("\t");
                QueryJudgments qs = qset.get(tokens[0]);
                if(qs.containsKey(tokens[2]))
                    tiledjudgedItems.add(tokens[0]);
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

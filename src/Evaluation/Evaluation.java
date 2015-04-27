package Evaluation;

import db.DBConnector;
import myungha.SimpleFileWriter;
import org.lemurproject.galago.core.eval.*;
import org.lemurproject.galago.tupleflow.Parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by mhjang on 3/26/15.
 */
public class Evaluation {

    public Evaluation() {
        loadCollectedJudgments();
    }

    public static void main(String[] args) {
  //      loadCollectedJudgments();
        try {
            queryEval();
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
            SimpleFileWriter sw = new SimpleFileWriter("wiki.qrel");
            ResultSet rs = db.getQueryResult("select *, avg(rating) r from rating group by clueweb_id, wiki_title");
            while (rs.next()) {
                String cluewebId = rs.getString("clueweb_id");
                String wikiTitle = rs.getString("wiki_title");
                int rating = Math.round((float) rs.getDouble("r"));
                if(rating > 0)
                    rating = 4 - rating + 1;
                System.out.println(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + rating);
                sw.writeLine(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + rating);
            }
            sw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void queryEval() throws IOException {
        QuerySetJudgments qset = new QuerySetJudgments("wiki.qrel", false, true);
        Parameters p = new Parameters();
        ///Users/mhjang/Downloads/
        //      p.set("baseline", "./robust.community.desc.mhjang.tr100.txt");
        //      p.set("baseline", "/Users/mhjang/Downloads/books.title.mhjang.tr100.txt");
        p.set("baseline", "tiled_query_ranking.txt");
     //   p.set("tile", "tiled_query_ranking.txt");

        //      p.set("baseline", "/Users/mhjang/Documents/teaching_documents/extracted/dataset/applydataset/experiments/c2/noise_output");
        //   p.set("noise", "/Users/mhjang/Documents/teaching_documents/extracted/dataset/applydataset/experiments/c2/noise_output");
        p.set("details", false);
        p.set("map", true);
        PrintStream ps = new PrintStream((OutputStream)(System.out));
        Eval evaluator = new Eval();
        //   evaluator.comparisonEvaluation(p, qset, ps);
        evaluator.singleEvaluation(p, qset, ps);
    }

}

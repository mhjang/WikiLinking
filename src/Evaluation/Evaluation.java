package Evaluation;

import db.DBConnector;
import myungha.SimpleFileWriter;
import org.lemurproject.galago.core.parse.Document;

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
        loadCollectedJudgments();

    }

    /**
     * Generate a relevance file from the database tables
     */
    private static void loadCollectedJudgments() {
        try {
            DBConnector db = new DBConnector("jdbc:mysql://localhost/", "page_to_table");
            SimpleFileWriter sw = new SimpleFileWriter("wiki.qrel");
            ResultSet rs = db.getQueryResult("select *, avg(rating) r from rating group by clueweb_id, wiki_title");
            while (rs.next()) {
                String cluewebId = rs.getString("clueweb_id");
                String wikiTitle = rs.getString("wiki_title");
                int rating = Math.round((float) rs.getDouble("r"));
                System.out.println(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + rating);
                sw.writeLine(cluewebId + "\t 0 \t" + wikiTitle.replace(" ", "_") + "\t" + rating);
            }
            sw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

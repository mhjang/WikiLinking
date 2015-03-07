import java.util.List;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.utility.Parameters;


import static org.lemurproject.galago.tupleflow.Parameters.*;

/**
 * Created by mhjang on 1/30/15.
 */
public class WikiRetrieval {
    Parameters p;
    public WikiRetrieval() {
        try {



        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    public List<ScoredDocument> runQuery(String query) {
        try {
            String jsonConfigFile = "search.params";

            Parameters globalParams = Parameters.parseFile(jsonConfigFile);
            Retrieval retrieval= RetrievalFactory.instance(globalParams);

            Parameters p = Parameters.instance();
            p.set("startAt", 0);
            p.set("requested", 10);
            p.set("metrics", "map");
            List<ScoredDocument> results = null;

            Node root = StructuredQuery.parse(query);
            Node transformed = retrieval.transformQuery(root, p);
            results = retrieval.executeQuery(transformed, p).scoredDocuments; // issue the query!

            for(ScoredDocument sd:results){ // print results
                System.out.println(sd.rank+" "+sd.documentName+ " ("+sd.score+")");
                Document document = retrieval.getDocument(sd.documentName, new Document.DocumentComponents(true, true, true));


            }
            return results;

        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        WikiRetrieval wr = new WikiRetrieval();
        wr.runQuery("#combine ( homeopathy trial medical clinical treatment health remedy medicine review found placebo \n )");

    }
}

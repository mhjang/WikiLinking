import java.util.List;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.core.retrieval.ScoredDocument;
import org.lemurproject.galago.core.retrieval.query.Node;
import org.lemurproject.galago.core.retrieval.query.StructuredQuery;
import org.lemurproject.galago.tupleflow.Parameters;


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
            query = query.replace("#","");

            System.out.println("query: " + query);
            TagTokenizer tt = new TagTokenizer();
            Document d = tt.tokenize(query);
            Parameters globalParams = Parameters.parseFile(jsonConfigFile);
            Retrieval retrieval= RetrievalFactory.instance(globalParams);

            Parameters p = new Parameters();
            p.set("startAt", 0);
            p.set("requested", 20);
            p.set("metrics", "map");
            List<ScoredDocument> results = null;

            Node root = StructuredQuery.parse(d.text);
            Node transformed = retrieval.transformQuery(root, p);
            results = retrieval.executeQuery(transformed, p).scoredDocuments; // issue the query!

            for(ScoredDocument sd:results){ // print results
         //       System.out.println(sd.rank+" "+sd.documentName+ " ("+sd.score+")");
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
        wr.runQuery("#combine ( award marley produce 2008 hollywood february marley  announce star bob walk greatest scorsese 2001: rita 11 biopic rock bbc rank roll )");

    }
}

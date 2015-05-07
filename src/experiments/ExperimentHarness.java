package experiments;

import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;

/**
 * Created by mhjang on 5/7/2015.
 */
public class ExperimentHarness {
    // Experiments options
    static int ONLY_MANUAL_QUERIES;
    static int ALL_QUERIES;
    public ExperimentHarness() {

    }

    public void runRanking() {

    }

    public static void main(String[] args) throws IOException {
        Parameters p = Parameters.parseArgs(args);
        System.out.println(p.get("experiment"));
        System.out.println(p.get("dataset"));
        System.out.println(p.get("querysize"));
        System.out.println(p.get("tiled"));



    }
}

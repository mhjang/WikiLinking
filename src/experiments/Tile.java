package experiments;

import Tokenizer.StopWordRemover;

/**
 * Created by mhjang on 5/11/2015.
 */
public class Tile {
    String text;
    static StopWordRemover sr;
    double stopwordContainment = 0.0;
    int numOfTokens = 0;
    double importance = 1.0;

    public Tile(String t) {
        text = t;
        String[] tokens = text.split(" ");
        String[] newTokens = sr.removeStopWords(tokens);
        numOfTokens = tokens.length;
        if (tokens.length > 0)
            stopwordContainment = (double) (tokens.length - newTokens.length) / (double) tokens.length;
        else
            stopwordContainment = 0.0;
    }
}
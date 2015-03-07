package surveyman;

import clojure.lang.Namespace;
import edu.umass.cs.surveyman.analyses.AbstractRule;
import edu.umass.cs.surveyman.input.csv.CSVLexer;
import edu.umass.cs.surveyman.input.csv.CSVParser;
import edu.umass.cs.surveyman.qc.Analyses;
import edu.umass.cs.surveyman.qc.Classifier;
import edu.umass.cs.surveyman.survey.Question;
import edu.umass.cs.surveyman.survey.StringComponent;
import edu.umass.cs.surveyman.survey.Survey;
import net.sourceforge.argparse4j.inf.ArgumentParser;

/**
 * Created by mhjang on 3/4/15.
 */
public class survey {

    /**
     * The main entry point for the program. Running the jar with no arguments will call this function and then print
     * out a description of the arguments.
     *
     * If you would like to embed a SurveyMan program in another program, you will need to:
     * <ol>
     *    <li>Instantiate a lexer:<br/>
     *    <code>CSVLexer lexer = new CSVLexer("my_survey.csv", ",");</code>
     *    </li>
     *    <li>Instantiate a parser:<br/>
     *    <code>CSVParser parser = new CSVParser(lexer);</code>
     *    </li>
     *    <li>Parse the survey:<br/>
     *    <code>Survey survey = parser.parse();</code>
     *    </li>
     *    <li>Specify the rules you want to use to statically analyse the survey:<br/>
     *    <code>AbstractRule.getDefaultRules();</code>
     *    </li>
     *    <li>Then call analyze:<br/>
     *    <code>SurveyMan.analyze(survey, analyses, classifier, n, granularity, alpha, outputfile, resultsfile, smoothing);</code>
     *    </li>
     *    </ol>
     * @param args Arguments the top-level program. Execute <code>java -jar target/surveyman-x.y.jar</code> for guidance.
     */
    public static void main(String[] args) {

        Survey survey = new Survey();
        try {
            Question q1 = new Question("asdf", 1, 1);
            survey.addQuestion(q1);

            Question q2 = new Question(new StringComponent("fdsa", 2, 2), 2, 1);
            survey.addQuestion(q2);

            Question q3 = new Question("foo", true, true);
            survey.addQuestion(q3);

            Question q4 = new Question("aaa");
            Question q5 = new Question("bbb");

            survey.addQuestions(q4, q5);
            survey.jsonize();
        }catch(Exception e) {
            e.printStackTrace();
        }


}
}

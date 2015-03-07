package annotation;

import myungha.SimpleFileWriter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mhjang on 3/4/15.
 */
public class GenAnnotation {
    public static void main(String[] args) {
        GenAnnotation gen = new GenAnnotation();
        ArrayList<String> list = new ArrayList<String>();
        list.add("Homeopathy");
        list.add("Hello World");
        list.add("Bagels");
        gen.generateAnnotationPage("clueweb09-en0000-02-02422", list);
    }

    public GenAnnotation() {

    }

    public void generateAnnotationPage(String cluewebId, ArrayList<String> list) {
        try {
            SimpleFileWriter sw = new SimpleFileWriter(cluewebId + ".html");
            StringBuilder builder = new StringBuilder();

            builder.append("<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "    <title>Bootstrap 101 Template</title>\n" +
                    "    <link href=\"/Users/mhjang/Downloads/bootstrap-3.3.2-dist/css/bootstrap.css\" rel=\"stylesheet\">\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "  <table width=100%>\n" +
                    "  <tr>\n" +
                    "  <td width=60%>\n" +
                    "    <h2>Welcome mhjang!</h2>\n" +
                    "    <iframe width=100% height=800px src=\"/Users/mhjang/Desktop/Research/WikiLinking/data/clueweb_pages/" + cluewebId + ".html\"> </iframe>\n" +
                    "   </td>\n" +
                    "\t<td>\n" +
                    "<FORM name =\"form1\" method =\"post\" action =\"sendQuery.php\">\n" +
                            "  \t<input type=\"hidden\" name=\"clueweb_id\" value = '"+ cluewebId + "'>"+
                    "\t<div class=\"list-group\">\n" +
                    "  <a href=\"#\" class=\"list-group-item active\">\n" +
                    "\tWikipedia Articles\n" +
                    "  </a>\n");
            for(String wikiTitle : list) {
                        builder.append("  <a href=\"http://en.wikipedia.org/wiki/\"" + wikiTitle + "\" class=\"list-group-item\">\n" +
                                "  \t<h4 class=\"list-group-item-heading\">" + wikiTitle + "</h4>\n" +
                                "<input type=\"hidden\" name=\"w1_title\" value = \"" + wikiTitle + "\">" +
                                "<div class=\"btn-group\" data-toggle=\"buttons\">\n" +
                                "  <label class=\"btn btn-notsure\">\n" +
                                "    <input type=\"radio\" name=\"w1\" id=\"option1\" value = \"1\" autocomplete=\"off\" checked> Not sure\n" +
                                "  </label>\n" +
                                "  <label class=\"btn btn-primary\">\n" +
                                "    <input type=\"radio\" name=\"w1\" id=\"option1\" value = \"2\" autocomplete=\"off\" checked> Relevant\n" +
                                "  </label>\n" +
                                "  <label class=\"btn btn-somewhat\">\n" +
                                "    <input type=\"radio\" name=\"w1\" id=\"option2\" value = \"3\" autocomplete=\"off\">  Somewhat\n" +
                                "  </label>\n" +
                                "  <label class=\"btn btn-relevant\">\n" +
                                "    <input type=\"radio\" name=\"w1\" id=\"option3\" value = \"4\" autocomplete=\"off\">  Irrelevant\n" +
                                "  </label>\n" +
                                "  </div> </a>\n");
                    };

                    builder.append("\n" +
                        "\n" +
                        "</p>\n" +
                        "  </a>\n" +
                            "<p><Input type = \"submit\" name = \"submit1\" button type=\"button\" class=\"btn btn-default btn-lg btn-block\">Send Annotations</button></p></form>" +
                            "\t</div>\n" +
                        "\t</td>\n" +
                        "\t</tr></table>\n" +
                    "    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->\n" +
                    "    <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js\"></script>\n" +
                    "    <!-- Include all compiled plugins (below), or include individual files as needed -->\n" +
                    "    <script src=\"/Users/mhjang/Downloads/bootstrap-3.3.2-dist/js/bootstrap.min.js\"></script>\n" +
                    "  </body>\n" +
                    "</html>");


            sw.writeLine(builder.toString());
            sw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

package annotation;

import db.DBConnector;
import myungha.utils.SimpleFileWriter;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashSet;

/**
 * Created by mhjang on 3/4/15.
 */
public class GenAnnotation {
    String annotationDir;
    public static void main(String[] args) {
  //      GenAnnotation gen = new GenAnnotation();
   //        gen.generateAnnotationPage("clueweb09-en0000-02-02422", list);
        traceMiscellaneous();
    }

    public GenAnnotation(String dir) {
        this.annotationDir = dir;
        if(!annotationDir.endsWith("/"))
            annotationDir += "/";
    }

    public void generateAnnotationPage(String cluewebId, HashSet<String> list) {
        try {
            SimpleFileWriter sw = new SimpleFileWriter(annotationDir + cluewebId);
            StringBuilder builder = new StringBuilder();

            builder.append("<!DOCTYPE text/html>\n" +
                    "<html lang=\"en\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "    <link href=\"css/bootstrap.css\" rel=\"stylesheet\">\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "  <h4>2. Read the webpage </h4>\n" +
                    "  <table width=100%>\n" +
                    "  <tr>\n" +
                    " <?PHP session_start(); ?>\n" +
                    "  <td width=65% valign=\"top\">\n" +
                    "    <iframe width=100% height=800px src=\"annotation_300_raw/"+cluewebId+"\" frameborder=0> </iframe>\n" +
                    "   </td>\n" +
                    "\t<td width=35% valign=\"top\">\n" +

                    "<FORM name =\"form1\" id = \"controversyForm\" method =\"POST\">\n" +
                    "<input type =\"hidden\" name=\"size\" value=\""+list.size()+"\">\n" +
                    "<input type=\"hidden\" name=\"docname\" value = \""+cluewebId+"\">\n" +
                    "  \n" +
                    "  \n" +
                    " <div class=\"list-group\">\n" +
                            "  <a href=\"#\" class=\"list-group-item active\">\n" +
                            "<h4>3. How controversial is this <u>topic</u>? </h4> \n" +
                            "<a href=\"#\" class=\"list-group-item\">\n" +
                            "<div class=\"btn-group\" data-toggle=\"buttons\">\n" +
                            "<label class=\"btn btn-notsure\">\n" +
                            "    <input type=\"radio\" name=\"topic_controversy\" id=\"option0\" value = \"0\" autocomplete=\"off\"> Not sure\n" +
                            "  </label>\n" +
                            "  <label class=\"btn btn-relevant\">\n" +
                            "    <input type=\"radio\" name=\"topic_controversy\" id=\"option1\" value = \"1\" autocomplete=\"off\"> Clearly controversial\n" +
                            "  </label>\n" +
                            "  <label class=\"btn btn-somewhat\">\n" +
                            "    <input type=\"radio\" name=\"topic_controversy\" id=\"option2\" value = \"2\" autocomplete=\"off\"> Possibly controversial \n" +
                            "  </label>\n" +
                            "  <label class=\"btn btn-slightlyon\">\n" +
                            "    <input type=\"radio\" name=\"topic_controversy\" id=\"option3\" value = \"3\" autocomplete=\"off\"> Possibly non-controversial\n" +
                            "  </label>\n" +
                            "  <label class=\"btn btn-primary\">\n" +
                            "    <input type=\"radio\" name=\"topic_controversy\" id=\"option4\" value = \"4\" autocomplete=\"off\"> Clearly non-controversial\n" +
                            "  </label>\n" +
                            "  </div>\n" +
                            "</a>\n" +
                            "\n" +
                            "  <div class=\"list-group\">\n" +
                            "  <a href=\"#\" class=\"list-group-item active\">\n" +
                            "<h4>4. How controversial is this <u>page</u>? </h4> \n" +
                            "<a href=\"#\" class=\"list-group-item\">\n" +
                            "<div class=\"btn-group\" data-toggle=\"buttons\">\n" +
                            "<label class=\"btn btn-notsure\">\n" +
                            "    <input type=\"radio\" name=\"page_controversy\" id=\"option0\" value = \"0\" autocomplete=\"off\"> Not sure\n" +
                            "  </label>\n" +
                            "  <label class=\"btn btn-relevant\">\n" +
                            "    <input type=\"radio\" name=\"page_controversy\" id=\"option1\" value = \"1\" autocomplete=\"off\"> Clearly controversial\n" +
                            "  </label>\n" +
                            "  <label class=\"btn btn-somewhat\">\n" +
                            "    <input type=\"radio\" name=\"page_controversy\" id=\"option2\" value = \"2\" autocomplete=\"off\"> Possibly controversial \n" +
                            "  </label>\n" +
                            "  <label class=\"btn btn-slightlyon\">\n" +
                            "    <input type=\"radio\" name=\"page_controversy\" id=\"option3\" value = \"3\" autocomplete=\"off\"> Possibly non-controversial\n" +
                            "  </label>\n" +
                            "  <label class=\"btn btn-primary\">\n" +
                            "    <input type=\"radio\" name=\"page_controversy\" id=\"option4\" value = \"4\" autocomplete=\"off\"> Clearly non-controversial\n" +
                            "  </label>\n" +
                            "  </div>\n" +
                            "</a>"+
                    "<div class=\"list-group\">\n" +
                    "  <a href=\"#\" class=\"list-group-item active\">\n" +
                    "<h4>4. Judge the level of Wikipedia's relevance </h4> ");



            int idx = 0;


            for(String item : list) {
                item = item.replace(".html", "");
                builder.append("<a href=\"http://en.wikipedia.org/wiki/" + item + "\" target=\"reference\" class=\"list-group-item\">\n" +
                        "  \t<h4 class=\"list-group-item-heading\">" + item.replace("_", " ") + "</h4>\n" +
                        "  \t<input type=\"hidden\" name=\"w"+idx+"_title\" value = \"" + item + "\">\n" +
                        "<div class=\"btn-group\" data-toggle=\"buttons\">\n" +
                        "  <label class=\"btn btn-notsure\">\n" +
                        "    <input type=\"radio\" name=\"w"+idx+"\" id=\"option0\" value = \"0\" autocomplete=\"off\"> Not sure\n" +
                        "  </label>\n" +
                        "  <label class=\"btn btn-primary\">\n" +
                        "    <input type=\"radio\" name=\"w"+idx+"\" id=\"option1\" value = \"1\" autocomplete=\"off\"> Highly on\n" +
                        "  </label>\n" +
                        "  <label class=\"btn btn-slightlyon\">\n" +
                        "    <input type=\"radio\" name=\"w"+idx+"\" id=\"option2\" value = \"2\" autocomplete=\"off\"> Slightly on\n" +
                        "  </label>\n" +
                        "  <label class=\"btn btn-somewhat\">\n" +
                        "    <input type=\"radio\" name=\"w"+idx+"\" id=\"option3\" value = \"3\" autocomplete=\"off\"> Slightly off\n" +
                        "  </label>\n" +
                        "  <label class=\"btn btn-relevant\">\n" +
                        "    <input type=\"radio\" name=\"w"+idx+"\" id=\"option4\" value = \"4\" autocomplete=\"off\"> Highly off\n" +
                        "  </label>\n" +
                        "  </div>\n" +
                        "\n" +
                        "\n" +
                        "</p>\n" +
                        "  </a>\n");
                idx++;
            }
            builder.append("<br><br>\n" +
                    "<p><Input type = \"submit\" id=\"submitButton\" name = \"submit1\" button type=\"button\" value=\"Save Choices\" class=\"btn btn-default btn-lg btn-block\"></button></p>\n" +
                    "</form>\n" +
                    "\t</div>\n" +
                    "\t</td>\n" +
                    "\t</tr></table>\n" +
                    "    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->\n" +
                    "    <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js\"></script>\n" +
                    "    <!-- Include all compiled plugins (below), or include individual files as needed -->\n" +
                    "    <script src=\"js/bootstrap.min.js\"></script>\n" +
                    "<script>$(document).ready(function() { \n" +
                            "$('#submitButton').click(function() { \n"
                    + " var f = $('#controversyForm');\n" +
                    "$.ajax({ type: \"POST\", url: \"sendQuery.php\", data: f.serialize()}); \n"
                    + "alert(\"Thanks for your submission!\");"+
                    "});\n }); \n</script>" +
                    "  </body>\n </html>");

            sw.writeLine(builder.toString());
            sw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // find annotation pages that were not categorized by the topic list
    public static void traceMiscellaneous() {

        DBConnector db = new DBConnector("jdbc:mysql://ayr.cs.umass.edu:3306/","wikilinking");

    /*
        DirectoryManager dr = new DirectoryManager("/Users/mhjang/Desktop/Research/WikiLinking/data/annotation");
        for(String filename : dr.getFileNameList()) {
            filename = filename.replace(".html", "");
            db.sendQuery("INSERT into annotation_pages values ('" + filename + "')");
        }
*/

        try {
            ResultSet rs = db.getQueryResult("Select * from topic_list");
            DBConnector db2 = new DBConnector("jdbc:mysql://ayr.cs.umass.edu:3306/","wikilinking");

            while (rs.next()) {
                String topic = rs.getString("title");
                System.out.println("insert into categorized_page (select distinct clueweb_id from rating where wiki_title like '% " + topic + " %')");
                db2.sendQuery("insert into categorized_page (select distinct clueweb_id from rating where wiki_title like '% " + topic + " %')");
            }
            db2.closeConnection();

        }catch(Exception e) {
            e.printStackTrace();
        }
        db.closeConnection();

    }
}

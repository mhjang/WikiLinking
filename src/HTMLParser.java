import myungha.utils.DirectoryManager;
import myungha.utils.SimpleFileReader;
import myungha.utils.SimpleFileWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by mhjang on 2/19/15.
 */
public class HTMLParser  {

    public static String getPlainString(String html) {
        if(html == null)
            return html;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    public static LinkedList<Document> splitDocument(String html) {
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        System.out.println("a paragraph:" + document.select("div").outerHtml());

        return null;


    }

    public static void main(String[] args) {

        HTMLParser parser = new HTMLParser();
        try {
      //      SimpleFileReader sr = new SimpleFileReader("/Users/mhjang/Desktop/Research/WikiLinking/data/clueweb_pages/clueweb09-en0002-60-24173.html");
            String inDir = "C:/Users/mhjang/Research/WikiLinking/clueweb_pages/";
            String outDir = "C:/Users/mhjang/Research/WikiLinking/clueweb_plaintext2/";
            DirectoryManager dr = new DirectoryManager(inDir);

            for(String file : dr.getFileNameList()) {
                StringBuilder builder = new StringBuilder();
                SimpleFileReader sr = new SimpleFileReader(inDir + file);
                while (sr.hasMoreLines()) {
                    builder.append(sr.readLine());

                }
                SimpleFileWriter sw = new SimpleFileWriter(outDir + file);
                String line = parser.getPlainString(builder.toString());
                line = line.replaceAll("[ ]{3,}", "\n");
                line = line.replaceAll("\t", "\n");
                line = line.replaceAll("&nbsp", " ");
                line = line.replaceAll("&amp;", "&");
                line = line.replaceAll("&quot", "\"");
                String[] lines = line.split("\n");
                for(String l : lines) {
                    if(!l.isEmpty())
                        sw.writeLine(l);
                }

         //       sw.writeLine(parser.getPlainString(builder.toString()));
           //     System.out.println(builder.toString());
             //
           //     parser.splitDocument(builder.toString());
                sw.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        };

    }
}

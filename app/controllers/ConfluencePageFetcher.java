package controllers;


import org.codehaus.swizzle.confluence.Confluence;
import org.codehaus.swizzle.confluence.ConfluenceException;
import org.codehaus.swizzle.confluence.Page;
import org.codehaus.swizzle.confluence.SwizzleException;
import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;

import java.io.StringWriter;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: maedhros
 * Date: 1/15/11
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfluencePageFetcher {
    private static final String SPACE = "forside";

    private final Confluence confluence;
    private final MarkupParser parser = new MarkupParser(new ConfluenceLanguage());

    public ConfluencePageFetcher() {
        this(System.getProperty("confluence.user"), System.getProperty("confluence.password"));
    }

    public ConfluencePageFetcher(String username, String password) {
        if (username != null || password != null) {
            try {
                confluence = new Confluence("http://wiki.java.no/rpc/xmlrpc");
                confluence.login(username, password);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (ConfluenceException e) {
                throw new RuntimeException(e);
            } catch (SwizzleException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            confluence = null;
        }
    }

    public String getPageAsHTMLFragment(String name) {
        if (confluence != null) {
            try {
                Page page = confluence.getPage(SPACE, name);
                StringWriter writer = toHTMLFragment(page);
                return writer.toString();
            } catch (SwizzleException e) {
                throw new RuntimeException(e);
            }
        }
        return String.format("<div class=\"error\">Static page %s has been disabled because of missing connection to source</div>", name);
    }

    private StringWriter toHTMLFragment(Page page) {
        StringWriter writer = new StringWriter();
        parser.setBuilder(new HtmlDocumentBuilder(writer));
        parser.parse(page.getContent(), false);
        parser.setBuilder(null);
        return writer;
    }

    public static void main(String[] args) {
        String username = args[0];
        String password = args[1];
        String fragment = new ConfluencePageFetcher(username, password).getPageAsHTMLFragment("Om JavaBin");
        System.out.println("fragment = " + fragment);
    }
}

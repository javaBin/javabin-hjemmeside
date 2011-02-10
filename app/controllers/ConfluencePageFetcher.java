package controllers;


import models.Announcement;

import org.codehaus.swizzle.confluence.BlogEntry;
import org.codehaus.swizzle.confluence.BlogEntrySummary;
import org.codehaus.swizzle.confluence.Confluence;
import org.codehaus.swizzle.confluence.ConfluenceException;
import org.codehaus.swizzle.confluence.Page;
import org.codehaus.swizzle.confluence.SwizzleException;
import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: maedhros
 * Date: 1/15/11
 * Time: 1:38 PM
 */
public class ConfluencePageFetcher {
    private static final String SPACE = "forside";

    private final Confluence confluence;
    private final MarkupParser parser = new MarkupParser(new ConfluenceLanguage());

	private final String username;

	private final String password;

    public ConfluencePageFetcher() {
        this(System.getProperty("confluence.user"), System.getProperty("confluence.password"));
    }

    public ConfluencePageFetcher(String username, String password) {
        this.username = username;
		this.password = password;
		if (username != null && password != null) {
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
    
    public List<Announcement> getNewsFeed() {
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DATE, -5);
    	Date limit = cal.getTime();
    	List<Announcement> display = new ArrayList<Announcement>();
    	try {
    		//TODO find a way to not load all entries.
			List<BlogEntrySummary> blogEntries = confluence.getBlogEntries(SPACE);
			Collections.sort(blogEntries, new Comparator<BlogEntrySummary>() {
				@Override
				public int compare(BlogEntrySummary o1, BlogEntrySummary o2) {
					return o2.getPublishDate().compareTo(o1.getPublishDate());
				}
			});
			for (BlogEntrySummary blogEntrySummary : blogEntries) {
				if (blogEntrySummary.getPublishDate().after(limit)) {
					BlogEntry entry = confluence.getBlogEntry(blogEntrySummary.getId());
					display.add(new Announcement(entry.getTitle(), toHTMLFragment(entry.getContent()).toString(), entry.getUrl()));
				}
			}
		} catch (ConfluenceException e) {
			e.printStackTrace();
		} catch (SwizzleException e) {
			e.printStackTrace();
		}
    	return display;
    }

    public String getPageAsHTMLFragment(String name) {
        if (confluence != null) {
            try {
                return fetchPage(name);
            } catch (SwizzleException e) {
            	try {
            		//TODO Actually check if exception is caused by session timeout or find a way to read this without login
					confluence.login(username, password);
					return fetchPage(name);
				} catch (ConfluenceException loginError) {
					throw new RuntimeException("Login failed on retry", loginError);
				} catch (SwizzleException loginError) {
					throw new RuntimeException("Login failed on retry", loginError);
				}
            }
        }
        return String.format("<div class=\"error\">Static page %s has been disabled because of missing connection to source</div>", name);
    }

	private String fetchPage(String name) throws SwizzleException, ConfluenceException {
		Page page = confluence.getPage(SPACE, name);
		StringWriter writer = toHTMLFragment(page.getContent());
		return writer.toString();
	}

    private StringWriter toHTMLFragment(String content) {
        StringWriter writer = new StringWriter();
        parser.setBuilder(new HtmlDocumentBuilder(writer));
        parser.parse(content, false);
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

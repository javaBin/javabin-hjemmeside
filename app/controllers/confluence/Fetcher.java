package controllers.confluence;

import com.google.common.collect.Lists;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.cache.HTTPCache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Fetcher {
    protected final HTTPCache cache;

    public Fetcher(HTTPCache cache) {
        this.cache = cache;
    }

    protected Map<String, Page> decodeResponse(HTTPResponse response) {
        try {
            if (response.hasPayload() && Confluence.ATOM.equals(response.getPayload().getMimeType())) {
                Document<Feed> document = Abdera.getInstance().getParser().parse(response.getPayload().getInputStream());
                List<Entry> entries = document.getRoot().getEntries();
                List<Page> spaces = Lists.transform(entries, new PageFetcher.Entry2Page());
                Map<String, Page> spaceMap = new LinkedHashMap<String, Page>();
                for (Page page : spaces) {
                    spaceMap.put(page.getTitle(), page);
                }
                return spaceMap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.consume();
        }
        return Collections.emptyMap();
    }
}

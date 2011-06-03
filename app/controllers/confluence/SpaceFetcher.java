package controllers.confluence;

import com.google.common.base.Function;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Entry;
import org.apache.abdera.util.Constants;
import org.codehaus.httpcache4j.cache.HTTPCache;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceFetcher {
    private static final String PAGES = "pages";
    private static final String NEWS = "news";
    private URI spacesURI;
    private final HTTPCache cache;
    private Fetcher<Space> spaceFetcher;
    private Map<Space, PageFetcher> pageFetcherMap = Collections.synchronizedMap(new HashMap<Space, PageFetcher>());


    public SpaceFetcher(URI spacesURI, HTTPCache cache) {
        this.spacesURI = spacesURI;
        this.cache = cache;
        this.spaceFetcher = new Fetcher<Space>(cache);
    }

    public Map<String, Space> getSpaces() {
        Map<String, Space> spaces = spaceFetcher.get(spacesURI, new Entry2Space());
        if (spaces.size() != pageFetcherMap.size()) {
            pageFetcherMap.clear();
        }
        for (Space space : spaces.values()) {
            if (!pageFetcherMap.containsKey(space)) {
                pageFetcherMap.put(space, new PageFetcher(cache, space));
            }
        }
        return spaces;
    }

    public Page getPage(Space space, String name) {
        PageFetcher pageFetcher = pageFetcherMap.get(space);
        Map<String, Page> pages = pageFetcher.getPages();
        Page page = pages.get(name);
        if (page == null) {
            Page home = pages.get("Home");
            if (home == null) {
                throw new IllegalArgumentException("No page called Home, cannot look in descendants");
            }
            page = pageFetcher.getChildrenAsMap(home).get(name);
            if (page == null) {
                throw new IllegalArgumentException(String.format("Could not find page with name %s in Home or a direct descendant of the Space", name));
            }
        }
        return page;
    }

    public List<Page> getChildren(Page page) {
        return pageFetcherMap.get(page.getSpace()).getChildren(page);
    }

    private static class Entry2Space implements Function<Entry, Space> {
        public Space apply(Entry entry) {
            Collection pages = null;
            Collection news = null;
            List<Collection> collections = entry.getExtensions(Constants.COLLECTION);
            for (Collection collection : collections) {
                if (PAGES.equals(collection.getTitle())) {
                    pages = collection;
                }
                else if (NEWS.equals(collection.getTitle())) {
                    news = collection;
                }
            }
            try {
                if (pages != null && news != null) {
                    return new Space(entry.getSelfLink().getHref().toURI(), entry.getTitle(), pages.getHref().toURI(), news.getHref().toURI());
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);

            }
            return null;
        }
    }
}

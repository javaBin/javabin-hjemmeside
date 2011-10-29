package controllers.confluence;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.joda.time.DateTime;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class PageFetcher {
    private final Space space;
    private Fetcher<Page> pageFetcher;
    private Fetcher<Page> newsFetcher;
    private Map<Page, Fetcher<Page>> children = new ConcurrentHashMap<Page, Fetcher<Page>>();
    private final HTTPCache cache;

    public PageFetcher(HTTPCache cache, Space space) {
        this.cache = cache;
        pageFetcher = new Fetcher<Page>(cache);
        newsFetcher = new Fetcher<Page>(cache);
        this.space = space;
    }

    public Map<String, Page> getPages() {
        return pageFetcher.get(space.getPages(), new Entry2Page(space));
    }

    public Map<String, Page> getNews() {
        return newsFetcher.get(space.getPages(), new Entry2Page(space));
    }

    public List<Page> getChildren(Page page) {
        return ImmutableList.copyOf(getChildrenAsMap(page).values());
    }

    public Map<String, Page> getChildrenAsMap(Page page) {
        Fetcher<Page> fetcher = children.get(page);
        if (fetcher == null) {
            fetcher = new Fetcher<Page>(cache);
            children.put(page, fetcher);
        }
        return fetcher.get(page.getSubPages(), new Entry2Page(space));
    }

    private static class Entry2Page implements Function<Entry, Page> {
        private final Space space;

        private Entry2Page(Space space) {
            this.space = space;
        }

        public Page apply(Entry entry) {
            if (entry != null) {
                try {
                    Link selfLink = entry.getSelfLink();
                    Link replies = entry.getLink("replies");
                    URI children = replies != null ? replies.getHref().toURI() : null;
                    return new Page(space, selfLink.getHref().toURI(), new DateTime(entry.getPublished()), entry.getTitle(), entry.getContent(), children);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }

}

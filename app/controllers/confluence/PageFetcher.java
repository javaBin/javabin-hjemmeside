package controllers.confluence;

import com.google.common.base.Function;
import org.apache.abdera.model.Entry;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.joda.time.DateTime;

import java.net.URISyntaxException;
import java.util.Map;

public class PageFetcher {
    private final Space space;
    private Fetcher<Page> pageFetcher;
    private Fetcher<Page> newsFetcher;

    public PageFetcher(HTTPCache cache, Space space) {
        pageFetcher = new Fetcher<Page>(cache);
        newsFetcher = new Fetcher<Page>(cache);
        this.space = space;
    }

    public Map<String, Page> getPages() {
        return pageFetcher.get(space.getPages(), new Entry2Page());
    }

    public Map<String, Page> getNews() {
        return newsFetcher.get(space.getPages(), new Entry2Page());
    }

    private static class Entry2Page implements Function<Entry, Page> {
        public Page apply(Entry entry) {
            try {
                return new Page(entry.getSelfLink().getHref().toURI(), new DateTime(entry.getPublished()), entry.getTitle(), entry.getContent());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

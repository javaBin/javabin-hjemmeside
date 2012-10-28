package controllers.confluence;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.joda.time.DateTime;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

class NewsFeedFetcher {
    private final HTTPCache cache;

    NewsFeedFetcher(HTTPCache cache) {
        this.cache = cache;
    }

    public List<NewsItem> getNewsFeed(final Space space) {
        HTTPResponse response = cache.execute(new HTTPRequest(space.getNews()));
        return RequestFunctions.withResponse(response, new Function<HTTPResponse, List<NewsItem>>() {
            public List<NewsItem> apply(HTTPResponse response) {
                List<NewsItem> list = Collections.emptyList();
                if (response.getStatus() == Status.OK) {
                    list = decodeResponse(response, new EntryNewsItemFunction(space));
                }
                return list;
            }
        });
    }

    private List<NewsItem> decodeResponse(HTTPResponse response, Function<Entry, NewsItem> function) {
        if (response.hasPayload() && Confluence.ATOM.equals(response.getPayload().getMimeType())) {
            return RequestFunctions.convertFeed(response.getPayload().getInputStream(), function);
        }
        return Collections.emptyList();
    }

    private static class EntryNewsItemFunction implements Function<Entry, NewsItem> {
        private final Space space;

        public EntryNewsItemFunction(Space space) {
            this.space = space;
        }

        public NewsItem apply(Entry entry) {
            try {
                return new NewsItem(space, entry.getSelfLink().getHref().toURI(), new DateTime(entry.getPublished()), entry.getTitle(), entry.getContent());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

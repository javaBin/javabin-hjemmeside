package controllers.confluence;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.HTTPCache;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Fetcher<T extends Nameable> {
    protected final HTTPCache cache;
    protected volatile Result<T> previous = new Result<T>();

    public Fetcher(HTTPCache cache) {
        this.cache = cache;
    }


    protected Map<String, T> get(URI uri, Function<Entry, T> function) {
        HTTPResponse response = cache.doCachedRequest(new HTTPRequest(uri).conditionals(previous.getTag() != null ? new Conditionals().addIfNoneMatch(previous.getTag()) : new Conditionals()));
        Map<String, T> map = Collections.emptyMap();
        if (response.getStatus() == Status.OK) {
            Tag tag = response.getETag();
            map = decodeResponse(response, function);
            previous = new Result<T>(map, tag);
        }
        else if (response.getStatus() == Status.NOT_MODIFIED) {
            map = previous.getResult();
        }
        return map;
    }


    protected Map<String, T> decodeResponse(HTTPResponse response, Function<Entry, T> function) {
        try {
            if (response.hasPayload() && Confluence.ATOM.equals(response.getPayload().getMimeType())) {
                Document<Feed> document = Abdera.getInstance().getParser().parse(response.getPayload().getInputStream());
                List<Entry> entries = document.getRoot().getEntries();
                List<T> items = Lists.transform(entries, function);
                Map<String, T> spaceMap = new LinkedHashMap<String, T>();
                for (T page : items) {
                    spaceMap.put(page.getName(), page);
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

    public static final class Result<T> {
        private final Map<String, T> result;
        private final Tag tag;

        public Result() {
            this(Collections.<String, T>emptyMap(), null);
        }

        public Result(Map<String, T> result, Tag tag) {
            this.result = result;
            this.tag = tag;
        }

        public Map<String, T> getResult() {
            return result;
        }

        public Tag getTag() {
            return tag;
        }
    }
}

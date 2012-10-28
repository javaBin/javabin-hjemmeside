package controllers.confluence;

import com.google.common.base.Function;
import org.apache.abdera.model.Entry;
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

    protected Map<String, T> get(URI uri, final Function<Entry, T> function) {
        HTTPResponse response = cache.execute(new HTTPRequest(uri).conditionals(previous.getTag() != null ? new Conditionals().addIfNoneMatch(previous.getTag()) : new Conditionals()));
        return RequestFunctions.withResponse(response, new Function<HTTPResponse, Map<String, T>>() {
            public Map<String, T> apply(HTTPResponse response) {
                Map<String, T> map = Collections.emptyMap();
                try {
                    if (response.getStatus() == Status.OK) {
                        Tag tag = response.getETag();
                        map = decodeResponse(response, function);
                        previous = new Result<T>(map, tag);
                    } else if (response.getStatus() == Status.NOT_MODIFIED) {
                        map = previous.getResult();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } 
                return map;
            }
        });
    }


    protected Map<String, T> decodeResponse(HTTPResponse response, Function<Entry, T> function) {
        if (response.hasPayload() && Confluence.ATOM.equals(response.getPayload().getMimeType())) {
            List<T> items = RequestFunctions.convertFeed(response.getPayload().getInputStream(), function);
            Map<String, T> spaceMap = new LinkedHashMap<String, T>();
            for (T page : items) {
                spaceMap.put(page.getName(), page);
            }
            return spaceMap;
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

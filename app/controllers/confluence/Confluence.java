package controllers.confluence;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.cache.HTTPCache;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Confluence {
    public static final MIMEType ATOM = MIMEType.valueOf("application/atom+xml");

    private final SpaceFetcher spaceFetcher;
    private ExecutorService service;

    public Confluence(URI server, HTTPCache cache) {
        spaceFetcher = new SpaceFetcher(findSpaceURI(server), cache);
        service = Executors.newSingleThreadExecutor();
    }

    private URI findSpaceURI(URI server) {
        
        return server;
    }

    public Future<Space> getSpace(final String name) {
        return service.submit(new Callable<Space>() {
            public Space call() throws Exception {
                Map<String, Space> spaces = spaceFetcher.getSpaces();
                return spaces.get(name);
            }
        }
        );
    }


    public Future<Page> getPage(final Space space, final String name) {
        return service.submit(new Callable<Page>() {
            public Page call() throws Exception {
                return spaceFetcher.getPage(space, name);
            }
        }
        );
    }

    public static void main(String[] args) {

    }
}

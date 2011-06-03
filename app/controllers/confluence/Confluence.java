package controllers.confluence;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Service;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.resolver.HTTPClientResponseResolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.*;

public class Confluence {
    public static final MIMEType ATOM = MIMEType.valueOf("application/atom+xml");
    protected static final MIMEType ATOM_SERVICE = MIMEType.valueOf("application/atomsvc+xml");

    private final SpaceFetcher spaceFetcher;
    private ExecutorService service;
    private final HTTPCache cache;

    public Confluence(URI server, HTTPCache cache) {
        this.cache = cache;
        service = Executors.newSingleThreadExecutor();
        spaceFetcher = new SpaceFetcher(findSpaceURI(server), cache);
    }

    private URI findSpaceURI(final URI server) {
        Future<URI> future = service.submit(new SpaceRootCallable(server));
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
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

    public void stop() {
        service.shutdown();
    }

    private class SpaceRootCallable implements Callable<URI> {
        private final URI server;

        public SpaceRootCallable(URI server) {
            this.server = server;
        }

        public URI call() throws Exception {
            HTTPResponse response = cache.doCachedRequest(new HTTPRequest(server));
            Status status = response.getStatus();
            try {
                if (status == Status.OK) {
                    return decodeResponse(response);
                }
            } finally {
                response.consume();
            }
            return null;
        }

        private URI decodeResponse(HTTPResponse response) {
            if (response.hasPayload() && ATOM_SERVICE.equals(response.getPayload().getMimeType(), false)) {
                Document<Service> doc = Abdera.getInstance().getParser().parse(response.getPayload().getInputStream());
                Collection collection = doc.getRoot().getCollection("spaces", "spaces");
                if (collection != null) {
                    try {
                        return collection.getHref().toURI();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
            return null;
        }
    }
}

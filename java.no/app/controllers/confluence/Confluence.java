package controllers.confluence;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Service;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.ProxyConfiguration;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.resolver.HTTPClientResponseResolver;
import org.joda.time.DateTime;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Confluence {
    public static final MIMEType ATOM = MIMEType.valueOf("application/atom+xml");
    protected static final MIMEType ATOM_SERVICE = MIMEType.valueOf("application/atomsvc+xml");

    private final SpaceFetcher spaceFetcher;
    private ExecutorService service;
    private final HTTPCache cache;

    public Confluence(URI server) {
        this(server, new HTTPCache(new MemoryCacheStorage(), createResolver()));
    }

    private static HTTPClientResponseResolver createResolver() {
        HTTPHost host = null;
        if (System.getProperty("http.proxyHost") != null) {
            host = new HTTPHost("http", System.getProperty("http.proxyHost"), Integer.getInteger("http.proxyPort", 80));
        }
        return new HTTPClientResponseResolver(new DefaultHttpClient(), new ProxyConfiguration(host, null, null));
    }

    public Confluence(URI server, HTTPCache cache) {
        this.cache = cache;
        service = Executors.newSingleThreadExecutor();
        spaceFetcher = new SpaceFetcher(findSpaceURI(server), cache);
    }

    private URI findSpaceURI(final URI server) {
        Future<URI> future = submit(new SpaceRootCallable(server));
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);

        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public Future<Space> getSpace(final String name) {
        return submit(new Callable<Space>() {
            public Space call() throws Exception {
                Map<String, Space> spaces = spaceFetcher.getSpaces();
                return spaces.get(name);
            }
        }
        );
    }


    public Future<Page> getPage(final Space space, final String name) {
        return submit(new Callable<Page>() {
            public Page call() throws Exception {
                return spaceFetcher.getPage(space, name);
            }
        }
        );
    }

    public Future<Page> getPage(final String spaceName, final String name) {
        return submit(new Callable<Page>() {
            public Page call() throws Exception {
                Space space = spaceFetcher.getSpaces().get(spaceName);
                return spaceFetcher.getPage(space, name);
            }
        }
        );
    }

    public Future<List<NewsItem>> getNewsFeed(final String spaceName) {
        return submit(new Callable<List<NewsItem>>() {
            public List<NewsItem> call() throws Exception {
                Space space = spaceFetcher.getSpaces().get(spaceName);
                return spaceFetcher.getNewsFeed(space);
            }
        });
    }

    public Future<java.util.Collection<NewsItem>> getNewsFeed(final String spaceName, final DateTime limit) {
        return submit(new Callable<java.util.Collection<NewsItem>>() {
            public java.util.Collection<NewsItem> call() throws Exception {
                Space space = spaceFetcher.getSpaces().get(spaceName);
                List<NewsItem> feed = spaceFetcher.getNewsFeed(space);
                return Collections2.filter(feed, new Predicate<NewsItem>() {
                    public boolean apply(NewsItem newsItem) {
                        return limit.isBefore(newsItem.getPublishDate());
                    }
                });
            }
        });
    }

    private <T> Future<T> submit(Callable<T> callable) {
        return service.submit(callable);
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
            HTTPResponse response = cache.execute(new HTTPRequest(server));
            return RequestFunctions.withResponse(response, new Function<HTTPResponse, URI>() {
                public URI apply(HTTPResponse response) {
                    Status status = response.getStatus();
                    if (status == Status.OK) {
                        return decodeResponse(response);
                    }
                    return null;
                }
            });
        }

        private URI decodeResponse(HTTPResponse response) {
            if (response.hasPayload() && ATOM_SERVICE.equals(response.getPayload().getMimeType(), false)) {
                InputStream stream = response.getPayload().getInputStream();
                return RequestFunctions.findInService(stream, new Function<Service, URI>() {
                    public URI apply(Service feed) {
                        Collection collection = feed.getCollection("spaces", "spaces");
                        if (collection != null) {
                            try {
                                return collection.getHref().toURI();
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        throw new IllegalArgumentException("Did not find URI");
                    }
                });
            }
            return null;
        }
    }
}

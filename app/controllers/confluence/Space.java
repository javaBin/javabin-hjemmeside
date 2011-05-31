package controllers.confluence;

import java.net.URI;

public class Space implements Nameable {
    private URI uri;
    private String name;
    private URI pages;
    private URI news;

    public Space(URI uri, String name, URI pages, URI news) {
        this.uri = uri;
        this.name = name;
        this.pages = pages;
        this.news = news;
    }

    public URI getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public URI getPages() {
        return pages;
    }

    public URI getNews() {
        return news;
    }
}

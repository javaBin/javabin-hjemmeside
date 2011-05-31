package controllers.confluence;

import org.joda.time.DateTime;

import java.net.URI;

public class Page implements Nameable {
    private final URI uri;
    private final DateTime publishDate;
    private final String title;
    private final String body;

    public Page(URI uri, DateTime publishDate, String title, String body) {
        this.uri = uri;
        this.publishDate = publishDate;
        this.title = title;
        this.body = body;
    }

    public URI getUri() {
        return uri;
    }

    public DateTime getPublishDate() {
        return publishDate;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return title;
    }

    public String getBody() {
        return body;
    }
}

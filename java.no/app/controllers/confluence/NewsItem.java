package controllers.confluence;

import org.joda.time.DateTime;

import java.net.URI;

public class NewsItem {
    private final Space space;
    private final URI uri;
    private final DateTime publishDate;
    private final String title;
    private final String body;

    public NewsItem(Space space, URI uri, DateTime publishDate, String title, String body) {
        this.space = space;
        this.uri = uri;
        this.publishDate = publishDate;
        this.title = title;
        this.body = body;
    }

    public Space getSpace() {
        return space;
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

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "space=" + space +
                ", uri=" + uri +
                ", publishDate=" + publishDate +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}

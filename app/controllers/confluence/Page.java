package controllers.confluence;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.DateTime;

import java.net.URI;

public class Page implements Nameable {
    private final Space space;
    private final URI uri;
    private final DateTime publishDate;
    private final String title;
    private final String body;
    private final URI subPages;

    public Page(Space space, URI uri, DateTime publishDate, String title, String body, URI subPages) {
        this.space = space;
        this.uri = uri;
        this.publishDate = publishDate;
        this.title = title;
        this.body = body;
        this.subPages = subPages;
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

    public String getName() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public URI getSubPages() {
        return subPages;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}

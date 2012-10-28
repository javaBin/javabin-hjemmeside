package controllers.confluence;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.HTTPResponse;

import java.io.InputStream;
import java.util.List;

class RequestFunctions {

    static <T> T withResponse(HTTPResponse response, Function<HTTPResponse, T> function) {
        try {
            return function.apply(response);
        } finally {
            response.consume();
        }
    }

    static <T> List<T> convertFeed(InputStream stream, Function<Entry, T> function) {
        try {
            Document<Feed> document = Abdera.getInstance().getParser().parse(stream);
            List<Entry> entries = document.getRoot().getEntries();
            return ImmutableList.copyOf(Lists.transform(entries, function));
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    static <T> T findInFeed(InputStream stream, Function<Feed, T> function) {
        try {
            Document<Feed> document = Abdera.getInstance().getParser().parse(stream);
            return function.apply(document.getRoot());
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    static <T> T findInService(InputStream stream, Function<Service, T> function) {
        try {
            Document<Service> document = Abdera.getInstance().getParser().parse(stream);
            return function.apply(document.getRoot());
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    static <T> T withStream(InputStream stream, Function<InputStream, T> function) {
        try {
            return function.apply(stream);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }
}

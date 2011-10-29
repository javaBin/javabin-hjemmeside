package controllers;

import static com.google.common.collect.Lists.newArrayList;
import static play.modules.pdf.PDF.renderPDF;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import models.Announcement;
import models.Event;
import models.FacebookUserInfo;
import models.LectureHolder;
import models.Participant;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import notifiers.MailMan;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONArray;

import play.Logger;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.Crypto;
import play.libs.Images;
import play.mvc.Controller;
import play.mvc.Http.Cookie;
import utils.facebook.FacebookHelper;

import com.google.code.facebookapi.FacebookJsonRestClient;
import com.google.code.facebookapi.ProfileField;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import controllers.confluence.Confluence;
import controllers.confluence.NewsItem;
import controllers.confluence.Page;

public class Application extends Controller {

    private static final String FLATPAGE_TRANSFORMATION_RULES = "/flatpage.xslt";

    private static final String XML_POSTFIX = "</xml>";

    private static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml> ";

    private static final String FACEBOOK_COOKIE_KEY = "fbs_227599930584065";

    private static final String FACEBOOK_API_SECRET = "34a5a0101025c27cae5d92882e421a2b";

    private static final String FACEBOOK_API_KEY = "e4e44da4796f00b0e97933b35e720f33";

    private static Confluence confluence;

    private static Confluence getConfluence() {

        if (confluence == null) {
            confluence = new Confluence(URI.create("http://wiki.java.no/rest/atompub/latest/"));
        }
        return confluence;
    }

    public static void index() {

        ArrayList<Announcement> announcements = Cache.get("announcements", ArrayList.class);

        if (announcements == null) {
            try {
                Future<Collection<NewsItem>> forsiden = getConfluence().getNewsFeed("Forsiden",
                        new DateTime().minus(Days.days(5).toStandardDuration()));
                Collection<NewsItem> items = forsiden.get();
                announcements = Lists.newArrayList(Collections2.transform(items,
                        new Function<NewsItem, Announcement>() {

                            @Override
                            public Announcement apply(final NewsItem newsItem) {

                                return new Announcement(newsItem.getTitle(), newsItem.getBody(),
                                        newsItem.getUri().toString());
                            }
                        }));
                Cache.add("announcements", announcements, "5mn");
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error("Confluence didn't load.", e);
            }
        }

        List<Event> events = Event.find("published is true and date >= ? order by date asc",
                new DateMidnight().plus(1).toDate()).fetch();
        String randomId = Codec.UUID();
        render(announcements, events, randomId);
    }

    public static void signUpForEvent(final Long eventId, final String randomId, final String code,
            @Required @Email final String email, @Required final String name,
            @Required final Integer howMany) {

        validation.equals(code.toLowerCase(), Cache.get(randomId)).message("Feil kode!");
        validation.match(howMany, "[1-3]").message("Feltet må være et siffer mellom 1 og 3");
        if (!validation.hasErrors()) {
            Participant participant = null;
            List<Participant> participantList = Participant.find("email = ?", email).fetch();
            if (participantList == null || participantList.isEmpty()) {
                participant = new Participant(email, name);
            } else {
                participant = participantList.get(0);
            }

            Event event = Event.findById(eventId);
            if (!event.participants.contains(participant)) {
                event.participants.add(participant);
                event.participantCount += howMany;
                event.save();
                String crypto = Crypto.encryptAES(participant.id + "_" + event.id);
                MailMan.signUp(participant, event, crypto);
            }
        } else {
            params.flash();
            renderJSON(validation); // gi tilbakemelding.
        }

        Cache.delete(randomId);
        renderJSON(validation); // be bruker sjekke postkassa si.
    }

    private static void signUpValidatedUser(final Long eventId, final String email,
            final String name, final Integer howMany) {

        Participant participant = null;
        List<Participant> participantList = Participant.find("email = ?", email).fetch();
        if (participantList == null || participantList.isEmpty()) {
            participant = new Participant(email, name);
        } else {
            participant = participantList.get(0);
        }

        Event event = Event.findById(eventId);
        if (!event.participants.contains(participant)) {
            event.participants.add(participant);
            event.participantCount += howMany;
            event.save();
            String crypto = Crypto.encryptAES(participant.id + "_" + event.id);
            MailMan.signUp(participant, event, crypto);
        }

    }

    public static void signUpForEventWithFacebook(final Long eventId, final Boolean fromfront) {

        Logger.debug("Signing up with Facebook for event " + eventId);
        Cookie fbCookie = request.cookies.get(FACEBOOK_COOKIE_KEY);

        validation.required(fbCookie);
        if (!validation.hasErrors()) {

            String facebookSessionId = FacebookHelper.getSessionIdFromCookie(fbCookie);
            Logger.debug("Facebook cookie value: %s", fbCookie.value);
            Logger.debug("FB Session key: %s", facebookSessionId);

            FacebookJsonRestClient facebookClient = new FacebookJsonRestClient(FACEBOOK_API_KEY,
                    FACEBOOK_API_SECRET, facebookSessionId);
            try {
                Long userId = facebookClient.users_getLoggedInUser();
                Logger.debug("Logged in with Facebook user ID %d", userId);

                JSONArray userInfo = facebookClient.users_getInfo(newArrayList(userId),
                        newArrayList(ProfileField.NAME, ProfileField.PROXIED_EMAIL));
                Logger.debug("User info: %s", userInfo.toString());

                FacebookUserInfo parsedUserInfo = new Gson().fromJson(userInfo.get(0).toString(),
                        FacebookUserInfo.class);

                signUpValidatedUser(eventId, parsedUserInfo.getProxied_email(),
                        parsedUserInfo.getName(), 1);

                Event event = Event.findById(eventId);

                facebookClient.stream_publish("I will be attending the javaBin meetup, \""
                        + event.title + "\" at " + event.location + " - http://www.java.no/event/"
                        + event.id, null, null, null, userId);

            } catch (Exception e) {
                Logger.error(e, "Failed to sign up using Facebook");
            }

        } else {
            Logger.debug("Validation errors detected - aborting facebook signup.");

        }

        if (fromfront) {
            index();
        } else {
            event(eventId);
        }

    }

    public static void regretSigningUp(final String id) {

        String decrypted = Crypto.decryptAES(id);
        String[] strings = StringUtils.split(decrypted, '_');
        if (strings != null && strings.length == 2) {
            Participant participant = Participant.findById(Long.parseLong(strings[0]));
            Event event = Event.findById(Long.parseLong(strings[1]));

            if (event != null && participant != null && event.participants.contains(participant)) {
                event.participants.remove(participant);
                event.participantCount--;
                event.save();
                render(participant, event);
            }

        }
        render();
    }

    public static void listOldEvents() {

        Date today = new DateMidnight().toDate();
        List<Event> osloEvents = Event.find(
                "published is true and region = ? and date < ? order by date desc",
                Event.Region.OSLO, today).fetch();
        List<Event> trondheimEvents = Event.find(
                "published is true and region = ? and date < ? order by date desc",
                Event.Region.TRONDHEIM, today).fetch();
        List<Event> sorlandetEvents = Event.find(
                "published is true and region = ? and date < ? order by date desc",
                Event.Region.SORLANDET, today).fetch();
        List<Event> bergenEvents = Event.find(
                "published is true and region = ? and date < ? order by date desc",
                Event.Region.BERGEN, today).fetch();
        List<Event> stavangerEvents = Event.find(
                "published is true and region = ? and date < ? order by date desc",
                Event.Region.STAVANGER, today).fetch();
        render(osloEvents, trondheimEvents, sorlandetEvents, bergenEvents, stavangerEvents);
    }

    public static void event(@Required final Long id) {

        String randomId = Codec.UUID();
        Event event = Event.findById(id);
        if (event == null)
            notFound();

        render(event, randomId);
    }

    public static void captcha(final String id) {

        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#000000");
        Cache.set(id, code.toLowerCase(), "10mn");
        renderBinary(captcha);
    }

    public static void lectureholders() {

        List<LectureHolder> lectureholders = LectureHolder.findAll();
        render(lectureholders);
    }

    public static void confluence(final String name) {

        String document = null; // Cache.get(name, String.class);
        if (document == null) {
            try {
                Future<Page> pageFuture = getConfluence().getPage("Forsiden", name);
                Page page = pageFuture.get();
                if (page != null) {

                    document = transform(page);
                }
                if (document != null) {
                    Cache.add(name, document, "5mn");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error("Confluence didn't load.", e);
            }
        }
        if (document == null)
            notFound();

        render(document);
    }

    /**
     * Transforms xhtml-fragments according to rules specified in
     * {@link #FLATPAGE_TRANSFORMATION_RULES}. Currently this results in
     * removing http://wiki.java.no/display/forside/ from page links.
     * 
     * @param page
     * @return
     */
    private static String transform(final Page page) {

        // Possible optimization is to reuse transformerfactory and or
        // transformerinstances, but they are not threadsafe.
        // TransformerPool?
        String xHtml = XML_PREFIX + page.getBody() + XML_POSTFIX;
        TransformerFactory tFactory = TransformerFactory.newInstance();
        StringWriter result = new StringWriter();
        try {
            Transformer transformer = tFactory.newTransformer(new StreamSource(Application.class
                    .getResourceAsStream(FLATPAGE_TRANSFORMATION_RULES)));
            transformer.transform(new StreamSource(new StringReader(xHtml)), new StreamResult(
                    result));
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return page.getBody();
        } catch (TransformerException e) {
            e.printStackTrace();
            return page.getBody();
        }
        result.flush();
        return result.toString();
    }

    public static void ical(final Long id) {

        try {
            Event event = Event.findById(id);
            Calendar calendar = ICalUtil.createCalendar(event);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, bos);
            response.setHeader("Content-Type", "application/ics");
            InputStream is = new ByteArrayInputStream(bos.toByteArray());
            renderBinary(is, "javaBin.ics");
            bos.close();
            is.close();
        } catch (IOException e) {
            Logger.error("Io feil ved ical", e);
        } catch (ValidationException e) {
            Logger.error("Feil ved generering av ical", e);
        }

    }

    public static String gravatarhash(final String input) {

        if (input != null)
            return Codec.hexMD5(input.toLowerCase().trim());
        else
            return null;
    }

    public static void pdf(final Long id) {

        Event event = Event.findById(id);
        renderPDF(event);
    }

}

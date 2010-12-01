package controllers;

import com.google.gson.JsonObject;
import models.Announcement;
import models.Event;
import models.Participant;
import notifiers.MailMan;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.Crypto;
import play.libs.Images;
import play.mvc.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class Application extends Controller {

    public static void index() {
        List<Announcement> announcements;

        announcements = getAnnouncements();

        List<Event> events = Event.find("current is true ").fetch();
        String randomId = Codec.UUID();
        render(announcements, events, randomId);
    }


    private static List<Announcement> getAnnouncements() {
        List<Announcement> announcements;
        Abdera abdera;
        Parser parser;
        URL url;
        Document<Feed> doc;
        Feed feed;

        announcements = new LinkedList<Announcement>();
        abdera = new Abdera();
        parser = abdera.getParser();

        try {
            url = new URL("http://wiki.java.no/createrssfeed.action?types=blogpost&sort=created&showContent=true&showDiff=true&spaces=javabin&labelString=forside&rssType=atom&maxResults=10&timeSpan=5&publicFeed=true&title=javaBin+RSS+Feed");

            doc = parser.parse(url.openStream());
            feed = doc.getRoot();

            for (Entry entry : feed.getEntries()) {
                announcements.add(new Announcement(entry.getTitle(), entry.getSummary(), entry.getLink("alternate").getHref().toString()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return announcements;
    }


    public static void signUpForEvent(Long eventId, String randomId, String code, @Required @Email String email, @Required String name, @Required Integer howMany) {
        validation.equals(code, Cache.get(randomId)).message("Feil kode!");
        validation.match(howMany, "[1-9]").message("Feltet må være et siffer mellom 1 og 9");
        if (!validation.hasErrors()) {
            Participant participant = null;
            List<Participant> participantList = Participant.find("email = ?", email).fetch();
            if (participantList == null || participantList.isEmpty()) {
                participant = new Participant(email, name);
            } else {
                participant = participantList.get(0);
            }

            Event event = Event.findById(eventId);
            String crypto = Crypto.encryptAES(participant.email + "_" + event.title);
            if (event.participants.contains(participant)) {
                event.participants.add(participant);
                event.participantCount += howMany;
                event.save();
                MailMan.signUp(participant, event, crypto);
            } else {
                // add to json that participant already was signed up. we have sent you another email with the details.
                MailMan.signUp(participant, event, crypto);
            }
        } else {
            params.flash();
            renderJSON(validation); // gi tilbakemelding.
        }
        Cache.delete(randomId);
        renderJSON("status:ok"); // be bruker sjekke postkassa si.
    }

    public static void regretSigningUp(String id) {
        String decrypted = Crypto.decryptAES(id);
        String[] strings = StringUtils.split(decrypted, '_');

        Event event = Event.find("title = ?", strings[1]).first();

        Participant p = null;
        for (Participant participant : event.participants) {
            if (participant.email.equalsIgnoreCase(strings[0])) {
                event.participants.remove(participant);
                p = participant;
            }
        }
        event.save();
        render(p, event);
    }

    public static void listOldEvents() {
        List<Event> osloEvents = Event.find("current is false and region = ?", Event.Region.OSLO).fetch();
        List<Event> trondheimEvents = Event.find("current is false and region = ?", Event.Region.TRONDHEIM).fetch();
        List<Event> sorlandetEvents = Event.find("current is false and region = ?", Event.Region.SORLANDET).fetch();
        List<Event> bergenEvents = Event.find("current is false and region = ?", Event.Region.BERGEN).fetch();
        List<Event> stavangerEvents = Event.find("current is false and region = ?", Event.Region.STAVANGER).fetch();
        render(osloEvents, trondheimEvents, sorlandetEvents, bergenEvents, stavangerEvents);
    }

    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#FFFFFF");
        Cache.set(id, code, "10mn");
        renderBinary(captcha);
    }


    // todo : finn ut hvordan man router til statiske sider.
    public static void about() {
        render();
    }

    public static void lectureholders() {
        render();
    }

    public static void contact() {
        render();
    }

}
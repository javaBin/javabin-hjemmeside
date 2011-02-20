package controllers;

import java.io.*;
import java.util.Date;
import java.util.List;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import models.Announcement;
import models.Event;
import models.LectureHolder;
import models.Participant;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import notifiers.MailMan;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;

import play.Logger;
import play.cache.Cache;
import play.data.validation.Email;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.Crypto;
import play.libs.Images;
import play.mvc.Controller;



public class Application extends Controller {

    private static ConfluencePageFetcher fetcher = new ConfluencePageFetcher();

	public static void index() {

        List<Announcement> announcements = Cache.get("announcements", List.class);

        if (announcements == null) {
            try {
                announcements = fetcher.getNewsFeed();
                Cache.add("announcements", announcements, "5mn");
            } catch (Exception e) {
                Logger.error("Confluence didn't load.", e);
            }
        }

        List<Event> events = Event.find("published is true and date >= ? order by date asc", new DateMidnight().plus(1).toDate()).fetch();
        String randomId = Codec.UUID();
        render(announcements, events, randomId);
    }


    public static void signUpForEvent(Long eventId, String randomId, String code, @Required @Email String email, @Required String name, @Required Integer howMany) {
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

    public static void regretSigningUp(String id) {
        String decrypted = Crypto.decryptAES(id);
        String[] strings = StringUtils.split(decrypted, '_');
        if(strings != null && strings.length == 2){
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
        List<Event> osloEvents = Event.find("published is true and region = ? and date < ? ", Event.Region.OSLO, today).fetch();
        List<Event> trondheimEvents = Event.find("published is true and region = ? and date < ?", Event.Region.TRONDHEIM, today).fetch();
        List<Event> sorlandetEvents = Event.find("published is true and region = ? and date < ?", Event.Region.SORLANDET, today).fetch();
        List<Event> bergenEvents = Event.find("published is true and region = ? and date < ?", Event.Region.BERGEN, today).fetch();
        List<Event> stavangerEvents = Event.find("published is true and region = ? and date < ?", Event.Region.STAVANGER, today).fetch();
        render(osloEvents, trondheimEvents, sorlandetEvents, bergenEvents, stavangerEvents);
    }


	public static void event(@Required Long id){
        String randomId = Codec.UUID();
		Event event = Event.findById(id);
        if(event == null)
            notFound();

		render(event, randomId);
	}

    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#000000");
        Cache.set(id, code.toLowerCase(), "10mn");
        renderBinary(captcha);
    }

    public static void lectureholders() {
        List<LectureHolder> lectureholders = LectureHolder.findAll();
		render(lectureholders);
    }

    public static void confluence(String name) {
        String document = Cache.get(name, String.class);
        if(document == null){
            try {
                document = fetcher.getPageAsHTMLFragment(name);
                if(document != null){
                    Cache.add(name,document, "5mn");
                }
            } catch (Exception e) {
                Logger.error("Confluence didn't load.", e);
            }
        }
        if(document==null)
            notFound();

        render(document);
    }

    public static void ical(Long id){
        try {
            Event event = Event.findById(id);
            Calendar calendar = ICalUtil.createCalendar(event);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, bos);
            response.setHeader("Content-Type", "application/ics");
            InputStream is = new ByteArrayInputStream(bos.toByteArray());
            renderBinary(is,"javaBin.ics");
            bos.close();
            is.close();
        } catch (IOException e) {
            Logger.error("Io feil ved ical", e);
        } catch (ValidationException e) {
            Logger.error("Feil ved generering av ical", e);
        }

    }

    public static String gravatarhash(String input){
        if(input != null)
          return Codec.hexMD5(input.toLowerCase().trim());
        else
            return null;
    }


}

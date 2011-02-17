package controllers;


import models.Event;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import java.net.SocketException;


public class ICalUtil {

    public ICalUtil(){

    }

    public static Calendar createCalendar(Event event) throws SocketException {
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone("Europe/Oslo");
        VTimeZone tz = timezone.getVTimeZone();
        // Create the event
        VEvent meeting = new VEvent(new DateTime(event.date), new DateTime(event.date), "javaBin: " + event.title);
        // Add timezone info..
        meeting.getProperties().add(tz.getTimeZoneId());
        UidGenerator ug = new UidGenerator(event.title);
        Uid uid = ug.generateUid();
        meeting.getProperties().add(uid);

        // Create a calendar
        net.fortuna.ical4j.model.Calendar icsCalendar = new net.fortuna.ical4j.model.Calendar();
        icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
        icsCalendar.getProperties().add(CalScale.GREGORIAN);
        icsCalendar.getProperties().add(Version.VERSION_2_0);
        icsCalendar.getComponents().add(meeting);
        return icsCalendar;
    }

}

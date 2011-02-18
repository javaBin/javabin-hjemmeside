package controllers;


import models.Event;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import org.joda.time.*;

import java.net.SocketException;


public class ICalUtil {

    public ICalUtil(){

    }

    public static Calendar createCalendar(Event event) throws SocketException {
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone("Europe/Oslo");
        VTimeZone tz = timezone.getVTimeZone();
        // Create the event
        org.joda.time.DateTime start = new org.joda.time.DateTime(event.date);
        start = start.hourOfDay().setCopy(18);
        org.joda.time.DateTime end = start.plusHours(3);
        VEvent meeting = new VEvent(new DateTime(start.toDate()), new DateTime(end.toDate()), "javaBin: " + event.title);
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

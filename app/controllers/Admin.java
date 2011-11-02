package controllers;

import models.*;
import notifiers.MailMan;
import org.joda.time.DateMidnight;
import play.data.validation.Valid;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

@With(Secure.class)
public class Admin extends Controller {

    @Before
     static void setConnectedUser() {
         if(Security.isConnected()) {
             User user = User.find("byEmail", Security.connected()).first();
             if(user != null)
             renderArgs.put("user", user.fullname);
         }
     }

    public static void index() {
        Date date = new DateMidnight().plus(1).toDate();
        List<Event> currentEvents = Event.find("published is true and date >= ? order by date desc", date).fetch();
        List<Event> oldEvents = Event.find("published is true and date < ? order by date desc", date).fetch();
        List<Event> unpublishedEvents = Event.find("published is false order by date desc").fetch();
        render(unpublishedEvents, currentEvents, oldEvents);
    }


    public static void saveEvent(@Valid Event event){
        if(validation.hasErrors()) {
            params.flash();
            validation.keep();
            showEvent(event.id);
        }

        event.edit("event", params.all());
        event.save();
        showEvent(event.id);
    }

    public static void showEvent(Long eventId){
        if(eventId == null){
            render();
        }

        Event event = Event.findById(eventId);
        render(event);
    }

    public static void deleteEvent(Long id){
        Event event = Event.findById(id);
        event.delete();
        index();
    }


    public static void addLectureHolder(Long eventId, Long lectureholderId, String lectureholderName, String lectureholderGravatar){
        Event event = Event.findById(eventId);
        LectureHolder lectureholder = null;
        if(lectureholderId != null){
            lectureholder = LectureHolder.findById(lectureholderId);
        }

        if(lectureholder == null){
            lectureholder = new LectureHolder();
        }
        lectureholder.gravatarId = lectureholderGravatar;
        lectureholder.fullName = lectureholderName;
        event.lectureholders.add(lectureholder);
        event.save();
    }
    
    public static void addSlide(Long eventId, String description, String link){
        Event event = Event.findById(eventId);
        Slides slide = new Slides();
        slide.event = event;
        slide.description = description;
        slide.link = link;
        event.slides.add(slide);
        event.save();
    }


    public static void removeLectureHolder(Long eventId, Long lectureholderId){
        Event event = Event.findById(eventId);
        LectureHolder lectureHolder = LectureHolder.findById(lectureholderId);
        if(event.lectureholders.contains(lectureHolder)){
            event.lectureholders.remove(lectureHolder);
            event.save();
        }
    }


    public static void deleteLectureholder(Long lectureholderId){
        if(lectureholderId == null)
            lectureholders();

        LectureHolder lectureholder = LectureHolder.findById(lectureholderId);
        for(Event event : lectureholder.events) {
            event.lectureholders.remove(lectureholder);
            event.save();
        }

        LectureHolder.delete("id = ?", lectureholderId);
        lectureholders();
    }


	public static void lectureholders(){
		List<LectureHolder> lectureholders = LectureHolder.findAll();
		render(lectureholders);
	}
	
	public static void saveLectureHolder(LectureHolder lectureHolder){
        lectureHolder.edit("lectureHolder", params.all());
        lectureHolder.save();
		lectureholders();
	}
	
	public static void participants(){
		List<Participant> participants = Participant.findAll();
		render(participants);
	}

    public static void addParticipant(Long eventId, String participantName, String participantEmail){
        Event event = Event.findById(eventId);
        event.participants.add(new Participant(participantEmail,participantName));
        if(event.participantCount != null)
            event.participantCount++;

        event.save();
    }

    public static void removeParticipant(Long eventId, Long participantId){
        Event event  = Event.findById(eventId);
        if(event.participantCount != null && event.participantCount > 0)
            event.participantCount--;

        event.participants.remove(Participant.<Participant>findById(participantId));
        event.save();
    }

    public static void remindParticipants(Long eventId){
	    Event event = Event.findById(eventId);
        if(event != null && event.participants != null)
	    for(Participant participant : event.participants){
            String crypto = Crypto.encryptAES(participant.id + "_" + event.id);
		    MailMan.remindParticipant(event, participant, crypto);
	    }
    }


    public static void sendMeetingMail(Long eventId){
	    Event event = Event.findById(eventId);
        if(event != null){
		    MailMan.sendMeetingMail(event);
	    }
    }

	
	


}

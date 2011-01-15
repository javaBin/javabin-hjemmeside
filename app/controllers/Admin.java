package controllers;

import models.Event;
import models.LectureHolder;
import models.Participant;

import models.User;
import org.joda.time.DateMidnight;
import play.data.validation.Valid;
import play.db.jpa.JPABase;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
import com.google.gson.Gson;

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
        List<Event> currentEvents = Event.find("published is true and date >= ?", date).fetch();
        List<Event> oldEvents = Event.find("published is true and date < ?", date).fetch();
        List<Event> unpublishedEvents = Event.find("published is false").fetch();
        render(unpublishedEvents, currentEvents, oldEvents);
    }


    public static void saveEvent(Event event){
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
            lectureholder = LectureHolder.<LectureHolder>findById(lectureholderId);
        }

        if(lectureholder == null){
            lectureholder = new LectureHolder();
        }
        lectureholder.gravatarId = lectureholderGravatar;
        lectureholder.fullName = lectureholderName;
        event.lectureholders.add(lectureholder);
        event.save();
    }

	public static void lectureholders(){
		List<LectureHolder> lectureholders = LectureHolder.findAll();
		render(lectureholders);
	}
	
	public static void saveLectureHolder(LectureHolder lectureHolder){
		lectureHolder.save();
		lectureholders();
	}
	
	public static void participants(){
		List<Participant> participants = Participant.findAll();
		render(participants);
	}

    public static void addParticipant(Long eventId, String name, String email){
        Event event = Event.findById(eventId);
        event.participants.add(new Participant(email, name));
        event.save();
    }
	
	


}
package controllers;

import models.Event;
import models.LectureHolder;
import models.Participant;

import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
import com.google.gson.Gson;

@With(Secure.class)
public class Admin extends Controller {

    public static void index() {
        Date date = new Date();
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


    public static void addLectureHolder(Long eventId, Long lectureholderId){
        Event event = Event.findById(eventId);
        LectureHolder lectureholder = LectureHolder.<LectureHolder>findById(lectureholderId);
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
	
	
	


}
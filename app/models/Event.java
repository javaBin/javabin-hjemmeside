package models;

import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;
import play.data.validation.*;

@Entity
public class Event extends Model {
	
	public Boolean published = false;

	@MaxSize(1000)
	public String notepad;

    @Required
	public String title;
	
	@MaxSize(200)
	public String description;

    @MaxSize(5000)
    @Column(length = 5000)
    public String extraInfo;

	public String location;

    public String address = "";
	
	@Enumerated(EnumType.STRING) 
	public Region region = Region.OSLO;
	
    @Required
	public Date date;

    @Required
    public Integer startHour = 18;

    @Required
    public Integer startMin = 0;

    @Required
    public Integer endHour = 21;

    @Required
    public Integer endMin = 0;

	public String slidelink1 = "http://";
	@MaxSize(100)
	public String slidedescription1 = "Beskrivelse";
	public String slidelink2 = "http://";
	@MaxSize(100)
	public String slidedescription2 = "Beskrivelse";
	public String slidelink3 = "http://";
	@MaxSize(100)
	public String slidedescription3 = "Beskrivelse";
	public String slidelink4 = "http://";
	@MaxSize(100)
	public String slidedescription4 = "Beskrivelse";



	@ManyToMany(cascade= {CascadeType.PERSIST, CascadeType.MERGE})
	public List<Participant> participants;

	@ManyToMany(cascade= {CascadeType.PERSIST, CascadeType.MERGE})
	public List<LectureHolder> lectureholders;


    public Boolean orderedFood = false;
    public Boolean bookedPlace = false;
    public Boolean writtenTwitter = false;
    public Boolean sentOutEMail = false;
    public Boolean boughtWine = false;


	public Integer participantCount = 0;
    public Integer participantLimit = 100;

	public enum Region {
		OSLO("Oslo", "oslo.png", "krakers@wikimedia",""), 
		BERGEN("Bergen", "bergen.png", "Frédéric de Goldschmidt www.frederic.net",""), 
		SORLANDET("Sørlandet", "grimstad.png", "jagels@flickr",""), 
		TRONDHEIM("Trondheim", "trondheim.png", "stevecadman@flickr",""), 
		STAVANGER("Stavanger", "stavanger.png", "dundak@wikimedia",""),
		SCALABIN("ScalaBin", "scalabin.png", "","");

		public final String realName;
		public final String picture;
		public final String photographer;
		public final String email;

		private Region(String realName, String picture, String photographer, String email) {
			this.realName = realName;
			this.picture = picture;
			this.photographer = photographer;
			this.email = email;
		}
	}
	
	public String toString(){
		return title;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Event event = (Event) o;

        if (published != null ? !published.equals(event.published) : event.published != null) return false;
        if (date != null ? !date.equals(event.date) : event.date != null) return false;
        if (description != null ? !description.equals(event.description) : event.description != null) return false;
        if (location != null ? !location.equals(event.location) : event.location != null) return false;
        if (notepad != null ? !notepad.equals(event.notepad) : event.notepad != null) return false;
        if (participantCount != null ? !participantCount.equals(event.participantCount) : event.participantCount != null)
            return false;
        if (participants != null ? !participants.equals(event.participants) : event.participants != null) return false;
        if (region != event.region) return false;
        if (title != null ? !title.equals(event.title) : event.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (published != null ? published.hashCode() : 0);
        result = 31 * result + (notepad != null ? notepad.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (region != null ? region.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (participants != null ? participants.hashCode() : 0);
        result = 31 * result + (participantCount != null ? participantCount.hashCode() : 0);
        return result;
    }
}


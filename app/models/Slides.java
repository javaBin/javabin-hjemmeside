package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.UniqueConstraint;

import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
public class Slides extends Model {
	
	@ManyToOne
	public Event event;
	
	
	public String link = "http://";
	@MaxSize(100)
	public String description = "Beskrivelse";

}

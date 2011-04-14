package models;

import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;
import play.data.validation.*;

@Entity
public class Announcement extends Model {
	
public String title;
public String content;
public Boolean frontpage = false;
public Boolean published = false;
public Date date = new Date();

    public Announcement(String title, String content) {
        this.title = title;
        this.content = content;
    }
	
	
}


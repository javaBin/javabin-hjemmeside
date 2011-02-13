package models;

import com.google.gson.Gson;
import play.*;
import play.db.jpa.*;
import play.libs.Codec;
import play.libs.Crypto;

import javax.persistence.*;
import java.util.*;

@Entity
public class LectureHolder extends Model {
	            
	public String fullName;
    public String gravatarId = "ingenting";

    @ManyToMany(mappedBy="lectureholders")
	public List<Event> events;


	 public static String toJSON() {
			List<LectureHolder> lectureHolders = LectureHolder.findAll();
	        StringBuilder sb = new StringBuilder();
	        sb.append("[");
	        boolean start = true;
	        for (LectureHolder lectureHolder: lectureHolders) {
	            if (start) {
	                start = false;
	            } else {
	                sb.append(',');
	            }
	            sb.append("{value: \"").append(lectureHolder.fullName).append("\",gravatarId:\"").append(lectureHolder.gravatarId).append("\",id:\"").append(lectureHolder.id).append("\"}");
	        }
	        sb.append("]");
	        return sb.toString();
	 }


    public static String gravatarhash(String gravatarId){
      if(gravatarId != null)
          return Codec.hexMD5(gravatarId.toLowerCase().trim());
        return null;
    }


}


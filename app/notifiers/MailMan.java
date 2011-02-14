package notifiers;
 
import play.*;
import play.mvc.*;
import java.util.*;
import models.*;
 
public class MailMan extends Mailer {
 
   public static void signUp(Participant participant, Event event, String crypto) {
      setSubject("javaBin: %s er registrert som deltager!", participant.name);
      addRecipient(participant.email);
      setFrom("JavaBin <portal@java.no>");
      send(participant, event, crypto);
   }

    public static void remindParticipant(Event event, Participant participant, String crypto) {
      setSubject("javaBin: Påminnelse for møte: %s!", event.title);
      addRecipient(participant.email);
      setFrom("JavaBin <portal@java.no>");
      send(participant, event, crypto);
    }
}
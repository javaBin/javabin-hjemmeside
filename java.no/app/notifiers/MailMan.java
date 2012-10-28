package notifiers;

import static java.lang.String.format;
import models.Event;
import models.Participant;
import models.Tip;
import play.Logger;
import play.mvc.Mailer;

public class MailMan extends Mailer {

	public static void signUp(final Participant participant, final Event event,
			final String crypto) {
		setSubject("javaBin: %s er registrert som deltager!", participant.name);
		addRecipient(participant.email);
		setFrom("JavaBin <portal@java.no>");
		send(participant, event, crypto);
	}

	public static void remindParticipant(final Event event,
			final Participant participant, final String crypto) {
		setSubject("javaBin: Påminnelse for møte: %s!", event.title);
		addRecipient(participant.email);
		setFrom("JavaBin <portal@java.no>");
		send(participant, event, crypto);
	}

	public static void sendMeetingMail(final Event event) {
		setSubject("javaBin: Nytt møte %s!", event.title);
		addRecipient(event.region.email);
		setFrom("JavaBin <portal@java.no>");
		send(event);
	}

	public static void sendLecturerTip(final Tip tip) {

		String groupEmail = format("%s@java.no", tip.who);
		Logger.debug("Sending tip to %s", groupEmail);

		setSubject("[java.no] Nytt tips om foredragsholder fra java.no");
		addRecipient(groupEmail);
		setFrom("JavaBin <portal@java.no>");
		send(tip);
	}
}
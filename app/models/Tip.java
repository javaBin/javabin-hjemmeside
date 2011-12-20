/**
 * 
 */
package models;

import static java.lang.String.format;
import play.data.validation.Email;
import play.data.validation.Required;

/**
 * @author jaran
 * 
 */
public class Tip {

	@Required
	public String name;

	@Required
	@Email
	public String email;

	@Required
	public String who;

	@Required
	public String info;

	@Override
	public String toString() {

		return format("[name=%s, email=%s, who=%s, info=%s]", name, email, who,
				info);
	}
}

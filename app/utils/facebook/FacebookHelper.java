/**
 * 
 */
package utils.facebook;

import play.mvc.Http.Cookie;

/**
 * Utility class for working with Facebook data.
 * 
 * @author jaran
 * 
 */
public class FacebookHelper {

    public static String getSessionIdFromCookie(final Cookie cookie) {

        String cookieValue = cookie.value;
        String[] keyValuePairs = cookieValue.split("&");

        for (String pair : keyValuePairs) {

            String[] keyValue = pair.split("=");
            if (keyValue[0].equals("session_key")) {

                return keyValue[1];
            }
        }

        return null;
    }
}

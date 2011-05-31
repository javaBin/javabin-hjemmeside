/**
 * 
 */
package models;

/**
 * @author jaran
 * 
 */
public class FacebookUserInfo {

    private String uid;

    private String name;

    private String proxied_email;

    public String getUid() {

        return uid;
    }

    public void setUid(final String uid) {

        this.uid = uid;
    }

    public String getName() {

        return name;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public String getProxied_email() {

        return proxied_email;
    }

    public void setProxied_email(final String proxied_email) {

        this.proxied_email = proxied_email;
    }
}

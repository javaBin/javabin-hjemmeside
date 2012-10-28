package controllers;

import models.*;
import play.Logger;

public class Security extends Secure.Security {

    static boolean authenticate(String username, String password) {
        Logger.info("Security: Logging in...", username);
        return User.connect(username, password) != null;
    }


    static void onDisconnected() {
        Application.index();
    }


    static void onAuthenticated() {
        Admin.index();
    }

}

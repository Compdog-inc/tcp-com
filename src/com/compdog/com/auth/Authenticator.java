package com.compdog.com.auth;

public class Authenticator {
    public static boolean Authenticate(String username, String password){
        int hash = password.hashCode();
        switch (username) {
            case "admin":
                return hash == "admin".hashCode();
            case "bob":
                return hash == "pass123".hashCode();
            default:
                return false;
        }

    }
}

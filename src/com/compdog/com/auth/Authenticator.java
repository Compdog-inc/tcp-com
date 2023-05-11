package com.compdog.com.auth;

public class Authenticator {
    public static boolean Authenticate(String username, String password){
        int hash = password.hashCode();
        return switch (username) {
            case "admin" -> hash == "admin".hashCode();
            case "bob" -> hash == "pass123".hashCode();
            default -> false;
        };

    }
}

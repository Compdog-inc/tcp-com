package com.compdog.com.auth;

import com.compdog.com.Test;
import com.sun.istack.internal.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Authenticator {
    @Nullable
    public static UserEntry FetchUser(String username){
        return Test.getCurrentUserList().FindUser(username, false);
    }

    public static UserEntry CreateUser(String username, String password){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(password.getBytes());
            return new UserEntry(username, hash, salt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean Authenticate(String username, String password){
        UserEntry user = FetchUser(username);
        if(user == null){
            return false;
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(user.getSalt());
            byte[] hash = md.digest(password.getBytes());
            return Arrays.equals(hash, user.getPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }
}

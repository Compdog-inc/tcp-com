package com.compdog.com.auth;

public class UserEntry {
    private final String username;
    private final byte[] password;
    private final byte[] salt;

    public UserEntry(String username, byte[] password, byte[] salt) {
        this.username = username;
        this.password = password;
        this.salt = salt;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getPassword() {
        return password;
    }
}

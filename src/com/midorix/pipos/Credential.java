package com.midorix.pipos;

public class Credential {
    public enum AuthType {
        MPPE_128,
        UNKNOWN
    }

    protected String username;
    protected String password;
    protected AuthType authType;


    public Credential(String username, String password, AuthType authType) {
        this.username = username;
        this.password = password;
        this.authType = authType;
    }
}

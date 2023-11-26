package com.example.videoshare;

public class Users {
    String userId, name, email, profile;

    public Users(String userId, String email, String name, String profile) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profile = profile;
    }

    public Users() {

    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return name;
    }

    public String getUserEmail() {
        return email;
    }

    public String getUserProfile() {
        return profile;
    }
}

package com.example.videoshare.ListItems;

public class UserListItem {
    private String id;
    private String email;
    private Boolean isAssigned;

    public UserListItem(String id, String email, Boolean isAssigned) {
        this.id = id;
        this.email = email;
        this.isAssigned = isAssigned;
    }

    public String getEmail() {
        return email;
    }
    public String getId() {
        return id;
    }

    public Boolean getIsAssigned() {
        return isAssigned;
    }
}


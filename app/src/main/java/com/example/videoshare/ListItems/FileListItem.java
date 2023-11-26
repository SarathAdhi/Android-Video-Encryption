package com.example.videoshare.ListItems;

public class FileListItem {
    private String name;
    private String uuid;

    public FileListItem(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }
}


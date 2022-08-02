package ru.gb.storage.commons.messages;

public class CreateDirectoryMessage extends Message {
    private String pathToCreate;

    public String getPathToCreate() {
        return pathToCreate;
    }

    public void setPathToCreate(String pathToCreate) {
        this.pathToCreate = pathToCreate;
    }
}

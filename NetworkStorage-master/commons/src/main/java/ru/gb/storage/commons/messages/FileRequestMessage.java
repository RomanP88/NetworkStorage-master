package ru.gb.storage.commons.messages;

public class FileRequestMessage extends Message {
    private String filename;
    private String pathToSave;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPathToSave() {
        return pathToSave;
    }

    public void setPathToSave(String pathToSave) {
        this.pathToSave = pathToSave;
    }
}

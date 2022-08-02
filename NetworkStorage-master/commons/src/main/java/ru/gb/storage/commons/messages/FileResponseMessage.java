package ru.gb.storage.commons.messages;

public class FileResponseMessage extends Message {
    private String filename;
    private int currentPart;
    private int allParts;
    private long startPosition;
    private byte[] content;
    private String pathToSave;

    public String getPathToSave() {
        return pathToSave;
    }

    public void setPathToSave(String pathToSave) {
        this.pathToSave = pathToSave;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getCurrentPart() {
        return currentPart;
    }

    public void setCurrentPart(int currentPart) {
        this.currentPart = currentPart;
    }

    public int getAllParts() {
        return allParts;
    }

    public void setAllParts(int allParts) {
        this.allParts = allParts;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}

package ru.gb.storage.commons.messages;

public class ChangeDirectoryMessage extends Message {
    private String dirName;

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }
}

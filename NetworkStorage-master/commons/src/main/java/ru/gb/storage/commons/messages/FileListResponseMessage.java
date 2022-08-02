package ru.gb.storage.commons.messages;

import ru.gb.storage.commons.helpers.FileInfo;

import java.util.ArrayList;
import java.util.List;

public class FileListResponseMessage extends Message {
    private List<FileInfo> fileList = new ArrayList<>();
    private String path;

    public List<FileInfo> getFileList() {
        return fileList;
    }

    public void setFileList(List<FileInfo> fileList) {
        this.fileList = fileList;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

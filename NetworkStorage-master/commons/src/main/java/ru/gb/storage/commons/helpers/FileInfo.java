package ru.gb.storage.commons.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileInfo {
    public enum FileTypes {
        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileTypes(String name) {
            this.name = name;
        }
    }

    private String filename;
    private FileTypes type;
    private long size;
    private String lastModified;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileTypes getType() {
        return type;
    }

    public void setType(FileTypes type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public FileInfo(Path path) {
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path);

            this.type = Files.isDirectory(path) ? FileTypes.DIRECTORY : FileTypes.FILE;
            if (this.type == FileTypes.DIRECTORY) {
                this.size = -1L;
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3)).format(dtf);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }

    public FileInfo() {
    }
}
package ru.gb.storage.commons.helpers;

import ru.gb.storage.commons.messages.FileResponseMessage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public class FileTransferHelper {
    private final int PACKET_SIZE = 64 * 1024;
    private RandomAccessFile file;
    private int currentPart;
    private int allParts;
    private FileResponseMessage response;

    public FileTransferHelper(Path fileToSend, String pathToSave) throws IOException {
        String filename = fileToSend.getFileName().toString();
        this.file = new RandomAccessFile(fileToSend.toFile(), "r");
        long length = file.length();
        this.allParts = (int) (length / PACKET_SIZE);
        if (length % PACKET_SIZE != 0) {
            allParts++;
        }
        this.currentPart = 1;
        this.response = new FileResponseMessage();
        response.setFilename(filename);
        response.setAllParts(allParts);
        response.setPathToSave(pathToSave);
    }

    public boolean hasNextPart() {
        return currentPart <= allParts;
    }

    public FileResponseMessage getNextPart() throws IOException {
        response.setCurrentPart(currentPart);
        currentPart++;
        response.setStartPosition(file.getFilePointer());
        long available = file.length() - file.getFilePointer();
        byte[] content;
        if (available > PACKET_SIZE) {
            content = new byte[PACKET_SIZE];
        } else {
            content = new byte[(int) available];
        }
        file.read(content);
        response.setContent(content);
        return response;
    }

    public void close() throws IOException {
        file.close();
        file = null;
    }
}

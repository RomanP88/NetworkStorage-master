package ru.gb.storage.client.services.interfaces;

import ru.gb.storage.commons.helpers.FileTransferHelper;
import ru.gb.storage.commons.messages.Message;

import java.io.IOException;

public interface NetworkService {
    void start() throws InterruptedException;
    void stop();
    void send(Message msg);
    void send(FileTransferHelper helper) throws IOException;
}

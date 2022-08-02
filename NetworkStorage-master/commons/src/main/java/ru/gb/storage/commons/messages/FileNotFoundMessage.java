package ru.gb.storage.commons.messages;

public class FileNotFoundMessage extends ErrorMessage {
    public FileNotFoundMessage() {
        super();
    }

    public FileNotFoundMessage(String errorMessage) {
        super(errorMessage);
    }
}

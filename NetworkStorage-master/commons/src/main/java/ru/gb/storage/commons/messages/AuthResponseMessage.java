package ru.gb.storage.commons.messages;

public class AuthResponseMessage extends Message {
    private MessageStatus status;
    private String text;

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

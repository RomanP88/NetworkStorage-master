package ru.gb.storage.commons.messages;

public class ErrorMessage extends Message{
    private String errorMessage;

    public ErrorMessage() {
    }

    public ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

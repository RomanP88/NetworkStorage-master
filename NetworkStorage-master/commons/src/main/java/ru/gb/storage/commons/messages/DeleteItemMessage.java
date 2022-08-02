package ru.gb.storage.commons.messages;

public class DeleteItemMessage extends Message {
    private String pathToDeleteItem;

    public String getPathToDeleteItem() {
        return pathToDeleteItem;
    }

    public void setPathToDeleteItem(String pathToDeleteItem) {
        this.pathToDeleteItem = pathToDeleteItem;
    }
}

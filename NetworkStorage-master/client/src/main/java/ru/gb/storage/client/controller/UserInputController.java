package ru.gb.storage.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class UserInputController {
    @FXML
    private TextField userInputField;
    @FXML
    private Button btnOk;

    public TextField getUserInputField() {
        return userInputField;
    }

    public Button getBtnOk() {
        return btnOk;
    }
}

package ru.gb.storage.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ProgressBarController {
    @FXML
    private Label lblPercent;
    @FXML
    private Label lblFilename;
    @FXML
    private ProgressBar pbCopy;

    public Label getLblFilename() {
        return lblFilename;
    }

    public ProgressBar getPbCopy() {
        return pbCopy;
    }

    public Label getLblPercent() {
        return lblPercent;
    }
}

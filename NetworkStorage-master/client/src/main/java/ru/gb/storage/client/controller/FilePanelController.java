package ru.gb.storage.client.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import ru.gb.storage.client.Client;
import ru.gb.storage.commons.helpers.FileInfo;
import ru.gb.storage.commons.messages.ChangeDirectoryMessage;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class FilePanelController {
    private boolean serverPanel;
    private Client client;

    @FXML
    private ComboBox<String> disksBox;
    @FXML
    private TextField pathField;
    @FXML
    private Button btnUp;
    @FXML
    private TableView<FileInfo> fileInfoView;
    @FXML
    private TableColumn<FileInfo, String> fileTypeColumn;
    @FXML
    private TableColumn<FileInfo, String> filename;
    @FXML
    private TableColumn<FileInfo, Long> fileSize;
    @FXML
    private TableColumn<FileInfo, String> lastModified;

    public TextField getPathField() {
        return pathField;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void initialize() {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().getName()));
        fileTypeColumn.setVisible(false);

        filename.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFilename()));
        fileSize.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSize()));
        fileSize.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        lastModified.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastModified()));

        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);
        fileInfoView.getColumns().add(fileTypeColumn);
        fileInfoView.getSortOrder().add(fileTypeColumn);
        fileInfoView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    if (serverPanel) {
                        if (fileInfoView.getSelectionModel().getSelectedItem().getType() == FileInfo.FileTypes.DIRECTORY) {
                            ChangeDirectoryMessage cdm = new ChangeDirectoryMessage();
                            cdm.setDirName(pathField.getText() + "\\" + fileInfoView.getSelectionModel().getSelectedItem().getFilename());
                            if (client != null) {
                                client.getNetworkService().send(cdm);
                            } else {
                                System.out.println("Client is null");
                            }
                        }
                    } else {
                        Path path = Paths.get(pathField.getText()).resolve(fileInfoView.getSelectionModel().getSelectedItem().getFilename());
                        if (Files.isDirectory(path)) {
                            updateList(path);
                        }
                    }
                }
            }
        });
    }

    public void setServerPanel(boolean serverPanel) {
        this.serverPanel = serverPanel;
        disksBox.setDisable(serverPanel);
        if (serverPanel) {
            disksBox.getItems().clear();
        }
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        if (serverPanel) {
            ChangeDirectoryMessage cdm = new ChangeDirectoryMessage();
            Path newPath = Paths.get(pathField.getText()).getParent();
            if (newPath != null) {
                cdm.setDirName(newPath.toString());
            } else {
                cdm.setDirName("\\");
            }
            client.getNetworkService().send(cdm);
        } else {
            Path upperPath = Paths.get(pathField.getText()).getParent();
            if (upperPath != null) {
                updateList(upperPath);
            }
        }
    }

    public void updateList(List<FileInfo> fileList) {
        fileInfoView.getItems().clear();
        fileInfoView.getItems().addAll(fileList);
        fileInfoView.sort();
        btnUp.setDisable(pathField.getText().equals("\\"));
    }

    public void updateList(Path currentDir) {
        try {
            fileInfoView.getItems().clear();
            fileInfoView.getItems().addAll(Files.list(currentDir).map(FileInfo::new).collect(Collectors.toList()));
            fileInfoView.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
        pathField.setText(currentDir.toString());
        btnUp.setDisable(currentDir.getParent() == null);
    }

    public String getSelectedFilename() {
        if (!fileInfoView.isFocused()) {
            return null;
        }
        return fileInfoView.getSelectionModel().getSelectedItem().getFilename();
    }

    public boolean isFocused() {
        return fileInfoView.isFocused();
    }

    public void setFocus() {
        Platform.runLater(fileInfoView::requestFocus);
    }
}

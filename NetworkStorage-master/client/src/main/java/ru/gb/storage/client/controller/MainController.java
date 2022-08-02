package ru.gb.storage.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.gb.storage.client.Client;
import ru.gb.storage.commons.helpers.FileInfo;
import ru.gb.storage.commons.helpers.FileTransferHelper;
import ru.gb.storage.commons.messages.CreateDirectoryMessage;
import ru.gb.storage.commons.messages.DeleteItemMessage;
import ru.gb.storage.commons.messages.FileRenameMessage;
import ru.gb.storage.commons.messages.FileRequestMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {
    @FXML
    private VBox clientPanel;
    @FXML
    private VBox serverPanel;

    private Stage userInputStage;
    private Stage pbStage;

    private FilePanelController serverCtrl;
    private FilePanelController clientCtrl;
    private UserInputController inputCtrl;
    private ProgressBarController pbController;

    private Client client;

    private Alert alert;

    public void setClient(Client client) {
        this.client = client;
        clientCtrl.setClient(client);
        serverCtrl.setClient(client);
    }

    @FXML
    private void initialize() {
        serverCtrl = (FilePanelController) serverPanel.getProperties().get("ctrl");
        clientCtrl = (FilePanelController) clientPanel.getProperties().get("ctrl");
        serverCtrl.setServerPanel(true);
        clientCtrl.setServerPanel(false);
        clientCtrl.getPathField().setText(Paths.get(".").normalize().toAbsolutePath().toString());
        alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
        clientCtrl.setFocus();
        initializeUserInput();
    }

    private void initializeUserInput() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/userInput.fxml"));
            HBox userInputBox = loader.load();
            inputCtrl = loader.getController();
            userInputStage = new Stage();
            userInputStage.setResizable(false);
            userInputStage.setScene(new Scene(userInputBox));

            loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/progressBar.fxml"));
            VBox pbBox = loader.load();
            pbController = loader.getController();
            pbStage = new Stage();
            pbStage.setScene(new Scene(pbBox));
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputCtrl.getBtnOk().setOnAction(event -> userInputStage.close());
        inputCtrl.getUserInputField().setOnAction(event -> userInputStage.close());
        userInputStage.setOnCloseRequest(event -> inputCtrl.getUserInputField().clear());
    }

    public void fillClientTable() {
        clientCtrl.updateList(Paths.get(clientCtrl.getPathField().getText()));
    }

    public void fillServerTable(List<FileInfo> fileList, String path) {
        serverCtrl.getPathField().setText(path);
        serverCtrl.updateList(fileList);
    }

    public void btnCopyClick(ActionEvent actionEvent) {
        if (clientCtrl.getSelectedFilename() == null && serverCtrl.getSelectedFilename() == null) {
            alert.setContentText("Ни один файл не был выбран");
            alert.showAndWait();
            return;
        }

        if (clientCtrl.getSelectedFilename() != null) {
            String pathToSave;
            if (serverCtrl.getPathField().getText().isEmpty()) {
                pathToSave = "/";
            } else {
                pathToSave = serverCtrl.getPathField().getText();
            }
            Path fileToTransfer = Paths.get(clientCtrl.getPathField().getText()).resolve(clientCtrl.getSelectedFilename());
            try {
                FileTransferHelper fth = new FileTransferHelper(fileToTransfer, pathToSave);
                client.getNetworkService().send(fth);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverCtrl.getSelectedFilename() != null) {
            FileRequestMessage frm = new FileRequestMessage();
            frm.setFilename(serverCtrl.getSelectedFilename());
            frm.setPathToSave(Paths.get(clientCtrl.getPathField().getText()).toAbsolutePath().toString());
            client.getNetworkService().send(frm);
        }
    }

    public void showErrorMessage(String errorMessage){
        // Пришлось добавить конвертацию, иначе слетала кодировка
        alert.setContentText(new String(errorMessage.getBytes(), StandardCharsets.UTF_8));
        alert.showAndWait();
    }

    public void btnDeleteClick(ActionEvent actionEvent) throws IOException {
        if (clientCtrl.getSelectedFilename() == null && serverCtrl.getSelectedFilename() == null) {
            alert.setContentText("Ни один файл не был выбран");
            alert.showAndWait();
            return;
        }
        if (clientCtrl.getSelectedFilename() != null) {
            Path itemToDelete = Paths.get(clientCtrl.getPathField().getText()).resolve(clientCtrl.getSelectedFilename());
            deleteFiles(itemToDelete.toAbsolutePath());
            fillClientTable();
        }
        if (serverCtrl.getSelectedFilename() != null) {
            DeleteItemMessage dim = new DeleteItemMessage();
            Path pathToDelete = Paths.get(serverCtrl.getPathField().getText()).resolve(serverCtrl.getSelectedFilename());
            dim.setPathToDeleteItem(pathToDelete.toString());
            client.getNetworkService().send(dim);
        }
    }

    public void btnNewDirClick(ActionEvent actionEvent) throws IOException {
        userInputStage.setTitle("Новая дирректория");
        userInputStage.showAndWait();
        if (inputCtrl.getUserInputField().getText().isEmpty()) {
            alert.setContentText("Имя создаваемой дирректории не может быть пустым");
            return;
        }
        Path newDir;
        if (clientCtrl.isFocused()) {
            newDir = Paths.get(clientCtrl.getPathField().getText()).resolve(inputCtrl.getUserInputField().getText());
            Files.createDirectory(newDir);
            fillClientTable();
        } else if (serverCtrl.isFocused()) {
            newDir = Paths.get(serverCtrl.getPathField().getText()).resolve(inputCtrl.getUserInputField().getText());
            CreateDirectoryMessage cdm = new CreateDirectoryMessage();
            cdm.setPathToCreate(newDir.toString());
            client.getNetworkService().send(cdm);
        } else {
            alert.setContentText("Не выбрана панель");
            alert.showAndWait();
        }
        inputCtrl.getUserInputField().clear();
        inputCtrl.getUserInputField().requestFocus();
    }

    private void deleteFiles(Path pathToDelete) throws IOException {
        if(Files.isDirectory(pathToDelete)) {
            for (Path file : Files.list(pathToDelete).collect(Collectors.toList())) {
                if (Files.isDirectory(file)) {
                    deleteFiles(file.toAbsolutePath());
                } else {
                    Files.deleteIfExists(file);
                }
            }
        }
        Files.deleteIfExists(pathToDelete);
    }

    public void btnRename(ActionEvent actionEvent) throws IOException {
        String oldFilename;
        String newFilename;
        userInputStage.setTitle("Переименовать");
        if (clientCtrl.getSelectedFilename() == null && serverCtrl.getSelectedFilename() == null) {
            alert.setContentText("Ни один файл не был выбран");
            alert.showAndWait();
            return;
        }

        if ((oldFilename = clientCtrl.getSelectedFilename()) != null) {
            newFilename = getUserInputNewFilename(oldFilename);
            if (newFilename.isEmpty()) {
                alert.setContentText("Новое имя не может быть пустым");
                alert.showAndWait();
                return;
            }
            Path oldPath = Paths.get(clientCtrl.getPathField().getText()).resolve(oldFilename);
            Path newPath = Paths.get(clientCtrl.getPathField().getText()).resolve(newFilename);
            if (Files.notExists(newPath) && !oldPath.equals(newPath)) {
                Files.move(oldPath, newPath);
            }
            clientCtrl.updateList(Paths.get(clientCtrl.getPathField().getText()));
        }

        if ((oldFilename = serverCtrl.getSelectedFilename()) != null) {
            newFilename = getUserInputNewFilename(oldFilename);
            if (newFilename.isEmpty()) {
                alert.setContentText("Новое имя не может быть пустым");
                alert.showAndWait();
                return;
            }
            FileRenameMessage frm = new FileRenameMessage();
            frm.setOldPath(Paths.get(serverCtrl.getPathField().getText()).resolve(oldFilename).toString());
            frm.setNewPath(Paths.get(serverCtrl.getPathField().getText()).resolve(newFilename).toString());
            client.getNetworkService().send(frm);
        }
    }

    public String getUserInputNewFilename(String oldFilename) {
        inputCtrl.getUserInputField().setText(oldFilename);
        userInputStage.showAndWait();
        String newFilename = inputCtrl.getUserInputField().getText();
        inputCtrl.getUserInputField().clear();
        return newFilename;
    }

    public void showProgressBar(String filename, double percent){
        pbController.getLblFilename().setText(filename);
        pbController.getPbCopy().setProgress(percent / 100);
        pbController.getLblPercent().setText(String.format("%.0f", percent) + "%");
        if(!pbStage.isShowing()){
            pbStage.show();
        }
        if(percent >= 100.0d){
            pbStage.hide();
        }
    }
}

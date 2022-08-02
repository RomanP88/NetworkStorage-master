package ru.gb.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.gb.storage.client.controller.LoginController;
import ru.gb.storage.client.controller.MainController;
import ru.gb.storage.client.services.NetworkServiceImpl;
import ru.gb.storage.client.services.interfaces.NetworkService;
import ru.gb.storage.commons.messages.FileListRequestMessage;

import java.io.IOException;

public class Client extends Application {

    private NetworkService networkService;
    private LoginController loginController;
    private MainController mainController;
    private boolean authenticate;
    private Stage loginForm;

    public NetworkService getNetworkService() {
        return networkService;
    }

    public LoginController getLoginController() {
        return loginController;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void setAuthenticate(boolean authenticate) {
        this.authenticate = authenticate;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start(Stage stage) throws Exception {
        this.networkService = new NetworkServiceImpl(this);
        networkService.start();
        prepareLoginWindow(stage);
        loginForm.showAndWait();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("/mainForm.fxml"));
        VBox mainForm = loader.load();
        stage.setTitle("Main Window");
        mainController = loader.getController();
        mainController.setClient(this);
        stage.setScene(new Scene(mainForm));
        if (authenticate) {
            stage.show();
            FileListRequestMessage flr = new FileListRequestMessage();
            networkService.send(flr);
            mainController.fillClientTable();
        }
    }

    public void prepareLoginWindow(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/loginForm.fxml"));
            AnchorPane loginPane = loader.load();
            loginForm = new Stage();
            loginForm.setTitle("Login");
            loginForm.initModality(Modality.WINDOW_MODAL);
            loginForm.initOwner(stage);
            loginController = loader.getController();
            loginController.setClient(this);
            loginForm.setScene(new Scene(loginPane));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        networkService.stop();
    }
}

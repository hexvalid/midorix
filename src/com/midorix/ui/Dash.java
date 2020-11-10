package com.midorix.ui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Dash extends Application {
    static DashController Controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Log.PRINT_UI = true;
        System.setProperty("prism.allowhidpi", "false");
        setUserAgentStylesheet(STYLESHEET_MODENA);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dash.fxml"));

        Parent root = loader.load();
        Controller = loader.getController();
        primaryStage.setResizable(false);
        primaryStage.setFullScreen(false);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}
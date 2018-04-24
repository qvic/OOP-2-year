package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import models.Message;
import socket.SocketClient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private SocketClient socketClient;

    @FXML
    private TextArea editorArea;

    @FXML
    private Button sendButton;

    private void onSendAction(ActionEvent actionEvent) {
        Message request = new Message(editorArea.getText());
        socketClient.send(request);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socketClient = new SocketClient("localhost", 8080,
                    message -> editorArea.setText(message.getText()));

        } catch (IOException e) {
            System.out.println("Connection refused");
            return;
        }

        sendButton.setOnAction(this::onSendAction);
//        editorArea.textProperty().addListener((observable, oldValue, newValue) -> {
//            // TODO fix invocation if getting data from socket
//            Message request = new Message(newValue);
//            try {
//                sendingQueue.put(request);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
    }
}

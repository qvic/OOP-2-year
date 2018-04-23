package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import socket.EchoClient;
import models.Message;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private EchoClient echoClient;

    @FXML
    private TextArea editorArea;

    @FXML
    private TextArea debugArea;

    @FXML
    private Button sendButton;

    private void onSendAction(ActionEvent actionEvent) {
        try {
            Message request = new Message(editorArea.getText());
            Message response = echoClient.sendAndGet(request);

            debugArea.setText(response.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            echoClient = new EchoClient("localhost", 8080);
        } catch (IOException e) {
            System.out.println("Connection refused");
        }

        sendButton.setOnAction(this::onSendAction);
    }
}

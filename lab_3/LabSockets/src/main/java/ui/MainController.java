package ui;

import client.SocketClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import models.Message;
import models.Messages;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController implements Initializable {

    private SocketClient socketClient;
    private boolean ignoreUpdate = false;

    @FXML
    private TextArea editorArea;

    @FXML
    private Button sendButton;

    private void onSendAction(ActionEvent actionEvent) {
        Message request = new Message(Messages.Type.TEXT, editorArea.getText(), socketClient.getAddress(), new Date());
        socketClient.send(request);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socketClient = new SocketClient("localhost", 8080,
                    message -> {
                        ignoreUpdate = true;
                        editorArea.setText(message.getBody());
                        ignoreUpdate = false;
                    });

        } catch (IOException e) {
            System.out.println("Connection refused");
            return;
        }

        editorArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreUpdate) {
                Message request = new Message(Messages.Type.TEXT, newValue, socketClient.getAddress(), new Date());
                socketClient.send(request);
            }
        });
    }
}

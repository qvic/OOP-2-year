package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import socket.EchoClient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private EchoClient echoClient;

    @FXML
    private Text textLabel;

    @FXML
    private TextField inputText;

    @FXML
    private Button sendButton;

    private void onSendAction(ActionEvent actionEvent) {
        try {
            textLabel.setText("Got: " + echoClient.sendAndGet(inputText.getText()));
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

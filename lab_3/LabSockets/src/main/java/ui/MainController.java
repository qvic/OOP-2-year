package ui;

import client.SocketClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import models.Message;
import models.Messages;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private SocketClient socketClient;
    private boolean ignoreUpdate = false;

    @FXML
    private TextArea editorArea;

    @FXML
    private Button loadButton;

    private void onLoadAction(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open");
//        chooser.setInitialDirectory(
//                new File(System.getProperty("/home/vic"))
//        );
//        chooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("Text files", "*.txt")
//        );
        File file = chooser.showOpenDialog(loadButton.getScene().getWindow());
        if (file == null) return;

        try (FileReader fileReader = new FileReader(file);
             BufferedReader reader = new BufferedReader(fileReader)) {

            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
                line = reader.readLine();
            }

            editorArea.setText(builder.toString());
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadButton.setOnAction(this::onLoadAction);

        try {
            socketClient = new SocketClient("localhost", 8080,
                    message -> {
                        ignoreUpdate = true;
                        editorArea.setText(message.getBody());
                        editorArea.positionCaret(message.getBody().length());
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

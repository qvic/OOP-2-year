package ui;

import client.SocketClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import models.Message;
import models.Messages;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private SocketClient socketClient;
    private boolean ignoreUpdate = false;

    @FXML
    private InlineCssTextArea editorArea;

    @FXML
    private Button loadButton;

    @FXML
    private Button saveButton;

    private void onLoadAction(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open");
        chooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text files", "*.txt")
        );
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

            editorArea.replaceText(builder.toString());
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    private void onSaveAction(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save");
        chooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = chooser.showSaveDialog(loadButton.getScene().getWindow());
        if (file == null) return;

        try (FileWriter fileWriter = new FileWriter(file);
             BufferedWriter writer = new BufferedWriter(fileWriter)) {

            writer.write(editorArea.getText());
        } catch (IOException e) {
            System.out.println("Error writing file");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadButton.setOnAction(this::onLoadAction);
        saveButton.setOnAction(this::onSaveAction);

        try {
            socketClient = new SocketClient("localhost", 8080,
                    message -> Platform.runLater(() -> {
                        ignoreUpdate = true;
                        editorArea.replaceText(message.getBody());
                        editorArea.displaceCaret(message.getBody().length());
                        ignoreUpdate = false;
                    }));

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

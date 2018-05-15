package ui;

import client.SocketClient;
import external.diff_match_patch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import models.Message;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.*;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private SocketClient socketClient;
    private boolean ignoreUpdate = false;
    private diff_match_patch dmp;
    private int stateId = 0;

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

            StringBuilder stringBuilder = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
                line = reader.readLine();
            }

            String result = stringBuilder.toString();

            Message message = new Message(dmp.patch_make(editorArea.getText(), result),
                    socketClient.getAddress(), stateId);

            socketClient.send(message);

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
        dmp = new diff_match_patch();

        loadButton.setOnAction(this::onLoadAction);
        saveButton.setOnAction(this::onSaveAction);

        try {
            socketClient = new SocketClient("localhost", 8080,
                    message -> Platform.runLater(() -> {
                        // TODO adapter for diff_match_patch
                        Object[] result = dmp.patch_apply(message.getPatches(), editorArea.getText());
                        System.out.println(result[1]);

                        replaceText((String) result[0]);

                        stateId = message.getStateId();
                    }));

        } catch (IOException e) {
            throw new NullPointerException("Connection refused");
        }

        editorArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreUpdate) {
                LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(oldValue, newValue);
                Message message = new Message(patches, socketClient.getAddress(), stateId);
                socketClient.send(message);

                replaceText(oldValue);
            }
        });
    }

    private void replaceText(String text) {
        ignoreUpdate = true;
        editorArea.replaceText(text);
        ignoreUpdate = false;
    }
}

package ui;

import client.SocketClient;
import external.diff_match_patch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import models.CursorChange;
import models.Message;
import models.Messages;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.InlineCssTextArea;
import org.reactfx.SuspendableNo;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private SocketClient socketClient;
    private boolean ignoreUpdate = false;
    private int stateId = 0;
    private String oldEditorValue = "";
    private int oldCaretPosition = 0;
    private diff_match_patch dmp;
    private HashMap<String, CaretNode> carets;

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

            socketClient.send("text", message);

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
        carets = new HashMap<>();
        editorArea.setWrapText(true);

        loadButton.setOnAction(this::onLoadAction);
        saveButton.setOnAction(this::onSaveAction);

        try {
            socketClient = new SocketClient("localhost", 8080,
                    request -> Platform.runLater(() -> {
                        String type = request.get("type").asText();
                        if (type.equals("text")) {
                            // TODO proxy for diff_match_patch
                            Message message = Messages.toMessage(request.get("body"));
                            if (message == null) return;

                            Object[] result = dmp.patch_apply(message.getPatches(), editorArea.getText());

                            replaceText((String) result[0]);

                            stateId = message.getStateId();
                        } else if (type.equals("cursor")) {
                            CursorChange message = Messages.toCursorChange(request.get("body"));
                            if (message == null) return;

                            if (!carets.containsKey(message.getAuthor()) && message.getPosition() <= editorArea.getLength()) {
                                CaretNode caret = new CaretNode(message.getAuthor(), editorArea,
                                        new SuspendableNo(), 0);

                                if (!editorArea.addCaret(caret)) {
                                    throw new IllegalStateException("caret was not added to area");
                                }
                                caret.moveTo(message.getPosition());

                                caret.setStroke(Color.RED);

                                carets.put(message.getAuthor(), caret);
                            } else if (message.getPosition() <= editorArea.getLength()) {
                                carets.get(message.getAuthor()).moveTo(message.getPosition());
                            }
                        }

                    }));

        } catch (IOException e) {
            throw new NullPointerException("Connection refused");
        }

        editorArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Changed caret to " + newValue);
            if (!ignoreUpdate) {
                CursorChange cursorChange = new CursorChange(newValue, socketClient.getAddress());
                socketClient.send("cursor", cursorChange);
            }
        });

        editorArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreUpdate) {
                LinkedList<diff_match_patch.Patch> patches = dmp.patch_make(oldValue, newValue);
                Message message = new Message(patches, socketClient.getAddress(), stateId);
//                oldEditorValue = oldValue;
                socketClient.send("text", message);
            }
        });
    }

    private void replaceText(String text) {
        ignoreUpdate = true;
        int caretPosition = editorArea.caretPositionProperty().getValue();
        editorArea.replaceText(text);
        if (caretPosition <= editorArea.getLength()) {
            editorArea.displaceCaret(caretPosition);
        }
        ignoreUpdate = false;
    }
}

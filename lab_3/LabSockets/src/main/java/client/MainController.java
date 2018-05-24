package client;

import external.diff_match_patch;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.CursorChange;
import models.Message;
import models.Messages;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.InlineCssTextArea;
import org.reactfx.SuspendableNo;

import java.io.*;
import java.net.URL;
import java.util.*;

import static client.SocketClient.SERVER_IP;
import static client.SocketClient.SERVER_PORT;

public class MainController implements Initializable {

    private SocketClient socketClient;

    private boolean ignoreUpdate = false;
    private int stateId = 0;
    private diff_match_patch patcher = new diff_match_patch();
    private HashMap<String, CaretNode> carets = new HashMap<>();
    private Random random = new Random();

    @FXML
    private InlineCssTextArea editorArea;

    @FXML
    private Button loadButton;

    @FXML
    private Button saveButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadButton.setOnAction(this::onLoadAction);
        saveButton.setOnAction(this::onSaveAction);

        try {
            socketClient = new SocketClient(SERVER_IP, SERVER_PORT,
                    jsonNode -> {
                        Messages.Type type = Messages.getType(jsonNode);

                        if (type == Messages.Type.TEXT) {

                            Message message = Messages.toMessage(jsonNode);
                            if (message == null) return;
                            Platform.runLater(() -> updateState(message));

                        } else if (type == Messages.Type.CURSOR) {

                            CursorChange cursorChange = Messages.toCursorChange(jsonNode);
                            if (cursorChange == null) return;
                            Platform.runLater(() -> updateCaret(cursorChange));
                        }

                    });

        } catch (IOException e) {
            throw new NullPointerException("Connection refused");
        }

        editorArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreUpdate) {
                CursorChange cursorChange = new CursorChange(newValue, socketClient.getAddress());
                socketClient.send(Messages.Type.CURSOR, cursorChange);
            }
        });

        editorArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreUpdate) {
                LinkedList<diff_match_patch.Patch> patches = patcher.patch_make(oldValue, newValue);
                Message message = new Message(patches, socketClient.getAddress(), stateId);
                socketClient.send(Messages.Type.TEXT, message);
                replaceText(oldValue);
            }
        });

        editorArea.sceneProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            Stage stage = (Stage) newValue.getWindow();
            stage.setOnCloseRequest(event -> {
                try {
                    socketClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }));
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

            Message message = new Message(patcher.patch_make(editorArea.getText(), result),
                    socketClient.getAddress(), 0);

            socketClient.send(Messages.Type.TEXT, message);

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    private void updateCaret(CursorChange cursorChange) {
        if (cursorChange.getPosition() <= editorArea.getLength()) {
            if (!carets.containsKey(cursorChange.getAuthor())) {

                CaretNode caret = new CaretNode(cursorChange.getAuthor(), editorArea,
                        new SuspendableNo(), cursorChange.getPosition());

                if (!editorArea.addCaret(caret)) {
                    throw new IllegalStateException("caret was not added to area");
                }
                caret.setStroke(Color.hsb(random.nextDouble() * 360.0, 0.9, 0.5));
                caret.setStrokeWidth(5.0);
                carets.put(cursorChange.getAuthor(), caret);

            } else {
                carets.get(cursorChange.getAuthor()).moveTo(cursorChange.getPosition());
            }
        }
    }

    private void updateState(Message message) {
        Object[] result = patcher.patch_apply(message.getPatches(), editorArea.getText());
        replaceText((String) result[0]);
        stateId = message.getStateId();
    }

    private void replaceText(String text) {
        ignoreUpdate = true;
        int caretPosition = editorArea.caretPositionProperty().getValue();

        editorArea.replaceText(text);

        if (caretPosition <= editorArea.getLength()) {
            editorArea.moveTo(caretPosition);
        } else {
            editorArea.moveTo(editorArea.getLength());
        }

        ignoreUpdate = false;
    }
}

package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    public Group canvas;

    private Properties gameProperties;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameProperties = new Properties();
        try (FileInputStream stream = new FileInputStream(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResource("game.properties")).getPath())) {
            gameProperties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GameBoard board = new GameBoard(canvas, gameProperties);
    }
}

package controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import models.GitWrapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TreeTableView<GitWrapper> groupsTable;

    @FXML
    Button addRepoButton;

    private TreeItem<GitWrapper> root;

    private void onLoadAction(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open git repository");
        chooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File selectedDirectory = chooser.showDialog(addRepoButton.getScene().getWindow());
        if (selectedDirectory == null) return;
        try {
            try (Git git = Git.open(selectedDirectory)) {
                TreeItem<GitWrapper> treeItem = new TreeItem<>(new GitWrapper(git, git.getRepository().getDirectory().toString()));
                root.getChildren().add(treeItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root = new TreeItem<>(new GitWrapper(null, "Group 1"));
        root.setExpanded(true);
        groupsTable.setRoot(root);

        TreeTableColumn<GitWrapper, String> pathColumn = new TreeTableColumn<>("Path");
        pathColumn.setPrefWidth(200);

        pathColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<GitWrapper, String> p) ->
                new ReadOnlyStringWrapper(p.getValue().getValue().getName()));

        TreeTableColumn<GitWrapper, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setPrefWidth(150);

        nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<GitWrapper, String> p) -> {
            Git git = p.getValue().getValue().getGit();
            if (git == null) return new ReadOnlyStringWrapper("");

            Config config = p.getValue().getValue().getGit().getRepository().getConfig();
            return new ReadOnlyStringWrapper(config.getString("user", null, "name"));
        });

        groupsTable.getColumns().add(pathColumn);
        groupsTable.getColumns().add(nameColumn);

        addRepoButton.setOnAction(this::onLoadAction);
    }
}


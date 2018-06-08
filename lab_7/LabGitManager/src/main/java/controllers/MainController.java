package controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import models.GitWrapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public static final String DEFAULT_GROUP = "Default";
    @FXML
    TreeTableView<GitWrapper> groupsTable;

    @FXML
    Button addRepoButton;

    @FXML
    Button addGroupButton;

    @FXML
    Button showStatisticsButton;

    private TreeItem<GitWrapper> root;

    private void onAddRepoAction(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open git repository");
        chooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File selectedDirectory = chooser.showDialog(addRepoButton.getScene().getWindow());
        if (selectedDirectory == null) return;

        try (Git git = Git.open(selectedDirectory)) {
            TreeItem<GitWrapper> newItem = new TreeItem<>(
                    new GitWrapper(git, git.getRepository().getDirectory().getParentFile().getName()));

            TreeItem<GitWrapper> selectedGroup = groupsTable.getSelectionModel().getSelectedItem();
            if (selectedGroup != null) {
                selectedGroup.setExpanded(true);
                selectedGroup.getChildren().add(newItem);
            } else {
                root.setExpanded(true);
                root.getChildren().get(0).getChildren().add(newItem);
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setHeaderText("Not a repository!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

    }

    private void onAddGroupAction(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("New group");
        dialog.setTitle("Add group");
        dialog.setHeaderText("Creating new group");
        dialog.setContentText("Enter new group name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            TreeItem<GitWrapper> item = new TreeItem<>(new GitWrapper(null, name));
            root.getChildren().add(item);
        });
    }

    private void showStatistics(ActionEvent actionEvent) {
        Dialog dialog = new Dialog();
        dialog.setTitle("Statistics");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        TreeItem<GitWrapper> group = groupsTable.getSelectionModel().getSelectedItem();
        if (group == null) {
            group = root.getChildren().get(0);
        }

        VBox content = new VBox();
        PieChart pieChart = new PieChart();
        content.getChildren().add(new Label("Commits statistics for " + group.getValue().getName()));
        content.getChildren().add(pieChart);

        for (TreeItem<GitWrapper> gitWrapperTreeItem : group.getChildren()) {

            Git git = gitWrapperTreeItem.getValue().getGit();
            int count = 0;
            Iterable<RevCommit> commits;

            try {
                commits = git.log().call();
            } catch (GitAPIException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Exception");
                alert.setHeaderText("Git API Exception");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                return;
            }
            for (RevCommit revCommit : commits) {
                count++;
            }
            pieChart.getData().add(new PieChart.Data(gitWrapperTreeItem.getValue().getName(), count));
        }


        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root = new TreeItem<>();
        root.setExpanded(true);
        groupsTable.setShowRoot(false);
        groupsTable.setRoot(root);

        TreeItem<GitWrapper> defaultGroup = new TreeItem<>(new GitWrapper(null, DEFAULT_GROUP));
        defaultGroup.setExpanded(true);
        root.getChildren().add(defaultGroup);

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

        showStatisticsButton.setOnAction(this::showStatistics);
        addRepoButton.setOnAction(this::onAddRepoAction);
        addGroupButton.setOnAction(this::onAddGroupAction);
    }
}


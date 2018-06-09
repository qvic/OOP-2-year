package controllers;

import com.jcraft.jsch.Session;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import models.GitWrapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

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

    @FXML
    Button executeButton;

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

            TreeItem<GitWrapper> selectedItem = groupsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                TreeItem<GitWrapper> group = getGroup(selectedItem);
                group.setExpanded(true);
                group.getChildren().add(newItem);
            } else {
                root.getChildren().get(0).setExpanded(true);
                root.getChildren().get(0).getChildren().add(newItem);
            }
        } catch (IOException e) {
            showAlert("Not a repository!", e.getMessage(), Alert.AlertType.ERROR);
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

    private void onExecuteAction(ActionEvent actionEvent) {
        List<String> choices = new ArrayList<>();
        choices.add("status");
        choices.add("pull");

        TreeItem<GitWrapper> selectedItem = groupsTable.getSelectionModel().getSelectedItem(), selectedGroup;
        if (selectedItem == null || selectedItem.getParent() == null) {
            selectedGroup = root.getChildren().get(0);
        } else if (isRepository(selectedItem)) {
            selectedGroup = selectedItem.getParent();
        } else {
            selectedGroup = selectedItem;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("status", choices);
        dialog.setTitle("Execute");
        dialog.setHeaderText("Execute command for all repositories in a group " + selectedGroup.getValue().getName());
        dialog.setContentText("git ");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(command -> {
            if (command.equals("status")) {
                for (TreeItem<GitWrapper> item : selectedGroup.getChildren()) {
                    try {
                        showAlert("Status of " + item.getValue().getName(),
                                String.valueOf(item.getValue().getGit().status().call().getUncommittedChanges()),
                                Alert.AlertType.INFORMATION);
                    } catch (GitAPIException e) {
                        showAlert("Git API Exception", e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            } else if (command.equals("pull")) {
                showTokenInput(token -> {

                    for (TreeItem<GitWrapper> item : selectedGroup.getChildren()) {
                        PullCommand pullCommand = item.getValue().getGit().pull();
                        pullCommand.setRemote("origin");
                        pullCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""));
                        try {
                            PullResult pullResult = pullCommand.call();
                            if (pullResult.isSuccessful()) {
                                showAlert("Successfully pulled",
                                        "Pulled " + pullCommand.getRemote(),
                                        Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("Unsuccessful", null, Alert.AlertType.WARNING);
                            }
                        } catch (GitAPIException e) {
                            e.printStackTrace();
                            showAlert("Git API Exception", e.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                });
            }

        });
    }

    private void onShowStatistics(ActionEvent actionEvent) {
        Dialog dialog = new Dialog();
        dialog.setTitle("Statistics");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        TreeItem<GitWrapper> selectedItem = groupsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getParent() == null) {
            selectedItem = root.getChildren().get(0);
        } else if (isRepository(selectedItem)) {
            selectedItem = selectedItem.getParent();
        }

        VBox content = new VBox();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        PieChart commitsChart = new PieChart();
        content.getChildren().add(new Label("Commits statistics for " + selectedItem.getValue().getName()));
        content.getChildren().add(commitsChart);

        PieChart usersChart = new PieChart();
        content.getChildren().add(new Label("Users statistics for " + selectedItem.getValue().getName()));
        content.getChildren().add(usersChart);

        HashMap<String, Integer> commitsByUsers = new HashMap<>();

        Iterable<TreeItem<GitWrapper>> items;
        items = selectedItem.getChildren();

        for (TreeItem<GitWrapper> gitWrapperTreeItem : items) {

            Git git = gitWrapperTreeItem.getValue().getGit();
            int count = 0;
            Iterable<RevCommit> commits;
            try {
                commits = git.log().call();
            } catch (GitAPIException e) {
                showAlert("Git API Exception", e.getMessage(), Alert.AlertType.ERROR);
                return;
            }
            for (RevCommit commit : commits) {
                String name = commit.getAuthorIdent().getName();
                commitsByUsers.put(name, commitsByUsers.getOrDefault(name, 0) + 1);
                count++;
            }
            commitsChart.getData().add(new PieChart.Data(gitWrapperTreeItem.getValue().getName(), count));
        }

        for (Map.Entry<String, Integer> entry : commitsByUsers.entrySet()) {
            usersChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private void showAlert(String headerText, String contentText, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(StringUtils.capitalize(type.toString().toLowerCase()));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private void showTokenInput(Consumer<? super String> ifPresent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Authentication");
        dialog.setHeaderText("Generate token to access this operation");
        dialog.setContentText("Enter token:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(ifPresent);
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

        TreeTableColumn<GitWrapper, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setPrefWidth(200);

        nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<GitWrapper, String> p) ->
                new ReadOnlyStringWrapper(p.getValue().getValue().getName()));

        TreeTableColumn<GitWrapper, String> usernameColumn = new TreeTableColumn<>("Username");
        usernameColumn.setPrefWidth(100);

        usernameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<GitWrapper, String> p) -> {
            Git git = p.getValue().getValue().getGit();
            if (git == null) return new ReadOnlyStringWrapper("");

            Config config = p.getValue().getValue().getGit().getRepository().getConfig();
            return new ReadOnlyStringWrapper(config.getString("user", null, "name"));
        });

        TreeTableColumn<GitWrapper, Date> lastEditedColumn = new TreeTableColumn<>("Last edited");
        lastEditedColumn.setPrefWidth(200);

        lastEditedColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<GitWrapper, Date> p) -> {
            Git git = p.getValue().getValue().getGit();
            if (git == null) return null;

            Date date = null;
            try {
                RevCommit commit = p.getValue().getValue().getGit().log().call().iterator().next();
                date = commit.getAuthorIdent().getWhen();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
            return new ReadOnlyObjectWrapper<>(date);
        });

        groupsTable.getColumns().add(nameColumn);
        groupsTable.getColumns().add(usernameColumn);
        groupsTable.getColumns().add(lastEditedColumn);

        groupsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.getParent() == null) return;
            TreeItem<GitWrapper> group = getGroup(newValue);
            addRepoButton.setText("Add to " + group.getValue().getName());
            executeButton.setText("Execute for " + group.getValue().getName());
        });

        showStatisticsButton.setOnAction(this::onShowStatistics);
        addRepoButton.setOnAction(this::onAddRepoAction);
        addGroupButton.setOnAction(this::onAddGroupAction);
        executeButton.setOnAction(this::onExecuteAction);
    }

    private boolean isGroup(TreeItem<GitWrapper> item) {
        return item.getParent() == root;
    }

    private boolean isRepository(TreeItem<GitWrapper> item) {
        return !isGroup(item);
    }

    private TreeItem<GitWrapper> getGroup(TreeItem<GitWrapper> item) {
        if (isGroup(item)) {
            return item;
        }
        return item.getParent();
    }
}


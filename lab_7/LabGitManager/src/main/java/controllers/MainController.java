package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import models.GitWrapper;
import models.GroupedTableView;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

public class MainController implements Initializable {

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

    private GroupedTableView table;

    private void onAddRepoAction(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Open git repository");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedDirectory = chooser.showDialog(addRepoButton.getScene().getWindow());
        if (selectedDirectory == null) return;

        try (Git git = Git.open(selectedDirectory)) {
            table.addToSelectedGroup(git);
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
            table.addGroup(name);
        });
    }

    private void onExecuteAction(ActionEvent actionEvent) {
        List<String> choices = new ArrayList<>();
        choices.add("status");
        choices.add("pull");

        TreeItem<GitWrapper> selectedGroup = table.getSelectedGroup();

        ChoiceDialog<String> dialog = new ChoiceDialog<>("status", choices);
        dialog.setTitle("Execute");
        dialog.setHeaderText("Execute command for all repositories in a group " + selectedGroup.getValue().getName());
        dialog.setContentText("git ");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(command -> {
            if (command.equals("status")) {
                for (TreeItem<GitWrapper> item : selectedGroup.getChildren()) {
                    try {
                        if (item.getValue().getGit().isPresent()) {
                            showAlert("Status of " + item.getValue().getName(),
                                    String.valueOf(item.getValue().getGit().get().status().call().getUncommittedChanges()),
                                    Alert.AlertType.INFORMATION);
                        }
                    } catch (GitAPIException e) {
                        showAlert("Git API Exception", e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            } else if (command.equals("pull")) {
                showTokenInput(token -> {
                            for (TreeItem<GitWrapper> item : selectedGroup.getChildren()) {
                                if (!item.getValue().getGit().isPresent()) continue;

                                PullCommand pullCommand = item.getValue().getGit().get().pull();
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
                        }
                );
            }
        });
    }

    private void onShowStatistics(ActionEvent actionEvent) {
        Dialog dialog = new Dialog();
        dialog.setTitle("Statistics");

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        TreeItem<GitWrapper> selectedGroup = table.getSelectedGroup();

        VBox content = new VBox();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        PieChart commitsChart = new PieChart();
        content.getChildren().add(new Label("Commits statistics for " + selectedGroup.getValue().getName()));
        content.getChildren().add(commitsChart);

        PieChart usersChart = new PieChart();
        content.getChildren().add(new Label("Users statistics for " + selectedGroup.getValue().getName()));
        content.getChildren().add(usersChart);

        HashMap<String, Integer> commitsByUsers = new HashMap<>();

        Iterable<TreeItem<GitWrapper>> items;
        items = selectedGroup.getChildren();

        for (TreeItem<GitWrapper> gitWrapperTreeItem : items) {

            Optional<Git> git = gitWrapperTreeItem.getValue().getGit();
            if (!git.isPresent()) continue;

            int count = 0;
            Iterable<RevCommit> commits;
            try {
                commits = git.get().log().call();
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
        table = new GroupedTableView(groupsTable);
        table.addSelectionListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.getParent() == null) return;

            TreeItem<GitWrapper> group = table.getGroupFor(newValue);
            addRepoButton.setText("Add to " + group.getValue().getName());
            executeButton.setText("Execute for " + group.getValue().getName());
        });

        showStatisticsButton.setOnAction(this::onShowStatistics);
        addRepoButton.setOnAction(this::onAddRepoAction);
        addGroupButton.setOnAction(this::onAddGroupAction);
        executeButton.setOnAction(this::onExecuteAction);
    }
}


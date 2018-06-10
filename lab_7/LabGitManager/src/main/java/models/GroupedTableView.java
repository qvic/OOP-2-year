package models;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class GroupedTableView {
    private static final String DEFAULT_GROUP = "Default";

    private TreeTableView<GitWrapper> table;
    private TreeItem<GitWrapper> hiddenRoot;

    public GroupedTableView(TreeTableView<GitWrapper> table) {

        this.table = Objects.requireNonNull(table, "null TreeTableView is not allowed");

        hiddenRoot = new TreeItem<>();
        hiddenRoot.setExpanded(true);
        table.setRoot(hiddenRoot);
        table.setShowRoot(false);

        TreeItem<GitWrapper> defaultGroup = new TreeItem<>(new GitWrapper(DEFAULT_GROUP));
        defaultGroup.setExpanded(true);
        hiddenRoot.getChildren().add(defaultGroup);

        // name column
        TreeTableColumn<GitWrapper, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setPrefWidth(200);
        nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<GitWrapper, String> p) ->
                new ReadOnlyStringWrapper(p.getValue().getValue().getName()));

        // username column
        TreeTableColumn<GitWrapper, String> usernameColumn = new TreeTableColumn<>("Username");
        usernameColumn.setPrefWidth(100);
        usernameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<GitWrapper, String> p) -> {
            Optional<Git> git = p.getValue().getValue().getGit();
            if (git.isPresent()) {
                Config config = git.get().getRepository().getConfig();
                return new ReadOnlyStringWrapper(config.getString("user", null, "name"));
            }
            return null;
        });

        TreeTableColumn<GitWrapper, Date> lastEditedColumn = new TreeTableColumn<>("Last edited");
        lastEditedColumn.setPrefWidth(200);

        lastEditedColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<GitWrapper, Date> p) -> {
            Optional<Git> git = p.getValue().getValue().getGit();

            if (git.isPresent()) {
                try {
                    RevCommit commit = git.get().log().call().iterator().next();
                    Date date = commit.getAuthorIdent().getWhen();
                    return new ReadOnlyObjectWrapper<>(date);
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }
            }

            return null;
        });

        table.getColumns().add(nameColumn);
        table.getColumns().add(usernameColumn);
        table.getColumns().add(lastEditedColumn);
    }

    public void addSelectionListener(ChangeListener<? super TreeItem<GitWrapper>> listener) {
        table.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    public void addGroup(String groupName) {
        TreeItem<GitWrapper> item = new TreeItem<>(new GitWrapper(groupName));
        hiddenRoot.getChildren().add(item);
    }

    public TreeItem<GitWrapper> getSelectedGroup() {
        TreeItem<GitWrapper> selectedItem = table.getSelectionModel().getSelectedItem(), group;
        if (selectedItem != null) {
            return getGroupFor(selectedItem);
        }
        return getDefaultGroup();
    }

    public void addToSelectedGroup(Git git) {
        TreeItem<GitWrapper> newItem = new TreeItem<>(
                new GitWrapper(git, git.getRepository().getDirectory().getParentFile().getName()));

        TreeItem<GitWrapper> selectedItem = table.getSelectionModel().getSelectedItem(), group;
        if (selectedItem != null) {
            group = getGroupFor(selectedItem);
        } else {
            group = getDefaultGroup();
        }
        group.setExpanded(true);
        group.getChildren().add(newItem);
    }

    private boolean isGroup(TreeItem<GitWrapper> item) {
        return item.getParent() == hiddenRoot;
    }

    public TreeItem<GitWrapper> getGroupFor(TreeItem<GitWrapper> item) {
        if (isGroup(item)) {
            return item;
        }
        return item.getParent();
    }

    private TreeItem<GitWrapper> getDefaultGroup() {
        return hiddenRoot.getChildren().get(0);
    }
}

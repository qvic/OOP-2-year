import client.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.SocketServer;

import java.io.IOException;

import static client.SocketClient.SERVER_PORT;

public class Main extends Application {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;
    private static final String RUN_SERVER = "server";

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals(RUN_SERVER)) {
            System.out.println("Running server...");
            runServer();
        } else {
            System.out.println("Running client...");
            launch(args);
        }
    }

    private static void runServer() {
        try {
            SocketServer socketServer = new SocketServer(SERVER_PORT);
            socketServer.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("layout/main.fxml"));
        loader.setController(new MainController());
        Parent root = loader.load();

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("LabSockets");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.SocketServer;

import java.io.IOException;

public class Main extends Application {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 300;
    private static final String RUN_SERVER = "server";

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals(RUN_SERVER)) {
            try {
                System.out.println("Running server...");
                SocketServer socketServer = new SocketServer(8080);
                socketServer.startListening();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Running client...");
            launch(args);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("layout/main.fxml"));
        loader.setController(new ui.MainController());
        Parent root = loader.load();

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Echo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

package frontend.admin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AdminMain extends Application {

    public static void main (String[] args) {launch();}

    @Override
    public void start (Stage stage) throws Exception {
        FXMLLoader adminLoader = new FXMLLoader(this
                .getClass()
                .getResource("/frontend/admin/admin-view.fxml"));
        Pane rootPane = adminLoader.load();
        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
        stage.show();
    }
}

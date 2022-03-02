/**
 * Handles the application launch configuration and logic
 */
package frontend.cashflowFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Launch logic
 */
public class CashflowMain extends Application {
    @Override
    public void start (Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CashflowMain.class.getResource("/frontend/cashflowFX/cashflow-view.fxml"));
        Pane root = fxmlLoader.load();
        // fmxl, width, height
        Scene scene = new Scene(root, 600, 600);
        stage.setTitle("Cashflow App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main (String[] args) {
        launch();
    }
}
/**
 * Utility class responsible for loading JavaFX windows.
 */
package frontend.utils;

import frontend.interfaces.Controller;
import frontend.interfaces.Window;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * WindowLoader can load java classes that implement Window.
 */
public class WindowLoader implements Window, Controller {
    private Window windowController;
    private final String title;
    private final int width;
    private final int height;
    private final String resource;
    private Stage returnStage;

    /**
     * Constructor
     * @param windowController Specifies the controller
     * @param title Window title
     * @param width Window width in px
     * @param height Window height in px
     * @param resource FXML file
     */
    public WindowLoader (Window windowController, String title, int width, int height, String resource) {
        this.windowController = windowController;
        this.title = title;
        this.width = width;
        this.height = height;
        this.resource = resource;
    }


    /**
     * Loads the window
     */
    public void loadWindow () {
        Parent root;
        try {
            FXMLLoader ldr = new FXMLLoader(this.windowController
                    .getClass()
                    .getResource(this.resource));
            root = ldr.load();
            Scene scene = new Scene(root, this.width, this.height);
            this.windowController = ldr.getController();
            Stage stage = new Stage();
            stage.setTitle(this.title);
            stage.setScene(scene);
            stage.show();
            this.returnStage = stage;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns the controller's stage
     * @return
     */
    public Stage getStage () {
        return this.returnStage;
    }

    /**
     * Sets the windows css component
     * @param styleURL
     */
    public void setStyles (String styleURL) {
        this
                .getStage()
                .getScene()
                .getStylesheets()
                .add(styleURL);
    }

    /**
     * This instance's controller
     * @return controller
     */
    public Window getController () {
        return this.windowController;
    }
}

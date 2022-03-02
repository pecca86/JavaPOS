/**
 * The main view's logic
 */
package frontend.cashflowFX;

import frontend.admin.AdminMain;
import frontend.interfaces.Controller;
import frontend.interfaces.Window;
import frontend.utils.WindowLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main view
 */
public class CashflowViewController implements Initializable, Window, Controller {
    private CashierViewController cashierViewController;
    private WindowLoader windowLoader;
    private AdminMain adminMain;

    @FXML
    private Button selectCashierBtn, openAdminBtn;


    /**
     * Initialize the window
     *
     * @param url            path to file
     * @param resourceBundle path to fmxl
     */
    @Override
    public void initialize (URL url, ResourceBundle resourceBundle) {
        this.selectCashierBtn.setOnAction(actionEvent -> this.openCashierDialog());
        this.openAdminBtn.setOnAction(actionEvent -> this.openAdminDialog());
    }


    /**
     * Open up a new JavaFX window for the Cashier Main Window
     */
    @FXML
    private void openCashierDialog () {
        this.cashierViewController = new CashierViewController();
        this.windowLoader = new WindowLoader(this.cashierViewController, "Cashier Main View", 600, 600, "/frontend/cashflowFX/cashier-view.fxml");
        this.windowLoader.loadWindow();
    }


    private void openAdminDialog () {
        try {
            this.adminMain = new AdminMain();
            this.adminMain.start(new Stage());
        } catch (Exception e ) {
            e.printStackTrace();
            System.out.println("Failed @ loading admin view");
        }
    }
}
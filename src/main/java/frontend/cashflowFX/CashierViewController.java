/**
 * This class is responsible for loading and closing the cashier UI.
 */
package frontend.cashflowFX;

import frontend.interfaces.Controller;
import frontend.interfaces.Window;
import frontend.utils.WindowLoader;
import javafx.fxml.FXML;
import javafx.stage.Stage;


/**
 * Controller
 */
public class CashierViewController implements Window, Controller {
    private WindowLoader windowLoader;
    private CashierTransactionViewController cashierTransactionViewController;


    /**
     * Loads a new Cashier window upon a new customer interaction.
     */
    @FXML
    public void onNewCustomerTransaction () {
        this.cashierTransactionViewController = new CashierTransactionViewController();
        this.windowLoader = new WindowLoader(this.cashierTransactionViewController, "Cashier Transaction View", 700, 500, "/frontend/cashflowFX/cashier-transaction-view.fxml");
        //this.windowLoader.setStyles("./cashierStyles,css");
        //this.windowLoader.getStage().getScene().getStylesheets().add("./cashierStyles,css");
        this.windowLoader.loadWindow();
        Stage stage = this.windowLoader.getStage();
        stage
                .getScene()
                .getStylesheets()
                .add(this
                        .getClass()
                        .getResource("/frontend/cashflowFX/cashierStyles.css")
                        .toExternalForm());
        stage.setOnCloseRequest(windowEvent -> {
            this.cashierTransactionViewController = (CashierTransactionViewController) this.windowLoader.getController();
            if (!this.cashierTransactionViewController.exitWithoutSaving())
                windowEvent.consume();
        });

    }

}

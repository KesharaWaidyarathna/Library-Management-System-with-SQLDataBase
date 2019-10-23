package Controller;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LDashboardController {


    public AnchorPane anpDashBoard;
    public ImageView icnManageMembers;
    public ImageView icnManageBooks;
    public ImageView icnIssueBook;
    public ImageView icnReturnbooks;
    public ImageView icnSearchBooks;
    public Label lblMenu;
    public Label lblDescription;


    @FXML
    private void navigate(MouseEvent event) throws IOException {
        if (event.getSource() instanceof ImageView){
            ImageView icon = (ImageView) event.getSource();

            Parent root = null;

            switch(icon.getId()){
                case "icnManageMembers":
                    root = FXMLLoader.load(this.getClass().getResource("/View/manageMembers.fxml"));
                    break;
                case "icnManageBooks":
                    root = FXMLLoader.load(this.getClass().getResource("/View/manageBooks.fxml"));
                    break;
                case "icnIssueBook":
                    root = FXMLLoader.load(this.getClass().getResource("/View/issueBooks.fxml"));
                    break;
                case "icnReturnbooks":
                    root = FXMLLoader.load(this.getClass().getResource("/View/returnBooks.fxml"));
                    break;
                case "icnSearchBooks":
                    root = FXMLLoader.load(this.getClass().getResource("/View/Search.fxml"));
                    break;
            }

            if (root != null){
                Scene subScene = new Scene(root);
                Stage primaryStage = (Stage) this.anpDashBoard.getScene().getWindow();
                primaryStage.setScene(subScene);
                primaryStage.centerOnScreen();

                TranslateTransition tt = new TranslateTransition(Duration.millis(350), subScene.getRoot());
                tt.setFromX(-subScene.getWidth());
                tt.setToX(0);
                tt.play();

            }
        }
    }

    @FXML
    private void playMouseExitAnimation(MouseEvent event) {
        if (event.getSource() instanceof ImageView){
            ImageView icon = (ImageView) event.getSource();
            ScaleTransition scaleT =new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1);
            scaleT.setToY(1);
            scaleT.play();

            icon.setEffect(null);
            lblMenu.setText("Welcome");
           lblDescription.setText("Please select one of above main operations to proceed");
        }
    }

    @FXML
    private void playMouseEnterAnimation(MouseEvent event) {
        if (event.getSource() instanceof ImageView){
            ImageView icon = (ImageView) event.getSource();

            switch(icon.getId()){
                case "icnManageMembers":
                    lblMenu.setText("Manage Members");
                    lblDescription.setText("Click to add, edit, delete, search or view members");
                    break;
                case "icnManageBooks":
                    lblMenu.setText("Manage Books");
                    lblDescription.setText("Click to add, edit, delete, search or view books");
                    break;
                case "icnIssueBook":
                    lblMenu.setText("Issue Books");
                    lblDescription.setText("Click here if you want to issue books");
                    break;
                case "icnReturnbooks":
                    lblMenu.setText("Return Books");
                    lblDescription.setText("Click if you want to Return books");
                    break;
                case "icnSearchBooks":
                    lblMenu.setText("Search Books");
                    lblDescription.setText("Click if you want to search Books");
                    break;
            }

            ScaleTransition scaleT =new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1.2);
            scaleT.setToY(1.2);
            scaleT.play();

            DropShadow glow = new DropShadow();
            glow.setColor(Color.CORNSILK);
            glow.setWidth(50);
            glow.setHeight(50);
            glow.setRadius(50);
            icon.setEffect(glow);
        }
    }


}

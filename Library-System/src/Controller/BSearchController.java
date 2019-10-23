package Controller;

import Model.BookTM;
import Model.DB;
import Model.ReturnTM;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;

public class BSearchController {
    public AnchorPane searchPane;
    public JFXTextField txtSearch;
    public TableView<BookTM> tableSearch;
    public ImageView icnBack;

    public void initialize(){

        tableSearch.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tableSearch.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("title"));
        tableSearch.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("author"));
        tableSearch.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("status"));

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","");
            ObservableList<BookTM> books = tableSearch.getItems();

            String sql = "SELECT * from book";
            PreparedStatement pstm = connection.prepareStatement(sql);
            ResultSet rst = pstm.executeQuery();
            while (rst.next()){
                System.out.println("Iitializer");
                books.add(new BookTM(rst.getString(1),rst.getString(2),rst.getString(3),rst.getString(4)));
            }
            tableSearch.setItems(books);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        txtSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                String searchText= txtSearch.getText();

                try{
                    tableSearch.getItems().clear();


                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","");
                        String sql = "Select * FROM book where bid like ? OR title like ? OR Author like ?";
                        PreparedStatement pstm =connection.prepareStatement(sql);
                        String like = "%"+searchText+"%";
                        pstm.setString(1,like);
                        pstm.setString(2,like);
                        pstm.setString(3,like);

                        ResultSet rst=pstm.executeQuery();

                        ObservableList tbl = tableSearch.getItems();
                        tbl.clear();

                        while (rst.next()){
                            tbl.add(new BookTM(rst.getString(1),rst.getString(2),rst.getString(3),rst.getString(4)));
                        }

                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                }catch (NullPointerException n){
                    return;
                }

            }
        });


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
        }
    }

    @FXML
    private void playMouseEnterAnimation(MouseEvent event) {
        if (event.getSource() instanceof ImageView){
            ImageView icon = (ImageView) event.getSource();


            ScaleTransition scaleT =new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1.2);
            scaleT.setToY(1.2);
            scaleT.play();

            DropShadow glow = new DropShadow();
            glow.setColor(Color.CORNFLOWERBLUE);
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(20);
            icon.setEffect(glow);
        }
    }

    public void icnBack_OnAction(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/Dashboard.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage= (Stage) this.searchPane.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
    }



}

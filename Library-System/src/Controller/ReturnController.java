package Controller;

import Model.BookTM;
import Model.DB;
import Model.IssueTM;
import Model.ReturnTM;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ReturnController {


    public AnchorPane anpReturnBooks;
    public TableView<ReturnTM> tblReturnBooks;
    public JFXTextField txtIssuedate;
    public JFXDatePicker dpReturnDate;
    public JFXTextField txtTotalFines;
    public JFXTextField txtBookid;
    public JFXComboBox<String> cmbIssueId;

    public void initialize() throws ClassNotFoundException {

        Class.forName("com.mysql.jdbc.Driver");

        tblReturnBooks.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblReturnBooks.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("issuedDate"));
        tblReturnBooks.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("returnedDate"));
        tblReturnBooks.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("fine"));


        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","mysql");
            ObservableList<ReturnTM> returns = tblReturnBooks.getItems();

            String sql = "SELECT * from returns";
            PreparedStatement pstm = connection.prepareStatement(sql);
            ResultSet rst = pstm.executeQuery();
            while (rst.next()){
                float fine = Float.parseFloat(rst.getString(4));
                returns.add(new ReturnTM(rst.getString(1),rst.getString(2),rst.getString(3),fine));
            }
            tblReturnBooks.setItems(returns);
            cmbIssueId.getItems().clear();
            ObservableList cmbIssue = cmbIssueId.getItems();
            String sql2 = "select * from issue LEFT JOIN returns ON issue.issueId = returns.issueId where returns.issueId is NULL";
            PreparedStatement pstm1 = connection.prepareStatement(sql2);
            ResultSet rst1 = pstm1.executeQuery();

            while(rst1.next()){
                cmbIssue.add(rst1.getString(1));
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        cmbIssueId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if(cmbIssueId.getSelectionModel().getSelectedItem()==null){
                    return;
                }
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","mysql");
                    String sql = "select  issueDate from issue where issueId =?";
                    PreparedStatement pstm = connection.prepareStatement(sql);
                    pstm.setString(1, (String) cmbIssueId.getSelectionModel().getSelectedItem());
                    ResultSet rst = pstm.executeQuery();

                    if(rst.next()){
                        txtIssuedate.setText(rst.getString(1));
                    }

                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }


            }
        });
        dpReturnDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {

                    if(dpReturnDate.getValue()==null){
                        return;
                    }

                LocalDate returned = dpReturnDate.getValue();
                LocalDate issued =  LocalDate.parse(txtIssuedate.getText());

                Date date1 = Date.valueOf(issued);
                Date date2 = Date.valueOf(returned);

                long diff = date2.getTime() - date1.getTime();

                System.out.println(TimeUnit.MILLISECONDS.toDays(diff));
                int dateCount = (int) TimeUnit.MILLISECONDS.toDays(diff);
                float fine = 0;

                if(dateCount>14){
                    fine=dateCount*15;
                }

                txtTotalFines.setText(Float.toString(fine));
            }
        });
    }

    public void btnReturn_Action(ActionEvent actionEvent) throws SQLException {

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","mysql");

        if(cmbIssueId.getSelectionModel().isEmpty() ||
        txtIssuedate.getText().isEmpty() ||
                dpReturnDate.getValue()==null||
                txtTotalFines.getText().isEmpty()
        ){
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill your details.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            System.out.println("You have empty fields!");
            return;
        }

        String issueID= cmbIssueId.getSelectionModel().getSelectedItem();

        String sql ="INSERT INTO returns values(?,?,?,?)";
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setString(1,cmbIssueId.getSelectionModel().getSelectedItem());
        pstm.setString(2,txtIssuedate.getText());
        pstm.setString(3, dpReturnDate.getValue().toString());
        pstm.setString(4, txtTotalFines.getText());

      //  String sql2 = "DELETE from issue where i";
        int affectedRows = pstm.executeUpdate();

        if(affectedRows>0){

            Alert alert=new Alert(Alert.AlertType.INFORMATION,"Book return Successfully !",ButtonType.OK);
            alert.show();

            String sql4 = "Update book SET states=? where bid=?";
            PreparedStatement pstm2 = connection.prepareStatement(sql4);

            String sql3 ="select bookId from issue where issueId=?";
            PreparedStatement pstm3 =connection.prepareStatement(sql3);
            pstm3.setString(1, cmbIssueId.getSelectionModel().getSelectedItem());
            ResultSet rst3 = pstm3.executeQuery();
            String id = null;

            if(rst3.next()){
                id=rst3.getString(1);
            }
            System.out.println(id);

            pstm2.setString(1,"Available");
            pstm2.setString(2, id);
            int affected=pstm2.executeUpdate();
            if (affected>0){
                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                        "Status updated.",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert.showAndWait();
            }

        }else{
            System.out.println("ERROR");
        }


        try {
            tableReturned.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        connection.close();


        comboIssueID.getItems().remove(issueID);
        comboIssueID.getSelectionModel().clearSelection();
        txtIssuedDate.clear();
        txtFine.clear();
        dateReturnedDate.getEditor().clear();

    }

    public void btnDelete_OnAction(ActionEvent actionEvent) {
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
        Stage primaryStage= (Stage) this.ReturnPane.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
    }

    public void btnNew_OnAction(ActionEvent actionEvent) {

    }
}

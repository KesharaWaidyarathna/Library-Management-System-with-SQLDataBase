package Controller;

import Model.BookTM;
import Model.DB;
import Model.IssueTM;
import Model.MemberTM;
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
import java.util.Optional;

public class IssueBooksController {


    public AnchorPane anpIssueBooks;
    public JFXTextField txtIssueId;
    public TableView<IssueTM> tblIssueBooks;
    public JFXDatePicker dpDate;
    public JFXComboBox<String> cmbBookId;
    public JFXComboBox<String> cmbMemberId;
    public JFXTextField txtMemberName;
    public JFXTextField txtBookName;

    public void initialize() throws ClassNotFoundException {

        Class.forName("com.mysql.jdbc.Driver");

            tblIssueBooks.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("issueId"));
            tblIssueBooks.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
            tblIssueBooks.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("memberId"));
            tblIssueBooks.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("bookId"));

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","mysql");
            ObservableList<IssueTM> issue = tblIssueBooks.getItems();

            String sql = "SELECT * from issue";
            PreparedStatement pstm = connection.prepareStatement(sql);
            ResultSet rst = pstm.executeQuery();
            while (rst.next()){
                issue.add(new IssueTM(rst.getString(1),rst.getString(2),rst.getString(3),rst.getString(4)));
            }
            tblIssueBooks.setItems(issue);

            cmbMemberId.getItems().clear();
            ObservableList cmbmembers = cmbMemberId.getItems();
            String sql2 = "select * from member";
            PreparedStatement pstm1 = connection.prepareStatement(sql2);
            ResultSet rst1 = pstm1.executeQuery();

            while(rst1.next()){
                cmbmembers.add(rst1.getString(1));
            }

            cmbBookId.getItems().clear();
            ObservableList cmbbooks = cmbBookId.getItems();
            String sql3 = "select bid from book";
            PreparedStatement pstm2 = connection.prepareStatement(sql3);
            ResultSet rst2 = pstm2.executeQuery();
            while (rst2.next()){
                cmbbooks.add(rst2.getString(1));
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        cmbMemberId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {


                if(cmbMemberId.getSelectionModel().getSelectedItem()!=null){
                    Object selectedItem = cmbMemberId.getSelectionModel().getSelectedItem();
                    if(selectedItem.equals(null) || cmbMemberId.getSelectionModel().isEmpty()){
                        return;
                    }
                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","mysql");
                        String sql= "select name from member where id=?";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1,selectedItem.toString());
                        ResultSet rst = pstm.executeQuery();

                        if(rst.next()){
                            txtMemberName.setText(rst.getString(1));
                        }
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        cmbBookId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if(cmbBookId.getSelectionModel().getSelectedItem()!=null){
                    Object selectedItem = cmbBookId.getSelectionModel().getSelectedItem();

                    try {
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","");
                        String sql= "select title,states from book where bid=?";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1,selectedItem.toString());
                        ResultSet rst = pstm.executeQuery();

                        if(rst.next()){
                            if(rst.getString(2).equals("Available")){
                                txtBookName.setText(rst.getString(1));
                            }else{
                                Alert alert = new Alert(Alert.AlertType.ERROR,
                                        "This book isn't available!",
                                        ButtonType.OK);
                                Optional<ButtonType> buttonType = alert.showAndWait();
                            }
                        }
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void btnNew_Action(ActionEvent actionEvent) throws SQLException {

        txtBookName.clear();
        txtMemberName.clear();
        cmbBookId.getSelectionModel().clearSelection();
        cmbMemberId.getSelectionModel().clearSelection();
        dpDate.setPromptText("Date");

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","mysql");
        String sql ="Select issueId from issue";
        PreparedStatement pstm =connection.prepareStatement(sql);
        ResultSet rst = pstm.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()){
            ids=rst.getString(1);

            int id = Integer.parseInt(ids.replace("I", ""));
            if (id > maxId) {
                maxId = id;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "I00" + maxId;
        } else if (maxId < 100) {
            id = "I0" + maxId;
        } else {
            id = "I" + maxId;
        }
        txtIssueId.setText(id);
        connection.close();
    }

    public void btnIssue_Action(ActionEvent actionEvent) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library","root","mysql");

        if(txtIssueId.getText().isEmpty() ||
                cmbMemberId.getSelectionModel().getSelectedItem().equals(null) ||
                cmbBookId.getSelectionModel().getSelectedItem().equals(null)
                || dpDate.getValue().toString().equals(null)){

            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill all issue details.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        }

        String sql ="INSERT INTO issue values(?,?,?,?)";
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setString(1,txtIssueId.getText());
        pstm.setString(2,dpDate.getValue().toString());
        pstm.setString(3,cmbMemberId.getSelectionModel().getSelectedItem());
        pstm.setString(4,cmbBookId.getSelectionModel().getSelectedItem());

        int affectedRows = pstm.executeUpdate();

        if(affectedRows>0){
            Alert alert=new Alert(Alert.AlertType.INFORMATION,"Book was issued successfully!",ButtonType.OK);
            alert.show();

            String sql2 = "Update book SET states=? where bid=?";
            PreparedStatement pstm2 = connection.prepareStatement(sql2);
            String id = (String) cmbBookId.getSelectionModel().getSelectedItem();
            pstm2.setString(1,"Unavailable");
            pstm2.setString(2, id);
            int affected=pstm2.executeUpdate();
            if (affected>0){
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION,
                        "Status updated.",
                        ButtonType.OK);
                Optional<ButtonType> buttonType = alert1.showAndWait();
            }

        }else{
            System.out.println("ERROR !");
        }
        try {
            tblIssueBooks.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        connection.close();
    }


    public void btnHome_Action(ActionEvent actionEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/MaindashBoard.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage= (Stage) this.anpIssueBooks.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
    }
}

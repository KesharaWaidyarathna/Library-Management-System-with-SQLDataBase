package Controller;

import Model.MemberTM;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.Optional;

public class MembersController {


    public AnchorPane anpManageMembers;
    public JFXTextField txtMemberId;
    public JFXTextField txtAddress;
    public JFXTextField txtName;
    public JFXButton btnAdd;
    public TableView<MemberTM> tblManageMembers;
    public JFXTextField txtContactNo;

    public void initialize() throws ClassNotFoundException {

        Class.forName("com.mysql.jdbc.Driver");

        tblManageMembers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblManageMembers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblManageMembers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));
        tblManageMembers.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("contact"));

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "");
            ObservableList<MemberTM> members = tblManageMembers.getItems();

            String sql = "SELECT * from member";
            PreparedStatement pstm = connection.prepareStatement(sql);
            ResultSet rst = pstm.executeQuery();
            while (rst.next()) {
                members.add(new MemberTM(rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4)));
            }
            tblManageMembers.setItems(members);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tblManageMembers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MemberTM>() {
            @Override
            public void changed(ObservableValue<? extends MemberTM> observable, MemberTM oldValue, MemberTM newValue) {
                MemberTM selectedItem = tblManageMembers.getSelectionModel().getSelectedItem();

                try {
                    Connection connection = null;
                    try {
                        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");
                        String sql = "select * from member where id=?";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1, selectedItem.getId());
                        ResultSet rst = pstm.executeQuery();

                        if (rst.next()) {
                            txtMemberId.setText(rst.getString(1));
                            txtName.setText(rst.getString(2));
                            txtAddress.setText(rst.getString(3));
                            txtContactNo.setText(rst.getString(4));
                            txtMemberId.setDisable(true);
                            btnAdd.setText("Update");
                        }
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (NullPointerException n) {
                    return;
                }

            }
        });


    }

    public void btnNew_Action(ActionEvent actionEvent) throws SQLException {

        txtName.clear();
        txtAddress.clear();
        txtContactNo.clear();
        btnAdd.setText("Add");
        txtMemberId.setDisable(false);

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");
        String sql = "Select id from member";
        PreparedStatement pstm = connection.prepareStatement(sql);
        ResultSet rst = pstm.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()) {
            ids = rst.getString(1);

            int id = Integer.parseInt(ids.replace("M", ""));
            if (id > maxId) {
                maxId = id;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "M00" + maxId;
        } else if (maxId < 100) {
            id = "M0" + maxId;
        } else {
            id = "M" + maxId;
        }
        txtMemberId.setText(id);
        connection.close();
    }

    public void btnAdd_Action(ActionEvent actionEvent) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");

        if (txtMemberId.getText().isEmpty() || txtName.getText().isEmpty() || txtAddress.getText().isEmpty() || txtContactNo.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill all members's details.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        } else if (txtName.getText().matches("^\\D+$") && txtAddress.getText().matches("^\\D+$") && txtContactNo.getText().matches("^\\d{3}-\\d{7}$")) {

            if (btnAdd.getText().equals("Add")) {

                String sql = "INSERT INTO member values(?,?,?,?)";
                PreparedStatement pstm = connection.prepareStatement(sql);
                pstm.setString(1, txtMemberId.getText());
                pstm.setString(2, txtName.getText());
                pstm.setString(3, txtAddress.getText());
                pstm.setString(4, txtContactNo.getText());

                int affectedRows = pstm.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Member was added successfully !");
                } else {
                    System.out.println("Something went Wrong !");
                }
                connection.close();
            } else {

                String sql = "UPDATE member SET name=? , address=? , contact=? where id=?";
                PreparedStatement pstm = connection.prepareStatement(sql);
                pstm.setString(1, txtName.getText());
                pstm.setString(2, txtAddress.getText());
                pstm.setString(3, txtContactNo.getText());
                pstm.setString(4, txtMemberId.getText());
                int affected = pstm.executeUpdate();

                if (affected > 0) {

                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Record updated!",
                            ButtonType.OK);
                    Optional<ButtonType> buttonType = alert.showAndWait();

                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                            "Update error!",
                            ButtonType.OK);
                    Optional<ButtonType> buttonType = alert.showAndWait();

                }
                connection.close();


            }
            tblManageMembers.refresh();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Invalid inputs!",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
        }

        try {
            tblManageMembers.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void btnDelete_Action(ActionEvent actionEvent) throws SQLException {
        MemberTM selectedItem = tblManageMembers.getSelectionModel().getSelectedItem();
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");

        if (tblManageMembers.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select the member who want delete !",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        }
        String sql = "DELETE from member where id=?";
        PreparedStatement pstm = connection.prepareStatement(sql);
        pstm.setString(1, selectedItem.getId());
        int affected = pstm.executeUpdate();
        if (affected > 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Record deleted!",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
        }
        try {
            tblManageMembers.getItems().clear();
            initialize();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void btnHome_Action(ActionEvent actionEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/MaindashBoard.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) this.anpManageMembers.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
    }
}

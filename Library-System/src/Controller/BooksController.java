package Controller;

import Model.BookTM;
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

public class BooksController {


    public AnchorPane anpManageBooks;
    public JFXTextField txtBookId;
    public JFXTextField txtAuthor;
    public JFXTextField txtName;
    public JFXButton btnAdd;
    public JFXTextField txtStatues;
    public TableView<BookTM> tblManageBooks;

    public void initialize() {
        tblManageBooks.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblManageBooks.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("title"));
        tblManageBooks.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("author"));
        tblManageBooks.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("status"));

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");
            ObservableList<BookTM> books = tblManageBooks.getItems();

            String sql = "SELECT * from book";
            PreparedStatement pstm = connection.prepareStatement(sql);
            ResultSet rst = pstm.executeQuery();
            while (rst.next()) {
                books.add(new BookTM(rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4)));
            }
            tblManageBooks.setItems(books);
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        tblManageBooks.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BookTM>() {
            @Override
            public void changed(ObservableValue<? extends BookTM> observable, BookTM oldValue, BookTM newValue) {
                BookTM selectedItem = tblManageBooks.getSelectionModel().getSelectedItem();

                try {
                    Connection connection = null;
                    try {
                        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");
                        String sql = "select * from book where bid=?";
                        PreparedStatement pstm = connection.prepareStatement(sql);
                        pstm.setString(1, selectedItem.getId());
                        ResultSet rst = pstm.executeQuery();

                        if (rst.next()) {
                            txtBookId.setDisable(true);
                            txtBookId.setText(rst.getString(1));
                            txtName.setText(rst.getString(2));
                            txtAuthor.setText(rst.getString(3));
                            txtStatues.setText(rst.getString(4));
                            txtStatues.setDisable(true);
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

        btnAdd.setText("Add");
        txtStatues.setText("Available");
        txtStatues.setDisable(true);
        txtBookId.setDisable(false);
        txtAuthor.clear();
        txtName.clear();

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");
        String sql = "Select bid from book";
        PreparedStatement pstm = connection.prepareStatement(sql);
        ResultSet rst = pstm.executeQuery();

        String ids = null;
        int maxId = 0;

        while (rst.next()) {
            ids = rst.getString(1);

            int id = Integer.parseInt(ids.replace("B", ""));
            if (id > maxId) {
                maxId = id;
            }
        }
        maxId = maxId + 1;
        String id = "";
        if (maxId < 10) {
            id = "B00" + maxId;
        } else if (maxId < 100) {
            id = "B0" + maxId;
        } else {
            id = "B" + maxId;
        }
        txtBookId.setText(id);
        connection.close();

    }

    public void btnAdd_Action(ActionEvent actionEvent) throws SQLException {

        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");

        if (txtBookId.getText().isEmpty() || txtName.getText().isEmpty() || txtAuthor.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please fill all book's details.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        }
        if (txtName.getText().matches("^\\D+$") && txtAuthor.getText().matches("^\\D+$")) {

            if (btnAdd.getText().equals("Add")) {

                String sql = "INSERT INTO book values(?,?,?,?)";
                PreparedStatement pstm = connection.prepareStatement(sql);
                pstm.setString(1, txtBookId.getText());
                pstm.setString(2, txtName.getText());
                pstm.setString(3, txtAuthor.getText());
                pstm.setString(4, txtStatues.getText());

                int affectedRows = pstm.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Books was added successfully");
                } else {
                    System.out.println("Something went wrong!");
                }

                connection.close();

            } else {
                String sql = "UPDATE book SET title=? , author=? where bid=?";
                PreparedStatement pstm = connection.prepareStatement(sql);
                pstm.setString(1, txtName.getText());
                pstm.setString(2, txtAuthor.getText());
                pstm.setString(3, txtBookId.getText());
                int affected = pstm.executeUpdate();

                if (affected > 0) {

                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Book's details was updated!",
                            ButtonType.OK);
                    Optional<ButtonType> buttonType = alert.showAndWait();

                }
            }
            tblManageBooks.refresh();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Invalid inputs!",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
        }
        tblManageBooks.getItems().clear();
        initialize();
    }

    public void btnDelete_Action(ActionEvent actionEvent) throws SQLException {
        BookTM selectedItem = tblManageBooks.getSelectionModel().getSelectedItem();
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "mysql");
        if (tblManageBooks.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Please select the book want delete.",
                    ButtonType.OK);
            Optional<ButtonType> buttonType = alert.showAndWait();
            return;
        }
        String sql = "DELETE from book where bid=?";
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
            tblManageBooks.getItems().clear();
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void btnHome_Action(ActionEvent actionEvent) throws IOException {
        URL resource = this.getClass().getResource("/View/MaindashBoard.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) this.anpManageBooks.getScene().getWindow();
        primaryStage.setScene(scene);

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), scene.getRoot());
        tt.setFromX(-scene.getWidth());
        tt.setToX(0);
        tt.play();
    }
}

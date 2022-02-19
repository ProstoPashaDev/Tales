package com.example.russiantales;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.HashMap;

public class RussianTales extends Application {
    static private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static private final double x = screenSize.getWidth();
    static private final double y = screenSize.getHeight() - 55;

    static private int lastId;
    static private HashMap<String, String> taleMap = new HashMap<>();
    static private AnchorPane mainPane;
    //
    static private Scene insertScene;
    private Stage stage = new Stage();
    //
    private File newTaleImgFile;
    private Image newTaleImg;
    static private Boolean isDeleted = false;
    static private HashMap<String, Integer> idTaleMap = new HashMap<>();

    @Override
    public void start(Stage stage1) throws IOException, ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:~/test", "user", "123");

        mainPane = new AnchorPane();
        refreshTales(connection);

        //Creating insert scene

        AnchorPane insertPane = new AnchorPane();
        insertScene = new Scene(insertPane, x, y);
        Button insertBtn = new Button("Добавить сказку");
        insertBtn.setPrefSize(150, 50);
        insertBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(insertScene);
            }
        });
        AnchorPane.setLeftAnchor(insertBtn, 50.0);
        AnchorPane.setBottomAnchor(insertBtn, 50.0);

        mainPane.getChildren().add(insertBtn);
        Scene mainScene = new Scene(mainPane, x, y);

        //continuing
        Label enterNameLbl = new Label("Название сказки:");
        AnchorPane.setTopAnchor(enterNameLbl, 50.0);
        AnchorPane.setLeftAnchor(enterNameLbl, x/2 - 350);
        TextField taleName = new TextField();
        AnchorPane.setTopAnchor(taleName, 75.0);
        AnchorPane.setLeftAnchor(taleName, x/2 - 350);
        Label enterTextLbl = new Label("Сюжет, персонажи, история создания сказки, автор (если есть):");
        AnchorPane.setTopAnchor(enterTextLbl, 104.0);
        AnchorPane.setLeftAnchor(enterTextLbl, x/2 - 350);
        TextArea taleText = new TextArea();
        taleText.setPrefSize(500, 650);
        AnchorPane.setTopAnchor(taleText, 125.0);
        AnchorPane.setLeftAnchor(taleText, x/2 - 350);
        Label addImageLbl = new Label("Загрузите картинку:");
        AnchorPane.setTopAnchor(addImageLbl, 800.0);
        AnchorPane.setLeftAnchor(addImageLbl, x/2 - 350);
        Button addImageBtn = new Button("Добавить изображение");
        AnchorPane.setTopAnchor(addImageBtn, 827.0);
        AnchorPane.setLeftAnchor(addImageBtn, x/2 - 350);
        addImageBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource Image");
                newTaleImgFile = fileChooser.showOpenDialog(stage);
                newTaleImg = new Image(newTaleImgFile.toURI().toString());
            }
        });
        Button addAllBtn = new Button("Добавить сказку");
        addAllBtn.setPrefSize(150, 50);
        addAllBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO TALES VALUES(" +
                            (lastId + 1) + ", '" + taleName.getText() + "', '" + taleText.getText() + "', " + "FILE_READ('" +
                            newTaleImgFile.getPath() + "'));");
                    prepareStatement.executeUpdate();

                    refreshTales(connection);

                    Button insertBtn = new Button("Добавить сказку");
                    insertBtn.setPrefSize(150, 50);
                    insertBtn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            stage.setScene(insertScene);
                        }
                    });
                    AnchorPane.setLeftAnchor(insertBtn, 50.0);
                    AnchorPane.setBottomAnchor(insertBtn, 50.0);

                    mainPane.getChildren().add(insertBtn);
                } catch (SQLException e) {e.printStackTrace();}
                stage.setScene(mainScene);
            }
        });
        AnchorPane.setTopAnchor(addAllBtn, 890.0);
        AnchorPane.setLeftAnchor(addAllBtn, x/2 - 350);

        insertPane.getChildren().addAll(enterNameLbl, taleName, enterTextLbl, taleText, addImageLbl, addImageBtn, addAllBtn);

        stage.setTitle("Сказки!");
        stage.setScene(mainScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private static Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }
        return new ImageView(wr).getImage();
    }
    private void refreshTales(Connection connection) throws SQLException {
        mainPane.getChildren().clear();

        Label txtLabel = new Label();
        AnchorPane.setLeftAnchor(txtLabel, 230.0);
        AnchorPane.setTopAnchor(txtLabel, 20.0);
        AnchorPane.setRightAnchor(txtLabel, 400.0);
        //TODO
        //txtLabel.setMaxWidth(x - 100);

        ImageView taleImg = new ImageView();
        AnchorPane.setRightAnchor(taleImg, 50.0);
        AnchorPane.setTopAnchor(taleImg, 50.0);
        taleImg.setFitWidth(440);
        taleImg.setFitHeight(400);

        Button deleteBtn = new Button("Удалить сказку");
        deleteBtn.setPrefSize(150, 50);
        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                isDeleted = true;
            }
        });
        AnchorPane.setLeftAnchor(deleteBtn, 220.0);
        AnchorPane.setBottomAnchor(deleteBtn, 50.0);


        ToggleGroup groupOfTales = new ToggleGroup();
        PreparedStatement prepareStatement = connection.prepareStatement("SELECT * FROM TALES");
        ResultSet resultSet = prepareStatement.executeQuery();
        int numb = 0;
        idTaleMap = new HashMap<>();
        while (resultSet.next()){
            ToggleButton taleButton = new ToggleButton(resultSet.getString("name"));
            taleButton.setPrefSize(150, 50);
            taleMap.put(resultSet.getString("name"), resultSet.getString("text"));
            AnchorPane.setLeftAnchor(taleButton, 50.0);
            AnchorPane.setTopAnchor(taleButton, 10.0 + numb * 75.0);
            taleButton.setToggleGroup(groupOfTales);
            mainPane.getChildren().add(taleButton);
            numb++;
            lastId = resultSet.getInt("ID");
            idTaleMap.put(resultSet.getString("name"), resultSet.getInt("ID"));
        }

        groupOfTales.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> changed, Toggle oldValue, Toggle newValue){
                ToggleButton selectedBtn = (ToggleButton) newValue;
                if (isDeleted){
                    PreparedStatement prepareStatement = null;
                    try {
                        prepareStatement = connection.prepareStatement("DELETE FROM TALES WHERE ID = " + idTaleMap.get(selectedBtn.getText()));
                        prepareStatement.executeUpdate();
                    } catch (SQLException e) {e.printStackTrace();}
                    isDeleted = false;
                    try {
                        refreshTales(connection);
                    } catch (SQLException ignored) {}

                    Button insertBtn = new Button("Добавить сказку");
                    insertBtn.setPrefSize(150, 50);
                    insertBtn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            stage.setScene(insertScene);
                        }
                    });
                    AnchorPane.setLeftAnchor(insertBtn, 50.0);
                    AnchorPane.setBottomAnchor(insertBtn, 50.0);

                    mainPane.getChildren().add(insertBtn);
                }
                else{
                    txtLabel.setText(taleMap.get(selectedBtn.getText()));
                    PreparedStatement prepareStatement;
                    ResultSet resultSet;
                    try {
                        prepareStatement = connection.prepareStatement("SELECT * FROM TALES");
                        resultSet = prepareStatement.executeQuery();
                        while (resultSet.next()){
                            if (resultSet.getString("name").equals(selectedBtn.getText())){
                                Blob clob = resultSet.getBlob(4);
                                byte[] byteArr = clob.getBytes(1,(int)clob.length());
                                Image img = convertToFxImage(ImageIO.read(new ByteArrayInputStream(byteArr)));
                                taleImg.setImage(img);
                                lastId = resultSet.getInt("ID");
                            }
                        }
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mainPane.getChildren().add(txtLabel);
        mainPane.getChildren().add(taleImg);
        mainPane.getChildren().add(deleteBtn);
    }
}
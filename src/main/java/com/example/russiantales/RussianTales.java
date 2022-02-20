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
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RussianTales extends Application {
    static private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static private final double x = screenSize.getWidth();
    static private final double y = screenSize.getHeight() - 55;

    static private int lastId;
    static private HashMap<String, String> taleMap = new HashMap<>();
    static private AnchorPane mainPane;
    //
    static private Scene mainScene;
    static private Scene insertScene;
    static private Scene searchingScene;
    static private Scene quizScene;
    static private Scene rewardScene;
    private Stage stage = new Stage();
    //
    private File newTaleImgFile;
    private Image newTaleImg;
    static private Boolean isDeleted = false;
    static private HashMap<String, Integer> idTaleMap = new HashMap<>();
    private int increasing = 1;
    private int textCount = 0;
    private TextArea needfulTaleText;
    private Label mistakeLabel;
    private String needfulText = "";
    private String copyOfRequestedText;
    private String copyOfText;
    private int points = 0;
    Label rewardLbl = new Label();
    Label infoLbl = new Label();

    @Override
    public void start(Stage stage1) throws IOException, ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:~/test", "user", "123");

        mainPane = new AnchorPane();
        refreshTales(connection);
        mainScene = new Scene(mainPane, x, y);

        optionInsertScene(connection);
        optionSearchingScene();
        optionQuizScene();
        optionRewardScene();

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

        Button quizBtn = new Button("Викторина");
        AnchorPane.setLeftAnchor(quizBtn, 390.0 + 170.0);
        AnchorPane.setBottomAnchor(quizBtn, 50.0);
        quizBtn.setPrefSize(150, 50);
        quizBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(quizScene);
            }
        });


        Label txtLabel = new Label();
        AnchorPane.setLeftAnchor(txtLabel, 230.0);
        AnchorPane.setTopAnchor(txtLabel, 20.0);
        AnchorPane.setRightAnchor(txtLabel, 500.0);
        //TODO
        //txtLabel.setMaxWidth(x - 100);

        ImageView taleImg = new ImageView();
        AnchorPane.setRightAnchor(taleImg, 50.0);
        AnchorPane.setTopAnchor(taleImg, 20.0);
        taleImg.setFitWidth(440);
        taleImg.setFitHeight(400);

        Button searchingSceneBtn = new Button("Поиск");
        AnchorPane.setLeftAnchor(searchingSceneBtn, 390.0);
        AnchorPane.setBottomAnchor(searchingSceneBtn, 50.0);
        searchingSceneBtn.setPrefSize(150, 50);
        searchingSceneBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(searchingScene);
            }
        });

        ToggleGroup groupOfTales = new ToggleGroup();

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
                selectedBtn.setSelected(false);
                if (isDeleted){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText("Вы точно уверены, что хотите удалить эту сказку?");
                    Optional<ButtonType> option = alert.showAndWait();
                    if (option.get() == ButtonType.OK) {
                        PreparedStatement prepareStatement = null;
                        try {
                            prepareStatement = connection.prepareStatement("DELETE FROM TALES WHERE ID = " + idTaleMap.get(selectedBtn.getText()));
                            prepareStatement.executeUpdate();
                        } catch (SQLException e) {e.printStackTrace();}
                        try {
                            refreshTales(connection);
                        } catch (SQLException ignored) {}
                    }
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
                isDeleted = false;
            }
        });

        mainPane.getChildren().add(txtLabel);
        mainPane.getChildren().add(taleImg);
        mainPane.getChildren().add(deleteBtn);
        mainPane.getChildren().add(searchingSceneBtn);
        mainPane.getChildren().add(insertBtn);
        mainPane.getChildren().add(quizBtn);
    }
    private void optionSearchingScene(){
        AnchorPane searchingPane = new AnchorPane();

        Label infoSearching = new Label("Введите искомое слово/выражение");
        AnchorPane.setTopAnchor(infoSearching, 50.0);
        AnchorPane.setLeftAnchor(infoSearching, x/2 - 600);

        TextField textFieldForSearching = new TextField();
        AnchorPane.setTopAnchor(textFieldForSearching, 90.0);
        AnchorPane.setLeftAnchor(textFieldForSearching, x/2 - 600);
        textFieldForSearching.setPrefSize(120, 40);

        Label searchingTaleTitle = new Label("---");

        Button searchingBtn = new Button("Поиск");
        searchingBtn.setPrefSize(120, 20);
        AnchorPane.setTopAnchor(searchingBtn, 150.0);
        AnchorPane.setLeftAnchor(searchingBtn, x/2 - 600);
        searchingBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                for (Map.Entry<String, String> pair: taleMap.entrySet()) {
                    boolean fl = false;
                    try {
                        fl = search(textFieldForSearching.getText(), pair.getValue());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if (fl){
                        searchingTaleTitle.setText(pair.getKey());
                        break;
                    }
                }
            }
        });

        Label infoTitle = new Label("Заголовок искомой сказки:");
        AnchorPane.setTopAnchor(infoTitle, 50.0);
        AnchorPane.setLeftAnchor(infoTitle, x/2 - 350);

        AnchorPane.setTopAnchor(searchingTaleTitle, 90.0);
        AnchorPane.setLeftAnchor(searchingTaleTitle, x/2 - 350);

        Label infoText = new Label("Текст искомой сказки и искомое слово:");
        AnchorPane.setTopAnchor(infoText, 130.0);
        AnchorPane.setLeftAnchor(infoText, x/2 - 350);

        needfulTaleText = new TextArea();
        AnchorPane.setTopAnchor(needfulTaleText, 170.0);
        AnchorPane.setLeftAnchor(needfulTaleText, x/2 - 350);
        needfulTaleText.setPrefSize(900, 600);

        mistakeLabel = new Label();
        AnchorPane.setTopAnchor(mistakeLabel, 190.0);
        AnchorPane.setLeftAnchor(mistakeLabel, x/2 - 600);

        Button returnBtn = new Button("Вернуться на главный экран");
        returnBtn.setPrefSize(200, 70);
        AnchorPane.setTopAnchor(returnBtn, 170.0 + 620);
        AnchorPane.setLeftAnchor(returnBtn, x/2 - 600);
        returnBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(mainScene);
                textFieldForSearching.clear();
                searchingTaleTitle.setText("---");
                needfulTaleText.clear();
            }
        });

        searchingPane.getChildren().addAll(infoSearching, textFieldForSearching, searchingBtn, infoTitle, searchingTaleTitle, infoText, needfulTaleText, mistakeLabel, returnBtn);
        searchingScene = new Scene(searchingPane, x, y);
    }

    private Boolean search(String requestedText, String text){
        needfulTaleText.setText(text);
        needfulTaleText.requestFocus();
        /*
        if (!text.equals(copyOfText)){
            textCount = 0;
            copyOfRequestedText = text;
        }

         */
        if (needfulText.equals(requestedText)) {
            copyOfRequestedText = copyOfRequestedText.substring(0, copyOfRequestedText.indexOf(requestedText)) + copyOfRequestedText.substring(copyOfRequestedText.indexOf(requestedText) + requestedText.length());
            textCount++;
        } else {
            textCount = 0;
            copyOfRequestedText = text;
        }
        if (copyOfRequestedText.contains(requestedText)) {
            needfulTaleText.positionCaret(copyOfRequestedText.indexOf(requestedText) + textCount * requestedText.length());
            mistakeLabel.setText(null);
        } else {
            if (textCount == 0) {
                mistakeLabel.setText("Такого текста нет");
                needfulTaleText.clear();
                copyOfRequestedText = null;
            } else {
                mistakeLabel.setText("Больше такого текста нет");
            }
            return false;
        }
        needfulText = requestedText;
        copyOfText = text;
        return true;

    }

    private void optionQuizScene(){
        AnchorPane quizPane = new AnchorPane();

        ToggleGroup firstGroup = new ToggleGroup();

        Label firstQuestion = createQuestionLbl("Где живет Бабушка Яга?", 0);
        RadioButton firstFirstRadioBtn = createRadioButton("В пещере", 0, 0);
        RadioButton firstSecondRadioBtn = createRadioButton("В избушке", 1, 0);
        RadioButton firstThirdRadioBtn = createRadioButton("Во дворце", 2, 0);
        firstFirstRadioBtn.setToggleGroup(firstGroup);
        firstSecondRadioBtn.setToggleGroup(firstGroup);
        firstThirdRadioBtn.setToggleGroup(firstGroup);

        ToggleGroup secondGroup = new ToggleGroup();

        Label secondQuestion = createQuestionLbl("Этого мальчугана унесли Гуси-лебеди в далекие края.", 1);
        RadioButton secondFirstRadioBtn = createRadioButton("Илья Муромец", 0, 1);
        RadioButton secondSecondRadioBtn = createRadioButton("Евгений Онегин", 1, 1);
        RadioButton secondThirdRadioBtn = createRadioButton("Иванушка", 2, 1);
        secondFirstRadioBtn.setToggleGroup(secondGroup);
        secondSecondRadioBtn.setToggleGroup(secondGroup);
        secondThirdRadioBtn.setToggleGroup(secondGroup);

        ToggleGroup thirdGroup = new ToggleGroup();

        Label thirdQuestion = createQuestionLbl("Эта болотная обитательница вышла замуж за царевича.", 2);
        RadioButton thirdFirstRadioBtn = createRadioButton("Лягушка", 0, 2);
        RadioButton thirdSecondRadioBtn = createRadioButton("Леший", 1, 2);
        RadioButton thirdThirdRadioBtn = createRadioButton("Баба Яга", 2, 2);
        thirdFirstRadioBtn.setToggleGroup(thirdGroup);
        thirdSecondRadioBtn.setToggleGroup(thirdGroup);
        thirdThirdRadioBtn.setToggleGroup(thirdGroup);

        ToggleGroup fourthGroup = new ToggleGroup();

        Label fourthQuestion = createQuestionLbl("В этой сказке рыба могла исполнять желания.", 3);
        RadioButton fourthFirstRadioBtn = createRadioButton("Золотая рыбка", 0, 3);
        RadioButton fourthSecondRadioBtn = createRadioButton("Теремок", 1, 3);
        RadioButton fourthThirdRadioBtn = createRadioButton("Репка", 2, 3);
        fourthFirstRadioBtn.setToggleGroup(fourthGroup);
        fourthSecondRadioBtn.setToggleGroup(fourthGroup);
        fourthThirdRadioBtn.setToggleGroup(fourthGroup);

        ToggleGroup fifthGroup = new ToggleGroup();

        Label fifthQuestion = createQuestionLbl("Как звать сестричку Бабы-Яги?", 4);
        RadioButton fifthFirstRadioBtn = createRadioButton("Лешая", 0, 4);
        RadioButton fifthSecondRadioBtn = createRadioButton("Кикимора", 1, 4);
        RadioButton fifthThirdRadioBtn = createRadioButton("Царевна", 2, 4);
        fifthFirstRadioBtn.setToggleGroup(fifthGroup);
        fifthSecondRadioBtn.setToggleGroup(fifthGroup);
        fifthThirdRadioBtn.setToggleGroup(fifthGroup);

        ToggleGroup sixthGroup = new ToggleGroup();

        Label sixthQuestion = createQuestionLbl("Старику понадобилось столько попыток закидывания невода.", 5);
        RadioButton sixthFirstRadioBtn = createRadioButton("2", 0, 5);
        RadioButton sixthSecondRadioBtn = createRadioButton("3", 1, 5);
        RadioButton sixthThirdRadioBtn = createRadioButton("4", 2, 5);
        sixthFirstRadioBtn.setToggleGroup(sixthGroup);
        sixthSecondRadioBtn.setToggleGroup(sixthGroup);
        sixthThirdRadioBtn.setToggleGroup(sixthGroup);

        ToggleGroup seventhGroup = new ToggleGroup();

        Label seventhQuestion = createQuestionLbl("Как звать ту курочку, которая снесла драгоценное яйцо старику и бабе?", 6);
        RadioButton seventhFirstRadioBtn = createRadioButton("Елена", 0, 6);
        RadioButton seventhSecondRadioBtn = createRadioButton("Ряба", 1, 6);
        RadioButton seventhThirdRadioBtn = createRadioButton("Рябчик", 2, 6);
        seventhFirstRadioBtn.setToggleGroup(seventhGroup);
        seventhSecondRadioBtn.setToggleGroup(seventhGroup);
        seventhThirdRadioBtn.setToggleGroup(seventhGroup);

        ToggleGroup eighthGroup = new ToggleGroup();

        Label eighthQuestion = createQuestionLbl("Это животное увидело первое теремок и поселилось там.", 7);
        RadioButton eighthFirstRadioBtn = createRadioButton("Крыска-норушка", 0, 7);
        RadioButton eighthSecondRadioBtn = createRadioButton("Мышь", 1, 7);
        RadioButton eighthThirdRadioBtn = createRadioButton("Кот", 2, 7);
        eighthFirstRadioBtn.setToggleGroup(eighthGroup);
        eighthSecondRadioBtn.setToggleGroup(eighthGroup);
        eighthThirdRadioBtn.setToggleGroup(eighthGroup);

        ToggleGroup ninthGroup = new ToggleGroup();

        Label ninthQuestion = createQuestionLbl("Эти птицы украли Иванушку, когда его сестричка играла и гуляла.", 8);
        RadioButton ninthFirstRadioBtn = createRadioButton("Гуси-лебеди", 0, 8);
        RadioButton ninthSecondRadioBtn = createRadioButton("Соколы", 1, 8);
        RadioButton ninthThirdRadioBtn = createRadioButton("Аисты", 2, 8);
        ninthFirstRadioBtn.setToggleGroup(ninthGroup);
        ninthSecondRadioBtn.setToggleGroup(ninthGroup);
        ninthThirdRadioBtn.setToggleGroup(ninthGroup);

        ToggleGroup tenthGroup = new ToggleGroup();

        Label tenthQuestion = createQuestionLbl("Какого цвета был у Петушка гребешок?", 9);
        RadioButton tenthFirstRadioBtn = createRadioButton("Ярко-красный", 0, 9);
        RadioButton tenthSecondRadioBtn = createRadioButton("Золотой", 1, 9);
        RadioButton tenthThirdRadioBtn = createRadioButton("Серый", 2, 9);
        tenthFirstRadioBtn.setToggleGroup(tenthGroup);
        tenthSecondRadioBtn.setToggleGroup(tenthGroup);
        tenthThirdRadioBtn.setToggleGroup(tenthGroup);

        Button checkBtn = new Button("Закончить викторину");
        checkBtn.setPrefSize(150, 50);
        AnchorPane.setTopAnchor(checkBtn, 900.0);
        AnchorPane.setLeftAnchor(checkBtn, x/2 - 340);
        checkBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                /*
                firstGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends Toggle> observableValue, Toggle toggle, Toggle btn) {
                        RadioButton selectedBtn = (RadioButton) btn;
                        if (selectedBtn.getText().equals("В избушке")){
                            points++;
                        }
                    }
                });

                 */
                try {
                    RadioButton selectedBtn = (RadioButton) firstGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("В избушке")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) secondGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("Иванушка")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) thirdGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("Лягушка")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) fourthGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("Золотая рыбка")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) fifthGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("Кикимора")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) sixthGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("3")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) seventhGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("Ряба")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) eighthGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("Мышь")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) ninthGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("Гуси-лебеди")) {
                        points++;
                    }
                    selectedBtn = (RadioButton) tenthGroup.getSelectedToggle();
                    selectedBtn.setSelected(false);
                    if (selectedBtn.getText().equals("Золотой")) {
                        points++;
                    }
                }
                catch (Exception ignored){}

                optionRewardsLbl();
                points = 0;
                stage.setScene(rewardScene);
            }
        });


        quizPane.getChildren().addAll(firstQuestion, firstFirstRadioBtn, firstSecondRadioBtn, firstThirdRadioBtn, secondQuestion, secondFirstRadioBtn, secondSecondRadioBtn, secondThirdRadioBtn);
        quizPane.getChildren().addAll(thirdQuestion, thirdFirstRadioBtn, thirdSecondRadioBtn, thirdThirdRadioBtn, fourthQuestion, fourthFirstRadioBtn, fourthSecondRadioBtn, fourthThirdRadioBtn);
        quizPane.getChildren().addAll(fifthQuestion, fifthFirstRadioBtn, fifthSecondRadioBtn, fifthThirdRadioBtn, sixthQuestion, sixthFirstRadioBtn, sixthSecondRadioBtn, sixthThirdRadioBtn);
        quizPane.getChildren().addAll(seventhQuestion, seventhFirstRadioBtn, seventhSecondRadioBtn, seventhThirdRadioBtn, eighthQuestion, eighthFirstRadioBtn, eighthSecondRadioBtn, eighthThirdRadioBtn);
        quizPane.getChildren().addAll(ninthQuestion, ninthFirstRadioBtn, ninthSecondRadioBtn, ninthThirdRadioBtn, tenthQuestion, tenthFirstRadioBtn, tenthSecondRadioBtn, tenthThirdRadioBtn);
        quizPane.getChildren().add(checkBtn);

        quizScene = new Scene(quizPane, x, y);
    }
    private RadioButton createRadioButton(String text, int column, int row){
        RadioButton radioButton = new RadioButton(text);
        AnchorPane.setTopAnchor(radioButton, 80.0 + row * 80);
        AnchorPane.setLeftAnchor(radioButton, x/2 - 60 - column * 140);
        return radioButton;
    }
    private Label createQuestionLbl(String text, int row){
        Label questionLbl  = new Label(text);
        AnchorPane.setTopAnchor(questionLbl, 50.0 + row * 80);
        AnchorPane.setLeftAnchor(questionLbl, x/2 - 340);
        return questionLbl;
    }

    private void optionRewardScene(){
        AnchorPane rewardPane = new AnchorPane();

        infoLbl.setFont(new Font("Arial", 30));
        AnchorPane.setTopAnchor(infoLbl, 50.0);
        AnchorPane.setLeftAnchor(infoLbl, x/2 - 400);

        rewardLbl.setFont(new Font("Arial", 30));
        AnchorPane.setTopAnchor(rewardLbl, 100.0);
        AnchorPane.setLeftAnchor(rewardLbl, x/2 - 400);

        Button returnBtn = new Button();
        returnBtn.setPrefSize(250, 100);
        returnBtn.setText("Вернуться на главный экран");
        returnBtn.setFont(new Font("Arial", 17));
        AnchorPane.setTopAnchor(returnBtn, 230.0);
        AnchorPane.setLeftAnchor(returnBtn, x/2 - 400);
        returnBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(mainScene);
            }
        });

        rewardPane.getChildren().addAll(infoLbl);
        rewardPane.getChildren().addAll(rewardLbl);
        rewardPane.getChildren().addAll(returnBtn);
        rewardScene = new Scene(rewardPane, x, y);
    }
    private void optionRewardsLbl(){
        if (points >= 7){
            infoLbl.setText("Поздравляем вы успешно прошли викторину по русским народным сказкам");
        }
        else {
            infoLbl.setText("Вы прошли викторину по русским народным сказкам");
        }
        rewardLbl.setText("Ваши баллы - " + points + "/10");
        if (points < 5){
            rewardLbl.setText(rewardLbl.getText() + "\nВаша оценка - 2");
        }
        else if (points == 6){
            rewardLbl.setText(rewardLbl.getText() + "\nВаша оценка - 3");
        }
        else if (points >= 7 && points < 9){
            rewardLbl.setText(rewardLbl.getText() + "\nВаша оценка - 4");
        }
        else if (points >= 9){
            rewardLbl.setText(rewardLbl.getText() + "\nВаша оценка - 5");
        }
    }

    private void optionInsertScene(Connection connection){
        AnchorPane insertPane = new AnchorPane();
        insertScene = new Scene(insertPane, x, y);


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
        Label addImageLbl = new Label("Загрузите изображение:");
        AnchorPane.setTopAnchor(addImageLbl, 800.0);
        AnchorPane.setLeftAnchor(addImageLbl, x/2 - 350);
        Label imageLbl = new Label();
        AnchorPane.setTopAnchor(imageLbl, 827.0);
        AnchorPane.setLeftAnchor(imageLbl, x/2 - 180);
        Button addImageBtn = new Button("Добавить изображение");
        AnchorPane.setTopAnchor(addImageBtn, 827.0);
        AnchorPane.setLeftAnchor(addImageBtn, x/2 - 350);
        addImageBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource Image");
                newTaleImgFile = fileChooser.showOpenDialog(stage);
                if (newTaleImgFile != null){
                    imageLbl.setText("Картинка успешно загружена");
                }
                else {
                    imageLbl.setText("Не удалось загрузить картинку");
                }
                try {
                    newTaleImg = new Image(newTaleImgFile.toURI().toString());
                }
                catch (Exception ignored){}
            }
        });
        Button addAllBtn = new Button("Добавить сказку");
        addAllBtn.setPrefSize(150, 50);
        addAllBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (!taleName.getText().equals("") && !taleText.getText().equals("") && newTaleImgFile != null){
                    try {
                        PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO TALES VALUES(" +
                                (lastId + increasing) + ", '" + taleName.getText() + "', '" + taleText.getText() + "', " + "FILE_READ('" +
                                newTaleImgFile.getPath() + "'));");
                        increasing++;
                        prepareStatement.executeUpdate();

                        taleName.clear();
                        taleText.clear();
                        newTaleImgFile = null;

                        refreshTales(connection);
                    } catch (SQLException e) {e.printStackTrace();}
                    stage.setScene(mainScene);
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Не все поля заполнены");
                    alert.setContentText("Пожалуйста, заполните все поля");
                    alert.showAndWait();
                }
            }
        });

        Button returnBtn = new Button("Отмена");
        returnBtn.setPrefSize(150, 50);
        AnchorPane.setTopAnchor(returnBtn, 890.0);
        AnchorPane.setLeftAnchor(returnBtn, x/2 - 150);
        returnBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                stage.setScene(mainScene);
                taleName.clear();
                taleText.clear();
                imageLbl.setText("");
            }
        });
        AnchorPane.setTopAnchor(addAllBtn, 890.0);
        AnchorPane.setLeftAnchor(addAllBtn, x/2 - 350);

        insertPane.getChildren().addAll(enterNameLbl, taleName, enterTextLbl, taleText, addImageLbl, addImageBtn, addAllBtn, imageLbl, returnBtn);
    }
}
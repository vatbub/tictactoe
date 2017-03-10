package com.github.vatbub.tictactoe.view;

import com.github.vatbub.tictactoe.Board;
import com.github.vatbub.tictactoe.NameList;
import com.github.vatbub.tictactoe.Player;
import common.Common;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import logging.FOKLogger;
import org.controlsfx.control.ToggleSwitch;
import view.ExceptionAlert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The main game view
 */
public class Main extends Application {
    private static final double animationSpeed = 0.3;
    private static final int gameRows = 3;
    private static final int gameCols = 3;
    private static final String player1Letter = "X";
    private static final String player2Letter = "O";
    StringProperty style = new SimpleStringProperty("");
    private String suggestedHumanName1;
    private String suggestedHumanName2;
    private String suggestedAIName1;
    private String suggestedAIName2;
    private Board board;
    @FXML
    private AnchorPane gamePane;

    @FXML
    private TableView<Row> gameTable;

    @FXML
    private VBox menuBox;

    @FXML
    private AnchorPane menuBackground;

    @FXML
    private TextField player1Name;

    @FXML
    private ToggleSwitch player1AIToggle;

    @FXML
    private TextField player2Name;

    @FXML
    private ToggleSwitch player2AIToggle;

    @FXML
    private Group winLineGroup;

    public static void main(String[] args) {
        Common.setAppName("tictactoev2");
        FOKLogger.enableLoggingOfUncaughtExceptions();

        for (String arg : args) {
            if (arg.toLowerCase().matches("mockappversion=.*")) {
                // Set the mock version
                String version = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.setMockAppVersion(version);
            } else if (arg.toLowerCase().matches("mockbuildnumber=.*")) {
                // Set the mock build number
                String buildnumber = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.setMockBuildNumber(buildnumber);
            } else if (arg.toLowerCase().matches("mockpackaging=.*")) {
                // Set the mock packaging
                String packaging = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                Common.setMockPackaging(packaging);
            } else if (arg.toLowerCase().matches("locale=.*")) {
                // set the gui language
                String guiLanguageCode = arg.substring(arg.toLowerCase().indexOf('=') + 1);
                FOKLogger.info(Main.class.getName(), "Setting language: " + guiLanguageCode);
                Locale.setDefault(new Locale(guiLanguageCode));
            }
        }

        launch(args);
    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * <p>
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("View.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setMinWidth(scene.getRoot().minWidth(0) + 70);
        primaryStage.setMinHeight(scene.getRoot().minHeight(0) + 70);

        primaryStage.setScene(scene);

        // Set Icon
        // primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));

        primaryStage.show();
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        // modify the default exception handler to show a good error message on every uncaught exception
        final Thread.UncaughtExceptionHandler currentUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            if (currentUncaughtExceptionHandler != null) {
                // execute current handler as we only want to append it
                currentUncaughtExceptionHandler.uncaughtException(thread, exception);
            }
            Platform.runLater(() -> new ExceptionAlert(exception).showAndWait());
        });

        player1AIToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue && player1Name.getText().equals(suggestedAIName1)) || (!oldValue && player1Name.getText().equals(suggestedHumanName1))) {
                player1SetSampleName();
            }
        });
        player2AIToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue && player2Name.getText().equals(suggestedAIName2)) || (!oldValue && player2Name.getText().equals(suggestedHumanName2))) {
                player2SetSampleName();
            }
        });

        gameTable.setSelectionModel(null);
        gameTable.heightProperty().addListener((observable, oldValue, newValue) -> {
            Pane header = (Pane) gameTable.lookup("TableHeaderRow");
            if (header.isVisible()) {
                header.setMaxHeight(0);
                header.setMinHeight(0);
                header.setPrefHeight(0);
                header.setVisible(false);
            }
            renderRows();
        });
        gameTable.setRowFactory(new Callback<TableView<Row>, TableRow<Row>>() {
            @Override
            public TableRow<Row> call(TableView<Row> param) {
                TableRow<Row> row = new TableRow<>();
                row.styleProperty().bind(style);
                return row;
            }
        });

        initBoard();
        initNewGame();
    }

    @FXML
    void startButtonOnAction(ActionEvent event) {
        initBoard();
        hideMenu();
        String finalPlayerName1 = player1Name.getText();
        if (player1AIToggle.isSelected()) {
            finalPlayerName1 = finalPlayerName1 + " (AI)";
        }

        String finalPlayerName2 = player2Name.getText();
        if (player2AIToggle.isSelected()) {
            finalPlayerName2 = finalPlayerName2 + " (AI)";
        }

        board.setPlayer1(new Player(player1AIToggle.isSelected(), finalPlayerName1));
        board.setPlayer2(new Player(player2AIToggle.isSelected(), finalPlayerName2));
    }

    @FXML
    void newGameOnAction(ActionEvent event) {
        initNewGame();
    }

    @FXML
    void thinkOnAction(ActionEvent event) {

    }

    private void initNewGame() {
        suggestedAIName1 = NameList.getNextAIName();
        suggestedAIName2 = NameList.getNextAIName();
        suggestedHumanName1 = NameList.getNextHumanName();
        suggestedHumanName2 = NameList.getNextHumanName();

        player1SetSampleName();
        player2SetSampleName();

        if (!isMenuShown()) {
            showMenu();
        }
    }

    private void initBoard() {
        board = new Board(gameRows, gameCols);
        board.setGameEndCallback((winner) -> System.out.println("The winner is: " + winner.getName()));
        while (gameTable.getColumns().size() > 0) {
            gameTable.getColumns().remove(0);
        }

        for (int i = 0; i < gameCols; i++) {
            TableColumn<Row, String> column = new TableColumn<>(Integer.toString(i + 1));
            //noinspection Convert2Lambda
            int finalI = i;
            column.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getValues().get(finalI)));

            column.setCellFactory(new Callback<TableColumn<Row, String>, TableCell<Row, String>>() {
                @Override
                public TableCell<Row, String> call(TableColumn col) {
                    TableCell<Row, String> cell = new TableCell<Row, String>() {
                        // The updateItem method is what is called when setting the cell's text.  You can customize formatting here
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            // calling super here is very important - don't skip this!
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item);
                            }
                        }
                    };

                    cell.setOnMouseClicked(event -> {
                        if (board.getPlayerAt(cell.getIndex(), gameTable.getColumns().indexOf(col)) == null) {
                            board.doTurn(cell.getIndex(), gameTable.getColumns().indexOf(col));
                            renderRows();
                        }
                    });
                    return cell;
                }
            });

            column.setStyle("-fx-alignment: CENTER;");
            gameTable.getColumns().add(column);
        }

        renderRows();
    }

    private void renderRows() {
        ObservableList<Row> generatedRows = FXCollections.observableArrayList();

        for (int r = 0; r < board.getRowCount(); r++) {
            List<String> values = new ArrayList<>();

            for (int c = 0; c < board.getColumnCount(); c++) {
                if (board.getPlayerAt(r, c) == null) {
                    values.add("");
                } else if (board.getPlayerAt(r, c) == board.getPlayer1()) {
                    values.add(player1Letter);
                } else if (board.getPlayerAt(r, c) == board.getPlayer2()) {
                    values.add(player2Letter);
                }
            }

            generatedRows.add(new Row(values));
        }

        gameTable.setItems(generatedRows);

        double effectiveHeight = gameTable.getHeight() - 2;
        gameTable.setFixedCellSize(effectiveHeight / board.getRowCount());
        style.set("-fx-font-size:" + Math.round((effectiveHeight - 250) / board.getRowCount()) + "px;");
        gameTable.refresh();
    }

    private void player1SetSampleName() {
        if (board.getPlayer1() == null) {
            player1Name.setText(player1AIToggle.isSelected() ? suggestedAIName1 : suggestedHumanName1);
        }
    }

    private void player2SetSampleName() {
        if (board.getPlayer2() == null) {
            player2Name.setText(player2AIToggle.isSelected() ? suggestedAIName2 : suggestedHumanName2);
        }
    }

    private boolean isMenuShown() {
        return menuBox.isVisible();
    }

    private void showMenu() {
        FadeTransition menuTransition = new FadeTransition();
        menuTransition.setNode(menuBox);
        menuTransition.setFromValue(menuBox.getOpacity());
        menuTransition.setToValue(1);
        menuTransition.setAutoReverse(false);
        menuTransition.setDuration(Duration.seconds(animationSpeed));

        FadeTransition menuBackgroundTransition = new FadeTransition();
        menuBackgroundTransition.setNode(menuBackground);
        menuBackgroundTransition.setFromValue(menuBackground.getOpacity());
        menuBackgroundTransition.setToValue(0.3);
        menuBackgroundTransition.setAutoReverse(false);
        menuBackgroundTransition.setDuration(Duration.seconds(animationSpeed));

        GaussianBlur gameEffect = new GaussianBlur(0.0);
        gamePane.setEffect(gameEffect);
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(gameEffect.radiusProperty(), 10.0);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(animationSpeed), keyValue);
        timeline.getKeyFrames().add(keyFrame);

        menuBackground.setVisible(true);
        menuBox.setVisible(true);
        menuTransition.play();
        menuBackgroundTransition.play();
        timeline.play();
    }

    private void hideMenu() {
        FadeTransition menuTransition = new FadeTransition();
        menuTransition.setNode(menuBox);
        menuTransition.setFromValue(menuBox.getOpacity());
        menuTransition.setToValue(0);
        menuTransition.setAutoReverse(false);
        menuTransition.setDuration(Duration.seconds(animationSpeed));

        FadeTransition menuBackgroundTransition = new FadeTransition();
        menuBackgroundTransition.setNode(menuBackground);
        menuBackgroundTransition.setFromValue(menuBackground.getOpacity());
        menuBackgroundTransition.setToValue(0);
        menuBackgroundTransition.setAutoReverse(false);
        menuBackgroundTransition.setDuration(Duration.seconds(animationSpeed));

        GaussianBlur gameEffect = (GaussianBlur) gamePane.getEffect();
        gamePane.setEffect(gameEffect);
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(gameEffect.radiusProperty(), 0.0);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(animationSpeed), keyValue);
        timeline.getKeyFrames().add(keyFrame);

        menuTransition.play();
        menuBackgroundTransition.play();
        timeline.play();

        menuTransition.setOnFinished((event) -> {
            menuBox.setVisible(false);
            menuBackground.setVisible(false);
        });
    }
}

package com.github.vatbub.tictactoe.view;

/*-
 * #%L
 * tictactoe
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.github.vatbub.tictactoe.Board;
import com.github.vatbub.tictactoe.NameList;
import com.github.vatbub.tictactoe.Player;
import com.github.vatbub.tictactoe.view.refreshables.Refreshable;
import com.github.vatbub.tictactoe.view.refreshables.RefreshableNodeList;
import com.sun.javafx.tk.Toolkit;
import common.Common;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import logging.FOKLogger;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.controlsfx.control.ToggleSwitch;
import view.ExceptionAlert;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

/**
 * The main game view
 */
@SuppressWarnings("JavaDoc")
public class Main extends Application {
    private static final double animationSpeed = 0.3;
    private static final int gameRows = 3;
    private static final int gameCols = 3;
    private static final String player1Letter = "X";
    private static final String player2Letter = "O";

    final StringProperty style = new SimpleStringProperty("");
    private final AnimationThreadPoolExecutor guiAnimationQueue = new AnimationThreadPoolExecutor(1);
    private final RefreshableNodeList refreshedNodes = new RefreshableNodeList();
    private final Map<String, Timer> loadTimerMap = new HashMap<>();
    private String suggestedHumanName1;
    private String suggestedHumanName2;
    private String suggestedAIName1;
    private String suggestedAIName2;
    private Board board;
    private ObjectProperty<Font> rowFont;
    private Rectangle aiLevelLabelClipRectangle;

    @FXML
    private AnchorPane root;

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

    @FXML
    private AnchorPane looserPane;

    @FXML
    private ImageView looseImage;

    @FXML
    private AnchorPane looseMessage;

    @FXML
    private AnchorPane tieMessage;

    @FXML
    private Label looserText;

    @FXML
    private Label currentPlayerLabel;

    @FXML
    private AnchorPane tiePane;

    @FXML
    private AnchorPane winPane;

    @FXML
    private ImageView confetti;

    @FXML
    private ImageView winningGirl;

    @FXML
    private AnchorPane winMessage;

    @FXML
    private Label winnerText;

    @FXML
    private ImageView bowTie;

    @FXML
    private Slider aiLevelSlider;

    @FXML
    private Pane aiLevelLabelPane;

    @FXML
    private HBox aiLevelLabelHBox;

    @FXML
    private Label aiLevelTitleLabel;
    @FXML
    private VBox menuSubBox;
    private boolean player1NameModified = false;
    private boolean player2NameModified = false;
    @FXML
    private Line aiLevelCenterLine;

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
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));

        primaryStage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
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

        aiLevelLabelClipRectangle = new Rectangle(0, 0, 0, 0);
        aiLevelLabelClipRectangle.setEffect(new MotionBlur(0, 10));
        aiLevelLabelPane.setClip(aiLevelLabelClipRectangle);
        aiLevelLabelClipRectangle.heightProperty().bind(aiLevelLabelPane.heightProperty());
        aiLevelLabelPane.widthProperty().addListener((observable, oldValue, newValue) -> updateAILevelLabel());

        suggestedAIName1 = NameList.getNextAIName();
        suggestedAIName2 = NameList.getNextAIName();
        suggestedHumanName1 = NameList.getNextHumanName();
        suggestedHumanName2 = NameList.getNextHumanName();

        gameTable.heightProperty().addListener((observable, oldValue, newValue) -> refreshedNodes.refreshAll(gameTable.getWidth(), oldValue.doubleValue(), gameTable.getWidth(), newValue.doubleValue()));
        gameTable.widthProperty().addListener((observable, oldValue, newValue) -> refreshedNodes.refreshAll(oldValue.doubleValue(), gameTable.getHeight(), newValue.doubleValue(), gameTable.getHeight()));

        player1AIToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showHideAILevelSlider(newValue, player2AIToggle.isSelected());
            if (!player1NameModified) {
                player1SetSampleName();
            }
        });
        player2AIToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showHideAILevelSlider(player1AIToggle.isSelected(), newValue);
            if (!player2NameModified) {
                player2SetSampleName();
            }
        });

        player1Name.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(player1AIToggle.isSelected() ? suggestedAIName1 : suggestedHumanName1)) {
                player1NameModified = true;
            }
        });
        player2Name.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(player2AIToggle.isSelected() ? suggestedAIName2 : suggestedHumanName2)) {
                player2NameModified = true;
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
        gameTable.setRowFactory(param -> {
            TableRow<Row> row = new TableRow<>();
            row.styleProperty().bind(style);

            if (rowFont == null) {
                rowFont = new SimpleObjectProperty<>();
                rowFont.bind(row.fontProperty());
            }

            return row;
        });

        looseImage.fitHeightProperty().bind(looserPane.heightProperty());
        looseImage.fitWidthProperty().bind(looserPane.widthProperty());
        looseImage.fitHeightProperty().addListener((observable, oldValue, newValue) -> reloadImage(looseImage, getClass().getResource("loose.png").toString(), looseImage.getFitWidth(), newValue.doubleValue()));
        looseImage.fitWidthProperty().addListener((observable, oldValue, newValue) -> reloadImage(looseImage, getClass().getResource("loose.png").toString(), newValue.doubleValue(), looseImage.getFitWidth()));

        confetti.fitHeightProperty().bind(winPane.heightProperty());
        confetti.fitWidthProperty().bind(winPane.widthProperty());
        confetti.fitHeightProperty().addListener((observable, oldValue, newValue) -> reloadImage(confetti, getClass().getResource("confetti.png").toString(), confetti.getFitWidth(), newValue.doubleValue()));
        confetti.fitWidthProperty().addListener((observable, oldValue, newValue) -> reloadImage(confetti, getClass().getResource("confetti.png").toString(), newValue.doubleValue(), confetti.getFitWidth()));

        aiLevelSlider.valueProperty().addListener((observable, oldValue, newValue) -> updateAILevelLabel());

        initBoard();
        initNewGame();
    }

    private void reloadImage(ImageView imageView, String imageURL, double newWidth, double newHeight) {
        if (loadTimerMap.get(imageURL) != null) {
            loadTimerMap.get(imageURL).cancel();
        }

        Timer loadTimer = new Timer();
        loadTimerMap.put(imageURL, loadTimer);
        loadTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Loaded '" + imageURL + "', h:" + newHeight + ", w:" + newWidth);
                Image image = new Image(imageURL, newWidth, newHeight, false, true);
                Platform.runLater(() -> imageView.setImage(image));
            }
        }, 300);
    }

    /*@FXML
    void aiLevelSliderOnMouseMoved(MouseEvent event){
        updateAILevelLabel();
    }*/

    @FXML
    void startButtonOnAction(ActionEvent event) {
        startGame();
    }

    @FXML
    void newGameOnAction(ActionEvent event) {
        initNewGame();
    }

    @FXML
    void thinkOnAction(ActionEvent event) {
        board.getCurrentPlayer().doAiTurn(board, AILevel.UNBEATABLE);
        updateCurrentPlayerLabel();
        renderRows();
    }

    private double getAILevelLabelCenter(int labelIndex) {
        double res = 0;

        for (int i = 0; i < labelIndex; i++) {
            res = res + ((Label) aiLevelLabelHBox.getChildren().get(i)).getWidth();
        }
        res = res + labelIndex * aiLevelLabelHBox.getSpacing();
        res = res - aiLevelLabelPane.getWidth() / 2;
        res = res + ((Label) aiLevelLabelHBox.getChildren().get(labelIndex)).getWidth() / 2;

        return res;
    }

    private void updateAILevelLabel() {
        // get the slider position
        double[] xDouble = new double[]{0, 100.0 / 3.0, 200.0 / 3.0, 300.0 / 3.0};
        double[] translationYDouble = new double[4];
        double[] widthYDouble = new double[4];
        double[] trueWidthYDouble = new double[4];
        for (int i = 0; i < translationYDouble.length; i++) {
            // {-getAILevelLabelCenter(0), -getAILevelLabelCenter(1), -getAILevelLabelCenter(2), -getAILevelLabelCenter(3)};
            translationYDouble[i] = -getAILevelLabelCenter(i);
            widthYDouble[i] = Math.max(90, ((Label) aiLevelLabelHBox.getChildren().get(i)).getWidth() + 8 * aiLevelLabelHBox.getSpacing());
            trueWidthYDouble[i] = ((Label) aiLevelLabelHBox.getChildren().get(i)).getWidth();
        }

        SplineInterpolator splineInterpolator = new SplineInterpolator();
        PolynomialSplineFunction translateFunction = splineInterpolator.interpolate(xDouble, translationYDouble);
        PolynomialSplineFunction widthFunction = splineInterpolator.interpolate(xDouble, widthYDouble);
        PolynomialSplineFunction trueWidthFunction = splineInterpolator.interpolate(xDouble, trueWidthYDouble);

        aiLevelLabelHBox.setLayoutX(translateFunction.value(aiLevelSlider.getValue()));
        aiLevelLabelClipRectangle.setWidth(widthFunction.value(aiLevelSlider.getValue()));
        aiLevelLabelClipRectangle.setX(aiLevelLabelPane.getWidth() / 2 - aiLevelLabelClipRectangle.getWidth() / 2);

        double interpolatedLabelWidth = trueWidthFunction.value(aiLevelSlider.getValue());
        aiLevelCenterLine.setStartX((aiLevelLabelPane.getWidth() - interpolatedLabelWidth) / 2);
        aiLevelCenterLine.setEndX((aiLevelLabelPane.getWidth() + interpolatedLabelWidth) / 2);
    }

    @FXML
    void aboutLinkOnAction(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/vatbub/tictactoe#tictactoe"));
        } catch (URISyntaxException | IOException e) {
            FOKLogger.log(Main.class.getName(), Level.SEVERE, "Typo in a hardcoded value", e);
        }
    }

    private void initNewGame() {
        guiAnimationQueue.submit(() -> {
            player1SetSampleName();
            player2SetSampleName();

            if (looserPane.isVisible()) {
                blurLooserPane();
            }
            if (tiePane.isVisible()) {
                blurTiePane();
            }
            if (winPane.isVisible()) {
                blurWinPane();
            }

            updateAILevelLabel();
            if (!isMenuShown()) {
                showMenu();
            }
        });
    }

    private void startGame() {
        initBoard();
        if (looserPane.isVisible()) {
            fadeNode(looserPane, 0, true);
        }
        if (tiePane.isVisible()) {
            fadeNode(tiePane, 0);
        }
        if (winPane.isVisible()) {
            fadeNode(winPane, 0);
        }
        hideMenu();
        fadeWinLineGroup();
        guiAnimationQueue.submit(() -> {
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
            updateCurrentPlayerLabel();

            if (board.getPlayer1().isAi()) {
                board.getPlayer1().doAiTurn(board);
            }
        });
    }

    private void updateCurrentPlayerLabel() {
        guiAnimationQueue.submit(() -> {
            Player currentPlayer = board.getCurrentPlayer();
            if (currentPlayer == board.getPlayer1() || currentPlayer == null) {
                currentPlayerLabel.setText(player1Letter);
            } else if (currentPlayer == board.getPlayer2()) {
                currentPlayerLabel.setText(player2Letter);
            }
        });
    }

    private void initBoard() {
        guiAnimationQueue.submit(() -> {
            board = new Board(gameRows, gameCols);
            // set the ai level
            int sliderPos = (int) Math.round(aiLevelSlider.getValue() * 3.0 / 100.0);

            switch (sliderPos) {
                case 0:
                    board.setAiLevel(AILevel.COMPLETELY_STUPID);
                    break;
                case 1:
                    board.setAiLevel(AILevel.SOMEWHAT_GOOD);
                    break;
                case 2:
                    board.setAiLevel(AILevel.GOOD);
                    break;
                case 3:
                    board.setAiLevel(AILevel.UNBEATABLE);
                    break;
            }

            board.setGameEndCallback((winnerInfo) -> guiAnimationQueue.submit(() -> {
                System.out.println("The winner is: " + winnerInfo.winningPlayer.getName());
                if (winnerInfo.isTie()) {
                    showTie();
                } else if (!winnerInfo.winningPlayer.isAi() && board.getOpponent(winnerInfo.winningPlayer).isAi()) {
                    showWinner(winnerInfo);
                } else {
                    showLooser(winnerInfo);
                }
            }));
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
                                board.doTurn(new Board.Move(cell.getIndex(), gameTable.getColumns().indexOf(col)));
                                updateCurrentPlayerLabel();
                                renderRows();
                            }
                        });
                        return cell;
                    }
                });

                column.setStyle("-fx-alignment: CENTER; -fx-padding: 0;");
                gameTable.getColumns().add(column);
            }

            renderRows();
        });
    }

    private void renderRows() {
        guiAnimationQueue.submit(() -> {
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

            double effectiveHeight = gameTable.getHeight() - 5;
            long fontSize = Math.round((effectiveHeight - 250) / board.getRowCount());

            // get letter widths;
            Font font = new Font(rowFont.getName(), fontSize);
            double player1SymbolWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(player1Letter, font);
            double player2SymbolWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(player2Letter, font);

            // make the font smaller so that it fits the cell even if the width is very small
            while (player1SymbolWidth > (gameTable.getWidth() / board.getColumnCount()) || player2SymbolWidth + 10 > (gameTable.getWidth() / board.getColumnCount())) {
                fontSize = fontSize - 1;
                font = new Font(rowFont.getName(), fontSize);
                player1SymbolWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(player1Letter, font);
                player2SymbolWidth = Toolkit.getToolkit().getFontLoader().computeStringWidth(player2Letter, font);
            }

            gameTable.setFixedCellSize(effectiveHeight / board.getRowCount());
            style.set("-fx-font-size:" + fontSize + "px; -fx-padding: 0;");
            gameTable.refresh();
        });
    }

    private void player1SetSampleName() {
        // if (board.getPlayer1() == null) {
        player1Name.setText(player1AIToggle.isSelected() ? suggestedAIName1 : suggestedHumanName1);
        // }
    }

    private void player2SetSampleName() {
        // if (board.getPlayer2() == null) {
        player2Name.setText(player2AIToggle.isSelected() ? suggestedAIName2 : suggestedHumanName2);
        // }
    }

    private boolean isMenuShown() {
        return menuBox.isVisible();
    }

    private void showMenu() {
        guiAnimationQueue.submit(() -> {
            fadeNode(menuBackground, 0.12);
            fadeNode(menuBox, 1);

            blurGamePane();

            menuBackground.setVisible(true);
            menuBox.setVisible(true);
        });
    }

    private void hideMenu() {
        guiAnimationQueue.submit(() -> {
            fadeNode(menuBackground, 0);
            fadeNode(menuBox, 0);

            unblurGamePane();
        });
    }

    private void showWinner(Board.WinnerInfo winnerInfo) {
        guiAnimationQueue.submitWaitForUnlock(() -> addWinLineOnWin(winnerInfo, () -> {
            winnerText.setText(winnerInfo.winningPlayer.getName() + " won :)");
            double endX = winningGirl.getX();
            double endY = winPane.getHeight() - winningGirl.getFitHeight();

            double confettiOffset = 30;

            double confettiX = confetti.getX();
            double confettiY = confetti.getY();

            AnchorPane.clearConstraints(winningGirl);
            winningGirl.setX(endX);
            winningGirl.setY(root.getHeight() + 140);

            blurGamePane();
            winMessage.setOpacity(0);
            confetti.setOpacity(0);
            winPane.setOpacity(1);
            winPane.setVisible(true);
            winningGirl.setVisible(true);

            Timeline timeline = new Timeline();
            double S4 = 1.45;
            double x0 = 0.33;
            KeyValue confettiKeyValue1x = new KeyValue(confetti.xProperty(), confettiX);
            KeyValue confettiKeyValue1y = new KeyValue(confetti.yProperty(), confettiY - confettiOffset);
            KeyValue confettiKeyValue1opacity = new KeyValue(confetti.opacityProperty(), 0);
            KeyFrame confettiKeyFrame1 = new KeyFrame(Duration.seconds(0), confettiKeyValue1x, confettiKeyValue1y, confettiKeyValue1opacity);

            KeyValue confettiKeyValue2x = new KeyValue(confetti.xProperty(), confettiX);
            KeyValue confettiKeyValue2y = new KeyValue(confetti.yProperty(), confettiY - confettiOffset);
            KeyValue confettiKeyValue2opacity = new KeyValue(confetti.opacityProperty(), 0);
            KeyFrame confettiKeyFrame2 = new KeyFrame(Duration.millis(500), confettiKeyValue2x, confettiKeyValue2y, confettiKeyValue2opacity);

            KeyValue confettiKeyValue3x = new KeyValue(confetti.xProperty(), confettiX, new CustomEaseOutInterpolator(S4, x0));
            KeyValue confettiKeyValue3y = new KeyValue(confetti.yProperty(), confettiY, new CustomEaseOutInterpolator(S4, x0));
            KeyValue confettiKeyValue3opacity = new KeyValue(confetti.opacityProperty(), 1);
            KeyFrame confettiKeyFrame3 = new KeyFrame(Duration.millis(1100), confettiKeyValue3x, confettiKeyValue3y, confettiKeyValue3opacity);

            KeyValue winningGirlKeyValue1x = new KeyValue(winningGirl.xProperty(), endX, new CustomEaseOutInterpolator(S4, x0));
            KeyValue winningGirlKeyValue1y = new KeyValue(winningGirl.yProperty(), endY, new CustomEaseOutInterpolator(S4, x0));
            KeyFrame winningGirlKeyFrame1 = new KeyFrame(Duration.seconds(1), winningGirlKeyValue1x, winningGirlKeyValue1y);
            timeline.getKeyFrames().addAll(winningGirlKeyFrame1, confettiKeyFrame1, confettiKeyFrame2, confettiKeyFrame3);

            timeline.setOnFinished((event) -> fadeNode(winMessage, 1, () -> {
                AnchorPane.setRightAnchor(winningGirl, 0.0);
                AnchorPane.setBottomAnchor(winningGirl, winPane.getHeight() - winningGirl.getFitHeight() - endY);
            }));

            timeline.play();
        }));
    }

    private void addWinLineOnWin(Board.WinnerInfo winnerInfo, Runnable onFinished) {
        final Paint color = new Color(1.0, 145.0 / 255.0, 30.0 / 255.0, 1.0);

        Line originLine = new Line(0, 0, 0, 0);
        winLineGroup.getChildren().add(originLine);

        WinLine winLine = new WinLine(winnerInfo);
        double winLineEndX = winLine.endArc.getCenterX();
        double winLineEndY = winLine.endArc.getCenterY();
        winLine.startArc.setFill(color);
        winLine.startArc.setStrokeWidth(0);
        winLine.endArc.setFill(color);
        winLine.endArc.setStrokeWidth(0);
        winLine.rightLine.setStrokeWidth(0);
        winLine.leftLine.setStrokeWidth(0);
        winLine.centerLine.setStroke(color);

        winLine.centerLine.strokeWidthProperty().bind(winLine.startArc.radiusXProperty().multiply(2));
        winLineGroup.getChildren().addAll(winLine.getAll());

        blurNode(gamePane, 4);

        winLineGroup.setOpacity(0);
        GaussianBlur blur = new GaussianBlur(100);
        winLineGroup.setEffect(blur);
        winLineGroup.setBlendMode(BlendMode.DARKEN);
        winLineGroup.setVisible(true);

        double winLineAnimationX1 = 0.2;
        double winLineAnimationX2 = 0.5;

        KeyValue stretchKeyValue1x = new KeyValue(winLine.endArc.centerXProperty(), winLine.startArc.getCenterX());
        KeyValue stretchKeyValue1y = new KeyValue(winLine.endArc.centerYProperty(), winLine.startArc.getCenterY());
        KeyFrame stretchKeyFrame1 = new KeyFrame(Duration.millis(0), stretchKeyValue1x, stretchKeyValue1y);

        KeyValue stretchKeyValue2x = new KeyValue(winLine.endArc.centerXProperty(), winLine.startArc.getCenterX(), new CustomEaseBothInterpolator(winLineAnimationX1, winLineAnimationX2));
        KeyValue stretchKeyValue2y = new KeyValue(winLine.endArc.centerYProperty(), winLine.startArc.getCenterY(), new CustomEaseBothInterpolator(winLineAnimationX1, winLineAnimationX2));
        KeyFrame stretchKeyFrame2 = new KeyFrame(Duration.millis(100), stretchKeyValue2x, stretchKeyValue2y);

        KeyValue stretchKeyValue3x = new KeyValue(winLine.endArc.centerXProperty(), winLineEndX, new CustomEaseBothInterpolator(winLineAnimationX1, winLineAnimationX2));
        KeyValue stretchKeyValue3y = new KeyValue(winLine.endArc.centerYProperty(), winLineEndY, new CustomEaseBothInterpolator(winLineAnimationX1, winLineAnimationX2));
        KeyFrame stretchKeyFrame3 = new KeyFrame(Duration.millis(800), stretchKeyValue3x, stretchKeyValue3y);

        KeyValue opacityKeyValue1 = new KeyValue(winLineGroup.opacityProperty(), 0.8);
        KeyFrame opacityKeyFrame1 = new KeyFrame(Duration.millis(400), opacityKeyValue1);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(stretchKeyFrame1, stretchKeyFrame2, stretchKeyFrame3, opacityKeyFrame1);
        timeline.play();

        timeline.setOnFinished((event) -> onFinished.run());
    }

    private void showTie() {
        guiAnimationQueue.submitWaitForUnlock(() -> {
            double endX = tiePane.getWidth() - 230;
            double endY = 90;

            AnchorPane.clearConstraints(bowTie);
            bowTie.setX(endX);
            bowTie.setY(-150);

            blurGamePane();
            tieMessage.setOpacity(0);
            tiePane.setOpacity(1);
            tiePane.setVisible(true);
            bowTie.setVisible(true);

            Timeline timeline = new Timeline();
            double S4 = 1.45;
            double x0 = 0.33;
            KeyValue keyValue1x = new KeyValue(bowTie.xProperty(), endX, new CustomEaseOutInterpolator(S4, x0));
            KeyValue keyValue1y = new KeyValue(bowTie.yProperty(), endY, new CustomEaseOutInterpolator(S4, x0));
            KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(1), keyValue1x, keyValue1y);
            timeline.getKeyFrames().add(keyFrame1);

            timeline.setOnFinished((event) -> fadeNode(tieMessage, 1, () -> {
                AnchorPane.setRightAnchor(bowTie, tiePane.getWidth() - bowTie.getFitWidth() - endX);
                AnchorPane.setTopAnchor(bowTie, endY);
            }));

            timeline.play();
        });
    }

    private void showLooser(Board.WinnerInfo winnerInfo) {
        String looserName = board.getOpponent(winnerInfo.winningPlayer).getName();
        guiAnimationQueue.submitWaitForUnlock(() -> {
            ShakeTransition anim = new ShakeTransition(gamePane, null);
            anim.playFromStart();

            Timeline timeline = new Timeline();

            Circle c1 = new Circle((452 / 600.0) * looserPane.getWidth(), (323 / 640.0) * looserPane.getHeight(), 0);
            GaussianBlur circleBlur = new GaussianBlur(30);
            c1.setEffect(circleBlur);
            looseImage.setClip(c1);
            addWinLineOnLoose(winnerInfo);

            KeyValue keyValue1 = new KeyValue(c1.radiusProperty(), 0);
            KeyFrame keyFrame1 = new KeyFrame(Duration.millis(800), keyValue1);
            KeyValue keyValue2 = new KeyValue(c1.radiusProperty(), (500 / 640.0) * looserPane.getHeight());
            KeyFrame keyFrame2 = new KeyFrame(Duration.millis(900), keyValue2);

            timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);


            looseMessage.setOpacity(0);
            looserText.setText(looserName + " lost :(");
            looserPane.setVisible(true);
            looserPane.setOpacity(1);

            timeline.setOnFinished((event) -> {
                looseImage.setClip(null);
                winLineGroup.setClip(null);
                blurGamePane();
                PauseTransition wait = new PauseTransition();
                wait.setDuration(Duration.seconds(1));
                wait.setOnFinished((event2) -> {
                    FadeTransition looseMessageTransition = new FadeTransition();
                    looseMessageTransition.setNode(looseMessage);
                    looseMessageTransition.setFromValue(0);
                    looseMessageTransition.setToValue(1);
                    looseMessageTransition.setDuration(Duration.millis(500));
                    looseMessageTransition.setAutoReverse(false);
                    looseMessageTransition.play();
                });

                wait.play();
            });

            timeline.play();
        });

    }

    private void addWinLineOnLoose(Board.WinnerInfo winnerInfo) {
        final double strokeWidth = 2;

        Line originLine = new Line(0, 0, 0, 0);
        winLineGroup.getChildren().add(originLine);

        WinLine winLine = new WinLine(winnerInfo);
        winLine.startArc.setFill(Color.TRANSPARENT);
        winLine.startArc.setStroke(Color.BLACK);
        winLine.startArc.setStrokeWidth(strokeWidth);
        winLine.endArc.setFill(Color.TRANSPARENT);
        winLine.endArc.setStroke(Color.BLACK);
        winLine.endArc.setStrokeWidth(strokeWidth);
        winLine.rightLine.setStrokeWidth(strokeWidth);
        winLine.leftLine.setStrokeWidth(strokeWidth);

        winLine.centerLine.setStrokeWidth(0);
        winLineGroup.getChildren().addAll(winLine.getAll());

        winLineGroup.setOpacity(0);
        GaussianBlur blur = new GaussianBlur(7);
        winLineGroup.setEffect(blur);
        winLineGroup.setVisible(true);
        KeyValue keyValue1 = new KeyValue(winLineGroup.opacityProperty(), 0);
        KeyFrame keyFrame1 = new KeyFrame(Duration.millis(900), keyValue1);
        KeyValue keyValue2 = new KeyValue(winLineGroup.opacityProperty(), 1);
        KeyFrame keyFrame2 = new KeyFrame(Duration.millis(950), keyValue2);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);
        timeline.play();
    }

    private void fadeWinLineGroup() {
        fadeNode(winLineGroup, 0, () -> {
            winLineGroup.setBlendMode(BlendMode.SRC_OVER);
            //noinspection SuspiciousMethodCalls
            refreshedNodes.removeAll(winLineGroup.getChildren());
            winLineGroup.getChildren().clear();
        });
    }

    private void blurGamePane() {
        blurGamePane(7.0);
    }

    private void unblurGamePane() {
        blurGamePane(0.0);
    }

    private void blurGamePane(double toValue) {
        blurNode(gamePane, toValue);
    }

    private void blurLooserPane() {
        blurNode(looserPane, 7);
    }

    private void blurTiePane() {
        blurNode(tiePane, 7);
    }

    private void blurWinPane() {
        blurNode(winPane, 7);
    }

    private void blurNode(Node node, double toValue) {
        blurNode(node, toValue, null);
    }

    private void blurNode(Node node, double toValue, @SuppressWarnings("SameParameterValue") Runnable onFinish) {
        guiAnimationQueue.submit(() -> {
            GaussianBlur blur = (GaussianBlur) node.getEffect();
            if (blur == null) {
                blur = new GaussianBlur(0);
                node.setEffect(blur);
            }
            node.setEffect(blur);
            Timeline timeline = new Timeline();
            KeyValue keyValue = new KeyValue(blur.radiusProperty(), toValue);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(animationSpeed), keyValue);
            timeline.getKeyFrames().add(keyFrame);

            timeline.setOnFinished((event) -> {
                if (toValue == 0) {
                    node.setEffect(null);
                }
                if (onFinish != null) {
                    onFinish.run();
                }
            });

            timeline.play();
        });
    }

    /**
     * Shows or hides the ai level slider according to the current ai configuration
     */
    private void showHideAILevelSlider(boolean player1IsAI, boolean player2IsAI) {
        if ((player1IsAI || player2IsAI) && !aiLevelSlider.isVisible()) {
            fadeAILevelSliderIn();
        } else if (!player1IsAI && !player2IsAI && aiLevelSlider.isVisible()) {
            fadeAILevelSliderOut();
        }
    }

    private void fadeAILevelSliderOut() {
        menuSubBox.setPrefHeight(menuSubBox.getHeight());

        guiAnimationQueue.submitWaitForUnlock(() -> {
            guiAnimationQueue.setBlocked(true);
            fadeNode(aiLevelTitleLabel, 0, false, () -> menuSubBox.getChildren().remove(aiLevelTitleLabel));
            fadeNode(aiLevelSlider, 0, false, () -> menuSubBox.getChildren().remove(aiLevelSlider));
            fadeNode(aiLevelLabelPane, 0, false, () -> {
                menuSubBox.getChildren().remove(aiLevelLabelPane);
                updateMenuHeight(false);
            });
        });
    }

    private void fadeAILevelSliderIn() {
        menuSubBox.setPrefHeight(menuSubBox.getHeight());
        guiAnimationQueue.submitWaitForUnlock(() -> updateMenuHeight(true));
    }

    private void updateMenuHeight(boolean includeAILevelSlider) {
        if (includeAILevelSlider) {
            guiAnimationQueue.setBlocked(true);
        }

        double toHeight = 0;
        int effectiveChildCount = 0;
        for (Node child : menuSubBox.getChildren()) {
            if (child.isVisible()) {
                toHeight = toHeight + child.getBoundsInParent().getHeight();
                effectiveChildCount = effectiveChildCount + 1;
            }
        }

        if (includeAILevelSlider) {
            toHeight = toHeight + aiLevelLabelPane.getPrefHeight();
            toHeight = toHeight + aiLevelSlider.getPrefHeight();
            toHeight = toHeight + aiLevelTitleLabel.getPrefHeight();
            effectiveChildCount = effectiveChildCount + 3;
        }

        toHeight = toHeight + menuSubBox.getSpacing() * (effectiveChildCount - 1);

        Timeline timeline = new Timeline();
        KeyValue keyValue0 = new KeyValue(menuSubBox.prefHeightProperty(), toHeight, Interpolator.EASE_BOTH);
        KeyFrame keyFrame0 = new KeyFrame(Duration.seconds(animationSpeed), keyValue0);
        timeline.getKeyFrames().add(keyFrame0);

        if (includeAILevelSlider) {
            timeline.setOnFinished((event) -> {
                menuSubBox.getChildren().addAll(aiLevelTitleLabel, aiLevelSlider, aiLevelLabelPane);
                fadeNode(aiLevelTitleLabel, 1, true);
                fadeNode(aiLevelSlider, 1);
                fadeNode(aiLevelLabelPane, 1);
            });
        } else {
            timeline.setOnFinished((event -> guiAnimationQueue.setBlocked(false)));
        }

        timeline.play();
    }

    private void fadeNode(Node node, double toValue) {
        fadeNode(node, toValue, false);
    }

    private void fadeNode(Node node, double toValue, @SuppressWarnings("SameParameterValue") boolean block) {
        fadeNode(node, toValue, block, null);
    }

    private void fadeNode(Node node, @SuppressWarnings("SameParameterValue") double toValue, Runnable onFinish) {
        fadeNode(node, toValue, false, onFinish);
    }

    private void fadeNode(Node node, double toValue, boolean block, Runnable onFinish) {
        guiAnimationQueue.submit(() -> {
            if (block) {
                guiAnimationQueue.setBlocked(true);
            }
            if (!node.isVisible()) {
                node.setVisible(true);
            }

            FadeTransition fadeTransition = new FadeTransition();
            fadeTransition.setNode(node);
            fadeTransition.setFromValue(node.getOpacity());
            fadeTransition.setToValue(toValue);
            fadeTransition.setDuration(Duration.seconds(animationSpeed));
            fadeTransition.setAutoReverse(false);

            fadeTransition.setOnFinished((event) -> {
                if (toValue == 0) {
                    node.setEffect(null);
                    node.setVisible(false);
                }
                if (block) {
                    guiAnimationQueue.setBlocked(false);
                }
                if (onFinish != null) {
                    onFinish.run();
                }
            });

            fadeTransition.play();
        });
    }

    private class WinLineGeometry {
        final double winLineWidth;
        final double startX;
        final double startY;
        final double endX;
        final double endY;
        double startAngle;

        WinLineGeometry(Board.WinnerInfo winnerInfo, double newHeight, double newWidth) {
            double cellWidth = newWidth / board.getColumnCount();
            double cellHeight = newHeight / board.getRowCount();
            winLineWidth = cellHeight / 2;
            startX = (winnerInfo.winLineStartColumn * cellWidth) + (cellWidth / 2.0);
            startY = (winnerInfo.winLineStartRow * cellHeight) + (cellHeight / 2.0);
            endX = (winnerInfo.winLineEndColumn * cellWidth) + (cellWidth / 2.0);
            endY = (winnerInfo.winLineEndRow * cellHeight) + (cellHeight / 2.0);
            startAngle = Math.atan2(endX - startX, endY - startY) * 180 / Math.PI;
        }
    }

    private class WinLine implements Refreshable {
        final Arc startArc;
        final Line leftLine;
        final Line centerLine;
        final Line rightLine;
        final Arc endArc;
        final Rectangle clipRectangle = new Rectangle();
        private final Board.WinnerInfo winnerInfo;
        private double lastWinLineWidth = 0;

        WinLine(Board.WinnerInfo winnerInfo) {
            this.winnerInfo = winnerInfo;
            startArc = new Arc();
            startArc.setType(ArcType.OPEN);

            //noinspection SuspiciousNameCombination
            endArc = new Arc();
            endArc.setType(ArcType.OPEN);

            leftLine = new Line();
            centerLine = new Line();
            rightLine = new Line();
            centerLine.setClip(clipRectangle);

            startArc.centerXProperty().addListener((observable, oldValue, newValue) -> {
                double startAngle = new WinLineGeometry(winnerInfo, gameTable.getHeight(), gameTable.getWidth()).startAngle;
                internalRefresh(newValue.doubleValue(), startArc.getCenterY(), endArc.getCenterX(), endArc.getCenterY(), lastWinLineWidth, startAngle);
            });
            startArc.centerYProperty().addListener((observable, oldValue, newValue) -> {
                double startAngle = new WinLineGeometry(winnerInfo, gameTable.getHeight(), gameTable.getWidth()).startAngle;
                internalRefresh(startArc.getCenterX(), newValue.doubleValue(), endArc.getCenterX(), endArc.getCenterY(), lastWinLineWidth, startAngle);
            });
            endArc.centerXProperty().addListener((observable, oldValue, newValue) -> {
                double startAngle = new WinLineGeometry(winnerInfo, gameTable.getHeight(), gameTable.getWidth()).startAngle;
                internalRefresh(startArc.getCenterX(), startArc.getCenterY(), newValue.doubleValue(), endArc.getCenterY(), lastWinLineWidth, startAngle);
            });
            endArc.centerYProperty().addListener((observable, oldValue, newValue) -> {
                double startAngle = new WinLineGeometry(winnerInfo, gameTable.getHeight(), gameTable.getWidth()).startAngle;
                internalRefresh(startArc.getCenterX(), startArc.getCenterY(), endArc.getCenterX(), newValue.doubleValue(), lastWinLineWidth, startAngle);
            });

            refreshedNodes.add(this);
            refreshedNodes.refreshAll(gameTable.getWidth(), gameTable.getHeight(), gameTable.getWidth(), gameTable.getHeight());
        }


        private void internalRefresh(double startX, double startY, double endX, double endY, double winLineWidth, double startAngle) {
            leftLine.setStartX(startX - Math.cos(startAngle * Math.PI / 180) * winLineWidth);
            leftLine.setStartY(startY + Math.sin(startAngle * Math.PI / 180) * winLineWidth);
            leftLine.setEndX(endX - Math.cos(startAngle * Math.PI / 180) * winLineWidth);
            leftLine.setEndY(endY + Math.sin(startAngle * Math.PI / 180) * winLineWidth);

            double tempStartAngle = -startAngle;
            clipRectangle.setWidth(winLineWidth * 2);
            clipRectangle.setHeight(Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2)));
            clipRectangle.setRotate(tempStartAngle);

            // calculate the coordinates of the center of the rectangle
            double centerX = (startX + endX) / 2;
            double centerY = (startY + endY) / 2;

            // now convert that into the coordinates of the upper left corner of the rectangle if the rotation was 0 (that's how javaFX needs them... :/ )
            clipRectangle.setX(centerX - clipRectangle.getWidth() / 2);
            clipRectangle.setY(centerY - clipRectangle.getHeight() / 2);

            centerLine.setStartX(startX);
            centerLine.setStartY(startY);
            centerLine.setEndX(endX);
            centerLine.setEndY(endY);

            rightLine.setStartX(startX + Math.cos(startAngle * Math.PI / 180) * winLineWidth);
            rightLine.setStartY(startY - Math.sin(startAngle * Math.PI / 180) * winLineWidth);
            rightLine.setEndX(endX + Math.cos(startAngle * Math.PI / 180) * winLineWidth);
            rightLine.setEndY(endY - Math.sin(startAngle * Math.PI / 180) * winLineWidth);
        }

        List<Shape> getAll() {
            List<Shape> res = new ArrayList<>(5);
            res.add(startArc);
            res.add(leftLine);
            res.add(centerLine);
            res.add(rightLine);
            res.add(endArc);
            return res;
        }

        /**
         * Called when the window is resized
         *
         * @param oldWindowWidth  The width of the window prior to resizing
         * @param oldWindowHeight The height of the window prior to resizing
         * @param newWindowWidth  The width of the window after to resizing
         * @param newWindowHeight The height of the window after to resizing
         */
        @Override
        public void refresh(double oldWindowWidth, double oldWindowHeight, double newWindowWidth, double newWindowHeight) {
            WinLineGeometry geometry = new WinLineGeometry(winnerInfo, newWindowHeight, newWindowWidth);
            lastWinLineWidth = geometry.winLineWidth;

            startArc.setCenterX(geometry.startX);
            startArc.setCenterY(geometry.startY);
            startArc.setRadiusX(geometry.winLineWidth);
            startArc.setRadiusY(geometry.winLineWidth);
            startArc.setStartAngle(geometry.startAngle);
            startArc.setLength(180);

            double tempStartAngle = geometry.startAngle = geometry.startAngle + 180;
            if (tempStartAngle > 360) {
                tempStartAngle = tempStartAngle - 360;
            }

            endArc.setCenterX(geometry.endX);
            endArc.setCenterY(geometry.endY);
            endArc.setRadiusX(geometry.winLineWidth);
            endArc.setRadiusY(geometry.winLineWidth);
            endArc.setStartAngle(tempStartAngle);
            endArc.setLength(180);
        }
    }
}

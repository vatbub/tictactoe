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
import com.github.vatbub.tictactoe.view.refreshables.RefreshableArc;
import com.github.vatbub.tictactoe.view.refreshables.RefreshableLine;
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
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
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
    private String suggestedHumanName1;
    private String suggestedHumanName2;
    private String suggestedAIName1;
    private String suggestedAIName2;
    private Board board;
    private ObjectProperty<Font> rowFont;
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
    private Label looserText;

    @FXML
    private Label currentPlayerLabel;

    @FXML
    private AnchorPane tiePane;

    @FXML
    private ImageView bowTie;

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
        // primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));

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

        gameTable.heightProperty().addListener((observable, oldValue, newValue) -> refreshedNodes.refreshAll(gameTable.getWidth(), oldValue.doubleValue(), gameTable.getWidth(), newValue.doubleValue()));
        gameTable.widthProperty().addListener((observable, oldValue, newValue) -> refreshedNodes.refreshAll(oldValue.doubleValue(), gameTable.getHeight(), newValue.doubleValue(), gameTable.getHeight()));

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
        looseImage.fitHeightProperty().addListener((observable, oldValue, newValue) -> reloadLooseImage(looseImage.getFitWidth(), newValue.doubleValue()));
        looseImage.fitWidthProperty().addListener((observable, oldValue, newValue) -> reloadLooseImage(newValue.doubleValue(), looseImage.getFitWidth()));

        initBoard();
        initNewGame();
    }

    private void reloadLooseImage(double newWidth, double newHeight) {
        Thread looseImageReloadThread = new Thread(() -> {
            Image image = new Image(getClass().getResource("loose.png").toString(), newWidth, newHeight, false, true);
            Platform.runLater(() -> looseImage.setImage(image));
        });
        looseImageReloadThread.setName("looseImageReloadThread");
        looseImageReloadThread.start();
    }

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

    }

    private void initNewGame() {
        guiAnimationQueue.submit(() -> {
            suggestedAIName1 = NameList.getNextAIName();
            suggestedAIName2 = NameList.getNextAIName();
            suggestedHumanName1 = NameList.getNextHumanName();
            suggestedHumanName2 = NameList.getNextHumanName();

            player1SetSampleName();
            player2SetSampleName();

            if (looserPane.isVisible()) {
                blurLooserPane();
            }

            if (!isMenuShown()) {
                showMenu();
            }
        });
    }

    private void startGame() {
        initBoard();
        if (looserPane.isVisible()) {
            fadeLooserPaneOut();
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
                board.getPlayer1().doAiTurn(board, board.getPlayer2());
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
            board.setGameEndCallback((winnerInfo) -> guiAnimationQueue.submit(() -> {
                System.out.println("The winner is: " + winnerInfo.winningPlayer.getName());
                if (winnerInfo.isTie()) {
                    showTie(winnerInfo);
                } else {
                    // showTie(winnerInfo);
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
                                board.doTurn(cell.getIndex(), gameTable.getColumns().indexOf(col));
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

    private void showWinner(String winnerName) {
        TranslateTransition girlTransition = new TranslateTransition();
        blurGamePane();
    }

    private void showTie(Board.WinnerInfo winnerInfo) {
        guiAnimationQueue.submitWaitForUnlock(() -> {
            double endX = tiePane.getWidth() - 230;
            double endY = 90;

            bowTie.setX(endX);
            bowTie.setY(-150);

            blurGamePane();
            tiePane.setVisible(true);
            bowTie.setVisible(true);

            Timeline timeline = new Timeline();
            double S4 = 1.45;
            double x0 = 0.33;
            KeyValue kv1x = new KeyValue(bowTie.xProperty(), endX, new CustomEaseOutInterpolator(S4, x0));
            KeyValue kv1y = new KeyValue(bowTie.yProperty(), endY, new CustomEaseOutInterpolator(S4, x0));
            KeyFrame kf1 = new KeyFrame(Duration.seconds(1), kv1x, kv1y);
            timeline.getKeyFrames().add(kf1);

            timeline.setOnFinished((event) -> {
                System.out.println("moved");
                root.getChildren().remove(bowTie);
                tiePane.getChildren().add(bowTie);
                AnchorPane.setRightAnchor(bowTie, tiePane.getWidth() - bowTie.getFitWidth() - endX);
                AnchorPane.setTopAnchor(bowTie, endY);
            });

            timeline.play();
            /*
            FadeTransition looseMessageTransition = new FadeTransition();
            looseMessageTransition.setNode(looseMessage);
            looseMessageTransition.setFromValue(0);
            looseMessageTransition.setToValue(1);
            looseMessageTransition.setDuration(Duration.millis(500));
            looseMessageTransition.setAutoReverse(false);
            looseMessageTransition.play();*/
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

            KeyValue kv1 = new KeyValue(c1.radiusProperty(), 0);
            KeyFrame kf1 = new KeyFrame(Duration.millis(800), kv1);
            KeyValue kv2 = new KeyValue(c1.radiusProperty(), (500 / 640.0) * looserPane.getHeight());
            KeyFrame kf2 = new KeyFrame(Duration.millis(900), kv2);

            timeline.getKeyFrames().addAll(kf1, kf2);


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
        KeyValue kv1 = new KeyValue(winLineGroup.opacityProperty(), 0);
        KeyFrame kf1 = new KeyFrame(Duration.millis(900), kv1);
        KeyValue kv2 = new KeyValue(winLineGroup.opacityProperty(), 1);
        KeyFrame kf2 = new KeyFrame(Duration.millis(950), kv2);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(kf1, kf2);
        timeline.play();
    }

    private void fadeLooserPaneOut() {
        fadeNode(looserPane, 0, true);
    }

    private void fadeWinLineGroup() {
        fadeNode(winLineGroup, 0, () -> {
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

    private void fadeNode(Node node, double toValue) {
        fadeNode(node, toValue, false);
    }

    private void fadeNode(Node node, double toValue, boolean block) {
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
        double winLineWidth;
        double startX;
        double startY;
        double endX;
        double endY;
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

    private class WinLine {
        final RefreshableArc startArc;
        final RefreshableLine leftLine;
        final RefreshableLine centerLine;
        final RefreshableLine rightLine;
        final RefreshableArc endArc;

        WinLine(Board.WinnerInfo winnerInfo) {
            //noinspection SuspiciousNameCombination
            startArc = new RefreshableArc() {
                @Override
                public void refresh(double oldWindowWidth, double oldWindowHeight, double newWindowWidth, double newWindowHeight) {
                    WinLineGeometry geometry = new WinLineGeometry(winnerInfo, newWindowHeight, newWindowWidth);
                    this.setCenterX(geometry.startX);
                    this.setCenterY(geometry.startY);
                    this.setRadiusX(geometry.winLineWidth);
                    this.setRadiusY(geometry.winLineWidth);
                    this.setStartAngle(geometry.startAngle);
                    this.setLength(180);
                }
            };
            refreshedNodes.add(startArc);
            startArc.setType(ArcType.OPEN);

            //noinspection SuspiciousNameCombination
            endArc = new RefreshableArc() {
                @Override
                public void refresh(double oldWindowWidth, double oldWindowHeight, double newWindowWidth, double newWindowHeight) {
                    WinLineGeometry geometry = new WinLineGeometry(winnerInfo, newWindowHeight, newWindowWidth);

                    geometry.startAngle = geometry.startAngle + 180;
                    if (geometry.startAngle > 360) {
                        geometry.startAngle = geometry.startAngle - 360;
                    }

                    this.setCenterX(geometry.endX);
                    this.setCenterY(geometry.endY);
                    this.setRadiusX(geometry.winLineWidth);
                    this.setRadiusY(geometry.winLineWidth);
                    this.setStartAngle(geometry.startAngle);
                    this.setLength(180);
                }
            };
            refreshedNodes.add(endArc);
            endArc.setType(ArcType.OPEN);

            leftLine = new RefreshableLine() {
                @Override
                public void refresh(double oldWindowWidth, double oldWindowHeight, double newWindowWidth, double newWindowHeight) {
                    WinLineGeometry geometry = new WinLineGeometry(winnerInfo, newWindowHeight, newWindowWidth);
                    this.setStartX(geometry.startX - Math.cos(geometry.startAngle * Math.PI / 180) * geometry.winLineWidth);
                    this.setStartY(geometry.startY + Math.sin(geometry.startAngle * Math.PI / 180) * geometry.winLineWidth);
                    this.setEndX(geometry.endX - Math.cos(geometry.startAngle * Math.PI / 180) * geometry.winLineWidth);
                    this.setEndY(geometry.endY + Math.sin(geometry.startAngle * Math.PI / 180) * geometry.winLineWidth);
                }
            };
            refreshedNodes.add(leftLine);
            centerLine = new RefreshableLine() {
                @Override
                public void refresh(double oldWindowWidth, double oldWindowHeight, double newWindowWidth, double newWindowHeight) {
                    WinLineGeometry geometry = new WinLineGeometry(winnerInfo, newWindowHeight, newWindowWidth);
                    this.setStartX(geometry.startX);
                    this.setStartY(geometry.startY);
                    this.setEndX(geometry.endX);
                    this.setEndY(geometry.endY);
                }
            };
            refreshedNodes.add(centerLine);
            rightLine = new RefreshableLine() {
                @Override
                public void refresh(double oldWindowWidth, double oldWindowHeight, double newWindowWidth, double newWindowHeight) {
                    WinLineGeometry geometry = new WinLineGeometry(winnerInfo, newWindowHeight, newWindowWidth);
                    this.setStartX(geometry.startX + Math.cos(geometry.startAngle * Math.PI / 180) * geometry.winLineWidth);
                    this.setStartY(geometry.startY - Math.sin(geometry.startAngle * Math.PI / 180) * geometry.winLineWidth);
                    this.setEndX(geometry.endX + Math.cos(geometry.startAngle * Math.PI / 180) * geometry.winLineWidth);
                    this.setEndY(geometry.endY - Math.sin(geometry.startAngle * Math.PI / 180) * geometry.winLineWidth);
                }
            };
            refreshedNodes.add(rightLine);
            refreshedNodes.refreshAll(gameTable.getWidth(), gameTable.getHeight(), gameTable.getWidth(), gameTable.getHeight());
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
    }
}

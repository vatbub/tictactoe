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


import com.github.vatbub.common.core.Common;
import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.common.view.core.ExceptionAlert;
import com.github.vatbub.tictactoe.Board;
import com.github.vatbub.tictactoe.NameList;
import com.github.vatbub.tictactoe.Player;
import com.github.vatbub.tictactoe.PlayerMode;
import com.github.vatbub.tictactoe.common.Move;
import com.github.vatbub.tictactoe.common.OnlineMultiplayerRequestOpponentResponse;
import com.github.vatbub.tictactoe.common.ResponseCode;
import com.github.vatbub.tictactoe.kryo.KryoGameConnections;
import com.github.vatbub.tictactoe.kryo.OnOpponentFoundRunnable;
import com.github.vatbub.tictactoe.view.refreshables.Refreshable;
import com.github.vatbub.tictactoe.view.refreshables.RefreshableNodeList;
import com.sun.javafx.tk.Toolkit;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.controlsfx.control.ToggleSwitch;

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
@SuppressWarnings({"JavaDoc", "WeakerAccess"})
public class Main extends Application {
    public static final double animationSpeed = 0.3;
    private static final String windowTitle = "Tic Tac Toe";
    private static final int gameRows = 3;
    private static final int gameCols = 3;
    private static final String player1Letter = "X";
    private static final String player2Letter = "O";
    public static Main currentMainWindowInstance;
    private static Stage stage;
    final StringProperty style = new SimpleStringProperty("");
    private final AnimationThreadPoolExecutor guiAnimationQueue = new AnimationThreadPoolExecutor(2);
    private final RefreshableNodeList refreshedNodes = new RefreshableNodeList();
    private final Map<String, Timer> loadTimerMap = new HashMap<>();
    private final DoubleProperty aiLevelLabelPositionProperty = new SimpleDoubleProperty();
    private final String player1SampleName = NameList.getNextName();
    private final String player2SampleName = NameList.getNextName();
    private final Timer runLaterTimer = new Timer();
    private Board board;
    private ObjectProperty<Font> rowFont;
    private Rectangle aiLevelLabelClipRectangle;
    private boolean blockedForInput;
    /**
     * We need to save this manually since {@code aiLevelSlider.isVisible} is delayed due to the animation
     */
    private boolean aiLevelSliderVisible = true;
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
    @FXML
    private Line aiLevelCenterLine;
    @FXML
    private AnchorPane currentPlayerLabelAnchorPane;
    @FXML
    private AnchorPane playOnlineAnchorPane;
    @FXML
    private Hyperlink playOnlineHyperlink;
    @FXML
    private AnchorPane loadingBackground;
    @FXML
    private HBox loadingBox;
    @FXML
    private Label loadingStatusText;
    @FXML
    private VBox errorBox;
    @FXML
    private Label errorReasonLabel;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private VBox onlineMenuBox;
    @FXML
    private VBox getOnlineMenuSubBox;
    @FXML
    private TextField onlineMyUsername;
    @FXML
    private TextField onlineDesiredOpponentName;
    @FXML
    private HBox opponentsTurnHBox;
    @FXML
    private AnchorPane opponentsTurnAnchorPane;
    @FXML
    private Label opponentsTurnLabel;

    public Main() {
        super();
        currentMainWindowInstance = this;
    }

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

    private String getWindowTitle() {
        String res = windowTitle;

        if (board != null && (board.getCurrentPlayer().getPlayerMode().equals(PlayerMode.internetHuman) || board.getOpponent(board.getCurrentPlayer()).getPlayerMode().equals(PlayerMode.internetHuman))) {
            Player internetPlayer;
            if (board.getCurrentPlayer().getPlayerMode().equals(PlayerMode.internetHuman)) {
                internetPlayer = board.getCurrentPlayer();
            } else {
                internetPlayer = board.getOpponent(board.getCurrentPlayer());
            }
            res = res + ": Playing against " + internetPlayer.getName();

            if (board.getCurrentPlayer() == internetPlayer) {
                res = res + " (Opponent's turn)";
            } else {
                res = res + " (Your turn)";
            }
        }

        return res;
    }

    @FXML
    void onlineStartButtonOnAction(ActionEvent event) {
        hideOnlineMenu();
        setLoadingStatusText("Searching for an opponent...", true);
        showLoadingScreen();
        String clientIdentifier = onlineMyUsername.getText();
        if (clientIdentifier.equals("")) {
            clientIdentifier = onlineMyUsername.getPromptText();
        }

        String desiredOpponentIdentifier = null;
        if (!onlineDesiredOpponentName.getText().equals("")) {
            desiredOpponentIdentifier = onlineDesiredOpponentName.getText();
        }

        KryoGameConnections.requestOpponent(clientIdentifier, desiredOpponentIdentifier, new OnOpponentFoundRunnable() {
            private boolean inversePlayerOrder = true;

            @Override
            public void run(OnlineMultiplayerRequestOpponentResponse response) {
                if (response.getResponseCode() == ResponseCode.WaitForOpponent) {
                    inversePlayerOrder = false;
                } else {
                    Main.this.setLoadingStatusText("Waiting for the opponent...");
                    Platform.runLater(() -> Main.this.startGame(response.getOpponentIdentifier(), inversePlayerOrder));
                }
            }
        }, () -> Platform.runLater(() -> showErrorMessage("The game was cancelled.", "Disconnected from the server.")));
    }

    public void showErrorMessage(Throwable e) {
        showErrorMessage("Something went wrong.", e);
    }

    public void showErrorMessage(@SuppressWarnings("SameParameterValue") String errorMessage, Throwable e) {
        Throwable finalException;
        if (ExceptionUtils.getRootCause(e) != null) {
            finalException = ExceptionUtils.getRootCause(e);
        } else {
            finalException = e;
        }
        String errorText = finalException.getClass().getSimpleName();
        if (finalException.getLocalizedMessage() != null) {
            errorText = errorText + ": " + finalException.getLocalizedMessage();
        }
        showErrorMessage(errorMessage, errorText);
    }

    public void showErrorMessage(String errorMessage, String errorReason) {
        String finalErrorMessage = errorMessage + "\nPlease try again later.\nReason:";
        errorMessageLabel.setText(finalErrorMessage);
        errorReasonLabel.setText(errorReason);
        showErrorScreen();
    }

    @FXML
    void playOnlineHyperlinkOnAction(ActionEvent event) {
        if (!onlineMenuBox.isVisible()) {
            connectToRelayServer();
        } else {
            guiAnimationQueue.submit(() -> {
                guiAnimationQueue.setBlocked(true);
                playOnlineHyperlink.setText("Play online");
                fadeNode(onlineMenuBox, 0, () -> fadeNode(menuBox, 1, () -> guiAnimationQueue.setBlocked(false)));
            });
        }
    }

    private void connectToRelayServer() {
        setLoadingStatusText("Connecting to the server...", true);
        showLoadingScreen();
        Thread connectionThread = new Thread(() -> {
            try {
                KryoGameConnections.connect(() -> Platform.runLater(() -> {
                    hideLoadingScreen();
                    showOnlineMenu();
                }));
            } catch (IOException e) {
                FOKLogger.log(Main.class.getName(), Level.SEVERE, "Could not connect to the relay server: " + e.getMessage(), e);
                Platform.runLater(() -> showErrorMessage(e));
            }
        });
        connectionThread.setName("connectionThread");
        connectionThread.start();
    }

    private void showErrorScreen() {
        blurGamePane();
        fadeNode(loadingBackground, 0.8);
        fadeNode(errorBox, 1, true);
    }

    private void hideErrorScreen() {
        fadeNode(errorBox, 0, true);
    }

    private void showLoadingScreen() {
        guiAnimationQueue.submitWaitForUnlock(() -> {
            guiAnimationQueue.setBlocked(true);
            fadeNode(loadingBackground, 0.8);
            fadeNode(loadingBox, 1, () -> guiAnimationQueue.setBlocked(false));
        });
    }

    private void hideLoadingScreen() {
        guiAnimationQueue.submitWaitForUnlock(() -> {
            guiAnimationQueue.setBlocked(true);
            fadeNode(loadingBackground, 0);
            fadeNode(loadingBox, 0, () -> guiAnimationQueue.setBlocked(false));
        });
    }

    private void setLoadingStatusText(@SuppressWarnings("SameParameterValue") String textToSet) {
        setLoadingStatusText(textToSet, false);
    }

    private void setLoadingStatusText(String textToSet, boolean noAnimation) {
        if (!loadingStatusText.getText().equals(textToSet) && !noAnimation) {
            KeyValue keyValueTranslation1 = new KeyValue(loadingStatusText.translateYProperty(), -loadingStatusText.getHeight());
            KeyValue keyValueOpacity1 = new KeyValue(loadingStatusText.opacityProperty(), 0);
            KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(animationSpeed), keyValueOpacity1, keyValueTranslation1);

            Timeline timeline1 = new Timeline(keyFrame1);

            timeline1.setOnFinished((event) -> {
                loadingStatusText.setText(textToSet);
                loadingStatusText.setTranslateY(loadingStatusText.getHeight());

                KeyValue keyValueTranslation2 = new KeyValue(loadingStatusText.translateYProperty(), 0);
                KeyValue keyValueOpacity2 = new KeyValue(loadingStatusText.opacityProperty(), 1);
                KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(animationSpeed), keyValueOpacity2, keyValueTranslation2);

                Timeline timeline2 = new Timeline(keyFrame2);

                timeline2.play();
            });

            timeline1.play();
        } else {
            loadingStatusText.setText(textToSet);
        }
    }

    private void showOnlineMenu() {
        guiAnimationQueue.submit(() -> {
            guiAnimationQueue.setBlocked(true);
            playOnlineHyperlink.setText("Play offline");
            fadeNode(menuBox, 0);
            showMenuBackground();
            fadeNode(onlineMenuBox, 1, () -> guiAnimationQueue.setBlocked(false));
        });
    }

    private void hideOnlineMenu() {
        guiAnimationQueue.submit(() -> {
            guiAnimationQueue.setBlocked(true);
            playOnlineHyperlink.setText("Play online");
            fadeNode(onlineMenuBox, 0, () -> guiAnimationQueue.setBlocked(false));
        });
    }

    @FXML
    void errorRetryOnAction(ActionEvent event) {
        hideErrorScreen();
        connectToRelayServer();
    }

    @FXML
    void errorPlayOfflineOnAction(ActionEvent event) {
        hideErrorScreen();
        hideLoadingScreen();
        showMenu();
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
        stage = primaryStage;
        primaryStage.setMinWidth(scene.getRoot().minWidth(0) + 70);
        primaryStage.setMinHeight(scene.getRoot().minHeight(0) + 70);

        primaryStage.setScene(scene);

        // Set Icon
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("icon.png")));

        primaryStage.setTitle(getWindowTitle());

        primaryStage.show();
    }

    @Override
    public void stop() {
        try {
            if (KryoGameConnections.isGameConnected()) {
                KryoGameConnections.sendCancelGameRequest();
            }
            KryoGameConnections.resetConnections();
        } catch (Exception e) {
            FOKLogger.log(Main.class.getName(), Level.SEVERE, "Exception in the application stop method", e);
        }
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

        opponentsTurnHBox.heightProperty().addListener((observable, oldValue, newValue) -> updateOpponentsTurnHBox(false));

        aiLevelLabelClipRectangle = new Rectangle(0, 0, 0, 0);
        aiLevelLabelClipRectangle.setEffect(new MotionBlur(0, 10));
        aiLevelLabelPane.setClip(aiLevelLabelClipRectangle);
        aiLevelLabelClipRectangle.heightProperty().bind(aiLevelLabelPane.heightProperty());
        aiLevelLabelPane.widthProperty().addListener((observable, oldValue, newValue) -> updateAILevelLabel(true));

        player1SetSampleName();
        player2SetSampleName();

        gameTable.heightProperty().addListener((observable, oldValue, newValue) -> refreshedNodes.refreshAll(gameTable.getWidth(), oldValue.doubleValue(), gameTable.getWidth(), newValue.doubleValue()));
        gameTable.widthProperty().addListener((observable, oldValue, newValue) -> refreshedNodes.refreshAll(oldValue.doubleValue(), gameTable.getHeight(), newValue.doubleValue(), gameTable.getHeight()));

        player1AIToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showHideAILevelSlider(newValue, player2AIToggle.isSelected());
            player1SetSampleName();
        });
        player2AIToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showHideAILevelSlider(player1AIToggle.isSelected(), newValue);
            player2SetSampleName();
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

        // Kunami code
        root.setOnKeyPressed(event -> {
            if (KunamiCode.isCompleted(event.getCode())) {
                if (root.getEffect() != null && root.getEffect() instanceof Blend) {
                    BlendMode currentMode = ((Blend) root.getEffect()).getMode();
                    BlendMode nextMode;
                    if (currentMode == BlendMode.values()[BlendMode.values().length - 1]) {
                        nextMode = BlendMode.values()[0];
                    } else {
                        nextMode = BlendMode.values()[Arrays.asList(BlendMode.values()).indexOf(currentMode) + 1];
                    }
                    ((Blend) root.getEffect()).setMode(nextMode);
                } else {
                    root.setEffect(new Blend(BlendMode.EXCLUSION));
                }
            }
        });

        // prompt text of the my username field in the online multiplayer menu
        onlineMyUsername.promptTextProperty().bind(player1Name.promptTextProperty());
        onlineMyUsername.textProperty().bindBidirectional(player1Name.textProperty());

        initBoard();
        initNewGame();
    }

    private void player1SetSampleName() {
        player1Name.setPromptText(player1AIToggle.isSelected() ? player1SampleName + " (AI)" : player1SampleName);
    }

    private void player2SetSampleName() {
        player2Name.setPromptText(player2AIToggle.isSelected() ? player2SampleName + " (AI)" : player2SampleName);
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
                Image image = new Image(imageURL, newWidth, newHeight, false, true);
                Platform.runLater(() -> imageView.setImage(image));
            }
        }, 300);
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
        if (!isBlockedForInput()) {
            setBlockedForInput(true);
            boolean opponentIsInternetPlayer = board.getOpponent(board.getCurrentPlayer()).getPlayerMode().equals(PlayerMode.internetHuman);
            board.getCurrentPlayer().doAiTurn(board, AILevel.UNBEATABLE);
            updateCurrentPlayerLabel(false, opponentIsInternetPlayer);
            renderRows();
        } else {
            flashOpponentsTurnHBox();
        }
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
        updateAILevelLabel(false);
    }

    private void updateAILevelLabel(boolean forceUpdate) {
        double sliderPos = 100 * Math.round(aiLevelSlider.getValue() * 3.0 / 100.0) / 3.0;

        if (sliderPos != aiLevelLabelPositionProperty.get() || forceUpdate) {
            aiLevelLabelPositionProperty.set(sliderPos);

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

            KeyValue hBoxLayoutXKeyValue1 = new KeyValue(aiLevelLabelHBox.layoutXProperty(), aiLevelLabelHBox.getLayoutX(), Interpolator.EASE_BOTH);
            KeyValue aiLevelLabelClipRectangleWidthKeyValue1 = new KeyValue(aiLevelLabelClipRectangle.widthProperty(), aiLevelLabelClipRectangle.getWidth(), Interpolator.EASE_BOTH);
            KeyValue aiLevelLabelClipRectangleXKeyValue1 = new KeyValue(aiLevelLabelClipRectangle.xProperty(), aiLevelLabelClipRectangle.getX(), Interpolator.EASE_BOTH);
            KeyValue aiLevelCenterLineStartXKeyValue1 = new KeyValue(aiLevelCenterLine.startXProperty(), aiLevelCenterLine.getStartX(), Interpolator.EASE_BOTH);
            KeyValue aiLevelCenterLineEndXKeyValue1 = new KeyValue(aiLevelCenterLine.endXProperty(), aiLevelCenterLine.getEndX(), Interpolator.EASE_BOTH);
            KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(0), hBoxLayoutXKeyValue1, aiLevelLabelClipRectangleWidthKeyValue1, aiLevelLabelClipRectangleXKeyValue1, aiLevelCenterLineStartXKeyValue1, aiLevelCenterLineEndXKeyValue1);

            double interpolatedLabelWidth = trueWidthFunction.value(sliderPos);

            KeyValue hBoxLayoutXKeyValue2 = new KeyValue(aiLevelLabelHBox.layoutXProperty(), translateFunction.value(sliderPos), Interpolator.EASE_BOTH);
            KeyValue aiLevelLabelClipRectangleWidthKeyValue2 = new KeyValue(aiLevelLabelClipRectangle.widthProperty(), widthFunction.value(sliderPos), Interpolator.EASE_BOTH);
            KeyValue aiLevelLabelClipRectangleXKeyValue2 = new KeyValue(aiLevelLabelClipRectangle.xProperty(), aiLevelLabelPane.getWidth() / 2 - widthFunction.value(sliderPos) / 2, Interpolator.EASE_BOTH);
            KeyValue aiLevelCenterLineStartXKeyValue2 = new KeyValue(aiLevelCenterLine.startXProperty(), (aiLevelLabelPane.getWidth() - interpolatedLabelWidth) / 2, Interpolator.EASE_BOTH);
            KeyValue aiLevelCenterLineEndXKeyValue2 = new KeyValue(aiLevelCenterLine.endXProperty(), (aiLevelLabelPane.getWidth() + interpolatedLabelWidth) / 2, Interpolator.EASE_BOTH);
            KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(animationSpeed * 0.9), hBoxLayoutXKeyValue2, aiLevelLabelClipRectangleWidthKeyValue2, aiLevelLabelClipRectangleXKeyValue2, aiLevelCenterLineStartXKeyValue2, aiLevelCenterLineEndXKeyValue2);

            Timeline timeline = new Timeline(keyFrame1, keyFrame2);
            timeline.play();
        }
    }

    @FXML
    void aboutLinkOnAction(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/vatbub/tictactoe#tictactoe"));
        } catch (URISyntaxException | IOException e) {
            FOKLogger.log(Main.class.getName(), Level.SEVERE, "Typo in a hardcoded value", e);
        }
    }

    public void initNewGame() {
        if (KryoGameConnections.isGameConnected()) {
            KryoGameConnections.sendCancelGameRequest();
        }

        KryoGameConnections.resetConnections();
        guiAnimationQueue.submit(() -> {
            if (looserPane.isVisible()) {
                blurLooserPane();
            }
            if (tiePane.isVisible()) {
                blurTiePane();
            }
            if (winPane.isVisible()) {
                blurWinPane();
            }

            updateAILevelLabel(true);
            if (!isMenuShown()) {
                showMenu();
            }
        });
    }

    private void startGame() {
        startGame(null, false);
    }

    private void startGame(String onlineOpponentName, boolean inversePlayerOrderForOnlineGame) {
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
        hideOnlineMenu();
        hideLoadingScreen();
        fadeWinLineGroup();
        if (onlineOpponentName != null) {
            guiAnimationQueue.submit(() -> {
                String finalOnlineUsername = onlineMyUsername.getText();
                if (finalOnlineUsername.equals("")) {
                    finalOnlineUsername = onlineMyUsername.getPromptText();
                }

                if (!inversePlayerOrderForOnlineGame) {
                    board.setPlayer1(new Player(PlayerMode.localHuman, finalOnlineUsername, player1Letter));
                    board.setPlayer2(new Player(PlayerMode.internetHuman, onlineOpponentName, player2Letter));
                } else {
                    board.setPlayer1(new Player(PlayerMode.internetHuman, onlineOpponentName, player1Letter));
                    board.setPlayer2(new Player(PlayerMode.localHuman, finalOnlineUsername, player2Letter));
                }
                updateCurrentPlayerLabel(true, inversePlayerOrderForOnlineGame);

                KryoGameConnections.setConnectedBoard(board);
            });
        } else {
            guiAnimationQueue.submit(() -> {
                String finalPlayerName1 = player1Name.getText();
                if (finalPlayerName1.equals("")) {
                    finalPlayerName1 = player1Name.getPromptText();
                }

                String finalPlayerName2 = player2Name.getText();
                if (finalPlayerName2.equals("")) {
                    finalPlayerName2 = player2Name.getPromptText();
                }

                board.setPlayer1(new Player(player1AIToggle.isSelected() ? PlayerMode.ai : PlayerMode.localHuman, finalPlayerName1, player1Letter));
                board.setPlayer2(new Player(player2AIToggle.isSelected() ? PlayerMode.ai : PlayerMode.localHuman, finalPlayerName2, player2Letter));
                updateCurrentPlayerLabel(true);

                if (board.getPlayer1().isAi()) {
                    board.getPlayer1().doAiTurn(board);
                }
            });
        }
    }

    public void updateCurrentPlayerLabel() {
        updateCurrentPlayerLabel(false);
    }

    public void updateCurrentPlayerLabel(boolean noAnimation) {
        updateCurrentPlayerLabel(noAnimation, false);
    }

    public void updateCurrentPlayerLabel(boolean noAnimation, boolean setBlockedValueAfterAnimation) {
        Platform.runLater(() -> stage.setTitle(getWindowTitle()));
        if (board.getCurrentPlayer() != null) {
            if (!board.getCurrentPlayer().getLetter().equals(currentPlayerLabel.getText())) {
                if (noAnimation) {
                    setCurrentPlayerValue();
                } else {
                    guiAnimationQueue.submitWaitForUnlock(() -> {
                        guiAnimationQueue.setBlocked(true);

                        GaussianBlur blur = (GaussianBlur) currentPlayerLabel.getEffect();
                        if (blur == null) {
                            blur = new GaussianBlur(0);
                        }

                        Calendar changeLabelTextDate = Calendar.getInstance();
                        changeLabelTextDate.add(Calendar.MILLISECOND, (int) (animationSpeed * 1000));
                        runLaterTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> setCurrentPlayerValue());
                            }
                        }, changeLabelTextDate.getTime());

                        currentPlayerLabel.setEffect(blur);
                        Timeline timeline = new Timeline();
                        KeyValue keyValue1 = new KeyValue(blur.radiusProperty(), 20);
                        KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(animationSpeed), keyValue1);

                        KeyValue keyValue2 = new KeyValue(blur.radiusProperty(), 0);
                        KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(2 * animationSpeed), keyValue2);

                        timeline.getKeyFrames().addAll(keyFrame1, keyFrame2);

                        timeline.setOnFinished((event) -> {
                            currentPlayerLabel.setEffect(null);
                            guiAnimationQueue.setBlocked(false);
                            setBlockedForInput(setBlockedValueAfterAnimation);
                        });

                        timeline.play();
                    });
                    return;
                }
            }
        }

        guiAnimationQueue.setBlocked(false);
        setBlockedForInput(setBlockedValueAfterAnimation);
    }

    private void setCurrentPlayerValue() {
        Player currentPlayer = board.getCurrentPlayer();
        if (currentPlayer != null) {
            currentPlayerLabel.setText(currentPlayer.getLetter());
        }
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
                // disconnect after ending the game
                if (board.getCurrentPlayer().getPlayerMode().equals(PlayerMode.internetHuman) || board.getOpponent(board.getCurrentPlayer()).getPlayerMode().equals(PlayerMode.internetHuman)) {
                    KryoGameConnections.resetConnections();
                }
                updateOpponentsTurnHBox(false, false);
                FOKLogger.info(Main.class.getName(), "The winner is: " + winnerInfo.winningPlayer.getName());
                if (winnerInfo.isTie()) {
                    showTie();
                } else if (winnerInfo.winningPlayer.getPlayerMode().equals(PlayerMode.localHuman) && !board.getOpponent(winnerInfo.winningPlayer).getPlayerMode().equals(PlayerMode.localHuman)) {
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
                            if (board.getPlayerAt(cell.getIndex(), gameTable.getColumns().indexOf(col)) == null && !isBlockedForInput()) {
                                setBlockedForInput(true);
                                boolean opponentIsInternetPlayer = board.getOpponent(board.getCurrentPlayer()).getPlayerMode().equals(PlayerMode.internetHuman);
                                board.doTurn(new Move(cell.getIndex(), gameTable.getColumns().indexOf(col)));
                                updateCurrentPlayerLabel(false, opponentIsInternetPlayer);
                                renderRows();
                            } else if (isBlockedForInput()) {
                                flashOpponentsTurnHBox();
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

    public void renderRows() {
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
            if (rowFont != null) {
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
                style.set("-fx-font-size:" + fontSize + "px; -fx-padding: 0;");
            }

            gameTable.setFixedCellSize(effectiveHeight / board.getRowCount());
            gameTable.refresh();
        });
    }

    private boolean isMenuShown() {
        return menuBox.isVisible();
    }

    private void showMenu() {
        guiAnimationQueue.submit(() -> {
            fadeNode(menuBox, 1);
            menuBox.setVisible(true);
        });
        showMenuBackground();
    }

    private void showMenuBackground() {
        guiAnimationQueue.submit(() -> {
            fadeNode(menuBackground, 0.12);

            if (currentPlayerLabelAnchorPane.isVisible()) {
                playOnlineAnchorPane.setVisible(true);
                KeyValue keyValuePlayOnline1 = new KeyValue(playOnlineAnchorPane.translateYProperty(), 3 + playOnlineAnchorPane.getHeight());
                KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(0), keyValuePlayOnline1);

                KeyValue keyValuePlayOnline2 = new KeyValue(playOnlineAnchorPane.translateYProperty(), 3 + playOnlineAnchorPane.getHeight());
                KeyValue keyValueCurrentPlayer2 = new KeyValue(currentPlayerLabelAnchorPane.translateYProperty(), 3 + currentPlayerLabelAnchorPane.getHeight());
                KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(animationSpeed), keyValueCurrentPlayer2, keyValuePlayOnline2);

                KeyValue keyValuePlayOnline3 = new KeyValue(playOnlineAnchorPane.translateYProperty(), 0);
                KeyFrame keyFrame3 = new KeyFrame(Duration.seconds(2 * animationSpeed), keyValuePlayOnline3);

                Timeline timeline = new Timeline(keyFrame1, keyFrame2, keyFrame3);
                timeline.setOnFinished((event) -> currentPlayerLabelAnchorPane.setVisible(false));
                timeline.play();
            }

            blurGamePane();

            menuBackground.setVisible(true);
        });
    }

    private void hideMenu() {
        guiAnimationQueue.submit(() -> {
            fadeNode(menuBackground, 0);
            fadeNode(menuBox, 0);

            currentPlayerLabelAnchorPane.setVisible(true);
            KeyValue keyValueCurrentPlayer1 = new KeyValue(currentPlayerLabelAnchorPane.translateYProperty(), 3 + currentPlayerLabelAnchorPane.getHeight());
            KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(0), keyValueCurrentPlayer1);

            KeyValue keyValuePlayOnline2 = new KeyValue(playOnlineAnchorPane.translateYProperty(), 3 + playOnlineAnchorPane.getHeight());
            KeyValue keyValueCurrentPlayer2 = new KeyValue(currentPlayerLabelAnchorPane.translateYProperty(), 3 + currentPlayerLabelAnchorPane.getHeight());
            KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(animationSpeed), keyValueCurrentPlayer2, keyValuePlayOnline2);

            KeyValue keyValueCurrentPlayer3 = new KeyValue(currentPlayerLabelAnchorPane.translateYProperty(), 0);
            KeyFrame keyFrame3 = new KeyFrame(Duration.seconds(2 * animationSpeed), keyValueCurrentPlayer3);

            Timeline timeline = new Timeline(keyFrame1, keyFrame2, keyFrame3);
            timeline.setOnFinished((event) -> playOnlineAnchorPane.setVisible(false));
            timeline.play();

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
        if ((player1IsAI || player2IsAI) && !aiLevelSliderVisible) {
            aiLevelSliderVisible = true;
            fadeAILevelSliderIn();
        } else if (!player1IsAI && !player2IsAI && aiLevelSliderVisible) {
            aiLevelSliderVisible = false;
            fadeAILevelSliderOut();
        }
    }

    private void fadeAILevelSliderOut() {
        guiAnimationQueue.submitWaitForUnlock(() -> {
            guiAnimationQueue.setBlocked(true);
            menuSubBox.setPrefHeight(menuSubBox.getHeight());
            fadeNode(aiLevelTitleLabel, 0, false, () -> menuSubBox.getChildren().remove(aiLevelTitleLabel));
            fadeNode(aiLevelSlider, 0, false, () -> menuSubBox.getChildren().remove(aiLevelSlider));
            fadeNode(aiLevelLabelPane, 0, false, () -> {
                menuSubBox.getChildren().remove(aiLevelLabelPane);
                updateMenuHeight(false);
            });
        });
    }

    private void fadeAILevelSliderIn() {
        guiAnimationQueue.submitWaitForUnlock(() -> {
            guiAnimationQueue.setBlocked(true);
            menuSubBox.setPrefHeight(menuSubBox.getHeight());
            updateMenuHeight(true);
        });
    }

    private void updateMenuHeight(boolean includeAILevelSlider) {
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
                node.setOpacity(0);
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

    public boolean isBlockedForInput() {
        return blockedForInput;
    }

    public void setBlockedForInput(boolean blockedForInput) {
        this.blockedForInput = blockedForInput;
        updateOpponentsTurnHBox();
    }

    public void updateOpponentsTurnHBox() {
        updateOpponentsTurnHBox(false);
    }

    public void updateOpponentsTurnHBox(boolean noAnimation) {
        updateOpponentsTurnHBox(noAnimation, isBlockedForInput());
    }

    public void updateOpponentsTurnHBox(boolean noAnimation, boolean isShown) {
        double destinationTranslate;
        if (!isShown) {
            destinationTranslate = opponentsTurnHBox.getHeight() + 3;
        } else {
            destinationTranslate = 0;
            String opponentsName;
            if (board.getCurrentPlayer() == null || board.getOpponent(board.getCurrentPlayer()) == null) {
                opponentsName = "Opponent";
            } else {
                Player player;
                if (board.getCurrentPlayer().getPlayerMode() == PlayerMode.localHuman) {
                    player = board.getOpponent(board.getCurrentPlayer());
                } else {
                    player = board.getCurrentPlayer();
                }
                opponentsName = player.getName();
            }
            opponentsTurnLabel.setText(opponentsName + "'s turn...");
        }

        if (noAnimation) {
            opponentsTurnHBox.setTranslateY(destinationTranslate);
        } else {
            KeyValue keyValue = new KeyValue(opponentsTurnHBox.translateYProperty(), destinationTranslate);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(animationSpeed), keyValue);
            Timeline timeline = new Timeline(keyFrame);
            timeline.play();
        }
    }

    public void flashOpponentsTurnHBox() {
        KeyValue keyValue1Color = new KeyValue(((DropShadow) opponentsTurnAnchorPane.getEffect()).colorProperty(), Color.RED);
        KeyValue keyValue1Width = new KeyValue(((DropShadow) opponentsTurnAnchorPane.getEffect()).widthProperty(), 30);
        KeyValue keyValue1Height = new KeyValue(((DropShadow) opponentsTurnAnchorPane.getEffect()).heightProperty(), 30);
        KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(animationSpeed / 2), keyValue1Color, keyValue1Width, keyValue1Height);

        KeyValue keyValue2Color = new KeyValue(((DropShadow) opponentsTurnAnchorPane.getEffect()).colorProperty(), Color.BLACK);
        KeyValue keyValue2Width = new KeyValue(((DropShadow) opponentsTurnAnchorPane.getEffect()).widthProperty(), 12);
        KeyValue keyValue2Height = new KeyValue(((DropShadow) opponentsTurnAnchorPane.getEffect()).heightProperty(), 12);
        KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(animationSpeed), keyValue2Color, keyValue2Width, keyValue2Height);

        Timeline timeline = new Timeline(keyFrame1, keyFrame2);
        // play it twice
        timeline.setOnFinished((event -> new Timeline(keyFrame1, keyFrame2).play()));
        timeline.play();
    }

    private static class KunamiCode {
        private static final KeyCode[] kunamiCodes = {KeyCode.U, KeyCode.U, KeyCode.D, KeyCode.D, KeyCode.L, KeyCode.R, KeyCode.L, KeyCode.R, KeyCode.B, KeyCode.A};
        private static int currentIndex = 0;

        private static boolean isCompleted(KeyCode nextKeyCode) {
            if (nextKeyCode.equals(kunamiCodes[currentIndex])) {
                if (currentIndex == kunamiCodes.length - 1) {
                    // we're at the end
                    currentIndex = 0;
                    return true;
                } else {
                    // The key was correct but we're not yet at the end
                    currentIndex++;
                    return false;
                }
            } else {
                // the key was false
                currentIndex = 0;
                return false;
            }
        }
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

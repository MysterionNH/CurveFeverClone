package CurveFeverClone;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

class GUI {
    // constants
    static final int DEFAULT_LINE_WIDTH = 3;
    // general settings
    private static final int FIELD_WIDTH = 750;
    private static final int FIELD_HEIGHT = FIELD_WIDTH;
    private static final double FOUR_THIRDS = 1.33333333;
    private static final int KEY_UPDATE_DELAY = 50;
    private static final boolean TELEPORT_ON_EDGE = true;
    private final LinkedList<KeyCode> keyStack = new LinkedList<>();
    // start paused
    private boolean setup = true;
    private boolean enteringSring = false;
    private boolean allowNext = false;
    private boolean pause = true;
    private boolean starting = false;
    // the players (currently maxed to four)
    private Player[] players = {new Player(), new Player(), new Player(), new Player()};
    // everything input related
    private long lastKeyUpdate;
    private String doNotMindMe = "";
    private String currentText = "";

    private KeyCode currentKey;
    // gui control
    private Text pauseText;
    private Text settingsText;
    private Scene rootScene;
    private Stage rootStage;
    private StackPane pauseScreen;
    private GraphicsContext fieldCanvas;

    private ArrayList<KeyCode> typedKeys = new ArrayList<>();

    GUI(Stage _stage) {
        rootStage = _stage;

        // stage settings
        rootStage.setFullScreenExitHint("");
        rootStage.setTitle("CurveFeverClone");
        rootStage.setMinHeight(FIELD_HEIGHT + 33.333);
        rootStage.setOnCloseRequest(event -> System.exit(0));
        rootStage.setMinWidth((FIELD_WIDTH * FOUR_THIRDS) + 33.333);
        //rootStage.getIcons().add(new Image(getClass().getClassLoader().getResource("icon.png").toString()));

        // setup for play field
        Canvas gameCanvas = new Canvas();
        gameCanvas.setWidth(FIELD_WIDTH);
        gameCanvas.setHeight(FIELD_HEIGHT);
        fieldCanvas = gameCanvas.getGraphicsContext2D();
        // wrap canvas in StackPane
        StackPane gameWrapper = new StackPane();
        gameWrapper.setMinSize(FIELD_WIDTH, FIELD_HEIGHT);
        gameWrapper.setMaxSize(FIELD_WIDTH, FIELD_HEIGHT);
        gameWrapper.setStyle("-fx-background-color: #000");
        gameWrapper.getChildren().add(gameCanvas);
        StackPane.setAlignment(gameWrapper, Pos.CENTER_LEFT);

        // setup for score view
        // title text
        Text title = new Text("CurveFeverClone");
        title.setFill(Paint.valueOf("WHITE"));
        title.setTextOrigin(VPos.BOTTOM);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setFont(Font.font("System", FontWeight.BOLD, 25));

        Text tableHead = new Text("Player | Score");
        tableHead.setFill(Paint.valueOf("WHITE"));
        tableHead.setTextOrigin(VPos.BOTTOM);
        tableHead.setTextAlignment(TextAlignment.CENTER);
        tableHead.setFont(Font.font(java.awt.Font.MONOSPACED, FontWeight.BOLD, 18));
        //tableHead.setFont(Font.font("Miriam Fixed", FontWeight.BOLD, 18));

        // wrap texts in VBox
        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.getChildren().add( title );
		for (int i = 0; i < 7; i++) vbox.getChildren().add( new Text(""));
        vbox.getChildren().add( tableHead );

        // wrap VBox with texts in StackPane
        StackPane scoreView = new StackPane();
        scoreView.setMinSize((FIELD_WIDTH / 3), FIELD_HEIGHT);
        scoreView.setMaxSize((FIELD_WIDTH / 3), FIELD_HEIGHT);
        scoreView.setStyle("-fx-background-color: #16161D");
        scoreView.getChildren().add(vbox);
        StackPane.setAlignment(vbox, Pos.CENTER);
        StackPane.setAlignment(scoreView, Pos.CENTER_RIGHT);

        // set up for pause display
        pauseScreen = new StackPane();
        pauseScreen.setOpacity(100.00);
        // make sure it covers the entire screen
        pauseScreen.setMinSize(100000, 100000);
        pauseScreen.setMaxSize(100000, 100000);
        // make background black, but see-through-ish
        pauseScreen.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");
        StackPane.setAlignment(pauseScreen, Pos.CENTER);

        // add text field to pause screen
        pauseText = new Text("PAUSE");
        pauseText.setTextOrigin(VPos.CENTER);
        pauseText.setFill(Paint.valueOf("WHITE"));
        pauseText.setTextAlignment(TextAlignment.CENTER);
        pauseText.setFont(Font.font("System", FontWeight.BOLD, 50));

        // wrap text in VBox
        VBox vboxPause = new VBox(5);
        vboxPause.setAlignment(Pos.CENTER);
        vboxPause.getChildren().add(pauseText);

        // add VBox with text to pauseScreen
        pauseScreen.getChildren().add(vboxPause);
        StackPane.setAlignment(vboxPause, Pos.CENTER);

        // setup screen
        StackPane settings = new StackPane();
        settings.setOpacity(100.00);
        // make sure it covers the entire screen
        settings.setMinSize(100000, 100000);
        settings.setMaxSize(100000, 100000);
        // make background black, but see-through-ish
        settings.setStyle("-fx-background-color: rgba(0, 0, 0, 1);");
        StackPane.setAlignment(settings, Pos.CENTER);

        // settings stuff
        // add text field to pause screen
        settingsText = new Text("PAUSE");
        settingsText.setTextOrigin(VPos.CENTER);
        settingsText.setFill(Paint.valueOf("WHITE"));
        settingsText.setTextAlignment(TextAlignment.CENTER);
        settingsText.setFont(Font.font("System", FontWeight.BOLD, 50));

        // wrap text in VBox
        VBox vboxSettings = new VBox(5);
        vboxSettings.setAlignment(Pos.CENTER);
        vboxSettings.getChildren().add(settingsText);

        // add VBox with text to pauseScreen
        settings.getChildren().add(vboxSettings);
        StackPane.setAlignment(vboxSettings, Pos.CENTER);

        // general layout setup
        StackPane layout = new StackPane();
        layout.setMinSize((FIELD_WIDTH * 4 / 3), FIELD_HEIGHT);
        layout.setMaxSize((FIELD_WIDTH * 4 / 3), FIELD_HEIGHT);
        layout.getChildren().addAll(gameWrapper, scoreView, pauseScreen, settings);
        StackPane.setAlignment(layout, Pos.CENTER);

        // putting it all together
        StackPane root = new StackPane();
        root.getChildren().add(layout);
        root.setStyle("-fx-padding: 20px; -fx-background-color: #111");
        StackPane.setAlignment(root, Pos.CENTER);

        // add layout to scene
        rootScene = new Scene(root);
        // add key listener for input
        rootScene.setOnKeyTyped(event -> typedKeys.add(event.getCode()));
        rootScene.setOnKeyReleased(event -> keyStack.remove(event.getCode()));
        rootScene.setOnKeyPressed(event -> {
            if (!keyStack.contains(event.getCode())) keyStack.push(event.getCode());
        });

        // display it
        rootStage.setScene(rootScene);
        rootStage.show();

        // temp players for testing until settings screen is implemented
        players[0] = new Player("Test", Color.BLUE, new Point(250, 250), 0, KeyCode.LEFT, KeyCode.RIGHT);
        players[1] = new Player("Test", Color.GOLD, new Point(125, 375), 180, KeyCode.A, KeyCode.D);
    }

    void setup() {
        System.out.println(1);
        setup = true;

        String currentSettingsText = "ENTER NAME FOR PLAYER ONE:\n";

        enteringSring = true;
        allowNext = false;
        settingsText.setText(currentSettingsText);

        while (true) {
            if (typedKeys.isEmpty()) continue;
            if (typedKeys.get(typedKeys.size() - 1).equals(KeyCode.ENTER)) break;
            for (KeyCode k : typedKeys) {
                currentText += k.getName();
                typedKeys.remove(k);
            }
            settingsText.setText(currentSettingsText + currentText);
        }
        System.out.println(4);

        players[0].setName(currentText);
        currentText = "";

        currentSettingsText = "ENTER NAME FOR PLAYER TWO:\n";

        enteringSring = true;
        allowNext = false;
        settingsText.setText(currentSettingsText);
/*
        while (!allowNext) {
            settingsText.setText(currentSettingsText + currentText);
            timer = new Timer();
            Timer finalTimer2 = timer;
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer2.cancel();
                }
            }, 0, 200);
        }

        players[1].setName(currentText);
        currentText = "";

        currentSettingsText = "ENTER NAME FOR PLAYER THREE:\n";

        enteringSring = true;
        allowNext = false;
        settingsText.setText(currentSettingsText);

        while (!allowNext) {
            settingsText.setText(currentSettingsText + currentText);
            timer = new Timer();
            Timer finalTimer3 = timer;
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer3.cancel();
                }
            }, 0, 200);
        }

        players[2].setName(currentText);
        currentText = "";

        currentSettingsText = "ENTER NAME FOR PLAYER FOUR:\n";

        enteringSring = true;
        allowNext = false;
        settingsText.setText(currentSettingsText);

        while (!allowNext) {
            settingsText.setText(currentSettingsText + currentText);
            timer = new Timer();
            Timer finalTimer4 = timer;
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer4.cancel();
                }
            }, 0, 200);
        }

        players[3].setName(currentText);
        currentText = "";

        allowNext = false;
        currentSettingsText = "PRESS RIGHT KEY FOR PLAYER ONE";
        settingsText.setText(currentSettingsText);
        players[0].setRightKeyCode(currentKey);
        while (!allowNext) timer = new Timer();
        Timer finalTimer5 = timer;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (allowNext) finalTimer5.cancel();
            }
        }, 0, 200);
        allowNext = false;
        currentSettingsText = "PRESS LEFT KEY FOR PLAYER ONE";
        settingsText.setText(currentSettingsText);
        while (!allowNext) timer = new Timer();
        Timer finalTimer6 = timer;
        timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer6.cancel();
                }
            }, 0, 200);
        players[0].setLeftKeyCode(currentKey);

        allowNext = false;
        currentSettingsText = "PRESS RIGHT KEY FOR PLAYER TWO";
        settingsText.setText(currentSettingsText);
        players[1].setRightKeyCode(currentKey);
        while (!allowNext) timer = new Timer();
        Timer finalTimer12 = timer;
        timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer12.cancel();
                }
            }, 0, 200);
        allowNext = false;
        currentSettingsText = "PRESS LEFT KEY FOR PLAYER TWO";
        settingsText.setText(currentSettingsText);
        while (!allowNext) timer = new Timer();
        Timer finalTimer7 = timer;
        timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer7.cancel();
                }
            }, 0, 200);
        players[1].setLeftKeyCode(currentKey);

        allowNext = false;
        currentSettingsText = "PRESS RIGHT KEY FOR PLAYER THREE";
        settingsText.setText(currentSettingsText);
        players[2].setRightKeyCode(currentKey);
        while (!allowNext) timer = new Timer();
        Timer finalTimer8 = timer;
        timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer8.cancel();
                }
            }, 0, 200);
        allowNext = false;
        currentSettingsText = "PRESS LEFT KEY FOR PLAYER THREE";
        settingsText.setText(currentSettingsText);
        while (!allowNext) timer = new Timer();
        Timer finalTimer9 = timer;
        timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer9.cancel();
                }
            }, 0, 200);
        players[2].setLeftKeyCode(currentKey);

        allowNext = false;
        currentSettingsText = "PRESS RIGHT KEY FOR PLAYER FOUR";
        settingsText.setText(currentSettingsText);
        players[3].setRightKeyCode(currentKey);
        while (!allowNext) timer = new Timer();
        Timer finalTimer10 = timer;
        timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer10.cancel();
                }
            }, 0, 200);
        allowNext = false;
        currentSettingsText = "PRESS LEFT KEY FOR PLAYER FOUR";
        settingsText.setText(currentSettingsText);
        while (!allowNext) timer = new Timer();
        Timer finalTimer11 = timer;
        timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateInputs();
                    if (allowNext) finalTimer11.cancel();
                }
            }, 0, 200);
        players[3].setLeftKeyCode(currentKey);

        setup = false;
        */
    }

    void update() {
        updateInputs();
        if (!pause) {		
            Platform.runLater(this::updateGame);
		}
    }

    private void updateGame() {
		WritableImage temp = new WritableImage(FIELD_WIDTH, FIELD_HEIGHT);
		fieldCanvas.getCanvas().snapshot(new SnapshotParameters(), temp);
        for (Player p : players) {
			if (!p.getAlive()) continue;
			// update player
			p.move();

            // add random breaks in lines
            if (p.updateDrawCount() > (p.getDraw() ? 50 : 25)) {
                if (Math.random() < (p.getDraw() ? 0.10 : 0.66)) {
                    p.setDraw(!p.getDraw());
                    p.setDrawCount(0);
                } else p.setDrawCount(1); // yes, one
            }

			Point oldPos = p.getOldPosition();
			Point newPos = p.getNewPosition();

			if (!TELEPORT_ON_EDGE) {
				p.setAlive(!(newPos.x < 0 || newPos.x >= FIELD_WIDTH || newPos.y < 0 || newPos.y >= FIELD_HEIGHT));
			}

			if (p.getAlive() && p.getDraw()) {
					PixelReader reader = temp.getPixelReader();

					int testX = (int) Math.round(p.getVelocity().x * p.getLineWidth()*0.65 + newPos.x);
					int testY = (int) Math.round(p.getVelocity().y * p.getLineWidth()*0.65 + newPos.y);

					if (testX < 0) testX = FIELD_WIDTH + testX;
					else if (testX >= FIELD_WIDTH) testX = testX - FIELD_WIDTH;

					if (testX < 0) testY = FIELD_HEIGHT + testY;
					else if (testY >= FIELD_HEIGHT) testY = testY - FIELD_HEIGHT;

					String pixelColor = reader.getColor(testX, testY).toString();

					p.setAlive(pixelColor.equals("0xffffffff") || pixelColor.equals("0x000000ff"));
			}

			if (!p.getAlive()) {
				// TODO: Eliminated player, calc and update score
			}

			fieldCanvas.setStroke(p.getColor());
			fieldCanvas.setLineWidth(p.getLineWidth());

			// draw update
			if (p.getDraw()) {
				fieldCanvas.strokeLine(oldPos.x, oldPos.y, newPos.x, newPos.y);
			} else {
			    // TODO: Still removing other lines as well
			    if (p.getDrawCount() == 0) { // just updated
                    fieldCanvas.setStroke(Color.BLACK);

                    int tempX = (-(int) Math.ceil(p.getLineWidth()/2.0)) + (int) oldPos.x;
                    int tempY = (-(int) Math.ceil(p.getLineWidth()/2.0)) + (int) oldPos.y;

                    if (tempX < 0) tempX = FIELD_WIDTH + tempX;
                    else if (tempX >= FIELD_WIDTH) tempX = tempX - FIELD_WIDTH;

                    if (tempY < 0) tempY = FIELD_HEIGHT + tempY;
                    else if (tempY >= FIELD_HEIGHT) tempY = tempY - FIELD_HEIGHT;

                    fieldCanvas.strokeRect(tempX, tempY, p.getLineWidth(), p.getLineWidth());
                }

                Color savedSquare[][] = p.getSavedSquare();

                for (int x = 0; x < savedSquare.length; x++) {
                    for (int y = 0; y < savedSquare[x].length; y++) {

                        int tempX = (int) Math.ceil(1.5* (-Math.ceil(p.getLineWidth() + 1/2.0) + x + oldPos.x));
                        int tempY = (int) Math.ceil(1.5* (-Math.ceil(p.getLineWidth() + 1/2.0) + y + oldPos.y));

                        if (tempX < 0) tempX = FIELD_WIDTH + tempX;
                        else if (tempX >= FIELD_WIDTH) tempX = tempX - FIELD_WIDTH;

                        if (tempY < 0) tempY = FIELD_HEIGHT + tempY;
                        else if (tempY >= FIELD_HEIGHT) tempY = tempY - FIELD_HEIGHT;

                        fieldCanvas.setStroke(savedSquare[x][y]);
                        fieldCanvas.fillRect(tempX, tempY, 1, 1);
                    }
                }

				PixelReader reader = temp.getPixelReader();
				savedSquare = p.getSavedSquare();

                for (int x = 0; x < savedSquare.length; x++) {
                    for (int y = 0; y < savedSquare[x].length; y++) {

                        int tempX = (int) Math.ceil(1.5* (-Math.ceil(p.getLineWidth() + 1/2.0) + x + newPos.x));
                        int tempY = (int) Math.ceil(1.5* (-Math.ceil(p.getLineWidth() + 1/2.0) + y + newPos.y));

                        if (tempX < 0) tempX = FIELD_WIDTH + tempX;
                        else if (tempX >= FIELD_WIDTH) tempX = tempX - FIELD_WIDTH;

                        if (tempY < 0) tempY = FIELD_HEIGHT + tempY;
                        else if (tempY >= FIELD_HEIGHT) tempY = tempY - FIELD_HEIGHT;

                        savedSquare[x][y] = reader.getColor(tempX, tempY);
                    }
                }
                p.setSavedSquare(savedSquare);

                /*fieldCanvas.setLineWidth(p.getLineWidth() + 1);
                fieldCanvas.setStroke(Color.BLACK);
                fieldCanvas.strokeLine(oldPos.x, oldPos.y, oldPos.x, oldPos.y);
                fieldCanvas.setLineWidth(p.getLineWidth());*/

                fieldCanvas.setStroke(p.getColor());
                fieldCanvas.strokeLine(newPos.x, newPos.y, newPos.x, newPos.y);
			}

			if (TELEPORT_ON_EDGE) {
				p.setNewPosition(new Point(newPos.x < 0 ? FIELD_WIDTH : newPos.x >= FIELD_WIDTH ? 0 : newPos.x,
						newPos.y < 0 ? FIELD_HEIGHT : newPos.y >= FIELD_HEIGHT ? 0 : newPos.y));
			}
        }
    }

    private void updateInputs() {
        if (!keyStack.isEmpty() && System.currentTimeMillis() - lastKeyUpdate > KEY_UPDATE_DELAY) {
            for (KeyCode temp : keyStack) {
                if (setup) {
                    if (enteringSring) {
                        if (temp.equals(KeyCode.ENTER)) {
                            enteringSring = false;
                        } else if (temp.equals(KeyCode.BACK_SPACE)) {
                            currentText = currentText.substring(0, currentText.length() - 2 > -1 ? currentText.length() - 2 : 0);
                        } else if (temp.isDigitKey() || temp.isLetterKey()) {
                            currentText += temp.getName();
                        }
                    } else if (!temp.equals(KeyCode.ESCAPE) && !temp.equals(KeyCode.SPACE)) {
                        boolean isUsed = false;
                        for (Player p : players) {
                            if (temp.equals(p.getLeftKeyCode()) || temp.equals(p.getRightKeyCode())) {
                                isUsed = true;
                                break;
                            }
                        }
                        if (!isUsed) {
                            currentKey = temp;
                        }
                    }
                    allowNext = true;
                    continue;
                }

                if (pause) {
                    doNotMindMe += temp.getName();
                    if (doNotMindMe.toLowerCase().contains("upupdowndownleftrightleftrightba")) {
                        doNotMindMe = "";
                        // easter egg code goes here
                        Media sound = new Media(new File("CurveFeverClone/resources/secret.mp3").toURI().toString());
                        MediaPlayer mediaPlayer = new MediaPlayer(sound);
                        mediaPlayer.setVolume(.5);
                        mediaPlayer.play();
                    } else if (doNotMindMe.toLowerCase().contains("resetplz")) {
                        // less an easter egg more an dev tool ^^
                        // reset to empty field, move players to distinguished locations
                        fieldCanvas.getCanvas().getGraphicsContext2D().setFill(Color.BLACK);
                        fieldCanvas.getCanvas().getGraphicsContext2D().fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

                        int x = 100;
                        int y = 200;
                        int i = 0;
                        for (Player p : players) {
                            i++;
                            p.setNewPosition(new Point(x * i, y * i));
                            p.setAlive(true);
                        }
                    }
                } else {
                    doNotMindMe = "";
                }

                if (temp.equals(KeyCode.F11)) {
                    // fullscreen toggle
                    Platform.runLater(() -> rootStage.setFullScreen(!rootStage.isFullScreen()));
                    updatePause(true); // pause on fullscreen toggle
                    keyStack.remove(temp);
                    continue;
                } else if (temp.equals(KeyCode.SPACE)) {
                    // pause toggle
                    updatePause(!pause);
                    keyStack.remove(temp);
                    continue;
                } else if (temp.equals(KeyCode.ESCAPE)) {
                    if (pause) System.exit(0);
                    updatePause(true); // pause on fullscreen toggle
                    continue;
                }

                if (pause) continue;

                // check whether a key is a turn key of one of the players
                for (Player p : players) {
                    if (p.getLeftKeyCode().equals(temp)) {
                        p.turnLeft();
                    } else if (p.getRightKeyCode().equals(temp)) {
                        p.turnRight();
                    }
                }
            }
            lastKeyUpdate = System.currentTimeMillis() + (pause ? 200 : 0);
        }
    }

    private void updatePause(boolean _pause) {
        if (pause != _pause && !starting) {
            if (!_pause) {
                starting = true;
                try {
                    pauseText.setText("3");
                    Thread.sleep(1000);
                    pauseText.setText("2");
                    Thread.sleep(1000);
                    pauseText.setText("1");
                    Thread.sleep(1000);
                    pauseText.setText("LOS!");
                    Thread.sleep(250);
                    pauseScreen.setOpacity(0);
                    pauseText.setText("PAUSE");
                    pause = false;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                starting = false;
            }
            pauseScreen.setOpacity(_pause ? 100 : 0);
            pause = _pause;
        }
    }
}
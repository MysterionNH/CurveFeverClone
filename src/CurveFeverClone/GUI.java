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
import java.util.LinkedList;

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
    private boolean pause = true;
    private boolean starting = false;
    // the players (currently maxed to four)
    private Player[] players = {new Player(), new Player(), new Player(), new Player()};
    // everything input related
    private long lastKeyUpdate;
    private String doNotMindMe = "";
    // gui control
    private Text pauseText;
    private Scene rootScene;
    private Stage rootStage;
    private StackPane pauseScreen;
    private GraphicsContext fieldCanvas;

    private boolean[][] field = new boolean[FIELD_WIDTH][FIELD_HEIGHT];

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

        // general layout setup
        StackPane layout = new StackPane();
        layout.setMinSize((FIELD_WIDTH * 4 / 3), FIELD_HEIGHT);
        layout.setMaxSize((FIELD_WIDTH * 4 / 3), FIELD_HEIGHT);
        layout.getChildren().addAll(gameWrapper, scoreView, pauseScreen);
        StackPane.setAlignment(layout, Pos.CENTER);

        // putting it all together
        StackPane root = new StackPane();
        root.getChildren().add(layout);
        root.setStyle("-fx-padding: 20px; -fx-background-color: #111");
        StackPane.setAlignment(root, Pos.CENTER);

        // add layout to scene
        rootScene = new Scene(root);
        // add key listener for input
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

					//p.setAlive(pixelColor.equals("0xffffffff") || pixelColor.equals("0x000000ff"));
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
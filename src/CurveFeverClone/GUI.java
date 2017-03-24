package CurveFeverClone;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
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
    private static final int FIELD_WIDTH = 700;
    private static final int FIELD_HEIGHT = FIELD_WIDTH;
    private static final double FOUR_THIRDS = 1.33333333;
    private static final int KEY_UPDATE_DELAY = 50;
    private static final boolean TELEPORT_ON_EDGE = true;
    private final LinkedList<KeyCode> keyStack = new LinkedList<>();
    // start paused
    private boolean pause = true;
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
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        // experiments regarding the score table
        /*TableView<Player> table = new TableView<Player>();
        final ObservableList<Player> data =
        FXCollections.observableArrayList(players[0], players[1], players[2], players[3]);
		
        table.setEditable(false);
		
		table.setMinSize(166.667, 300);
        table.setMaxSize(166.667, 300);
		
        TableColumn playerCol = new TableColumn("Player");
        playerCol.setMinWidth(82);
		playerCol.setMaxWidth(82);
        playerCol.setCellValueFactory( new PropertyValueFactory<Player, String>("name") );

        TableColumn scoreCol = new TableColumn("Score");
        scoreCol.setMinWidth(82);
		scoreCol.setMaxWidth(82);
        scoreCol.setCellValueFactory( new PropertyValueFactory<Player, String>("score") );
 
        playerCol.setStyle("-fx-background-color: #16161D");
        scoreCol.setStyle("-fx-background-color: #16161D");
        table.setStyle("-fx-background-color: #16161D");
 
        table.setItems(data);
        table.getColumns().addAll(playerCol, scoreCol);*/

        // wrap texts in VBox
        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.getChildren().addAll(title);
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
        pauseScreen.setOpacity(pause ? 100 : 0);
        updateInputs();
        if (!pause) updateGame();
    }

    private void updateGame() {
        for (Player p : players) {
            if (!p.getAlive()) continue;
            // update player
            p.move();

            // collision detection
            // TODO: Implement a better (real) version
            boolean collided = false;

            Point oldPos = p.getOldPosition();
            Point newPos = p.getNewPosition();

            if (!TELEPORT_ON_EDGE) {
                collided = (newPos.x < 0 || newPos.x >= FIELD_WIDTH || newPos.y < 0 || newPos.y >= FIELD_HEIGHT);
            }

            /*if (!collided) {
                if ((int) newPos.x != (int) oldPos.x && (int) newPos.y != (int) oldPos.y) {
                    WritableImage temp = new WritableImage(FIELD_WIDTH, FIELD_HEIGHT);
                    Platform.runLater(() -> fieldCanvas.getCanvas().snapshot(null, temp));
                    PixelReader reader = temp.getPixelReader();
                    // TODO: This goes out of bounds sometimes, crashing the application
                    //       needs only a small check against 0 and FIELD_WIDTH/HEIGHT
                    Color tempColor = reader.getColor((int)( newPos.x + (p.getVelocity().x * 2)), (int)( newPos.y + (p.getVelocity().y * 2)));
					System.out.println(tempColor);
					collided = !(tempColor.equals(Color.BLACK) || tempColor.equals(Color.TRANSPARENT));
					System.out.println(collided);
					
					/*if (newPos.x > oldPos.x) {
						if (newPos.y > oldPos.y) {
							tempColor = reader.getColor((int) newPos.x+3, (int) newPos.y+3);
						}
						else {
							tempColor = reader.getColor((int) newPos.x+3, (int) newPos.y-3);
						}
					}
					else {
						if (newPos.y > oldPos.y) {
							tempColor = reader.getColor((int) newPos.x-3, (int) newPos.y+3);
						}
						else {
							tempColor = reader.getColor((int) newPos.x-3, (int) newPos.y-3);
						}
					}*/

            // TODO: this still detects itself:
            //collided = !(tempColor == Color.BLACK || tempColor == Color.TRANSPARENT);
                    /*
                    for (double x = (newPos.x > oldPos.x ? oldPos.x : newPos.x);
                         x < (newPos.x > oldPos.x ? newPos.x : oldPos.x); x = (newPos.x > oldPos.x ? x + 1 : x - 1)) {
                        for (double y = (newPos.y > oldPos.y ? oldPos.y : newPos.y);
                             y < (newPos.y > oldPos.y ? newPos.y : oldPos.y); y = (newPos.y > oldPos.y ? y + 1 : y - 1)) {
                                Color tempColor = reader.getColor((int) x, (int) y);
                                if (tempColor == Color.BLACK || tempColor == Color.TRANSPARENT) continue;
                                collided = true;
                                break;
                        }
                        if (collided) break;
                    }
                    ///
                }
            }*/
            if (collided) {
                // TODO: Eliminated player, calc and update score
                p.setAlive(false);
            }

            // add random breaks in lines
            if (p.updateDrawCount() > (p.getDraw() ? 50 : 25)) {
                if (Math.random() < (p.getDraw() ? 0.10 : 0.66)) {
                    p.setDraw(!p.getDraw());
                    p.setDrawCount(0);
                } else p.setDrawCount(0);
            }

            fieldCanvas.setStroke(p.getColor());
            fieldCanvas.setLineWidth(p.getLineWidth());

            // draw update
            if (p.getDraw()) {
                fieldCanvas.strokeLine(oldPos.x, oldPos.y, newPos.x, newPos.y);
            } else {
                // TODO: fix this mess
                WritableImage temp = new WritableImage(FIELD_WIDTH, FIELD_HEIGHT);
                Platform.runLater(() -> fieldCanvas.getCanvas().snapshot(null, temp));
                PixelReader reader = temp.getPixelReader();

                boolean differentColor = false;

                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        int finalX = x;
                        int finalY = y;

                        if ((int)p.getOldPosition().x + finalX >= FIELD_WIDTH ||
                                (int)p.getOldPosition().x + finalX < 0 ||
                                (int)p.getOldPosition().y + finalY >= FIELD_WIDTH ||
                                (int)p.getOldPosition().y + finalY < 0) continue;

                        final boolean[] tempB = new boolean[1];
                        Platform.runLater(() -> tempB[0] = reader.getColor((int)p.getOldPosition().x + finalX,
                                (int)p.getOldPosition().y + finalY).equals(Color.BLACK));
                        if (tempB[0]){
                            differentColor = true;
                            break;
                        }
                    }
                    if (differentColor) break;
                }

                fieldCanvas.setLineWidth(p.getLineWidth() + 1);
                fieldCanvas.setStroke(differentColor ? Color.TRANSPARENT : Color.BLACK);
                fieldCanvas.strokeLine(oldPos.x, oldPos.y, oldPos.x, oldPos.y);
                fieldCanvas.setLineWidth(p.getLineWidth());
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
                        fieldCanvas.getCanvas().getGraphicsContext2D().fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

                        int x = 100;
                        int y = 200;
                        int i = 0;
                        for (Player p : players) {
                            i++;
                            p.setNewPosition(new Point(x * i, y * i));
                        }
                    }
                } else {
                    doNotMindMe = "";
                }

                if (temp.equals(KeyCode.F11)) {
                    // fullscreen toggle
                    Platform.runLater(() -> rootStage.setFullScreen(!rootStage.isFullScreen()));
                    pause = true; // pause on fullscreen toggle
                    keyStack.remove(temp);
                    continue;
                } else if (temp.equals(KeyCode.SPACE)) {
                    // pause toggle
                    pause = !pause;
                    keyStack.remove(temp);
                    continue;
                } else if (temp.equals(KeyCode.ESCAPE)) {
                    if (pause) System.exit(0);
                    pause = true;
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
}
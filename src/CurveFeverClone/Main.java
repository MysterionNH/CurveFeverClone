package CurveFeverClone;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        GUI gui = new GUI(stage);
        Platform.runLater(gui::setup);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gui.update();
            }
        }, 0, 16); // every 16 ms
    }
}

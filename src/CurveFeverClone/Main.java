package CurveFeverClone;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    static int TICK_SPEED = 16; // every 16 ms, approximately 62.5 fps

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        GUI gui = new GUI(stage);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gui.update();
            }
        }, 0, TICK_SPEED);
    }
}

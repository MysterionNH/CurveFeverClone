package CurveFeverClone;

import java.util.Timer;
import java.util.TimerTask;

import javafx.stage.Stage;
import javafx.application.Application;

public class Main extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        GUI gui = new GUI(stage);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gui.update();
            }
        }, 0, 15); // every 15 ms
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class AccountStatusChecker {
    private static final Duration CHECK_INTERVAL = Duration.seconds(30);

    public static void startChecking() {
        Timeline timeline = new Timeline(
                new KeyFrame(CHECK_INTERVAL, event -> {
                    SessionManager.getInstance().checkAccountStatus();
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
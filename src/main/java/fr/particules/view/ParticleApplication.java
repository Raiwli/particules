package fr.particules.view;

import fr.particules.model.Particle;
import fr.particules.model.ParticleSystem;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class ParticleApplication extends Application {
    private static final Color BACKGROUND = Color.rgb(14, 16, 22);

    private ParticleSystem particleSystem;
    private Canvas canvas;
    private boolean paused;
    private long previousFrame;
    private long collisionCount;

    @Override
    public void start(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        canvas = new Canvas(bounds.getWidth(), bounds.getHeight());
        particleSystem = new ParticleSystem(bounds.getWidth(), bounds.getHeight());
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight(), BACKGROUND);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            } else if (event.getCode() == KeyCode.SPACE) {
                paused = !paused;
                previousFrame = 0L;
            }
        });
        stage.setTitle("Particules");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (previousFrame != 0L && !paused) {
                    double elapsedSeconds = Math.min((now - previousFrame) / 1_000_000_000.0, 0.05);
                    collisionCount += particleSystem.step(elapsedSeconds);
                }
                previousFrame = now;
                draw();
            }
        }.start();
    }

    private void draw() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(BACKGROUND);
        graphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Particle particle : particleSystem.getParticles()) {
            int rgb = particle.getColor();
            graphics.setFill(Color.rgb((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff));
            double diameter = particle.getRadius() * 2.0;
            graphics.fillOval(particle.getX() - particle.getRadius(),
                    particle.getY() - particle.getRadius(), diameter, diameter);
        }
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font(18));
        graphics.fillText("Collisions : " + collisionCount, 24, 32);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

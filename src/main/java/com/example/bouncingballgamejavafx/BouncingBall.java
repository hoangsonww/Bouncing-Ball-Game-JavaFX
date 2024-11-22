package com.example.bouncingballgamejavafx;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Random;

public class BouncingBall extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double BALL_RADIUS = 20;
    private static final double SPEED_INCREMENT = 0.2;

    private double ballSpeedX = 3;
    private double ballSpeedY = 3;
    private double ballX = WIDTH / 2;
    private double ballY = HEIGHT / 2;

    private final Random random = new Random();
    private Color ballColor = Color.RED;

    private int score = 0;
    private boolean paused = false;
    private int targetScore = 20 + random.nextInt(31); // Random target score between 20 and 50
    private double maxSpeed = 10 + random.nextInt(6); // Random max speed between 10 and 15

    private final Label speedLabel = new Label();
    private final Label scoreLabel = new Label();
    private final Label maxSpeedLabel = new Label();
    private Scene welcomeScene;
    private Scene gameScene;
    private Scene endScene;

    private AnimationTimer gameLoop;
    private Image ballImage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Load the ball image
        ballImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ball.png")));

        // Set the application icon
        primaryStage.getIcons().add(ballImage);

        // Create the scenes
        welcomeScene = createWelcomeScene(primaryStage);
        gameScene = createGameScene(primaryStage);

        // Set the welcome scene initially
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Bouncing Ball Game");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Scene createWelcomeScene(Stage stage) {
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(30);
        layout.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 50px;");

        Label title = new Label("Welcome to the Bouncing Ball Game");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        Label instructions = new Label(String.format(
          "Win by reaching a score of %d!\nDon't exceed the max speed of %.2f.",
          targetScore, maxSpeed));
        instructions.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        instructions.setTextFill(Color.LIGHTGRAY);
        instructions.setWrapText(true);
        instructions.setAlignment(Pos.CENTER);

        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10px 20px;");
        startButton.setOnAction(event -> stage.setScene(gameScene));

        Button exitButton = new Button("Exit Game");
        exitButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10px 20px;");
        exitButton.setOnAction(event -> System.exit(0));

        layout.getChildren().addAll(title, instructions, startButton, exitButton);

        return new Scene(layout, WIDTH, HEIGHT);
    }

    private Scene createGameScene(Stage stage) {
        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);

        // Speed Label
        speedLabel.setTranslateX(10);
        speedLabel.setTranslateY(10);
        speedLabel.setTextFill(Color.RED);
        speedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Score Label
        scoreLabel.setTranslateX(10);
        scoreLabel.setTranslateY(40);
        scoreLabel.setTextFill(Color.GREEN);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Max Speed Label
        maxSpeedLabel.setTranslateX(10);
        maxSpeedLabel.setTranslateY(70);
        maxSpeedLabel.setTextFill(Color.ORANGE);
        maxSpeedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        maxSpeedLabel.setText(String.format("Max Speed: %.2f", maxSpeed));

        root.getChildren().addAll(speedLabel, scoreLabel, maxSpeedLabel);
        updateSpeedLabel();
        updateScoreLabel();

        // Game Loop
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!paused) {
                    gc.clearRect(0, 0, WIDTH, HEIGHT);

                    // Draw the ball image tinted with the current ball color
                    gc.setFill(ballColor);
                    gc.fillOval(ballX - BALL_RADIUS, ballY - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
                    gc.setGlobalAlpha(0.8); // Tint effect
                    gc.drawImage(ballImage, ballX - BALL_RADIUS, ballY - BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
                    gc.setGlobalAlpha(1.0); // Reset alpha

                    // Update ball position
                    ballX += ballSpeedX;
                    ballY += ballSpeedY;

                    // Bounce logic
                    if (ballX - BALL_RADIUS < 0 || ballX + BALL_RADIUS > WIDTH) {
                        ballSpeedX = -ballSpeedX;
                        ballColor = randomColor();
                        score++;
                        updateScoreLabel();
                    }
                    if (ballY - BALL_RADIUS < 0 || ballY + BALL_RADIUS > HEIGHT) {
                        ballSpeedY = -ballSpeedY;
                        ballColor = randomColor();
                        score++;
                        updateScoreLabel();
                    }

                    // Check win condition
                    if (score >= targetScore) {
                        gameLoop.stop();
                        stage.setScene(createEndScene(stage, true));
                    }

                    // Check game-over condition
                    double speed = Math.sqrt(ballSpeedX * ballSpeedX + ballSpeedY * ballSpeedY);
                    if (speed > maxSpeed) {
                        gameLoop.stop();
                        stage.setScene(createEndScene(stage, false));
                    }
                }
            }
        };

        gameLoop.start();

        // Key Events
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.UP) {
                ballSpeedX *= (1 + SPEED_INCREMENT);
                ballSpeedY *= (1 + SPEED_INCREMENT);
                updateSpeedLabel();
            } else if (event.getCode() == KeyCode.DOWN) {
                ballSpeedX *= (1 - SPEED_INCREMENT);
                ballSpeedY *= (1 - SPEED_INCREMENT);
                updateSpeedLabel();
            } else if (event.getCode() == KeyCode.SPACE) {
                paused = !paused;
            }
        });

        return scene;
    }

    private Scene createEndScene(Stage stage, boolean win) {
        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(30);
        layout.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 50px;");

        Label message = new Label(win ? "You Win!" : "Game Over!");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        message.setTextFill(win ? Color.GREEN : Color.RED);

        Label finalScoreLabel = new Label(String.format("Final Score: %d / %d", score, targetScore));
        finalScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        finalScoreLabel.setTextFill(Color.WHITE);

        Button restartButton = new Button("Restart Game");
        restartButton.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10px 20px;");
        restartButton.setOnAction(event -> {
            resetGame();
            stage.setScene(gameScene);
        });

        Button exitButton = new Button("Exit Game");
        exitButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10px 20px;");
        exitButton.setOnAction(event -> System.exit(0));

        layout.getChildren().addAll(message, finalScoreLabel, restartButton, exitButton);

        return new Scene(layout, WIDTH, HEIGHT);
    }

    private void resetGame() {
        score = 0;
        ballSpeedX = 3;
        ballSpeedY = 3;
        ballX = WIDTH / 2;
        ballY = HEIGHT / 2;
        paused = false;
        targetScore = 20 + random.nextInt(31); // New random target score
        maxSpeed = 10 + random.nextInt(6); // New random max speed
        maxSpeedLabel.setText(String.format("Max Speed: %.2f", maxSpeed));
        updateSpeedLabel();
        updateScoreLabel();
        gameLoop.start();
    }

    private Color randomColor() {
        return Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private void updateSpeedLabel() {
        double speed = Math.sqrt(ballSpeedX * ballSpeedX + ballSpeedY * ballSpeedY);
        speedLabel.setText(String.format("Speed: %.2f", speed));
    }

    private void updateScoreLabel() {
        scoreLabel.setText(String.format("Score: %d / %d", score, targetScore));
    }
}
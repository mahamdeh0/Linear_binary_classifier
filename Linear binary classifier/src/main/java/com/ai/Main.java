package com.ai;

import com.jfoenix.controls.*;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    private JFXComboBox<Character> classComboBox;
    private JFXTextField learningRateField;
    private JFXTextField maxIterationsField;
    private JFXButton trainButton;
    private JFXButton resetButton;
    private Label classLabel;
    private Label learningRateLabel;
    private Label maxIterationsLabel;
    private Label performanceLabel;
    private Label performance;
    private HBox titleBar;
    private VBox resultBox;
    private VBox performanceBox;
    private StackPane bottomBar;
    private StackPane spacing;
    private StackPane main;

    private PerceptronAlgorithm perceptron;
    private Canvas canvas;
    private List<Point> points = new ArrayList<>();
    private ArrayList<String> classes = new ArrayList<>();

    
    @Override
    public void start(Stage primaryStage) {
        titleBar = new HBox();
        titleBar.setStyle("-fx-background-color: transparent; -fx-background-radius: 18px 18px 0 0; ; -fx-padding: 5px;");
        titleBar.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = new Button("\u274C");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14;");
        closeButton.setOnMouseEntered(event -> closeButton.setStyle("-fx-background-color: #e81123; -fx-text-fill: white; -fx-font-size: 14;"));
        closeButton.setOnMouseExited(event -> closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14;"));

        closeButton.setOnAction(event -> {
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(0.8), new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 0.0))
            );
            timeline.play();
            timeline.setOnFinished(finishedEvent -> {
                primaryStage.close();
                System.exit(0);
            });
        });
        
        Button minimizeButton = new Button("\u2014");
        minimizeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14;");
        minimizeButton.setOnAction(event -> {
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(0.8), new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 0.0))
            );
            timeline.play();
            timeline.setOnFinished(finishedEvent -> {
                primaryStage.setIconified(true);
            });
        });

        primaryStage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 0.0)),
                        new KeyFrame(Duration.seconds(0.8), new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 1.0))
                );
                timeline.play();
            }

            else {
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 1.0)),
                        new KeyFrame(Duration.seconds(0.8), new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 0.0))
                );
                timeline.play();
            }
        });

        minimizeButton.setOnMouseEntered(event -> minimizeButton.setStyle("-fx-background-color: #353840; -fx-text-fill: white; -fx-font-size: 14;"));
        minimizeButton.setOnMouseExited(event -> minimizeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14;"));

     

        titleBar.getChildren().addAll(minimizeButton, closeButton);

        titleBar.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged((MouseEvent event) -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });


        classLabel = new Label("Class:");
        classComboBox = new JFXComboBox<>();
        classComboBox.getItems().addAll('A', 'B');
        classComboBox.setValue('A');
        classComboBox.setPrefWidth(100);
        classComboBox.setMaxWidth(100);
        classComboBox.setButtonCell(new ListCell<Character>() {
            @Override
            protected void updateItem(Character item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null){
                    setText(item.toString());
                    setAlignment(Pos.CENTER);
                }
            }
        });

        learningRateLabel = new Label("Learning Rate:");
        learningRateField = new JFXTextField();
        learningRateField.setAlignment(Pos.CENTER);
        learningRateField.setMaxWidth(100);
        learningRateField.setPrefWidth(100);

        maxIterationsLabel = new Label("Max Iterations:");
        maxIterationsField = new JFXTextField();
        maxIterationsField.setAlignment(Pos.CENTER);
        maxIterationsField.setMaxWidth(100);
        maxIterationsField.setPrefWidth(100);

        trainButton = new JFXButton("Train");
        trainButton.setPrefWidth(100);
        trainButton.getStyleClass().add("button-raised");
        trainButton.setOnAction(e -> {

            if (learningRateField.getText().isEmpty() && maxIterationsField.getText().isEmpty()) {
                displayAlert("No training parameters specified", "Please specify either a learning rate or a maximum number of iterations.");
                return;
            }

            if (learningRateField.getText().isEmpty()) {
                displayAlert("Learning rate not specified", "Please specify a learning rate.");
                return;
            }

            if (maxIterationsField.getText().isEmpty()) {
                displayAlert("Max iterations not specified", "Please specify the maximum number of iterations.");
                return;
            }

            if (!(classes.contains("A") && classes.contains("B"))) {
                displayAlert("Not enough points", "Please add at least one point for each class.");
                return;
            }

            train();

            Timeline fadeInTime = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(resultBox.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(resultBox.opacityProperty(), 1.0))
            );
            fadeInTime.play();

            Timeline fadeInTime2 = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(performanceBox.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(performanceBox.opacityProperty(), 1.0))
            );
            fadeInTime2.play();
        });

        resetButton = new JFXButton("Reset");
        resetButton.setPrefWidth(100);
        resetButton.getStyleClass().add("button-raised");

        resetButton.setOnAction(e -> {
            reset();

            Timeline fadeInTime = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(resultBox.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(resultBox.opacityProperty(), 1.0))
            );
            fadeInTime.play();

            Timeline fadeInTime2 = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(performanceBox.opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(performanceBox.opacityProperty(), 1.0))
            );
            fadeInTime2.play();
        } );

        canvas = new Canvas(400, 400);
        canvas.setOnMouseClicked(this::handleCanvasClick);
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        stackPane.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");
        stackPane.getChildren().add(canvas);

        HBox L = createH();

        performanceLabel = new Label("\n\nSystem Performance");
        performanceLabel.setVisible(false);
        performanceLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");
        performance = new Label("");
        performance.setWrapText(true);
        performance.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        performance.setVisible(false);

        performanceBox = new VBox();
        performanceBox.setAlignment(Pos.TOP_LEFT);
        performanceBox.setSpacing(10);
        performanceBox.getChildren().addAll(performanceLabel, performance);

        resultBox = new VBox();
        resultBox.setAlignment(Pos.TOP_CENTER);
        resultBox.setSpacing(10);
        Label gap = new Label();
        gap.setStyle("-fx-font-size: 50;");
        resultBox.getChildren().addAll(gap, stackPane, L);

        GridPane mainContent = new GridPane();
        mainContent.setHgap(20);
        mainContent.setVgap(10);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(10));
        mainContent.getStyleClass().add("root");

        Label title = new Label("Linear Binary Classifier");
        title.setStyle("-fx-font-size: 20; -fx-text-fill: white; -fx-font-weight: bold;");

        mainContent.add(title, 0, 0, 2, 1);
        mainContent.add(classLabel, 0, 2);
        mainContent.add(classComboBox, 1, 2);
        mainContent.add(learningRateLabel, 0, 3);
        mainContent.add(learningRateField, 1, 3);
        mainContent.add(maxIterationsLabel, 0, 4);
        mainContent.add(maxIterationsField, 1, 4);        
        mainContent.add(trainButton, 0, 6);
        mainContent.add(resetButton, 0, 7);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.TOP_LEFT);
        vBox.setSpacing(10);
        vBox.getChildren().addAll(mainContent, performanceBox);

        spacing = new StackPane();
        spacing.setPrefSize(50, 50);
        spacing.setAlignment(Pos.CENTER);

        HBox box = new HBox();
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(30);
        box.getChildren().addAll(vBox, spacing, resultBox);

        main = new StackPane();
        main.getChildren().addAll(box);




        StackPane spacingH = new StackPane();
        spacingH.setPrefWidth(500);
        spacingH.setMinWidth(500);
        spacingH.setAlignment(Pos.CENTER);

        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.BOTTOM_CENTER);
        bottomBox.setSpacing(10);



        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        root.setCenter(main);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1000, 600);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false);
        primaryStage.setTitle("Linear Binary Classifier");
        primaryStage.show();

        primaryStage.getScene().getRoot().setOpacity(0.0);
        Timeline fadeInTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 0.0)),
                new KeyFrame(Duration.seconds(0.8), new KeyValue(primaryStage.getScene().getRoot().opacityProperty(), 1.0))
        );
        fadeInTimeline.play();
    }

    private HBox createH() {
        HBox legend = new HBox(5);
        legend.setAlignment(Pos.CENTER);

        Label classALabel = new Label("Class A");
        Circle classACircle = new Circle(10, Color.TOMATO);
        HBox.setMargin(classACircle, new Insets(0, 0, 0, 0));

        Label classBLabel = new Label("Class B");
        Polygon classBTriangle = new Polygon(0, 0, 10, 20, -10, 20);
        classBTriangle.setFill(Color.DODGERBLUE);
        HBox.setMargin(classBTriangle, new Insets(0, 0, 0, 25));

        Label decisionBoundaryLabel = new Label("Decision Line");
        Line decisionBoundaryLine = new Line(0, 0, 25, 0);
        decisionBoundaryLine.setStroke(Color.ORCHID);
        decisionBoundaryLine.setStrokeWidth(4);
        HBox.setMargin(decisionBoundaryLine, new Insets(0, 0, 0, 25));
      
        legend.getChildren().addAll(classACircle, classALabel, classBTriangle, classBLabel, decisionBoundaryLine, decisionBoundaryLabel);
        return legend;
    }

    private void train() {
        double learningRate = Double.parseDouble(learningRateField.getText());
        int maxIterations = Integer.parseInt(maxIterationsField.getText());

        double[][] inputs = new double[points.size()][];
        int[] labels = new int[points.size()];

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            inputs[i] = new double[]{point.x, point.y};
            labels[i] = point.label;
        }

        perceptron = new PerceptronAlgorithm(2, learningRate);
        perceptron.train(inputs, labels, maxIterations);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawClassificationLine(gc);
        drawPoints(gc);

        performance.setText("MSE = " + perceptron.getMSE());
        performanceLabel.setVisible(true);
        performance.setVisible(true);
    }


    private void drawClassificationLine(GraphicsContext gc) {
        double[] weights = perceptron.getWeights();
        double x1 = -canvas.getWidth() / 2;
        double y1 = (-weights[0] - weights[1] * x1) / weights[2];
        double x2 = canvas.getWidth() / 2;
        double y2 = (-weights[0] - weights[1] * x2) / weights[2];

        gc.setStroke(Color.ORCHID);
        gc.setLineWidth(4);
        gc.strokeLine(x1 + canvas.getWidth() / 2, y1 + canvas.getHeight() / 2,
            x2 + canvas.getWidth() / 2, y2 + canvas.getHeight() / 2);
    }

    private void handleCanvasClick(MouseEvent event) {
        double mouseX = event.getX() - canvas.getWidth() / 2;
        double mouseY = event.getY() - canvas.getHeight() / 2;

     

        int label = (classComboBox.getValue() == 'A') ? 1 : -1;

        points.add(new Point(mouseX, mouseY, label));

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawPoints(gc);
    }

    private void drawPoints(GraphicsContext gc) {
        for (Point point : points) {
            if (point.label == 1) {
                if (!classes.contains("A")) {
                    classes.add("A");
                }

                gc.setFill(Color.TOMATO);
                gc.fillOval(point.x + canvas.getWidth() / 2 - 10, point.y + canvas.getHeight() / 2 - 10, 20, 20);
            }
            
            else {
                if (!classes.contains("B")) {
                    classes.add("B");
                }

                double[] xPoints = {
                    point.x + canvas.getWidth() / 2 - 10,
                    point.x + canvas.getWidth() / 2 + 10,
                    point.x + canvas.getWidth() / 2
                };

                double[] yPoints = {
                    point.y + canvas.getHeight() / 2 + 10,
                    point.y + canvas.getHeight() / 2 + 10,
                    point.y + canvas.getHeight() / 2 - 10
                };
    
                gc.setFill(Color.DODGERBLUE);
                gc.fillPolygon(xPoints, yPoints, 3);
            }
        }
    }    

    private void reset() {
        classes.clear();
        points.clear();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        performanceLabel.setVisible(false);
        performance.setVisible(false);
    }

    private static class Point {
        double x;
        double y;
        int label;

        Point(double x, double y, int label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
    }

    private void displayAlert(String title, String message) {
        titleBar.setStyle("-fx-background-color: #24282f; -fx-background-radius: 18px 18px 0 0; -fx-padding: 5px;");

        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setStyle("-fx-background-color: white;");
        
        Label heading = new Label(title);
        heading.setWrapText(true);
        heading.getStyleClass().add("dialog-heading");
        layout.setHeading(heading);

        Label body = new Label(message);
        body.setWrapText(true);
        body.getStyleClass().add("dialog-body");
        layout.setBody(body);

        JFXDialog dialog = new JFXDialog();
        dialog.setContent(layout);
        dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
        dialog.setDialogContainer(main);

        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-button");
        closeButton.setOnAction(e -> dialog.close());

        layout.setActions(closeButton);

        dialog.setOnDialogClosed(e -> {
            titleBar.setStyle("-fx-background-color: transparent; -fx-background-radius: 18px 18px 0 0; -fx-padding: 5px;");
        });

        dialog.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.Objects;

public class JavaFXChess extends Application {

    static final int TILE_SIZE = 75;
    static final int MODE_PVP = 0;
    static final int MODE_BOT = 1;

    ChessLogic engine = new ChessLogic();
    StackPane[][] visualTiles = new StackPane[8][8];

    int lastFromRow = -1, lastFromCol = -1;
    int lastToRow = -1, lastToCol = -1;
    int selectedRow = -1, selectedCol = -1;

    BorderPane rootLayout;
    GridPane chessGrid = new GridPane();
    Label statusLabel = new Label();
    Label scoreLabel = new Label();
    VBox notationContainer = new VBox(6);
    VBox controlsContainer = new VBox(10);
    ScrollPane notationScrollPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #1e1e1e;");
        showMainMenu();
        Image appLogo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        stage.getIcons().add(appLogo);
        Scene scene = new Scene(rootLayout, (TILE_SIZE * 8) + 350, (TILE_SIZE * 8));
        stage.setScene(scene);
        stage.setTitle("Interactive Chess");
        stage.setResizable(false);
        stage.show();
    }

    void showMainMenu() {
        engine.gameOver = false;
        VBox menuBox = new VBox(25);
        menuBox.setAlignment(Pos.CENTER);

        Label title = new Label("CHESS ENGINE HUB");
        title.setFont(new Font("Segoe UI Bold", 36));
        title.setTextFill(Color.web("#eeeed2"));

        Button pvpBtn = createMenuButton("Local Arena (Player vs Player)");
        Button botBtn = createMenuButton("Bot Match (Player vs Computer)");
        Button profileBtn = createMenuButton("Check Profile Stats");

        pvpBtn.setOnAction(e -> startNewGame(MODE_PVP));
        botBtn.setOnAction(e -> startNewGame(MODE_BOT));
        profileBtn.setOnAction(e -> showProfileModal());

        menuBox.getChildren().addAll(title, pvpBtn, botBtn, profileBtn);
        rootLayout.setCenter(menuBox);
        rootLayout.setRight(null);
    }

    void showProfileModal() {
        Stage popupStage = new Stage();
        popupStage.setTitle("User Arena Profile");
        popupStage.setResizable(false);

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: #151515; -fx-border-color: #bacb46; -fx-border-width: 2px;");

        String statisticsText = DatabaseManager.fetchProfileStats();

        Label statsLabel = new Label(statisticsText);
        statsLabel.setFont(new Font("Consolas", 14));
        statsLabel.setTextFill(Color.web("#eeeed2"));
        statsLabel.setWrapText(true);

        Button closeBtn = new Button("Close Profile");
        closeBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #eeeed2; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #c33737; -fx-text-fill: white; -fx-padding: 8 20 8 20; -fx-cursor: hand;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: #eeeed2; -fx-padding: 8 20 8 20; -fx-cursor: hand;"));
        closeBtn.setOnAction(e -> popupStage.close());

        container.getChildren().addAll(statsLabel, closeBtn);

        Scene popupScene = new Scene(container, 380, 320);
        popupStage.setScene(popupScene);
        popupStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popupStage.showAndWait();
    }

    Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setFont(new Font("Segoe UI Semibold", 15));
        String baseStyle = "-fx-background-color: #2d2d2d; -fx-text-fill: #eeeed2; -fx-min-width: 320px; -fx-padding: 14px; -fx-background-radius: 6px; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #3d3d3d; -fx-text-fill: #bacb46; -fx-min-width: 320px; -fx-padding: 14px; -fx-background-radius: 6px; -fx-cursor: hand;";
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    void startNewGame(int mode) {
        engine.gameMode = mode;
        engine.gameOver = false;
        engine.isWhiteTurn = true;
        selectedRow = -1; selectedCol = -1;
        lastFromRow = -1; lastFromCol = -1;
        lastToRow = -1; lastToCol = -1;
        ChessLogic.enPassantCol = -1;
        engine.whiteScore = 39;
        engine.blackScore = 39;
        engine.movesList.clear();
        notationContainer.getChildren().clear();
        controlsContainer.getChildren().clear();

        VBox sidebar = new VBox(15);
        sidebar.setStyle("-fx-background-color: #151515; -fx-padding: 20px; -fx-min-width: 350px; -fx-border-color: #2d2d2d; -fx-border-width: 0 0 0 2px;");
        sidebar.setAlignment(Pos.TOP_CENTER);

        statusLabel.setFont(new Font("Segoe UI Bold", 16));
        statusLabel.setTextFill(Color.web("#bacb46"));

        scoreLabel.setFont(new Font("Segoe UI Semibold", 14));
        scoreLabel.setTextFill(Color.web("#eeeed2"));
        updateScoreDisplay();

        Button menuBtn = new Button("Return to Main Menu");
        menuBtn.setStyle("-fx-background-color: #c33737; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 310px; -fx-padding: 10px; -fx-background-radius: 4px; -fx-cursor: hand;");
        menuBtn.setOnAction(e -> showMainMenu());

        notationScrollPane = new ScrollPane(notationContainer);
        notationScrollPane.setFitToWidth(true);
        notationScrollPane.setStyle("-fx-background: #1e1e1e; -fx-border-color: #2d2d2d;");
        notationScrollPane.setPrefHeight(300);

        sidebar.getChildren().addAll(statusLabel, scoreLabel, notationScrollPane, controlsContainer, menuBtn);
        rootLayout.setRight(sidebar);
        rootLayout.setCenter(chessGrid);

        engine.initializeStandardBoard();
        statusLabel.setText("White to Move");
        buildGraphicBoard();
    }

    void updateScoreDisplay() {
        scoreLabel.setText(String.format("Material — White: %d  |  Black: %d", engine.whiteScore, engine.blackScore));
    }

    void buildGraphicBoard() {
        chessGrid.getChildren().clear();
        boolean whiteInCheck = engine.isInCheck(true, engine.board);
        boolean blackInCheck = engine.isInCheck(false, engine.board);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = new StackPane();
                Rectangle square = new Rectangle(TILE_SIZE, TILE_SIZE);
                Color baseColor = (row + col) % 2 == 0 ? Color.web("#eeeed2") : Color.web("#739552");

                if ((row == lastFromRow && col == lastFromCol) || (row == lastToRow && col == lastToCol)) {
                    baseColor = Color.web("#f7f48b");
                }
                if (engine.board[row][col] instanceof Piece.King) {
                    if ((engine.board[row][col].isWhite && whiteInCheck) || (!engine.board[row][col].isWhite && blackInCheck)) {
                        baseColor = Color.web("#e05353");
                    }
                }
                square.setFill(baseColor);
                tile.getChildren().add(square);

                String emoji = engine.board[row][col] != null ? engine.board[row][col].getEmoji() : "";
                Label pieceLabel = new Label(emoji);
                pieceLabel.setFont(new Font("Arial", 52));

                if (engine.board[row][col] != null) {
                    if (engine.board[row][col].isWhite) {
                        pieceLabel.setTextFill(Color.web("#ffffff"));
                        pieceLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(61, 61, 61, 0.85), 2, 0.9, 0, 0);");
                    } else {
                        pieceLabel.setTextFill(Color.web("#3d3d3d"));
                        pieceLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255, 255, 255, 0.9), 1.5, 0.95, 0, 0);");
                    }
                }

                tile.getChildren().add(pieceLabel);
                final int finalRow = row;
                final int finalCol = col;
                tile.setOnMouseClicked(e -> handleTileClick(finalRow, finalCol));
                visualTiles[row][col] = tile;
                chessGrid.add(tile, col, row);
            }
        }

        if (selectedRow != -1 && selectedCol != -1) {
            Piece currentPiece = engine.board[selectedRow][selectedCol];
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (currentPiece.isValidMove(selectedRow, selectedCol, row, col, engine.board)) {
                        if (engine.willMoveResolveCheck(selectedRow, selectedCol, row, col, engine.isWhiteTurn)) {
                            Circle dot = new Circle(9, Color.web("#3d3d3d", 0.45));
                            visualTiles[row][col].getChildren().add(dot);
                        }
                    }
                }
            }
        }
    }

    void handleTileClick(int row, int col) {
        if (engine.gameOver || (engine.gameMode == MODE_BOT && !engine.isWhiteTurn)) return;

        if (selectedRow == -1) {
            if (engine.board[row][col] != null && engine.board[row][col].isWhite == engine.isWhiteTurn) {
                selectedRow = row;
                selectedCol = col;
                buildGraphicBoard();
                StackPane tile = visualTiles[row][col];
                Rectangle rect = (Rectangle) tile.getChildren().getFirst();
                rect.setFill(Color.web("#bacb46"));
            }
        } else {
            int fromRow = selectedRow;
            int fromCol = selectedCol;
            selectedRow = -1;

            if (fromRow == row && fromCol == col) {
                buildGraphicBoard();
                return;
            }
            if (engine.board[fromRow][fromCol].isValidMove(fromRow, fromCol, row, col, engine.board)) {
                if (engine.willMoveResolveCheck(fromRow, fromCol, row, col, engine.isWhiteTurn)) {
                    engine.recordAndLogMove(fromRow, fromCol, row, col);
                    lastFromRow = fromRow; lastFromCol = fromCol;
                    lastToRow = row; lastToCol = col;

                    engine.calculateLiveScores();
                    engine.isWhiteTurn = !engine.isWhiteTurn;
                    postMoveEvaluation();

                    if (engine.gameMode == MODE_BOT && !engine.isWhiteTurn && !engine.gameOver) {
                        PauseTransition pause = new PauseTransition(Duration.millis(500));
                        pause.setOnFinished(e -> {
                            executeBotAction();
                            engine.calculateLiveScores();
                        });
                        pause.play();
                    }
                    return;
                }
            }
            statusLabel.setText("Illegal Move Attempted!");
            buildGraphicBoard();
        }
    }

    void executeBotAction() {
        int[] choice = engine.getBotMove();
        if (choice == null) {
            postMoveEvaluation();
            return;
        }
        engine.recordAndLogMove(choice[0], choice[1], choice[2], choice[3]);
        lastFromRow = choice[0]; lastFromCol = choice[1];
        lastToRow = choice[2]; lastToCol = choice[3];

        engine.isWhiteTurn = true;
        postMoveEvaluation();
    }

    void updateLiveNotationUI() {
        notationContainer.getChildren().clear();
        for (int i = 0; i < engine.movesList.size(); i += 2) {
            HBox row = new HBox(20);
            row.setPadding(new Insets(3, 8, 3, 8));
            Label numLabel = new Label((i / 2 + 1) + ".");
            numLabel.setTextFill(Color.GRAY);
            numLabel.setPrefWidth(30);
            Label whiteLabel = new Label(engine.movesList.get(i));
            whiteLabel.setTextFill(Color.web("#eeeed2"));
            whiteLabel.setPrefWidth(80);
            String blackMove = i + 1 < engine.movesList.size() ? engine.movesList.get(i + 1) : "";
            Label blackLabel = new Label(blackMove);
            blackLabel.setTextFill(Color.web("#eeeed2"));
            row.getChildren().addAll(numLabel, whiteLabel, blackLabel);
            notationContainer.getChildren().add(row);
        }
        notationScrollPane.setVvalue(1.0);
    }

    void enableEndGameLayout() {
        controlsContainer.getChildren().clear();
        Button btn = new Button("Launch New Match");
        btn.setStyle("-fx-background-color: #bacb46; -fx-text-fill: #151515; -fx-font-weight: bold; -fx-min-width: 310px; -fx-padding: 10px; -fx-cursor: hand;");
        btn.setOnAction(e -> startNewGame(engine.gameMode));
        controlsContainer.getChildren().add(btn);
    }

    void postMoveEvaluation() {
        updateLiveNotationUI();
        buildGraphicBoard();
        updateScoreDisplay();

        if (engine.hasNoLegalMoves(engine.isWhiteTurn)) {
            engine.gameOver = true;
            String outcomeType;
            String winnerSide;

            if (engine.isInCheck(engine.isWhiteTurn, engine.board)) {
                outcomeType = "CHECKMATE";
                winnerSide = engine.isWhiteTurn ? "Black" : "White";
                statusLabel.setText("CHECKMATE! " + winnerSide + " wins.");
            } else {
                outcomeType = "STALEMATE";
                winnerSide = "Draw";
                statusLabel.setText("STALEMATE!");
            }

            enableEndGameLayout();

            if (engine.gameMode == MODE_BOT) {
                String modeStr = "Player vs Bot";
                String dbWinnerValue = winnerSide;
                if (!winnerSide.equals("Draw")) {
                    dbWinnerValue = winnerSide.equals("White") ? "Player" : "Bot";
                }
                DatabaseManager.saveGame(modeStr, engine.whiteScore, engine.blackScore, outcomeType, dbWinnerValue, engine.movesList);
            }

        } else {
            statusLabel.setText(engine.isWhiteTurn ? "White's Turn" : "Black's Turn");
        }
    }
}
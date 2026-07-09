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
import java.util.*;

public class JavaFXChess extends Application {

    static final int TILE_SIZE = 75;
    static final int MODE_PVP = 0;
    static final int MODE_BOT = 1;
    static int enPassantCol = -1;

    Piece[][] board = new Piece[8][8];
    StackPane[][] visualTiles = new StackPane[8][8];
    boolean isWhiteTurn = true;
    boolean gameOver = false;
    int gameMode = MODE_PVP;
    List<String> movesList = new ArrayList<>();

    int whiteScore = 0;
    int blackScore = 0;

    int lastFromRow = -1;
    int lastFromCol = -1;
    int lastToRow = -1;
    int lastToCol = -1;
    int selectedRow = -1;
    int selectedCol = -1;

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
        gameOver = false;
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
        profileBtn.setOnAction(e -> showProfileModal()); // Links to the modal pop-up

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
        gameMode = mode;
        gameOver = false;
        isWhiteTurn = true;
        selectedRow = -1;
        selectedCol = -1;
        lastFromRow = -1;
        lastFromCol = -1;
        lastToRow = -1;
        lastToCol = -1;
        enPassantCol = -1;
        whiteScore = 39;
        blackScore = 39;
        movesList.clear();
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
        initializeStandardBoard();
        statusLabel.setText("White to Move");
        buildGraphicBoard();
    }

    void updateScoreDisplay() {
        scoreLabel.setText(String.format("Material — White: %d  |  Black: %d", whiteScore, blackScore));
    }

    void calculateLiveScores() {
        int whiteMaterial = 0;
        int blackMaterial = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    if (p.isWhite) {
                        whiteMaterial += p.getValue();
                    } else {
                        blackMaterial += p.getValue();
                    }
                }
            }
        }
        whiteScore = whiteMaterial;
        blackScore = blackMaterial;
        updateScoreDisplay();
    }

    void buildGraphicBoard() {
        chessGrid.getChildren().clear();
        boolean whiteInCheck = isInCheck(true, board);
        boolean blackInCheck = isInCheck(false, board);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = new StackPane();
                Rectangle square = new Rectangle(TILE_SIZE, TILE_SIZE);

                Color baseColor = (row + col) % 2 == 0 ? Color.web("#eeeed2") : Color.web("#739552");

                if ((row == lastFromRow && col == lastFromCol) || (row == lastToRow && col == lastToCol)) {
                    baseColor = Color.web("#f7f48b");
                }
                if (board[row][col] instanceof King) {
                    if ((board[row][col].isWhite && whiteInCheck) || (!board[row][col].isWhite && blackInCheck)) {
                        baseColor = Color.web("#e05353");
                    }
                }
                square.setFill(baseColor);
                tile.getChildren().add(square);

                String emoji = board[row][col] != null ? board[row][col].getEmoji() : "";
                Label pieceLabel = new Label(emoji);

                pieceLabel.setFont(new Font("Arial", 52));

                if (board[row][col] != null) {
                    if (board[row][col].isWhite) {
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
            Piece currentPiece = board[selectedRow][selectedCol];
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (currentPiece.isValidMove(selectedRow, selectedCol, row, col, board)) {
                        if (willMoveResolveCheck(selectedRow, selectedCol, row, col, isWhiteTurn)) {
                            Circle dot = new Circle(9, Color.web("#3d3d3d", 0.45));
                            visualTiles[row][col].getChildren().add(dot);
                        }
                    }
                }
            }
        }
    }

    void handleTileClick(int row, int col) {
        if (gameOver) {
            return;
        }
        if (gameMode == MODE_BOT && !isWhiteTurn) {
            return;
        }
        if (selectedRow == -1) {
            if (board[row][col] != null && board[row][col].isWhite == isWhiteTurn) {
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
            if (board[fromRow][fromCol].isValidMove(fromRow, fromCol, row, col, board)) {
                if (willMoveResolveCheck(fromRow, fromCol, row, col, isWhiteTurn)) {
                    recordAndLogMove(fromRow, fromCol, row, col);
                    calculateLiveScores();
                    isWhiteTurn = !isWhiteTurn;
                    postMoveEvaluation();
                    if (gameMode == MODE_BOT && !isWhiteTurn && !gameOver) {
                        PauseTransition pause = new PauseTransition(Duration.millis(500));
                        pause.setOnFinished(e -> {
                            executeBotLogicAlgorithm();
                            calculateLiveScores();
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

    void updateLiveNotationUI() {
        notationContainer.getChildren().clear();
        for (int i = 0; i < movesList.size(); i += 2) {
            HBox row = new HBox(20);
            row.setPadding(new Insets(3, 8, 3, 8));
            Label numLabel = new Label((i / 2 + 1) + ".");
            numLabel.setTextFill(Color.GRAY);
            numLabel.setPrefWidth(30);
            Label whiteLabel = new Label(movesList.get(i));
            whiteLabel.setTextFill(Color.web("#eeeed2"));
            whiteLabel.setPrefWidth(80);
            String blackMove = i + 1 < movesList.size() ? movesList.get(i + 1) : "";
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
        btn.setOnAction(e -> startNewGame(gameMode));
        controlsContainer.getChildren().add(btn);
    }

    void executeBotLogicAlgorithm() {
        List<int[]> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null && !board[row][col].isWhite) {
                    for (int tr = 0; tr < 8; tr++) {
                        for (int tc = 0; tc < 8; tc++) {
                            if (board[row][col].isValidMove(row, col, tr, tc, board)) {
                                if (willMoveResolveCheck(row, col, tr, tc, false)) {
                                    moves.add(new int[]{row, col, tr, tc});
                                }
                            }
                        }
                    }
                }
            }
        }
        if (moves.isEmpty()) {
            postMoveEvaluation();
            return;
        }
        int maxVal = -100;
        for (int[] m : moves) {
            int val = board[m[2]][m[3]] != null ? board[m[2]][m[3]].getValue() : -1;
            if (val > maxVal) {
                maxVal = val;
            }
        }
        List<int[]> bestMoves = new ArrayList<>();
        for (int[] m : moves) {
            int val = board[m[2]][m[3]] != null ? board[m[2]][m[3]].getValue() : -1;
            if (val == maxVal) {
                bestMoves.add(m);
            }
        }
        Random rand = new Random();
        int[] choice = bestMoves.get(rand.nextInt(bestMoves.size()));
        recordAndLogMove(choice[0], choice[1], choice[2], choice[3]);
        isWhiteTurn = true;
        postMoveEvaluation();
    }

    void initializeStandardBoard() {
        for (int row = 0; row < 8; row++) {
            Arrays.fill(board[row], null);
        }
        board[0][0] = new Rook(false);
        board[0][1] = new Knight(false);
        board[0][2] = new Bishop(false);
        board[0][3] = new Queen(false);
        board[0][4] = new King(false);
        board[0][5] = new Bishop(false);
        board[0][6] = new Knight(false);
        board[0][7] = new Rook(false);
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn(false);
        }
        for (int i = 0; i < 8; i++) {
            board[6][i] = new Pawn(true);
        }
        board[7][0] = new Rook(true);
        board[7][1] = new Knight(true);
        board[7][2] = new Bishop(true);
        board[7][3] = new Queen(true);
        board[7][4] = new King(true);
        board[7][5] = new Bishop(true);
        board[7][6] = new Knight(true);
        board[7][7] = new Rook(true);
    }

    void recordAndLogMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        boolean capture = (board[toRow][toCol] != null);
        String note = "";
        if (piece instanceof Pawn) {
            if (capture) {
                note += String.valueOf((char) ('a' + fromCol));
            }
        } else {
            note += piece.getNotationLetter();
        }
        if (piece instanceof King && Math.abs(fromCol - toCol) == 2) {
            int rookSrcCol = toCol == 6 ? 7 : 0;
            int rookDestCol = toCol == 6 ? 5 : 3;
            board[fromRow][rookDestCol] = board[fromRow][rookSrcCol];
            board[fromRow][rookSrcCol] = null;
            if (board[fromRow][rookDestCol] != null) {
                board[fromRow][rookDestCol].hasMoved = true;
            }
            note = toCol == 6 ? "O-O" : "O-O-O";
        } else if (piece instanceof Pawn && fromCol != toCol && board[toRow][toCol] == null) {
            board[fromRow][toCol] = null;
            capture = true;
            note += "x" + (char) ('a' + toCol) + (8 - toRow) + " e.p.";
        }
        if (!note.startsWith("O-O")) {
            if (capture) {
                note += "x";
            }
            note += (char) ('a' + toCol) + String.valueOf(8 - toRow);
        }
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.hasMoved = true;
        if (piece instanceof Pawn && Math.abs(fromRow - toRow) == 2) {
            enPassantCol = toCol;
        } else {
            enPassantCol = -1;
        }
        if (piece instanceof Pawn && (toRow == 0 || toRow == 7)) {
            board[toRow][toCol] = new Queen(piece.isWhite);
            note += "=Q";
        }
        if (isInCheck(!piece.isWhite, board)) {
            note += hasNoLegalMoves(!piece.isWhite) ? "#" : "+";
        }
        movesList.add(note);
        lastFromRow = fromRow;
        lastFromCol = fromCol;
        lastToRow = toRow;
        lastToCol = toCol;
        updateLiveNotationUI();
        buildGraphicBoard();
    }

    void postMoveEvaluation() {
        if (hasNoLegalMoves(isWhiteTurn)) {
            gameOver = true;
            String outcomeType; // "CHECKMATE" or "STALEMATE"
            String winnerSide;  // "White", "Black", or "Draw"

            if (isInCheck(isWhiteTurn, board)) {
                outcomeType = "CHECKMATE";
                winnerSide = isWhiteTurn ? "Black" : "White";
                statusLabel.setText("CHECKMATE! " + winnerSide + " wins.");
            } else {
                outcomeType = "STALEMATE";
                winnerSide = "Draw";
                statusLabel.setText("STALEMATE!");
            }

            enableEndGameLayout();

            String modeStr = (gameMode == MODE_BOT) ? "Player vs Bot" : "Player vs Player";

            String dbWinnerValue = winnerSide;
            if (gameMode == MODE_BOT && !winnerSide.equals("Draw")) {
                dbWinnerValue = winnerSide.equals("White") ? "Player" : "Bot";
            }

            DatabaseManager.saveGame(modeStr, whiteScore, blackScore, outcomeType, dbWinnerValue, movesList);

        } else {
            statusLabel.setText(isWhiteTurn ? "White's Turn" : "Black's Turn");
        }
    }

    boolean isInCheck(boolean whiteSide, Piece[][] b) {
        int kingRow = -1;
        int kingCol = -1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (b[row][col] instanceof King && b[row][col].isWhite == whiteSide) {
                    kingRow = row;
                    kingCol = col;
                }
            }
        }
        if (kingRow == -1) {
            return false;
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (b[row][col] != null && b[row][col].isWhite != whiteSide) {
                    if (b[row][col].isValidMove(row, col, kingRow, kingCol, b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean willMoveResolveCheck(int fromRow, int fromCol, int toRow, int toCol, boolean whiteSide) {
        Piece[][] temp = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, temp[i], 0, 8);
        }
        if (temp[fromRow][fromCol] instanceof Pawn && fromCol != toCol && temp[toRow][toCol] == null) {
            temp[fromRow][toCol] = null;
        }
        temp[toRow][toCol] = temp[fromRow][fromCol];
        temp[fromRow][fromCol] = null;
        return !isInCheck(whiteSide, temp);
    }

    boolean hasNoLegalMoves(boolean whiteSide) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null && board[row][col].isWhite == whiteSide) {
                    for (int tr = 0; tr < 8; tr++) {
                        for (int tc = 0; tc < 8; tc++) {
                            if (board[row][col].isValidMove(row, col, tr, tc, board)) {
                                if (willMoveResolveCheck(row, col, tr, tc, whiteSide)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    static boolean isPathClear(int startRow, int startCol, int targetRow, int targetCol, Piece[][] b) {
        int stepRow = Integer.compare(targetRow, startRow);
        int stepCol = Integer.compare(targetCol, startCol);
        int row = startRow + stepRow;
        int col = startCol + stepCol;
        while (row != targetRow || col != targetCol) {
            if (b[row][col] != null) {
                return false;
            }
            row += stepRow;
            col += stepCol;
        }
        return true;
    }

    static abstract class Piece {
        boolean isWhite;
        boolean hasMoved = false;

        Piece(boolean w) {
            isWhite = w;
        }

        abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b);
        abstract String getEmoji();
        abstract String getNotationLetter();
        abstract int getValue();
        abstract Piece clonePiece();

        boolean basicCheck(int toRow, int toCol, Piece[][] b) {
            return b[toRow][toCol] == null || b[toRow][toCol].isWhite != isWhite;
        }
    }

    static class Pawn extends Piece {
        Pawn(boolean w) {
            super(w);
        }

        boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) {
                return false;
            }
            int dir = isWhite ? -1 : 1;
            int start = isWhite ? 6 : 1;
            if (fromCol == toCol && toRow == fromRow + dir && b[toRow][toCol] == null) {
                return true;
            }
            if (fromCol == toCol && fromRow == start && toRow == fromRow + 2 * dir) {
                if (b[fromRow + dir][toCol] == null && b[toRow][toCol] == null) {
                    return true;
                }
            }
            if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + dir && b[toRow][toCol] != null) {
                return true;
            }
            if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + dir && b[toRow][toCol] == null) {
                return enPassantCol == toCol && fromRow == (isWhite ? 3 : 4);
            }
            return false;
        }

        String getEmoji() {
            return isWhite ? "♙" : "♟";
        }

        String getNotationLetter() {
            return "";
        }

        int getValue() {
            return 1;
        }

        Piece clonePiece() {
            Pawn p = new Pawn(isWhite);
            p.hasMoved = hasMoved;
            return p;
        }
    }

    static class Rook extends Piece {
        Rook(boolean w) {
            super(w);
        }

        boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            boolean passBasic = basicCheck(toRow, toCol, b);
            boolean inline = (fromRow == toRow || fromCol == toCol);
            if (passBasic && inline) {
                return isPathClear(fromRow, fromCol, toRow, toCol, b);
            }
            return false;
        }

        String getEmoji() {
            return isWhite ? "♖" : "♜";
        }

        String getNotationLetter() {
            return "R";
        }

        int getValue() {
            return 5;
        }

        Piece clonePiece() {
            Rook r = new Rook(isWhite);
            r.hasMoved = hasMoved;
            return r;
        }
    }

    static class Knight extends Piece {
        Knight(boolean w) {
            super(w);
        }

        boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) {
                return false;
            }
            int dR = Math.abs(fromRow - toRow);
            int dC = Math.abs(fromCol - toCol);
            return (dR == 2 && dC == 1) || (dR == 1 && dC == 2);
        }

        String getEmoji() {
            return isWhite ? "♘" : "♞";
        }

        String getNotationLetter() {
            return "N";
        }

        int getValue() {
            return 3;
        }

        Piece clonePiece() {
            Knight k = new Knight(isWhite);
            k.hasMoved = hasMoved;
            return k;
        }
    }

    static class Bishop extends Piece {
        Bishop(boolean w) {
            super(w);
        }

        boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            boolean passBasic = basicCheck(toRow, toCol, b);
            boolean diagonal = Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol);
            if (passBasic && diagonal) {
                return isPathClear(fromRow, fromCol, toRow, toCol, b);
            }
            return false;
        }

        String getEmoji() {
            return isWhite ? "♗" : "♝";
        }

        String getNotationLetter() {
            return "B";
        }

        int getValue() {
            return 3;
        }

        Piece clonePiece() {
            Bishop b = new Bishop(isWhite);
            b.hasMoved = hasMoved;
            return b;
        }
    }

    static class Queen extends Piece {
        Queen(boolean w) {
            super(w);
        }

        boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) {
                return false;
            }
            boolean diagonal = Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol);
            boolean straight = fromRow == toRow || fromCol == toCol;
            if (diagonal || straight) {
                return isPathClear(fromRow, fromCol, toRow, toCol, b);
            }
            return false;
        }

        String getEmoji() {
            return isWhite ? "♕" : "♛";
        }

        String getNotationLetter() {
            return "Q";
        }

        int getValue() {
            return 9;
        }

        Piece clonePiece() {
            Queen q = new Queen(isWhite);
            q.hasMoved = hasMoved;
            return q;
        }
    }

    static class King extends Piece {
        King(boolean w) {
            super(w);
        }

        boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) {
                return false;
            }
            if (Math.abs(fromRow - toRow) <= 1 && Math.abs(fromCol - toCol) <= 1) {
                return true;
            }
            if (!hasMoved && fromRow == toRow && Math.abs(fromCol - toCol) == 2) {
                int rookCol = toCol == 6 ? 7 : 0;
                if (b[fromRow][rookCol] instanceof Rook && !b[fromRow][rookCol].hasMoved) {
                    return isPathClear(fromRow, fromCol, fromRow, rookCol, b);
                }
            }
            return false;
        }

        String getEmoji() {
            return isWhite ? "♔" : "♚";
        }

        String getNotationLetter() {
            return "K";
        }

        int getValue() {
            return 0;
        }

        Piece clonePiece() {
            King k = new King(isWhite);
            k.hasMoved = hasMoved;
            return k;
        }
    }
}
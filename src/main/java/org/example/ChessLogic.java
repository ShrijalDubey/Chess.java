package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChessLogic {
    public static int enPassantCol = -1;

    public Piece[][] board = new Piece[8][8];
    public boolean isWhiteTurn = true;
    public boolean gameOver = false;
    public int gameMode = 0; // 0 = PVP, 1 = BOT
    public List<String> movesList = new ArrayList<>();

    public int whiteScore = 39;
    public int blackScore = 39;

    public void initializeStandardBoard() {
        for (int row = 0; row < 8; row++) {
            Arrays.fill(board[row], null);
        }
        board[0][0] = new Piece.Rook(false);
        board[0][1] = new Piece.Knight(false);
        board[0][2] = new Piece.Bishop(false);
        board[0][3] = new Piece.Queen(false);
        board[0][4] = new Piece.King(false);
        board[0][5] = new Piece.Bishop(false);
        board[0][6] = new Piece.Knight(false);
        board[0][7] = new Piece.Rook(false);
        for (int i = 0; i < 8; i++) board[1][i] = new Piece.Pawn(false);
        for (int i = 0; i < 8; i++) board[6][i] = new Piece.Pawn(true);
        board[7][0] = new Piece.Rook(true);
        board[7][1] = new Piece.Knight(true);
        board[7][2] = new Piece.Bishop(true);
        board[7][3] = new Piece.Queen(true);
        board[7][4] = new Piece.King(true);
        board[7][5] = new Piece.Bishop(true);
        board[7][6] = new Piece.Knight(true);
        board[7][7] = new Piece.Rook(true);
    }

    public void calculateLiveScores() {
        int whiteMaterial = 0;
        int blackMaterial = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    if (p.isWhite) whiteMaterial += p.getValue();
                    else blackMaterial += p.getValue();
                }
            }
        }
        whiteScore = whiteMaterial;
        blackScore = blackMaterial;
    }

    public void recordAndLogMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        boolean capture = (board[toRow][toCol] != null);
        String note = "";

        if (piece instanceof Piece.Pawn) {
            if (capture) note += String.valueOf((char) ('a' + fromCol));
        } else {
            note += piece.getNotationLetter();
        }

        if (piece instanceof Piece.King && Math.abs(fromCol - toCol) == 2) {
            int rookSrcCol = toCol == 6 ? 7 : 0;
            int rookDestCol = toCol == 6 ? 5 : 3;
            board[fromRow][rookDestCol] = board[fromRow][rookSrcCol];
            board[fromRow][rookSrcCol] = null;
            if (board[fromRow][rookDestCol] != null) board[fromRow][rookDestCol].hasMoved = true;
            note = toCol == 6 ? "O-O" : "O-O-O";
        } else if (piece instanceof Piece.Pawn && fromCol != toCol && board[toRow][toCol] == null) {
            board[fromRow][toCol] = null;
            capture = true;
            note += "x" + (char) ('a' + toCol) + (8 - toRow) + " e.p.";
        }

        if (!note.startsWith("O-O")) {
            if (capture) note += "x";
            note += (char) ('a' + toCol) + String.valueOf(8 - toRow);
        }

        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.hasMoved = true;

        if (piece instanceof Piece.Pawn && Math.abs(fromRow - toRow) == 2) enPassantCol = toCol;
        else enPassantCol = -1;

        if (piece instanceof Piece.Pawn && (toRow == 0 || toRow == 7)) {
            board[toRow][toCol] = new Piece.Queen(piece.isWhite);
            note += "=Q";
        }

        if (isInCheck(!piece.isWhite, board)) {
            note += hasNoLegalMoves(!piece.isWhite) ? "#" : "+";
        }
        movesList.add(note);
    }



    //Bots logic
    public int[] getBotMove() {
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
        if (moves.isEmpty()) return null;

        int maxVal = -100;
        for (int[] m : moves) {
            int val = board[m[2]][m[3]] != null ? board[m[2]][m[3]].getValue() : -1;
            if (val > maxVal) maxVal = val;
        }

        List<int[]> bestMoves = new ArrayList<>();
        for (int[] m : moves) {
            int val = board[m[2]][m[3]] != null ? board[m[2]][m[3]].getValue() : -1;
            if (val == maxVal) bestMoves.add(m);
        }
        return bestMoves.get(new Random().nextInt(bestMoves.size()));
    }

    public boolean isInCheck(boolean whiteSide, Piece[][] b) {
        int kingRow = -1, kingCol = -1;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (b[row][col] instanceof Piece.King && b[row][col].isWhite == whiteSide) {
                    kingRow = row;
                    kingCol = col;
                }
            }
        }
        if (kingRow == -1) return false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (b[row][col] != null && b[row][col].isWhite != whiteSide) {
                    if (b[row][col].isValidMove(row, col, kingRow, kingCol, b)) return true;
                }
            }
        }
        return false;
    }

    public boolean willMoveResolveCheck(int fromRow, int fromCol, int toRow, int toCol, boolean whiteSide) {
        Piece[][] temp = new Piece[8][8];
        for (int i = 0; i < 8; i++) System.arraycopy(board[i], 0, temp[i], 0, 8);
        if (temp[fromRow][fromCol] instanceof Piece.Pawn && fromCol != toCol && temp[toRow][toCol] == null) {
            temp[fromRow][toCol] = null;
        }
        temp[toRow][toCol] = temp[fromRow][fromCol];
        temp[fromRow][fromCol] = null;
        return !isInCheck(whiteSide, temp);
    }

    public boolean hasNoLegalMoves(boolean whiteSide) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] != null && board[row][col].isWhite == whiteSide) {
                    for (int tr = 0; tr < 8; tr++) {
                        for (int tc = 0; tc < 8; tc++) {
                            if (board[row][col].isValidMove(row, col, tr, tc, board)) {
                                if (willMoveResolveCheck(row, col, tr, tc, whiteSide)) return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean isPathClear(int startRow, int startCol, int targetRow, int targetCol, Piece[][] b) {
        int stepRow = Integer.compare(targetRow, startRow);
        int stepCol = Integer.compare(targetCol, startCol);
        int row = startRow + stepRow;
        int col = startCol + stepCol;
        while (row != targetRow || col != targetCol) {
            if (b[row][col] != null) return false;
            row += stepRow;
            col += stepCol;
        }
        return true;
    }
}
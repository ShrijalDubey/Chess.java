package org.example;

public abstract class Piece {
    public boolean isWhite;
    public boolean hasMoved = false;

    public Piece(boolean w) {
        isWhite = w;
    }

    public abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b);
    public abstract String getEmoji();
    public abstract String getNotationLetter();
    public abstract int getValue();
    public abstract Piece clonePiece();

    public boolean basicCheck(int toRow, int toCol, Piece[][] b) {
        return toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8 &&
                (b[toRow][toCol] == null || b[toRow][toCol].isWhite != isWhite);
    }
}
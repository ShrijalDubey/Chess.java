package org.example;

public abstract class Piece {
    public boolean isWhite;
    public boolean hasMoved = false;

    public Piece(boolean w) {
        this.isWhite = w;
    }

    public abstract boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b);
    public abstract String getEmoji();
    public abstract String getNotationLetter();
    public abstract int getValue();
    public abstract Piece clonePiece();

    protected boolean basicCheck(int toRow, int toCol, Piece[][] b) {
        return b[toRow][toCol] == null || b[toRow][toCol].isWhite != isWhite;
    }

    // --- Piece Implementations ---

    public static class Pawn extends Piece {
        public Pawn(boolean w) { super(w); }

        @Override
        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) return false;
            int dir = isWhite ? -1 : 1;
            int start = isWhite ? 6 : 1;

            if (fromCol == toCol && toRow == fromRow + dir && b[toRow][toCol] == null) return true;
            if (fromCol == toCol && fromRow == start && toRow == fromRow + 2 * dir) {
                if (b[fromRow + dir][toCol] == null && b[toRow][toCol] == null) return true;
            }
            if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + dir && b[toRow][toCol] != null) return true;
            if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + dir && b[toRow][toCol] == null) {
                return ChessLogic.enPassantCol == toCol && fromRow == (isWhite ? 3 : 4);
            }
            return false;
        }

        @Override public String getEmoji() { return isWhite ? "♙" : "♟"; }
        @Override public String getNotationLetter() { return ""; }
        @Override public int getValue() { return 1; }
        @Override public Piece clonePiece() {
            Pawn p = new Pawn(isWhite);
            p.hasMoved = hasMoved;
            return p;
        }
    }

    public static class Rook extends Piece {
        public Rook(boolean w) { super(w); }

        @Override
        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (basicCheck(toRow, toCol, b) && (fromRow == toRow || fromCol == toCol)) {
                return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
            }
            return false;
        }

        @Override public String getEmoji() { return isWhite ? "♖" : "♜"; }
        @Override public String getNotationLetter() { return "R"; }
        @Override public int getValue() { return 5; }
        @Override public Piece clonePiece() {
            Rook r = new Rook(isWhite);
            r.hasMoved = hasMoved;
            return r;
        }
    }

    public static class Knight extends Piece {
        public Knight(boolean w) { super(w); }

        @Override
        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) return false;
            int dR = Math.abs(fromRow - toRow);
            int dC = Math.abs(fromCol - toCol);
            return (dR == 2 && dC == 1) || (dR == 1 && dC == 2);
        }

        @Override public String getEmoji() { return isWhite ? "♘" : "♞"; }
        @Override public String getNotationLetter() { return "N"; }
        @Override public int getValue() { return 3; }
        @Override public Piece clonePiece() {
            Knight k = new Knight(isWhite);
            k.hasMoved = hasMoved;
            return k;
        }
    }

    public static class Bishop extends Piece {
        public Bishop(boolean w) { super(w); }

        @Override
        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (basicCheck(toRow, toCol, b) && Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
                return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
            }
            return false;
        }

        @Override public String getEmoji() { return isWhite ? "♗" : "♝"; }
        @Override public String getNotationLetter() { return "B"; }
        @Override public int getValue() { return 3; }
        @Override public Piece clonePiece() {
            Bishop b = new Bishop(isWhite);
            b.hasMoved = hasMoved;
            return b;
        }
    }

    public static class Queen extends Piece {
        public Queen(boolean w) { super(w); }

        @Override
        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) return false;
            boolean diagonal = Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol);
            boolean straight = fromRow == toRow || fromCol == toCol;
            if (diagonal || straight) {
                return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
            }
            return false;
        }

        @Override public String getEmoji() { return isWhite ? "♕" : "♛"; }
        @Override public String getNotationLetter() { return "Q"; }
        @Override public int getValue() { return 9; }
        @Override public Piece clonePiece() {
            Queen q = new Queen(isWhite);
            q.hasMoved = hasMoved;
            return q;
        }
    }

    public static class King extends Piece {
        public King(boolean w) { super(w); }

        @Override
        public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
            if (!basicCheck(toRow, toCol, b)) return false;
            if (Math.abs(fromRow - toRow) <= 1 && Math.abs(fromCol - toCol) <= 1) return true;
            if (!hasMoved && fromRow == toRow && Math.abs(fromCol - toCol) == 2) {
                int rookCol = toCol == 6 ? 7 : 0;
                if (b[fromRow][rookCol] instanceof Rook && !b[fromRow][rookCol].hasMoved) {
                    return ChessLogic.isPathClear(fromRow, fromCol, fromRow, rookCol, b);
                }
            }
            return false;
        }

        @Override public String getEmoji() { return isWhite ? "♔" : "♚"; }
        @Override public String getNotationLetter() { return "K"; }
        @Override public int getValue() { return 0; }
        @Override public Piece clonePiece() {
            King k = new King(isWhite);
            k.hasMoved = hasMoved;
            return k;
        }
    }
}
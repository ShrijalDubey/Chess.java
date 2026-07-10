package org.example;

class Pawn extends Piece {
    Pawn(boolean w) { super(w); }

    @Override
    boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
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

    @Override String getEmoji() { return isWhite ? "♙" : "♟"; }
    @Override String getNotationLetter() { return ""; }
    @Override int getValue() { return 1; }
    @Override Piece clonePiece() { Pawn p = new Pawn(isWhite); p.hasMoved = hasMoved; return p; }
}

class Rook extends Piece {
    Rook(boolean w) { super(w); }

    @Override
    boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
        if (!basicCheck(toRow, toCol, b)) return false;
        if (fromRow == toRow || fromCol == toCol) {
            return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
        }
        return false;
    }

    @Override String getEmoji() { return isWhite ? "♖" : "♜"; }
    @Override String getNotationLetter() { return "R"; }
    @Override int getValue() { return 5; }
    @Override Piece clonePiece() { Rook r = new Rook(isWhite); r.hasMoved = hasMoved; return r; }
}

class Knight extends Piece {
    Knight(boolean w) { super(w); }

    @Override
    boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
        if (!basicCheck(toRow, toCol, b)) return false;
        int dR = Math.abs(fromRow - toRow);
        int dC = Math.abs(fromCol - toCol);
        return (dR == 2 && dC == 1) || (dR == 1 && dC == 2);
    }

    @Override String getEmoji() { return isWhite ? "♘" : "♞"; }
    @Override String getNotationLetter() { return "N"; }
    @Override int getValue() { return 3; }
    @Override Piece clonePiece() { Knight k = new Knight(isWhite); k.hasMoved = hasMoved; return k; }
}

class Bishop extends Piece {
    Bishop(boolean w) { super(w); }

    @Override
    boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
        if (!basicCheck(toRow, toCol, b)) return false;
        if (Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
            return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
        }
        return false;
    }

    @Override String getEmoji() { return isWhite ? "♗" : "♝"; }
    @Override String getNotationLetter() { return "B"; }
    @Override int getValue() { return 3; }
    @Override Piece clonePiece() { Bishop b = new Bishop(isWhite); b.hasMoved = hasMoved; return b; }
}

class Queen extends Piece {
    Queen(boolean w) { super(w); }

    @Override
    boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
        if (!basicCheck(toRow, toCol, b)) return false;
        boolean diagonal = Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol);
        boolean straight = fromRow == toRow || fromCol == toCol;
        if (diagonal || straight) {
            return ChessLogic.isPathClear(fromRow, fromCol, toRow, toCol, b);
        }
        return false;
    }

    @Override String getEmoji() { return isWhite ? "♕" : "♛"; }
    @Override String getNotationLetter() { return "Q"; }
    @Override int getValue() { return 9; }
    @Override Piece clonePiece() { Queen q = new Queen(isWhite); q.hasMoved = hasMoved; return q; }
}

class King extends Piece {
    King(boolean w) { super(w); }

    @Override
    boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Piece[][] b) {
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

    @Override String getEmoji() { return isWhite ? "♔" : "♚"; }
    @Override String getNotationLetter() { return "K"; }
    @Override int getValue() { return 0; }
    @Override Piece clonePiece() { King k = new King(isWhite); k.hasMoved = hasMoved; return k; }
}
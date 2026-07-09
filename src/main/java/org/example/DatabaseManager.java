package org.example;

import java.sql.*;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/chess_db";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password_here";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void saveGame(String gameMode, int whiteScore, int blackScore, String outcome, String winner, List<String> movesList) {
        String insertGameSQL = "INSERT INTO games (game_mode, final_white_score, final_black_score, outcome, winner) VALUES (?, ?, ?, ?, ?)";
        String insertMoveSQL = "INSERT INTO moves (game_id, move_number, notation) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement gameStmt = conn.prepareStatement(insertGameSQL, Statement.RETURN_GENERATED_KEYS)) {
                gameStmt.setString(1, gameMode);
                gameStmt.setInt(2, whiteScore);
                gameStmt.setInt(3, blackScore);
                gameStmt.setString(4, outcome);
                gameStmt.setString(5, winner); // Bind the winner variable
                gameStmt.executeUpdate();

                int gameId = -1;
                try (ResultSet generatedKeys = gameStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        gameId = generatedKeys.getInt(1);
                    }
                }

                if (gameId != -1) {
                    try (PreparedStatement moveStmt = conn.prepareStatement(insertMoveSQL)) {
                        for (int i = 0; i < movesList.size(); i++) {
                            moveStmt.setInt(1, gameId);
                            moveStmt.setInt(2, i + 1);
                            moveStmt.setString(3, movesList.get(i));
                            moveStmt.addBatch();
                        }
                        moveStmt.executeBatch();
                    }
                }

                conn.commit();
                System.out.println("Game details (Winner: " + winner + ") saved successfully!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Database saving transaction failed: " + e.getMessage());
        }
    }


    public static String fetchProfileStats() {
        String query = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN winner = 'Player' THEN 1 ELSE 0 END) as player_wins, " +
                "SUM(CASE WHEN winner = 'Bot' THEN 1 ELSE 0 END) as bot_wins, " +
                "SUM(CASE WHEN outcome = 'STALEMATE' THEN 1 ELSE 0 END) as draws, " +
                "SUM(CASE WHEN winner = 'Player' AND outcome = 'CHECKMATE' THEN 1 ELSE 0 END) as cm_wins " +
                "FROM games";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int total = rs.getInt("total");
                if (total == 0) {
                    return "No games recorded yet! Play a match to build your profile.";
                }

                int playerWins = rs.getInt("player_wins");
                int botWins = rs.getInt("bot_wins");
                int draws = rs.getInt("draws");
                int checkmateWins = rs.getInt("cm_wins");
                double winRate = ((double) playerWins / total) * 100;

                return String.format(
                        "=== PLAYER PROFILE STATS ===\n\n" +
                                "Total Matches Played: %d\n" +
                                "Your Total Wins: %d\n" +
                                "Bot Wins: %d\n" +
                                "Stalemates / Draws: %d\n\n" +
                                "--- Performance Metrics ---\n" +
                                "Wins via Checkmate: %d\n" +
                                "Overall Win Rate: %.1f%%",
                        total, playerWins, botWins, draws, checkmateWins, winRate
                );
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch profile stats: " + e.getMessage());
        }
        return "Error loading profile data from database.";
    }
}
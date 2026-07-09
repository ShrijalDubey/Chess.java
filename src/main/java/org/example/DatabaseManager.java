package org.example;

import java.sql.*;
import java.util.List;

public class DatabaseManager {

    static final String DB_URL = "jdbc:mysql://localhost:3306/chess_db";
    static final String DB_USER = "root";
    static final String DB_PASS = "Shrijal@mysql";


    static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found in project paths!");
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void saveGame(String mode, int whitePts, int blackPts, String result, String winner, List<String> moves) {
        String addGameQuery = "INSERT INTO games (game_mode, final_white_score, final_black_score, outcome, winner) VALUES (?, ?, ?, ?, ?)";
        String addMoveQuery = "INSERT INTO moves (game_id, move_number, notation) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement gameCmd = conn.prepareStatement(addGameQuery, Statement.RETURN_GENERATED_KEYS)) {
                gameCmd.setString(1, mode);
                gameCmd.setInt(2, whitePts);
                gameCmd.setInt(3, blackPts);
                gameCmd.setString(4, result);
                gameCmd.setString(5, winner);
                gameCmd.executeUpdate();

                int gameId = -1;
                try (ResultSet keys = gameCmd.getGeneratedKeys()) {
                    if (keys.next()) {
                        gameId = keys.getInt(1);
                    }
                }


                if (gameId != -1) {
                    try (PreparedStatement moveCmd = conn.prepareStatement(addMoveQuery)) {
                        for (int i = 0; i < moves.size(); i++) {
                            moveCmd.setInt(1, gameId);
                            moveCmd.setInt(2, i + 1);
                            moveCmd.setString(3, moves.get(i));
                            moveCmd.addBatch();
                        }
                        moveCmd.executeBatch();
                    }
                }

                conn.commit();
                System.out.println("Match saved to MySQL successfully!");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Failed to save match data: " + e.getMessage());
        }
    }

    public static String fetchProfileStats() {
        String statsQuery = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN winner = 'Player' THEN 1 ELSE 0 END) as p_wins, " +
                "SUM(CASE WHEN winner = 'Bot' THEN 1 ELSE 0 END) as b_wins, " +
                "SUM(CASE WHEN outcome = 'STALEMATE' THEN 1 ELSE 0 END) as draws, " +
                "SUM(CASE WHEN winner = 'Player' AND outcome = 'CHECKMATE' THEN 1 ELSE 0 END) as cm_wins " +
                "FROM games";

        try (Connection conn = getConnection();
             PreparedStatement cmd = conn.prepareStatement(statsQuery);
             ResultSet data = cmd.executeQuery()) {

            if (data.next()) {
                int totalGames = data.getInt("total");

                if (totalGames == 0) {
                    return "No games recorded yet! Play a match to build your profile.";
                }

                int playerWins = data.getInt("p_wins");
                int botWins = data.getInt("b_wins");
                int totalDraws = data.getInt("draws");
                int mateWins = data.getInt("cm_wins");

                double pct = ((double) playerWins / totalGames) * 100;
                double roundedPct = Math.round(pct * 10.0) / 10.0;

                return "=== PLAYER PROFILE STATS ===\n\n" +
                        "Total Matches Played: " + totalGames + "\n" +
                        "Your Total Wins: " + playerWins + "\n" +
                        "Bot Wins: " + botWins + "\n" +
                        "Stalemates / Draws: " + totalDraws + "\n\n" +
                        "--- Performance Metrics ---\n" +
                        "Wins : " + mateWins + "\n" +
                        "Overall Win Rate: " + roundedPct + "%";
            }
        } catch (SQLException e) {
            System.err.println("Failed to read profile data: " + e.getMessage());
        }
        return "Error loading profile details.";
    }
}
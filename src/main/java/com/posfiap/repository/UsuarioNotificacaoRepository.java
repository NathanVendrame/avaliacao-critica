package com.posfiap.repository;

import java.sql.*;

public class UsuarioNotificacaoRepository {

    private static final String SQL =
            "SELECT email FROM UsuariosNotificacao WHERE id = ?";

    public String buscarEmailPorId(int idUsuario) {

        try (Connection conn = DriverManager.getConnection(
                System.getenv("SQL_CONNECTION_STRING"));
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("email");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao acessar Azure SQL", e);
        }

        return null;
    }

}

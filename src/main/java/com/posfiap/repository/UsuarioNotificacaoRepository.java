package com.posfiap.repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioNotificacaoRepository {

    private final DataSource dataSource;

    public UsuarioNotificacaoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final String SQL =
            "SELECT EMAIL_USUARIO FROM USUARIO_NOTIFICACAO";

    public List<String> buscarEmailsParaNotificacao() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(SQL)) {

            List<String> listaEmails = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("EMAIL_USUARIO");
                    listaEmails.add(email);
                }
            }
            return listaEmails;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar emails para notificação. " + e.getMessage(), e);
        }
    }



}

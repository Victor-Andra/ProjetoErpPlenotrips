package com.plenotrip.motorista;

import com.plenotrip.nucleo.util.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MotoristaDAO implements AutoCloseable {

    private final Connection conexao;

    public MotoristaDAO() throws SQLException {
        this.conexao = Conexao.getConexao();
    }

    public void inserir(Motorista motorista) throws SQLException {
        String sql = "INSERT INTO drivers (name, license_number, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, motorista.getName());
            stmt.setString(2, motorista.getLicenseNumber());
            stmt.setString(3, motorista.getStatus());
            Date agora = new Date();
            stmt.setTimestamp(4, new Timestamp(agora.getTime()));
            stmt.setTimestamp(5, new Timestamp(agora.getTime()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    motorista.setId((UUID) rs.getObject(1));
                }
            }
        }
    }

    public boolean existeLicenseNumber(String licenseNumber) throws SQLException {
        String sql = "SELECT 1 FROM drivers WHERE license_number = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, licenseNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Motorista> listar(int pagina, int tamanhoPagina) throws SQLException {
        if (pagina < 1) pagina = 1;
        if (tamanhoPagina < 1) tamanhoPagina = 10;
        if (tamanhoPagina > 100) tamanhoPagina = 100;

        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        String sql = "SELECT id, name, license_number, status, created_at, updated_at " +
                     "FROM drivers " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ? OFFSET ?";

        List<Motorista> motoristas = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, tamanhoPagina + 1);
            stmt.setLong(2, deslocamento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Motorista m = new Motorista();
                    m.setId((UUID) rs.getObject("id"));
                    m.setName(rs.getString("name"));
                    m.setLicenseNumber(rs.getString("license_number"));
                    m.setStatus(rs.getString("status"));
                    m.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    m.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    motoristas.add(m);
                }
            }
        }
        return motoristas;
    }

    public Motorista buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, name, license_number, status, created_at, updated_at FROM drivers WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Motorista m = new Motorista();
                    m.setId((UUID) rs.getObject("id"));
                    m.setName(rs.getString("name"));
                    m.setLicenseNumber(rs.getString("license_number"));
                    m.setStatus(rs.getString("status"));
                    m.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    m.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    return m;
                }
            }
        }
        return null;
    }

    public void atualizar(Motorista motorista) throws SQLException {
        String sql = "UPDATE drivers SET name = ?, license_number = ?, status = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, motorista.getName());
            stmt.setString(2, motorista.getLicenseNumber());
            stmt.setString(3, motorista.getStatus());
            Date agora = new Date();
            stmt.setTimestamp(4, new Timestamp(agora.getTime()));
            stmt.setObject(5, motorista.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(UUID id) throws SQLException {
        String sql = "DELETE FROM drivers WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        if (conexao != null && !conexao.isClosed()) {
            conexao.close();
        }
    }
    
    public List<Motorista> listarPorStatus(String status, int pagina, int tamanhoPagina) throws SQLException {
        if (pagina < 1) pagina = 1;
        if (tamanhoPagina < 1) tamanhoPagina = 1;
        if (tamanhoPagina > 100) tamanhoPagina = 100;

        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        String sql = "SELECT id, name, license_number, status, created_at, updated_at " +
                     "FROM drivers " +
                     "WHERE status = ? " +
                     "ORDER BY created_at ASC " + // Pega o MAIS ANTIGO primeiro
                     "LIMIT ? OFFSET ?";

        List<Motorista> motoristas = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, tamanhoPagina);
            stmt.setLong(3, deslocamento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Motorista m = new Motorista();
                    m.setId((UUID) rs.getObject("id"));
                    m.setName(rs.getString("name"));
                    m.setLicenseNumber(rs.getString("license_number"));
                    m.setStatus(rs.getString("status"));
                    m.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    m.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    motoristas.add(m);
                }
            }
        }
        return motoristas;
    }
}
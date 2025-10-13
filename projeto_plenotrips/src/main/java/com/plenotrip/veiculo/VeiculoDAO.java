package com.plenotrip.veiculo;

import com.plenotrip.nucleo.util.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VeiculoDAO implements AutoCloseable {

    private final Connection conexao;

    public VeiculoDAO() throws SQLException {
        this.conexao = Conexao.getConexao();
    }

    public void inserir(Veiculo veiculo) throws SQLException {
        String sql = "INSERT INTO vehicles (plate_number, model, status, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, veiculo.getPlateNumber());
            stmt.setString(2, veiculo.getModel());
            stmt.setString(3, veiculo.getStatus());
            Date agora = new Date();
            stmt.setTimestamp(4, new Timestamp(agora.getTime()));
            stmt.setTimestamp(5, new Timestamp(agora.getTime()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    veiculo.setId((UUID) rs.getObject(1));
                }
            }
        }
    }

    public boolean existePlaca(String plateNumber) throws SQLException {
        String sql = "SELECT 1 FROM vehicles WHERE plate_number = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, plateNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Veiculo> listar(int pagina, int tamanhoPagina) throws SQLException {
        if (pagina < 1) pagina = 1;
        if (tamanhoPagina < 1) tamanhoPagina = 10;
        if (tamanhoPagina > 100) tamanhoPagina = 100;

        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        String sql = "SELECT id, plate_number, model, status, created_at, updated_at " +
                     "FROM vehicles " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ? OFFSET ?";

        List<Veiculo> veiculos = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, tamanhoPagina + 1); // ← pega UM a mais
            stmt.setLong(2, deslocamento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Veiculo v = new Veiculo();
                    v.setId((UUID) rs.getObject("id"));
                    v.setPlateNumber(rs.getString("plate_number"));
                    v.setModel(rs.getString("model"));
                    v.setStatus(rs.getString("status"));
                    v.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    v.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    veiculos.add(v);
                }
            }
        }
        return veiculos;
    }

    public Veiculo buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, plate_number, model, status, created_at, updated_at FROM vehicles WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Veiculo v = new Veiculo();
                    v.setId((UUID) rs.getObject("id"));
                    v.setPlateNumber(rs.getString("plate_number"));
                    v.setModel(rs.getString("model"));
                    v.setStatus(rs.getString("status"));
                    v.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    v.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    return v;
                }
            }
        }
        return null;
    }
    
    public List<Veiculo> buscarPorPlaca(String placa, int pagina, int tamanhoPagina) throws SQLException {
        if (pagina < 1) pagina = 1;
        if (tamanhoPagina < 1) tamanhoPagina = 1;
        if (tamanhoPagina > 100) tamanhoPagina = 100;

        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        String sql = "SELECT id, plate_number, model, status, created_at, updated_at " +
                     "FROM vehicles " +
                     "WHERE plate_number = ? " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ? OFFSET ?";

        List<Veiculo> veiculos = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, placa);
            stmt.setInt(2, tamanhoPagina);
            stmt.setLong(3, deslocamento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Veiculo v = new Veiculo();
                    v.setId((UUID) rs.getObject("id"));
                    v.setPlateNumber(rs.getString("plate_number"));
                    v.setModel(rs.getString("model"));
                    v.setStatus(rs.getString("status"));
                    v.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    v.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    veiculos.add(v);
                }
            }
        }
        return veiculos;
    }

    public void atualizar(Veiculo veiculo) throws SQLException {
        String sql = "UPDATE vehicles SET plate_number = ?, model = ?, status = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, veiculo.getPlateNumber());
            stmt.setString(2, veiculo.getModel());
            stmt.setString(3, veiculo.getStatus());
            Date agora = new Date();
            stmt.setTimestamp(4, new Timestamp(agora.getTime()));
            stmt.setObject(5, veiculo.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(UUID id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, id);
            int linhasAfetadas = stmt.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        if (conexao != null && !conexao.isClosed()) {
            conexao.close();
        }
    }
    
    public List<Veiculo> listarPorStatus(String status, int pagina, int tamanhoPagina) throws SQLException {
        if (pagina < 1) pagina = 1;
        if (tamanhoPagina < 1) tamanhoPagina = 1;
        if (tamanhoPagina > 100) tamanhoPagina = 100;

        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        String sql = "SELECT id, plate_number, model, status, created_at, updated_at " +
                     "FROM vehicles " +
                     "WHERE status = ? " +
                     "ORDER BY created_at ASC " + 
                     "LIMIT ? OFFSET ?";

        List<Veiculo> veiculos = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, tamanhoPagina);
            stmt.setLong(3, deslocamento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Veiculo v = new Veiculo();
                    v.setId((UUID) rs.getObject("id"));
                    v.setPlateNumber(rs.getString("plate_number"));
                    v.setModel(rs.getString("model"));
                    v.setStatus(rs.getString("status"));
                    v.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    v.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    veiculos.add(v);
                }
            }
        }
        return veiculos;
    }
}
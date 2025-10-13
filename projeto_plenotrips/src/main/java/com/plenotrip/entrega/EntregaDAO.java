package com.plenotrip.entrega;

import com.plenotrip.nucleo.util.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EntregaDAO implements AutoCloseable {

    private final Connection conexao;

    public EntregaDAO() throws SQLException {
        this.conexao = Conexao.getConexao();
    }

    public void inserir(Entrega entrega) throws SQLException {
        String sql = "INSERT INTO delivery_assignments (route_description, driver_id, vehicle_id, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entrega.getRouteDescription());
            stmt.setObject(2, entrega.getDriverId());
            stmt.setObject(3, entrega.getVehicleId());
            stmt.setString(4, entrega.getStatus());
            stmt.setTimestamp(5, new Timestamp(new Date().getTime()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    entrega.setId((UUID) rs.getObject(1));
                }
            }
        }
    }

    public List<Entrega> listar(int pagina, int tamanhoPagina) throws SQLException {
        if (pagina < 1) pagina = 1;
        if (tamanhoPagina < 1) tamanhoPagina = 10;
        if (tamanhoPagina > 100) tamanhoPagina = 100;

        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        // Query com JOINs para buscar nome do motorista e placa do veículo
        String sql = "SELECT " +
                     "da.id, da.route_description, da.driver_id, da.vehicle_id, da.status, da.created_at, " +
                     "d.name as driver_name, " +
                     "v.plate_number as vehicle_plate " +
                     "FROM delivery_assignments da " +
                     "LEFT JOIN drivers d ON da.driver_id = d.id " +
                     "LEFT JOIN vehicles v ON da.vehicle_id = v.id " +
                     "ORDER BY da.created_at DESC " +
                     "LIMIT ? OFFSET ?";

        List<Entrega> entregas = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, tamanhoPagina + 1);
            stmt.setLong(2, deslocamento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Entrega e = new Entrega();
                    e.setId((UUID) rs.getObject("id"));
                    e.setRouteDescription(rs.getString("route_description"));
                    e.setDriverId((UUID) rs.getObject("driver_id"));
                    e.setDriverName(rs.getString("driver_name"));
                    e.setVehicleId((UUID) rs.getObject("vehicle_id"));
                    e.setVehiclePlate(rs.getString("vehicle_plate"));
                    e.setStatus(rs.getString("status"));
                    e.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    entregas.add(e);
                }
            }
        }
        return entregas;
    }

    public Entrega buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, route_description, driver_id, vehicle_id, status, created_at " +
                     "FROM delivery_assignments WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Entrega e = new Entrega();
                    e.setId((UUID) rs.getObject("id"));
                    e.setRouteDescription(rs.getString("route_description"));
                    e.setDriverId((UUID) rs.getObject("driver_id"));
                    e.setVehicleId((UUID) rs.getObject("vehicle_id"));
                    e.setStatus(rs.getString("status"));
                    e.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    return e;
                }
            }
        }
        return null;
    }

    public void atualizar(Entrega entrega) throws SQLException {
        String sql = "UPDATE delivery_assignments SET route_description = ?, driver_id = ?, vehicle_id = ?, status = ? WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, entrega.getRouteDescription());
            stmt.setObject(2, entrega.getDriverId());
            stmt.setObject(3, entrega.getVehicleId());
            stmt.setString(4, entrega.getStatus());
            stmt.setObject(5, entrega.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(UUID id) throws SQLException {
        String sql = "DELETE FROM delivery_assignments WHERE id = ?";
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
}
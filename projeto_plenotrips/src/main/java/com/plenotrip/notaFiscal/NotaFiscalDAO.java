package com.plenotrip.notaFiscal;

import com.plenotrip.nucleo.util.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NotaFiscalDAO implements AutoCloseable {

    private final Connection conexao;

    public NotaFiscalDAO() throws SQLException {
        this.conexao = Conexao.getConexao();
    }

    public void inserir(NotaFiscal notaFiscal) throws SQLException {
        String sql = "INSERT INTO delivery_invoices (delivery_assignment_id, invoice_number, invoice_details, created_at) " +
                     "VALUES (?, ?, ?::jsonb, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, notaFiscal.getDeliveryAssignmentId());
            stmt.setString(2, notaFiscal.getInvoiceNumber());
            stmt.setString(3, notaFiscal.getInvoiceDetails() != null ? notaFiscal.getInvoiceDetails() : "{}");
            stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    notaFiscal.setId((UUID) rs.getObject(1));
                }
            }
        }
    }

    public boolean existeInvoiceNumber(String invoiceNumber) throws SQLException {
        String sql = "SELECT 1 FROM delivery_invoices WHERE invoice_number = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, invoiceNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<NotaFiscal> listar(int pagina, int tamanhoPagina) throws SQLException {
        if (pagina < 1) pagina = 1;
        if (tamanhoPagina < 1) tamanhoPagina = 10;
        if (tamanhoPagina > 100) tamanhoPagina = 100;

        long deslocamento = (long) (pagina - 1) * tamanhoPagina;

        String sql = "SELECT id, delivery_assignment_id, invoice_number, invoice_details, created_at " +
                     "FROM delivery_invoices " +
                     "ORDER BY created_at DESC " +
                     "LIMIT ? OFFSET ?";

        List<NotaFiscal> notasFiscais = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, tamanhoPagina + 1);
            stmt.setLong(2, deslocamento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaFiscal n = new NotaFiscal();
                    n.setId((UUID) rs.getObject("id"));
                    n.setDeliveryAssignmentId((UUID) rs.getObject("delivery_assignment_id"));
                    n.setInvoiceNumber(rs.getString("invoice_number"));
                    n.setInvoiceDetails(rs.getString("invoice_details"));
                    n.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    notasFiscais.add(n);
                }
            }
        }
        return notasFiscais;
    }

    public NotaFiscal buscarPorId(UUID id) throws SQLException {
        String sql = "SELECT id, delivery_assignment_id, invoice_number, invoice_details, created_at " +
                     "FROM delivery_invoices WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    NotaFiscal n = new NotaFiscal();
                    n.setId((UUID) rs.getObject("id"));
                    n.setDeliveryAssignmentId((UUID) rs.getObject("delivery_assignment_id"));
                    n.setInvoiceNumber(rs.getString("invoice_number"));
                    n.setInvoiceDetails(rs.getString("invoice_details"));
                    n.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    return n;
                }
            }
        }
        return null;
    }

    public void atualizar(NotaFiscal notaFiscal) throws SQLException {
        String sql = "UPDATE delivery_invoices SET invoice_number = ?, invoice_details = ?::jsonb, delivery_assignment_id = ? WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, notaFiscal.getInvoiceNumber());
            stmt.setString(2, notaFiscal.getInvoiceDetails() != null ? notaFiscal.getInvoiceDetails() : "{}");
            stmt.setObject(3, notaFiscal.getDeliveryAssignmentId());
            stmt.setObject(4, notaFiscal.getId());
            stmt.executeUpdate();
        }
    }

    public void excluir(UUID id) throws SQLException {
        String sql = "DELETE FROM delivery_invoices WHERE id = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }
    
    public List<NotaFiscal> listarPorAtribuicao(UUID assignmentId) throws SQLException {
        String sql = "SELECT id, delivery_assignment_id, invoice_number, invoice_details, created_at " +
                     "FROM delivery_invoices " +
                     "WHERE delivery_assignment_id = ? " +
                     "ORDER BY created_at ASC";

        List<NotaFiscal> notas = new ArrayList<>();
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setObject(1, assignmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NotaFiscal n = new NotaFiscal(); // Corrija para NotaFiscal
                    n.setId((UUID) rs.getObject("id"));
                    n.setDeliveryAssignmentId((UUID) rs.getObject("delivery_assignment_id"));
                    n.setInvoiceNumber(rs.getString("invoice_number"));
                    n.setInvoiceDetails(rs.getString("invoice_details"));
                    n.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    notas.add(n);
                }
            }
        }
        return notas;
    }

    @Override
    public void close() throws SQLException {
        if (conexao != null && !conexao.isClosed()) {
            conexao.close();
        }
    }
}
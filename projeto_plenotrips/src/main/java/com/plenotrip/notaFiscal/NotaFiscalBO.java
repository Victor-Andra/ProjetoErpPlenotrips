package com.plenotrip.notaFiscal;

import com.plenotrip.nucleo.excecao.ExcecaoRegraNegocio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotaFiscalBO {

    public static class ResultadoPaginado {
        private final List<NotaFiscal> notasFiscais;
        private final boolean temProximaPagina;

        public ResultadoPaginado(List<NotaFiscal> notasFiscais, boolean temProximaPagina) {
            this.notasFiscais = notasFiscais;
            this.temProximaPagina = temProximaPagina;
        }

        public List<NotaFiscal> getNotasFiscais() { return notasFiscais; }
        public boolean isTemProximaPagina() { return temProximaPagina; }
    }

    public void salvar(NotaFiscal notaFiscal) throws ExcecaoRegraNegocio {
        validarCamposObrigatorios(notaFiscal);
        try (NotaFiscalDAO dao = new NotaFiscalDAO()) {
            if (dao.existeInvoiceNumber(notaFiscal.getInvoiceNumber())) {
                throw new ExcecaoRegraNegocio("Já existe uma nota fiscal com o número '" + notaFiscal.getInvoiceNumber() + "'.");
            }
            dao.inserir(notaFiscal);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao salvar nota fiscal no banco de dados: " + e.getMessage());
        }
    }

    public void atualizar(NotaFiscal notaFiscal) throws ExcecaoRegraNegocio {
        if (notaFiscal.getId() == null) {
            throw new ExcecaoRegraNegocio("ID da nota fiscal é obrigatório para atualização.");
        }
        validarCamposObrigatorios(notaFiscal);
        try (NotaFiscalDAO dao = new NotaFiscalDAO()) {
            NotaFiscal existente = dao.buscarPorId(notaFiscal.getId());
            if (existente == null) {
                throw new ExcecaoRegraNegocio("Nota fiscal não encontrada para atualização.");
            }
            // Permite reutilizar número se for a mesma nota
            if (!existente.getInvoiceNumber().equals(notaFiscal.getInvoiceNumber()) &&
                dao.existeInvoiceNumber(notaFiscal.getInvoiceNumber())) {
                throw new ExcecaoRegraNegocio("Já existe uma nota fiscal com o número '" + notaFiscal.getInvoiceNumber() + "'.");
            }
            dao.atualizar(notaFiscal);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao atualizar nota fiscal no banco de dados: " + e.getMessage());
        }
    }

    public ResultadoPaginado listar(int pagina, int tamanhoPagina) throws ExcecaoRegraNegocio {
        try (NotaFiscalDAO dao = new NotaFiscalDAO()) {
            List<NotaFiscal> todos = dao.listar(pagina, tamanhoPagina);
            boolean temProxima = false;
            List<NotaFiscal> resultado;

            if (todos.size() > tamanhoPagina) {
                temProxima = true;
                resultado = new ArrayList<>(todos.subList(0, tamanhoPagina));
            } else {
                resultado = todos;
            }
            return new ResultadoPaginado(resultado, temProxima);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao listar notas fiscais paginadas: " + e.getMessage());
        }
    }

    public NotaFiscal buscarPorId(UUID id) throws ExcecaoRegraNegocio {
        if (id == null) {
            throw new ExcecaoRegraNegocio("O ID da nota fiscal não pode ser nulo.");
        }
        try (NotaFiscalDAO dao = new NotaFiscalDAO()) {
            NotaFiscal notaFiscal = dao.buscarPorId(id);
            if (notaFiscal == null) {
                throw new ExcecaoRegraNegocio("Nota fiscal não encontrada.");
            }
            return notaFiscal;
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao buscar nota fiscal no banco de dados: " + e.getMessage());
        }
    }

    public void excluir(UUID id) throws ExcecaoRegraNegocio {
        if (id == null) {
            throw new ExcecaoRegraNegocio("ID da nota fiscal não pode ser nulo.");
        }
        try (NotaFiscalDAO dao = new NotaFiscalDAO()) {
            if (dao.buscarPorId(id) == null) {
                throw new ExcecaoRegraNegocio("Nota fiscal não encontrada para exclusão.");
            }
            dao.excluir(id);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao excluir nota fiscal no banco de dados: " + e.getMessage());
        }
    }

    private void validarCamposObrigatorios(NotaFiscal notaFiscal) throws ExcecaoRegraNegocio {
        if (notaFiscal.getDeliveryAssignmentId() == null) {
            throw new ExcecaoRegraNegocio("A atribuição de entrega é obrigatória.");
        }
        if (notaFiscal.getInvoiceNumber() == null || notaFiscal.getInvoiceNumber().trim().isEmpty()) {
            throw new ExcecaoRegraNegocio("O número da nota fiscal é obrigatório.");
        }
        // invoiceDetails pode ser nulo ou vazio
    }
}
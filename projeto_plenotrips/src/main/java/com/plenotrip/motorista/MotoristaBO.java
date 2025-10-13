package com.plenotrip.motorista;

import com.plenotrip.nucleo.excecao.ExcecaoRegraNegocio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MotoristaBO {

    private static final List<String> STATUS_VALIDOS = Arrays.asList("AVAILABLE", "IN_TRANSIT", "OFF_DUTY");

    public static class ResultadoPaginado {
        private final List<Motorista> motoristas;
        private final boolean temProximaPagina;

        public ResultadoPaginado(List<Motorista> motoristas, boolean temProximaPagina) {
            this.motoristas = motoristas;
            this.temProximaPagina = temProximaPagina;
        }

        public List<Motorista> getMotoristas() { return motoristas; }
        public boolean isTemProximaPagina() { return temProximaPagina; }
    }

    public void salvar(Motorista motorista) throws ExcecaoRegraNegocio {
        try (MotoristaDAO dao = new MotoristaDAO()) {
            validarLicenseNumberUnico(dao, motorista.getLicenseNumber(), null);
            validarStatus(motorista.getStatus());
            dao.inserir(motorista);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao salvar motorista no banco de dados: " + e.getMessage());
        }
    }

    public void atualizar(Motorista motorista) throws ExcecaoRegraNegocio {
        if (motorista.getId() == null) {
            throw new ExcecaoRegraNegocio("ID do motorista é obrigatório para atualização.");
        }
        try (MotoristaDAO dao = new MotoristaDAO()) {
            Motorista existente = dao.buscarPorId(motorista.getId());
            if (existente == null) {
                throw new ExcecaoRegraNegocio("Motorista não encontrado para atualização.");
            }
            validarLicenseNumberUnico(dao, motorista.getLicenseNumber(), motorista.getId());
            validarStatus(motorista.getStatus());
            dao.atualizar(motorista);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao atualizar motorista no banco de dados: " + e.getMessage());
        }
    }

    public ResultadoPaginado listar(int pagina, int tamanhoPagina) throws ExcecaoRegraNegocio {
        try (MotoristaDAO dao = new MotoristaDAO()) {
            List<Motorista> todos = dao.listar(pagina, tamanhoPagina);
            boolean temProxima = false;
            List<Motorista> resultado;

            if (todos.size() > tamanhoPagina) {
                temProxima = true;
                resultado = new ArrayList<>(todos.subList(0, tamanhoPagina));
            } else {
                resultado = todos;
            }
            return new ResultadoPaginado(resultado, temProxima);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao listar motoristas paginados: " + e.getMessage());
        }
    }

    public Motorista buscarPorId(UUID id) throws ExcecaoRegraNegocio {
        if (id == null) {
            throw new ExcecaoRegraNegocio("O ID do motorista não pode ser nulo.");
        }
        try (MotoristaDAO dao = new MotoristaDAO()) {
            Motorista motorista = dao.buscarPorId(id);
            if (motorista == null) {
                throw new ExcecaoRegraNegocio("Motorista não encontrado.");
            }
            return motorista;
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao buscar motorista no banco de dados: " + e.getMessage());
        }
    }

    public void excluir(UUID id) throws ExcecaoRegraNegocio {
        if (id == null) {
            throw new ExcecaoRegraNegocio("ID do motorista não pode ser nulo.");
        }
        try (MotoristaDAO dao = new MotoristaDAO()) {
            if (dao.buscarPorId(id) == null) {
                throw new ExcecaoRegraNegocio("Motorista não encontrado para exclusão.");
            }
            dao.excluir(id);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao excluir motorista no banco de dados: " + e.getMessage());
        }
    }

    private void validarLicenseNumberUnico(MotoristaDAO dao, String licenseNumber, UUID idExcluido) throws SQLException, ExcecaoRegraNegocio {
        if (dao.existeLicenseNumber(licenseNumber)) {
            if (idExcluido == null) {
                throw new ExcecaoRegraNegocio("Já existe um motorista com a CNH '" + licenseNumber + "'.");
            } else {
                Motorista existente = dao.buscarPorId(idExcluido);
                if (existente == null || !existente.getLicenseNumber().equals(licenseNumber)) {
                    throw new ExcecaoRegraNegocio("Já existe um motorista com a CNH '" + licenseNumber + "'.");
                }
            }
        }
    }

    private void validarStatus(String status) throws ExcecaoRegraNegocio {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new ExcecaoRegraNegocio("Status de motorista inválido. Valores permitidos: AVAILABLE, IN_TRANSIT, OFF_DUTY.");
        }
    }
    
    public List<Motorista> listarPorStatus(String status, int pagina, int tamanhoPagina) throws ExcecaoRegraNegocio {
        try (MotoristaDAO dao = new MotoristaDAO()) {
            return dao.listarPorStatus(status, pagina, tamanhoPagina);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao listar motoristas por status: " + e.getMessage());
        }
    }
    
    public ResultadoPaginado listarPorStatusPaginado(String status, int pagina, int tamanhoPagina) 
            throws ExcecaoRegraNegocio {
        try (MotoristaDAO dao = new MotoristaDAO()) {
            List<Motorista> todos = dao.listarPorStatus(status, pagina, tamanhoPagina);
            boolean temProxima = false;
            List<Motorista> resultado;

            if (todos.size() > tamanhoPagina) {
                temProxima = true;
                resultado = new ArrayList<>(todos.subList(0, tamanhoPagina));
            } else {
                resultado = todos;
            }
            return new ResultadoPaginado(resultado, temProxima);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao listar motoristas por status: " + e.getMessage());
        }
    }
}
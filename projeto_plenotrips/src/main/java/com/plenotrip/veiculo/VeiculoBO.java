package com.plenotrip.veiculo;

import com.plenotrip.nucleo.excecao.ExcecaoRegraNegocio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class VeiculoBO {

    private static final List<String> STATUS_VALIDOS = Arrays.asList("OPERATIONAL", "MAINTENANCE", "IN_TRANSIT");

    public static class ResultadoPaginado {
        private final List<Veiculo> veiculos;
        private final boolean temProximaPagina;

        public ResultadoPaginado(List<Veiculo> veiculos, boolean temProximaPagina) {
            this.veiculos = veiculos;
            this.temProximaPagina = temProximaPagina;
        }

        public List<Veiculo> getVeiculos() { return veiculos; }
        public boolean isTemProximaPagina() { return temProximaPagina; }
    }

    public void salvar(Veiculo veiculo) throws ExcecaoRegraNegocio {
        try (VeiculoDAO dao = new VeiculoDAO()) {
            validarPlacaUnica(dao, veiculo.getPlateNumber(), null);
            validarStatus(veiculo.getStatus());
            dao.inserir(veiculo);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao salvar veículo no banco de dados: " + e.getMessage());
        }
    }

    public void atualizar(Veiculo veiculo) throws ExcecaoRegraNegocio {
        if (veiculo.getId() == null) {
            throw new ExcecaoRegraNegocio("ID do veículo é obrigatório para atualização.");
        }
        try (VeiculoDAO dao = new VeiculoDAO()) {
            Veiculo existente = dao.buscarPorId(veiculo.getId());
            if (existente == null) {
                throw new ExcecaoRegraNegocio("Veículo não encontrado para atualização.");
            }
            validarPlacaUnica(dao, veiculo.getPlateNumber(), veiculo.getId());
            validarStatus(veiculo.getStatus());
            dao.atualizar(veiculo);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao atualizar veículo no banco de dados: " + e.getMessage());
        }
    }

    public ResultadoPaginado listar(int pagina, int tamanhoPagina) throws ExcecaoRegraNegocio {
        try (VeiculoDAO dao = new VeiculoDAO()) {
            List<Veiculo> todos = dao.listar(pagina, tamanhoPagina);
            
            boolean temProxima = false;
            List<Veiculo> resultado;

            if (todos.size() > tamanhoPagina) {
                // Remove o último item (só usado para verificação)
                temProxima = true;
                resultado = new ArrayList<>(todos.subList(0, tamanhoPagina));
            } else {
                resultado = todos;
            }

            return new ResultadoPaginado(resultado, temProxima);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao listar veículos paginados: " + e.getMessage());
        }
    }

    public ResultadoPaginado listar(int pagina) throws ExcecaoRegraNegocio {
        return listar(pagina, 10);
    }

    public Veiculo buscarPorId(UUID id) throws ExcecaoRegraNegocio {
        if (id == null) {
            throw new ExcecaoRegraNegocio("O ID do veículo não pode ser nulo.");
        }
        try (VeiculoDAO dao = new VeiculoDAO()) {
            Veiculo veiculo = dao.buscarPorId(id);
            if (veiculo == null) {
                throw new ExcecaoRegraNegocio("Veículo não encontrado.");
            }
            return veiculo;
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao buscar veículo no banco de dados: " + e.getMessage());
        }
    }
    
    public List<Veiculo> listarPorPlaca(String placa, int pagina, int tamanhoPagina) throws ExcecaoRegraNegocio {
        try (VeiculoDAO dao = new VeiculoDAO()) {
            return dao.buscarPorPlaca(placa, pagina, tamanhoPagina);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao buscar veículo por placa: " + e.getMessage());
        }
    }

    public void excluir(UUID id) throws ExcecaoRegraNegocio {
        if (id == null) {
            throw new ExcecaoRegraNegocio("ID do veículo não pode ser nulo.");
        }
        try (VeiculoDAO dao = new VeiculoDAO()) {
            // Verifica se o veículo existe antes de excluir
            if (dao.buscarPorId(id) == null) {
                throw new ExcecaoRegraNegocio("Veículo não encontrado para exclusão.");
            }
            dao.excluir(id);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao excluir veículo no banco de dados: " + e.getMessage());
        }
    }

    private void validarPlacaUnica(VeiculoDAO dao, String plateNumber, UUID idExcluido) throws SQLException, ExcecaoRegraNegocio {
        if (dao.existePlaca(plateNumber)) {
            if (idExcluido == null) {
                throw new ExcecaoRegraNegocio("Já existe um veículo com a placa '" + plateNumber + "'.");
            } else {
                // Edição: verifica se a placa pertence a outro veículo
                Veiculo veiculoExistente = dao.buscarPorId(idExcluido);
                if (veiculoExistente == null || !veiculoExistente.getPlateNumber().equals(plateNumber)) {
                    throw new ExcecaoRegraNegocio("Já existe um veículo com a placa '" + plateNumber + "'.");
                }
            }
        }
    }

    private void validarStatus(String status) throws ExcecaoRegraNegocio {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new ExcecaoRegraNegocio("Status de veículo inválido. Valores permitidos: OPERATIONAL, MAINTENANCE, IN_TRANSIT.");
        }
    }
    
    public List<Veiculo> listarPorStatus(String status, int pagina, int tamanhoPagina) throws ExcecaoRegraNegocio {
        try (VeiculoDAO dao = new VeiculoDAO()) {
            return dao.listarPorStatus(status, pagina, tamanhoPagina);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao listar veículos por status: " + e.getMessage());
        }
    }
    
    public ResultadoPaginado listarPorStatusPaginado(String status, int pagina, int tamanhoPagina) 
            throws ExcecaoRegraNegocio {
        try (VeiculoDAO dao = new VeiculoDAO()) {
            List<Veiculo> todos = dao.listarPorStatus(status, pagina, tamanhoPagina);
            boolean temProxima = false;
            List<Veiculo> resultado;

            if (todos.size() > tamanhoPagina) {
                temProxima = true;
                resultado = new ArrayList<>(todos.subList(0, tamanhoPagina));
            } else {
                resultado = todos;
            }
            return new ResultadoPaginado(resultado, temProxima);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao listar veículos por status: " + e.getMessage());
        }
    }
}
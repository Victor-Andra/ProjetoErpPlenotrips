package com.plenotrip.entrega;

import com.plenotrip.nucleo.excecao.ExcecaoRegraNegocio;
import com.plenotrip.nucleo.util.JwtUtil;

//Imports Java padrão
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//Imports do Spring para HTTP
//import org.springframework.http.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class EntregaBO {

    private static final List<String> STATUS_VALIDOS = Arrays.asList("ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED");

    public static class ResultadoPaginado {
        private final List<Entrega> entregas;
        private final boolean temProximaPagina;

        public ResultadoPaginado(List<Entrega> entregas, boolean temProximaPagina) {
            this.entregas = entregas;
            this.temProximaPagina = temProximaPagina;
        }

        public List<Entrega> getEntregas() { return entregas; }
        public boolean isTemProximaPagina() { return temProximaPagina; }
    }

    public void salvar(Entrega entrega) throws ExcecaoRegraNegocio {
        validarCamposObrigatorios(entrega);
        validarStatus(entrega.getStatus());
        try (EntregaDAO dao = new EntregaDAO()) {
            dao.inserir(entrega);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao salvar atribuição de entrega no banco de dados: " + e.getMessage());
        }
    }

    public void atualizar(Entrega entrega) throws ExcecaoRegraNegocio {
        if (entrega.getId() == null) {
            throw new ExcecaoRegraNegocio("ID da atribuição de entrega é obrigatório para atualização.");
        }
        validarCamposObrigatorios(entrega);
        validarStatus(entrega.getStatus());
        try (EntregaDAO dao = new EntregaDAO()) {
            Entrega existente = dao.buscarPorId(entrega.getId());
            if (existente == null) {
                throw new ExcecaoRegraNegocio("Atribuição de entrega não encontrada para atualização.");
            }
            dao.atualizar(entrega);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao atualizar atribuição de entrega no banco de dados: " + e.getMessage());
        }
    }

    public ResultadoPaginado listar(int pagina, int tamanhoPagina) throws ExcecaoRegraNegocio {
        try (EntregaDAO dao = new EntregaDAO()) {
            List<Entrega> todos = dao.listar(pagina, tamanhoPagina);
            boolean temProxima = false;
            List<Entrega> resultado;

            if (todos.size() > tamanhoPagina) {
                temProxima = true;
                resultado = new ArrayList<>(todos.subList(0, tamanhoPagina));
            } else {
                resultado = todos;
            }
            return new ResultadoPaginado(resultado, temProxima);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao listar atribuições de entrega: " + e.getMessage());
        }
    }

    public Entrega buscarPorId(UUID id) throws ExcecaoRegraNegocio {
        if (id == null) {
            throw new ExcecaoRegraNegocio("O ID da atribuição de entrega não pode ser nulo.");
        }
        try (EntregaDAO dao = new EntregaDAO()) {
            Entrega entrega = dao.buscarPorId(id);
            if (entrega == null) {
                throw new ExcecaoRegraNegocio("Atribuição de entrega não encontrada.");
            }
            return entrega;
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao buscar atribuição de entrega no banco de dados: " + e.getMessage());
        }
    }

    public void excluir(UUID id) throws ExcecaoRegraNegocio {
        if (id == null) {
            throw new ExcecaoRegraNegocio("ID da atribuição de entrega não pode ser nulo.");
        }
        try (EntregaDAO dao = new EntregaDAO()) {
            if (dao.buscarPorId(id) == null) {
                throw new ExcecaoRegraNegocio("Atribuição de entrega não encontrada para exclusão.");
            }
            dao.excluir(id);
        } catch (SQLException e) {
            throw new ExcecaoRegraNegocio("Erro ao excluir atribuição de entrega no banco de dados: " + e.getMessage());
        }
    }

    private void validarCamposObrigatorios(Entrega entrega) throws ExcecaoRegraNegocio {
        if (entrega.getRouteDescription() == null || entrega.getRouteDescription().trim().isEmpty()) {
            throw new ExcecaoRegraNegocio("A descrição da rota é obrigatória.");
        }
        if (entrega.getDriverId() == null) {
            throw new ExcecaoRegraNegocio("O motorista é obrigatório.");
        }
        if (entrega.getVehicleId() == null) {
            throw new ExcecaoRegraNegocio("O veículo é obrigatório.");
        }
    }

    private void validarStatus(String status) throws ExcecaoRegraNegocio {
        if (!STATUS_VALIDOS.contains(status)) {
            throw new ExcecaoRegraNegocio("Status de entrega inválido. Valores permitidos: ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED.");
        }
    }
    
    public void cadastrarAtribuicao(DadosAtribuicao dados) throws ExcecaoRegraNegocio {
        try {
            // Monta o payload
            NotificacaoGO payload = new NotificacaoGO();
            payload.setDriverId(dados.getDriverId());
            payload.setVehiclePlate(dados.getVehiclePlate());
            payload.setRouteDescription(dados.getRouteDescription());
            
            // Processa as chaves de NF-e
            List<NotificacaoGO.InvoiceItem> invoices = new ArrayList<>();
            if (dados.getInvoiceKeys() != null) {
                for (String chave : dados.getInvoiceKeys()) {
                    if (chave != null && chave.trim().length() == 44) {
                        NotificacaoGO.InvoiceItem item = new NotificacaoGO.InvoiceItem();
                        item.setKey(chave.trim());
                        invoices.add(item);
                    }
                }
            }
            payload.setInvoices(invoices);

            // Gera token JWT
            String token = JwtUtil.gerarToken();
            // Envia para a API Go
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            HttpEntity<NotificacaoGO> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8081/v1/assignments/notify", 
                request, 
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ExcecaoRegraNegocio("API retornou erro: " + response.getStatusCode());
            }

        } catch (ExcecaoRegraNegocio e) {
            throw e; // Relança exceções de negócio
        } catch (Exception e) {
            throw new ExcecaoRegraNegocio("Falha ao comunicar com a API Go: " + e.getMessage());
        }
    }
    
    @GetMapping
    public String listarEntregas(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            Model model) {

        try {
            String token = JwtUtil.gerarToken();
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<EntregaCompleta[]> response = restTemplate.exchange(
                "http://localhost:8081/v1/assignments?page=" + pagina + "&size=" + tamanho,
                HttpMethod.GET,
                request,
                EntregaCompleta[].class
            );

            List<EntregaCompleta> entregas = response.getBody() != null ? 
                Arrays.asList(response.getBody()) : new ArrayList<>();
                
            model.addAttribute("entregas", entregas);
            model.addAttribute("paginaAtual", pagina);
            model.addAttribute("tamanhoPagina", tamanho);
            model.addAttribute("temProximaPagina", entregas.size() == tamanho);

        } catch (Exception e) {
            model.addAttribute("mensagemErro", "Erro ao buscar atribuições: " + e.getMessage());
            model.addAttribute("entregas", new ArrayList<>());
        }
        return "entrega/lista_entrega";
    }
}
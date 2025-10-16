package com.plenotrip.entrega;

import com.plenotrip.motorista.Motorista;
import com.plenotrip.motorista.MotoristaBO;
import com.plenotrip.nucleo.excecao.ExcecaoRegraNegocio;
import com.plenotrip.veiculo.Veiculo;
import com.plenotrip.veiculo.VeiculoBO;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/entrega")
public class EntregaControlador {

    private final EntregaBO entregaBO = new EntregaBO();

    @GetMapping
    public String listarEntregas(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestParam(required = false) String sucesso,
            Model model) {

        model.addAttribute("entregas", new ArrayList<Entrega>()); // ← Tipo atualizado
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("tamanhoPagina", tamanho);
        model.addAttribute("temProximaPagina", false);
        
        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        try {
            EntregaBO.ResultadoPaginado resultado = entregaBO.listar(pagina, tamanho);
            model.addAttribute("entregas", resultado.getEntregas()); // ← Já inclui nome/placa
            model.addAttribute("temProximaPagina", resultado.isTemProximaPagina());
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
        }
        return "entrega/lista_entrega";
    }

    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        try {
            // Carrega primeiro motorista AVAILABLE
            MotoristaBO motoristaBO = new MotoristaBO();
            List<Motorista> motoristasDisponiveis = motoristaBO.listarPorStatus("AVAILABLE", 1, 1);
            
            // Carrega primeiro veículo OPERATIONAL
            VeiculoBO veiculoBO = new VeiculoBO();
            List<Veiculo> veiculosOperacionais = veiculoBO.listarPorStatus("OPERATIONAL", 1, 1);

            // Passa para o template
            model.addAttribute("motoristaSelecionado", 
                motoristasDisponiveis.isEmpty() ? null : motoristasDisponiveis.get(0));
            model.addAttribute("veiculoSelecionado", 
                veiculosOperacionais.isEmpty() ? null : veiculosOperacionais.get(0));
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", "Erro ao carregar dados iniciais: " + e.getMessage());
        }
        return "entrega/formulario_entrega";
    }

    @GetMapping("/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable String id, Model model) {
        try {
            UUID uuid = UUID.fromString(id);
            Entrega entrega = entregaBO.buscarPorId(uuid);
            
            // Carrega nome do motorista
            if (entrega.getDriverId() != null) {
                MotoristaBO motoristaBO = new MotoristaBO();
                Motorista motorista = motoristaBO.buscarPorId(entrega.getDriverId());
                entrega.setDriverName(motorista.getName());
            }
            
            // Carrega placa do veículo
            if (entrega.getVehicleId() != null) {
                VeiculoBO veiculoBO = new VeiculoBO();
                Veiculo veiculo = veiculoBO.buscarPorId(entrega.getVehicleId());
                entrega.setVehiclePlate(veiculo.getPlateNumber());
            }
            
            model.addAttribute("entrega", entrega);
            model.addAttribute("modoEdicao", true);
        } catch (IllegalArgumentException | ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", "ID de atribuição de entrega inválido ou não encontrado.");
            return "redirect:/entrega";
        }
        return "entrega/formulario_entrega";
    }

    @GetMapping("/excluir/{id}")
    public String excluirEntrega(@PathVariable String id, Model model) {
        try {
            UUID uuid = UUID.fromString(id);
            entregaBO.excluir(uuid);
            return "redirect:/entrega?sucesso=Atribuicao de entrega excluida com sucesso!";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensagemErro", "ID de atribuicao de entrega invalido.");
            return "redirect:/entrega";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "redirect:/entrega";
        }
    }
    
    @GetMapping("/selecionar/motorista")
    public String selecionarMotoristaDisponivel(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            Model model) {
        try {
            MotoristaBO motoristaBO = new MotoristaBO();
            MotoristaBO.ResultadoPaginado resultado = motoristaBO.listarPorStatusPaginado("AVAILABLE", pagina, tamanho);
                
            model.addAttribute("motoristas", resultado.getMotoristas());
            model.addAttribute("paginaAtual", pagina);
            model.addAttribute("tamanhoPagina", tamanho);
            model.addAttribute("temProximaPagina", resultado.isTemProximaPagina());
            model.addAttribute("tipo", "motorista");
            model.addAttribute("statusFiltro", "AVAILABLE");
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
        }
        return "entrega/selecionar_motorista_veiculo";
    }

    // Lista APENAS veículos OPERATIONAL
    @GetMapping("/selecionar/veiculo")
    public String selecionarVeiculoOperacional(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            Model model) {
        try {
            VeiculoBO veiculoBO = new VeiculoBO();
            VeiculoBO.ResultadoPaginado resultado = veiculoBO.listarPorStatusPaginado("OPERATIONAL", pagina, tamanho);
                
            model.addAttribute("veiculos", resultado.getVeiculos());
            model.addAttribute("paginaAtual", pagina);
            model.addAttribute("tamanhoPagina", tamanho);
            model.addAttribute("temProximaPagina", resultado.isTemProximaPagina());
            model.addAttribute("tipo", "veiculo");
            model.addAttribute("statusFiltro", "OPERATIONAL");
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
        }
        return "entrega/selecionar_motorista_veiculo";
    }

    @GetMapping("/selecionar/{tipo}/{id}")
    public String selecionarEntidade(
            @PathVariable String tipo,
            @PathVariable String id,
            @RequestParam(required = false) String returnUrl) {
        
        String url = returnUrl != null ? returnUrl : "/entrega/novo";
        return "redirect:" + url + "?" + tipo + "Id=" + id;
    }
    
    @PostMapping
    public String cadastrarAtribuicao(@ModelAttribute DadosAtribuicao dados, Model model) {
        System.out.println("Dados recebidos: " + dados.getDriverId() + ", " + dados.getVehiclePlate());
        
        try {
            if (dados.getDriverId() == null || dados.getVehiclePlate() == null || 
                dados.getRouteDescription() == null) {
                throw new ExcecaoRegraNegocio("Dados obrigatórios ausentes");
            }

            entregaBO.cadastrarAtribuicao(dados);
            return "redirect:/entrega?sucesso=Atribuição criada com sucesso!";
            
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", "Erro: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("mensagemErro", "Falha na API: " + e.getMessage());
        }
        
        // Reexibe formulário
        model.addAttribute("motoristaSelecionado", 
            dados.getDriverId() != null ? buscarMotorista(dados.getDriverId()) : null);
        model.addAttribute("veiculoSelecionado", 
            dados.getVehiclePlate() != null ? buscarVeiculoPorPlaca(dados.getVehiclePlate()) : null);
        model.addAttribute("routeDescription", dados.getRouteDescription());
        model.addAttribute("invoiceKeys", dados.getInvoiceKeys());
        
        return "entrega/formulario_entrega";
    }

    // Métodos auxiliares para reexibir dados (opcional)
    private Motorista buscarMotorista(UUID id) {
        try {
            return new MotoristaBO().buscarPorId(id);
        } catch (ExcecaoRegraNegocio e) {
            return null;
        }
    }

    private Veiculo buscarVeiculoPorPlaca(String placa) {
        try {
            VeiculoBO veiculoBO = new VeiculoBO();
            // Você precisará adicionar um método listarPorPlaca no VeiculoBO/DAO
            List<Veiculo> veiculos = veiculoBO.listarPorPlaca(placa, 1, 1);
            return veiculos.isEmpty() ? null : veiculos.get(0);
        } catch (ExcecaoRegraNegocio e) {
            return null;
        }
    }
}
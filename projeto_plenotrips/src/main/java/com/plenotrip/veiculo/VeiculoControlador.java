package com.plenotrip.veiculo;

import com.plenotrip.nucleo.excecao.ExcecaoRegraNegocio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/veiculo")
public class VeiculoControlador {

    private final VeiculoBO veiculoBO = new VeiculoBO();

    @GetMapping
    public String listar(
    		@RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            Model model) {

        // ✅ Inicializa com lista vazia por padrão
        model.addAttribute("veiculos", new ArrayList<Veiculo>());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("tamanhoPagina", tamanho);
        model.addAttribute("temProximaPagina", false);

        try {
            VeiculoBO.ResultadoPaginado resultado = veiculoBO.listar(pagina, tamanho);
            model.addAttribute("veiculos", resultado.getVeiculos());
            model.addAttribute("temProximaPagina", resultado.isTemProximaPagina());
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
        }
        return "veiculo/lista_veiculo";
    }
    
    private Veiculo buscarVeiculoPorPlaca(String placa) {
        try {
            VeiculoBO veiculoBO = new VeiculoBO();
            List<Veiculo> veiculos = veiculoBO.listarPorPlaca(placa, 1, 1);
            return veiculos.isEmpty() ? null : veiculos.get(0);
        } catch (ExcecaoRegraNegocio e) {
            return null;
        }
    }

    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("veiculo", new Veiculo());
        model.addAttribute("modoEdicao", false);
        return "veiculo/formulario_veiculo";
    }

    @PostMapping
    public String cadastrarVeiculo(@ModelAttribute Veiculo veiculo, Model model) {
        try {
            veiculoBO.salvar(veiculo);
            return "redirect:/veiculo";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("modoEdicao", false);
            return "veiculo/formulario_veiculo";
        }
    }

    @GetMapping("/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable String id, Model model) {
        try {
            UUID uuid = UUID.fromString(id);
            Veiculo veiculo = veiculoBO.buscarPorId(uuid);
            model.addAttribute("veiculo", veiculo);
            model.addAttribute("modoEdicao", true);
        } catch (IllegalArgumentException | ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", "ID de veículo inválido ou não encontrado.");
            return "redirect:/veiculo";
        }
        return "veiculo/formulario_veiculo";
    }

    @PostMapping("/atualizar")
    public String atualizarVeiculo(@ModelAttribute Veiculo veiculo, Model model) {
        try {
            veiculoBO.atualizar(veiculo);
            return "redirect:/veiculo?sucesso=Veículo atualizado com sucesso!";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("modoEdicao", true);
            return "veiculo/formulario_veiculo";
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluirVeiculo(@PathVariable String id, Model model) {
        try {
            UUID uuid = UUID.fromString(id);
            veiculoBO.excluir(uuid);
            return "redirect:/veiculo?sucesso=Veículo excluído com sucesso!";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensagemErro", "ID de veículo inválido.");
            return "redirect:/veiculo";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "redirect:/veiculo";
        }
    }
}
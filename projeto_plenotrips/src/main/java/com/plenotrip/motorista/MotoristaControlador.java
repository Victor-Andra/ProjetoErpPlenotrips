package com.plenotrip.motorista;

import com.plenotrip.nucleo.excecao.ExcecaoRegraNegocio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

@Controller
@RequestMapping("/motorista")
public class MotoristaControlador {

    private final MotoristaBO motoristaBO = new MotoristaBO();

    @GetMapping
    public String listarMotoristas(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestParam(required = false) String sucesso,
            Model model) {

        model.addAttribute("motoristas", new ArrayList<Motorista>());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("tamanhoPagina", tamanho);
        model.addAttribute("temProximaPagina", false);
        
        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        try {
            MotoristaBO.ResultadoPaginado resultado = motoristaBO.listar(pagina, tamanho);
            model.addAttribute("motoristas", resultado.getMotoristas());
            model.addAttribute("temProximaPagina", resultado.isTemProximaPagina());
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
        }
        return "motorista/lista_motorista";
    }

    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("motorista", new Motorista());
        model.addAttribute("modoEdicao", false);
        return "motorista/formulario_motorista";
    }

    @PostMapping
    public String cadastrarMotorista(@ModelAttribute Motorista motorista, Model model) {
        try {
            motoristaBO.salvar(motorista);
            return "redirect:/motorista?sucesso=Motorista cadastrado com sucesso!";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("modoEdicao", false);
            return "motorista/formulario_motorista";
        }
    }

    @GetMapping("/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable String id, Model model) {
        try {
            UUID uuid = UUID.fromString(id);
            Motorista motorista = motoristaBO.buscarPorId(uuid);
            model.addAttribute("motorista", motorista);
            model.addAttribute("modoEdicao", true);
        } catch (IllegalArgumentException | ExcecaoRegraNegocio e) {
        	String mensagem = URLEncoder.encode("ID de motorista inválido ou não encontrado.", StandardCharsets.UTF_8);
            model.addAttribute("mensagemErro", mensagem);
            return "redirect:/motorista";
        }
        return "motorista/formulario_motorista";
    }

    @PostMapping("/atualizar")
    public String atualizarMotorista(@ModelAttribute Motorista motorista, Model model) {
        try {
            motoristaBO.atualizar(motorista);
            return "redirect:/motorista?sucesso=Motorista atualizado com sucesso!";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("modoEdicao", true);
            return "motorista/formulario_motorista";
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluirMotorista(@PathVariable String id, Model model) {
        try {
            UUID uuid = UUID.fromString(id);
            motoristaBO.excluir(uuid);
            String mensagem = URLEncoder.encode("Motorista excluído com sucesso!", StandardCharsets.UTF_8);
            return "redirect:/motorista?sucesso=" + mensagem;
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensagemErro", "ID de motorista inválido.");
            return "redirect:/motorista";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "redirect:/motorista";
        }
    }
}
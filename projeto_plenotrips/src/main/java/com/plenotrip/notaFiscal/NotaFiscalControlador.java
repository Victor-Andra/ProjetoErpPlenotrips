package com.plenotrip.notaFiscal;

import com.plenotrip.nucleo.excecao.ExcecaoRegraNegocio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@Controller
@RequestMapping("/nota_fiscal")
public class NotaFiscalControlador {

    private final NotaFiscalBO notaFiscalBO = new NotaFiscalBO();

    @GetMapping
    public String listarNotasFiscais(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int tamanho,
            @RequestParam(required = false) String sucesso,
            Model model) {

        model.addAttribute("notasFiscais", new ArrayList<NotaFiscal>());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("tamanhoPagina", tamanho);
        model.addAttribute("temProximaPagina", false);
        
        if (sucesso != null) {
            model.addAttribute("mensagemSucesso", sucesso);
        }

        try {
            NotaFiscalBO.ResultadoPaginado resultado = notaFiscalBO.listar(pagina, tamanho);
            model.addAttribute("notasFiscais", resultado.getNotasFiscais());
            model.addAttribute("temProximaPagina", resultado.isTemProximaPagina());
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
        }
        return "notaFiscal/lista_nota_fiscal";
    }

    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("notaFiscal", new NotaFiscal());
        model.addAttribute("modoEdicao", false);
        return "notaFiscal/formulario_nota_fiscal";
    }

    @PostMapping
    public String cadastrarNotaFiscal(@ModelAttribute NotaFiscal notaFiscal, Model model) {
        try {
            notaFiscalBO.salvar(notaFiscal);
            return "redirect:/notaFiscal?sucesso=Nota fiscal cadastrada com sucesso!";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("modoEdicao", false);
            return "notaFiscal/formulario_nota_fiscal";
        }
    }

    @GetMapping("/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable String id, Model model) {
        try {
            UUID uuid = UUID.fromString(id);
            NotaFiscal notaFiscal = notaFiscalBO.buscarPorId(uuid);
            model.addAttribute("notaFiscal", notaFiscal);
            model.addAttribute("modoEdicao", true);
        } catch (IllegalArgumentException | ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", "ID de nota fiscal inválido ou não encontrado.");
            return "nota_fiscal/formulario_nota_fiscal";
        }
        return "nota_fiscal/formulario_nota_fiscal";
    }

    @PostMapping("/atualizar")
    public String atualizarNotaFiscal(@ModelAttribute NotaFiscal notaFiscal, Model model) {
        try {
            notaFiscalBO.atualizar(notaFiscal);
            return "redirect:/nota_fiscal?sucesso=Nota fiscal atualizada com sucesso!";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            model.addAttribute("modoEdicao", true);
            return "notaFiscal/formulario_nota_fiscal";
        }
    }

    @GetMapping("/excluir/{id}")
    public String excluirNotaFiscal(@PathVariable String id, Model model) {
        try {
            UUID uuid = UUID.fromString(id);
            notaFiscalBO.excluir(uuid);
            return "redirect:/notaFiscal?sucesso=Nota fiscal excluida com sucesso!";
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensagemErro", "ID de nota fiscal invalido.");
            return "redirect:/notaFiscal";
        } catch (ExcecaoRegraNegocio e) {
            model.addAttribute("mensagemErro", e.getMessage());
            return "redirect:/notaFiscal";
        }
    }
}
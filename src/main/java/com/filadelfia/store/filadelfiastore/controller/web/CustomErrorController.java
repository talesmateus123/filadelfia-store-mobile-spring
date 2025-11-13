package com.filadelfia.store.filadelfiastore.controller.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("pageTitle", "Página Não Encontrada");
                model.addAttribute("errorMessage", "A página que você está procurando não existe ou foi movida.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("pageTitle", "Erro Interno do Servidor");
                model.addAttribute("errorMessage", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("pageTitle", "Acesso Negado");
                model.addAttribute("errorMessage", "Você não tem permissão para acessar esta página.");
            } else {
                model.addAttribute("errorCode", statusCode);
                model.addAttribute("pageTitle", "Erro " + statusCode);
                model.addAttribute("errorMessage", "Ocorreu um erro inesperado.");
            }
        } else {
            model.addAttribute("errorCode", "404");
            model.addAttribute("pageTitle", "Página Não Encontrada");
            model.addAttribute("errorMessage", "A página que você está procurando não existe.");
        }
        
        model.addAttribute("path", path != null ? path.toString() : "N/A");
        model.addAttribute("timestamp", new Date());
        
        return "error";
    }
}
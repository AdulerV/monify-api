package com.monify_api.service;

import com.monify_api.exception.RegraNegocioException;
import com.monify_api.model.entity.Usuario;
import com.monify_api.model.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// Identifica o dono dos dados a partir do token JWT da requisicao.
// Todos os services de dominio dependem desta classe para isolar os dados por usuario
// (e ela e o ponto natural para objetos mock nos testes unitarios).
@Service
public class UsuarioLogadoService {

    private final UsuarioRepository repository;

    public UsuarioLogadoService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario getUsuarioLogado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null || autenticacao instanceof AnonymousAuthenticationToken) {
            throw new RegraNegocioException("Usuário não autenticado");
        }
        return repository.findByLogin(autenticacao.getName())
                .orElseThrow(() -> new RegraNegocioException("Usuário não autenticado"));
    }
}

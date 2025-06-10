package com.senai.sistema.controller;

import com.senai.sistema.entity.Usuario;
import com.senai.sistema.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    // Injeção de dependência do UsuarioService via construtor
    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/novoUsuario") // Mapeia requisições POST para /api/usuarios
    public ResponseEntity<Usuario> criarUsuario(@RequestBody @Valid Usuario usuario) {
        // O @Valid aciona as validações definidas na entidade Usuario.
        // Se a validação falhar, uma MethodArgumentNotValidException será lançada e tratada abaixo.

        // Chama o serviço para criar o usuário
        Usuario createdUsuario = usuarioService.createUser(usuario);

        // --- CUIDADO DE SEGURANÇA: REMOVER A SENHA ANTES DE ENVIAR A RESPOSTA ---
        createdUsuario.setSenha(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUsuario);
    }

    @GetMapping("/buscar") // Mapeia requisições GET para /api/usuarios
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        // --- CUIDADO DE SEGURANÇA: REMOVER A SENHA PARA CADA USUÁRIO NA LISTA ---
        usuarios.forEach(u -> u.setSenha(null));
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/buscar/{id}") // Mapeia requisições GET para /api/usuarios/{id}
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuarioOptional = usuarioService.getUsuarioById(id);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            // --- CUIDADO DE SEGURANÇA: REMOVER A SENHA ANTES DE ENVIAR A RESPOSTA ---
            usuario.setSenha(null);
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.notFound().build(); // Retorna 404 Not Found
        }
    }

    @PutMapping("/editar/{id}") // Mapeia requisições PUT para /api/usuarios/{id}
    public ResponseEntity<Usuario> updateUser(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        // O @Valid aciona as validações na entidade Usuario.
        Optional<Usuario> updatedUsuarioOptional = usuarioService.updateUser(id, usuario);
        if (updatedUsuarioOptional.isPresent()) {
            Usuario updatedUsuario = updatedUsuarioOptional.get();
            // --- CUIDADO DE SEGURANÇA: REMOVER A SENHA ANTES DE ENVIAR A RESPOSTA ---
            updatedUsuario.setSenha(null);
            return ResponseEntity.ok(updatedUsuario);
        } else {
            return ResponseEntity.notFound().build(); // Retorna 404 Not Found
        }
    }

    @DeleteMapping("/deletar/{id}") // Mapeia requisições DELETE para /api/usuarios/{id}
    public ResponseEntity<String> deleteUser(@PathVariable Long id) { // Retorno ResponseEntity<String>
        boolean deletado = usuarioService.deleteUser(id); // Armazena o resultado em uma variável booleana
        if (deletado) {
            return ResponseEntity.ok("Usuário removido com sucesso."); // Mensagem de sucesso
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) // Status 404 Not Found
                    .body("Erro ao remover usuário. Usuário não encontrado ou já removido."); // Mensagem de erro
        }
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Coleta todas as mensagens de erro dos campos que falharam na validação
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());

        // Cria um mapa para formatar a resposta JSON (ex: {"errors": ["msg1", "msg2"]})
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);

        // Retorna a resposta com o status HTTP 400 Bad Request
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

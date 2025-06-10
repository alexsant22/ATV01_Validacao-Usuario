package com.senai.sistema.service;

import com.senai.sistema.entity.Usuario;
import com.senai.sistema.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Buscar todos os usuários (retorna lista de entidades)
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    // Buscar por ID (retorna Optional<Usuario>)
    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    // Criar novo usuário (recebe entidade, retorna entidade)
    @Transactional
    public Usuario createUser(Usuario usuario) {
        // Lógica de Negócio: verificar se o e-mail já existe
        Optional<Usuario> existingUser = usuarioRepository.findByEmail(usuario.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("O e-mail '" + usuario.getEmail() + "' já está em uso.");
        }

        return usuarioRepository.save(usuario);
    }

    // Atualizar usuário existente (recebe ID e entidade, retorna Optional<Usuario>)
    @Transactional
    public Optional<Usuario> updateUser(Long id, Usuario usuarioAtualizado) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);

        if (optionalUsuario.isPresent()) {
            Usuario usuarioExistente = optionalUsuario.get();

            // Atualiza os dados do usuário existente com os dados de 'usuarioAtualizado'.
            usuarioExistente.setNome(usuarioAtualizado.getNome());
            usuarioExistente.setEmail(usuarioAtualizado.getEmail());
            usuarioExistente.setSenha(usuarioAtualizado.getSenha());

            Usuario usuarioSalvo = usuarioRepository.save(usuarioExistente);
            return Optional.of(usuarioSalvo); // Retorna o usuário atualizado
        } else {
            return Optional.empty(); // Usuário não encontrado
        }
    }

    // Deletar usuário (retorna booleano indicando sucesso)
    @Transactional
    public boolean deleteUser(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true; // Sucesso na exclusão
        } else {
            return false; // Usuário não encontrado
        }
    }
}

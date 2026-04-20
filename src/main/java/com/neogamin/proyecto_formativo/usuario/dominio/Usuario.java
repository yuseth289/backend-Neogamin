package com.neogamin.proyecto_formativo.usuario.dominio;

import com.neogamin.proyecto_formativo.compartido.dominio.EntidadBase;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario extends EntidadBase implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 30)
    private String telefono;

    @Column(name = "numero_documento", length = 30)
    private String numeroDocumento;

    @Column(name = "sobre_mi", length = 500)
    private String sobreMi;

    @Column(name = "foto_perfil_url", length = 500)
    private String fotoPerfilUrl;

    @Column(name = "prefiere_noticias", nullable = false)
    private Boolean prefiereNoticias = false;

    @Column(name = "prefiere_ofertas", nullable = false)
    private Boolean prefiereOfertas = false;

    @Column(nullable = false)
    private RolUsuario rol;

    @Column(nullable = false)
    private EstadoGenerico estado;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return estado == EstadoGenerico.ACTIVO && deletedAt == null;
    }
}

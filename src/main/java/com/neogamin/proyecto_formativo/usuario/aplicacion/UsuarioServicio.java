package com.neogamin.proyecto_formativo.usuario.aplicacion;

import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.usuario.api.dto.ActualizarPerfilUsuarioRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.ConvertirseVendedorRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.DireccionResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.GuardarDireccionRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.PerfilUsuarioResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.UsuarioResponse;
import com.neogamin.proyecto_formativo.usuario.dominio.Direccion;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.DireccionRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServicio {

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final DireccionRepositorioJpa direccionRepositorioJpa;

    @Transactional(readOnly = true)
    public UsuarioResponse perfilActual() {
        var usuario = usuarioRepositorioJpa.findById(SeguridadUtils.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return toResponse(usuario);
    }

    @Transactional(readOnly = true)
    public PerfilUsuarioResponse obtenerPerfilActual() {
        return toPerfilResponse(cargarUsuarioActual());
    }

    @Transactional
    public PerfilUsuarioResponse actualizarPerfilActual(ActualizarPerfilUsuarioRequest request) {
        var usuario = cargarUsuarioActual();

        var nombre = normalizarTextoObligatorio(request.nombre(), "El nombre es obligatorio", 120);
        var email = normalizarEmail(request.email());
        var telefono = normalizarTelefono(request.telefono());
        var sobreMi = normalizarTextoOpcional(request.sobreMi(), 500);
        var fotoPerfilUrl = normalizarUrlOpcional(request.fotoPerfilUrl(), 500);

        usuarioRepositorioJpa.findByEmailIgnoreCase(email)
                .filter(otro -> !otro.getId().equals(usuario.getId()))
                .ifPresent(otro -> {
                    throw new BadRequestException("El correo electrónico ya está registrado por otro usuario");
                });

        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setSobreMi(sobreMi);
        usuario.setFotoPerfilUrl(fotoPerfilUrl);
        usuario.setPrefiereNoticias(Boolean.TRUE.equals(request.prefiereNoticias()));
        usuario.setPrefiereOfertas(Boolean.TRUE.equals(request.prefiereOfertas()));

        return toPerfilResponse(usuarioRepositorioJpa.save(usuario));
    }

    @Transactional
    public UsuarioResponse convertirseEnVendedor(ConvertirseVendedorRequest request) {
        var usuario = usuarioRepositorioJpa.findById(SeguridadUtils.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == RolUsuario.ADMIN) {
            return toResponse(usuario);
        }

        var numeroDocumento = request.numeroDocumento().trim();
        usuarioRepositorioJpa.findByNumeroDocumento(numeroDocumento)
                .filter(otro -> !otro.getId().equals(usuario.getId()))
                .ifPresent(otro -> {
                    throw new BadRequestException("El número de documento ya está registrado por otro usuario");
                });

        usuario.setNumeroDocumento(numeroDocumento);
        usuario.setRol(RolUsuario.VENDEDOR);

        return toResponse(usuarioRepositorioJpa.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<DireccionResponse> listarMisDirecciones() {
        return direccionRepositorioJpa.findByUsuarioIdAndEstadoAndDeletedAtIsNullOrderByPrincipalDescCreatedAtDesc(
                        SeguridadUtils.usuarioId(),
                        EstadoGenerico.ACTIVO
                ).stream()
                .map(this::toDireccionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DireccionResponse obtenerMiDireccion(Long idDireccion) {
        return toDireccionResponse(cargarDireccionActivaPropia(idDireccion));
    }

    @Transactional
    public DireccionResponse crearDireccion(GuardarDireccionRequest request) {
        var usuario = cargarUsuarioActual();
        var direccion = new Direccion();
        direccion.setUsuario(usuario);
        aplicarDireccion(direccion, request);

        var primeraActiva = !direccionRepositorioJpa.existsByUsuarioIdAndEstadoAndDeletedAtIsNull(
                usuario.getId(),
                EstadoGenerico.ACTIVO
        );
        var debeSerPrincipal = primeraActiva || Boolean.TRUE.equals(request.esPrincipal());
        direccion.setPrincipal(debeSerPrincipal);
        if (debeSerPrincipal) {
            desmarcarDireccionesPrincipales(usuario.getId());
        }

        return toDireccionResponse(direccionRepositorioJpa.save(direccion));
    }

    @Transactional
    public DireccionResponse actualizarDireccion(Long idDireccion, GuardarDireccionRequest request) {
        var direccion = cargarDireccionActivaPropia(idDireccion);
        aplicarDireccion(direccion, request);

        if (Boolean.TRUE.equals(request.esPrincipal())) {
            desmarcarDireccionesPrincipales(direccion.getUsuario().getId());
            direccion.setPrincipal(true);
        }

        return toDireccionResponse(direccionRepositorioJpa.save(direccion));
    }

    @Transactional
    public DireccionResponse marcarPrincipal(Long idDireccion) {
        var direccion = cargarDireccionActivaPropia(idDireccion);
        desmarcarDireccionesPrincipales(direccion.getUsuario().getId());
        direccion.setPrincipal(true);
        return toDireccionResponse(direccionRepositorioJpa.save(direccion));
    }

    @Transactional
    public void eliminarDireccion(Long idDireccion) {
        var direccion = cargarDireccionActivaPropia(idDireccion);
        direccion.setEstado(EstadoGenerico.INACTIVO);
        direccion.setDeletedAt(OffsetDateTime.now());
        direccion.setPrincipal(false);
        direccionRepositorioJpa.save(direccion);

        var restantes = direccionRepositorioJpa.findByUsuarioIdAndEstadoAndDeletedAtIsNullOrderByPrincipalDescCreatedAtDesc(
                direccion.getUsuario().getId(),
                EstadoGenerico.ACTIVO
        );
        if (!restantes.isEmpty() && restantes.stream().noneMatch(Direccion::getPrincipal)) {
            var nuevaPrincipal = restantes.get(0);
            nuevaPrincipal.setPrincipal(true);
            direccionRepositorioJpa.save(nuevaPrincipal);
        }
    }

    private Usuario cargarUsuarioActual() {
        return usuarioRepositorioJpa.findById(SeguridadUtils.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private Direccion cargarDireccionActivaPropia(Long idDireccion) {
        return direccionRepositorioJpa.findByIdAndUsuarioIdAndEstadoAndDeletedAtIsNull(
                        idDireccion,
                        SeguridadUtils.usuarioId(),
                        EstadoGenerico.ACTIVO
                )
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada"));
    }

    private void aplicarDireccion(Direccion direccion, GuardarDireccionRequest request) {
        direccion.setTipo(request.tipo());
        direccion.setPais(normalizarTextoObligatorio(request.pais(), "El país es obligatorio", 80));
        direccion.setDepartamento(normalizarTextoOpcional(request.departamento(), 100));
        direccion.setCiudad(normalizarTextoObligatorio(request.ciudad(), "La ciudad es obligatoria", 100));
        direccion.setComuna(normalizarTextoOpcional(request.comuna(), 100));
        direccion.setCodigoPostal(normalizarTextoOpcional(request.codigoPostal(), 20));
        direccion.setCalle(normalizarTextoObligatorio(request.calle(), "La calle es obligatoria", 150));
        direccion.setNumero(normalizarTextoObligatorio(request.numero(), "El número es obligatorio", 30));
        direccion.setReferencia(normalizarTextoOpcional(request.referencia(), 255));
        direccion.setEstado(EstadoGenerico.ACTIVO);
        direccion.setDeletedAt(null);
    }

    private void desmarcarDireccionesPrincipales(Long usuarioId) {
        direccionRepositorioJpa.findByUsuarioIdAndEstadoAndDeletedAtIsNullOrderByPrincipalDescCreatedAtDesc(
                        usuarioId,
                        EstadoGenerico.ACTIVO
                ).forEach(dir -> dir.setPrincipal(false));
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("El correo electrónico es obligatorio");
        }
        var normalizado = email.trim().toLowerCase();
        if (normalizado.length() > 190) {
            throw new BadRequestException("El correo electrónico supera la longitud permitida");
        }
        return normalizado;
    }

    private String normalizarTelefono(String telefono) {
        var normalizado = normalizarTextoOpcional(telefono, 30);
        if (normalizado != null && (normalizado.length() < 7 || normalizado.length() > 20)) {
            throw new BadRequestException("El teléfono debe tener una longitud razonable");
        }
        return normalizado;
    }

    private String normalizarUrlOpcional(String url, int maximo) {
        var normalizada = normalizarTextoOpcional(url, maximo);
        if (normalizada == null) {
            return null;
        }
        try {
            var uri = new URI(normalizada);
            if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
                throw new BadRequestException("La foto de perfil debe ser una URL válida");
            }
            return normalizada;
        } catch (URISyntaxException ex) {
            throw new BadRequestException("La foto de perfil debe ser una URL válida");
        }
    }

    private String normalizarTextoObligatorio(String valor, String mensaje, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new BadRequestException(mensaje);
        }
        var normalizado = valor.trim();
        if (normalizado.length() > maximo) {
            throw new BadRequestException("Uno de los campos supera la longitud permitida");
        }
        return normalizado;
    }

    private String normalizarTextoOpcional(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        var normalizado = valor.trim();
        if (normalizado.isEmpty()) {
            return null;
        }
        if (normalizado.length() > maximo) {
            throw new BadRequestException("Uno de los campos supera la longitud permitida");
        }
        return normalizado;
    }

    private UsuarioResponse toResponse(com.neogamin.proyecto_formativo.usuario.dominio.Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getNumeroDocumento(),
                usuario.getRol().name(),
                usuario.getEstado().name()
        );
    }

    private PerfilUsuarioResponse toPerfilResponse(Usuario usuario) {
        return new PerfilUsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getNumeroDocumento(),
                usuario.getSobreMi(),
                usuario.getFotoPerfilUrl(),
                Boolean.TRUE.equals(usuario.getPrefiereNoticias()),
                Boolean.TRUE.equals(usuario.getPrefiereOfertas()),
                usuario.getRol().name(),
                usuario.getEstado().name()
        );
    }

    private DireccionResponse toDireccionResponse(Direccion direccion) {
        if (!direccion.getUsuario().getId().equals(SeguridadUtils.usuarioId())) {
            throw new ForbiddenException("No puedes operar sobre direcciones de otro usuario");
        }
        return new DireccionResponse(
                direccion.getId(),
                direccion.getTipo().name(),
                Boolean.TRUE.equals(direccion.getPrincipal()),
                direccion.getPais(),
                direccion.getDepartamento(),
                direccion.getCiudad(),
                direccion.getComuna(),
                direccion.getCodigoPostal(),
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getReferencia(),
                direccion.getEstado().name()
        );
    }
}

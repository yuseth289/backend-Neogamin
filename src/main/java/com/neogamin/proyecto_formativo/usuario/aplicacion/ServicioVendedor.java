package com.neogamin.proyecto_formativo.usuario.aplicacion;

import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.usuario.api.dto.ConvertirVendedorRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.DatosPagoResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.VendedorResponse;
import com.neogamin.proyecto_formativo.usuario.dominio.DatosPagoEntidad;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.TipoCuentaPago;
import com.neogamin.proyecto_formativo.usuario.dominio.TipoDocumentoVendedor;
import com.neogamin.proyecto_formativo.usuario.dominio.VendedorEntidad;
import com.neogamin.proyecto_formativo.usuario.infraestructura.DatosPagoRepositorio;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.VendedorRepositorio;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioVendedor {

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final VendedorRepositorio vendedorRepositorio;
    private final DatosPagoRepositorio datosPagoRepositorio;

    @Transactional
    @PreAuthorize("hasRole('CLIENTE')")
    public VendedorResponse convertirEnVendedor(ConvertirVendedorRequest request) {
        var usuario = usuarioRepositorioJpa.findById(SeguridadUtils.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (usuario.getRol() != RolUsuario.CLIENTE) {
            throw new BadRequestException("Solo un usuario cliente puede convertirse en vendedor");
        }

        if (vendedorRepositorio.existsByUsuarioId(usuario.getId())) {
            throw new BadRequestException("El usuario ya tiene un perfil de vendedor registrado");
        }

        String numeroDocumento = request.numeroDocumento().trim();
        if (vendedorRepositorio.existsByNumeroDocumentoIgnoreCase(numeroDocumento)
                || usuarioRepositorioJpa.findByNumeroDocumento(numeroDocumento).isPresent()) {
            throw new BadRequestException("El numero de documento ya esta registrado en el sistema");
        }

        validarTelefono(request.telefono());

        VendedorEntidad vendedor = new VendedorEntidad();
        vendedor.setUsuario(usuario);
        vendedor.setNombreCompletoORazonSocial(request.nombreCompletoORazonSocial().trim());
        vendedor.setTipoDocumento(parseTipoDocumento(request.tipoDocumento()));
        vendedor.setNumeroDocumento(numeroDocumento);
        vendedor.setPais(request.pais().trim());
        vendedor.setTelefono(request.telefono().trim());
        vendedor.setCorreo(request.correo().trim().toLowerCase(Locale.ROOT));
        vendedor.setNombreComercial(request.nombreComercial().trim());
        vendedor.setAceptaTerminos(request.aceptaTerminos());

        VendedorEntidad vendedorGuardado = vendedorRepositorio.save(vendedor);

        DatosPagoEntidad datosPagoGuardados = null;
        if (request.datosPago() != null) {
            DatosPagoEntidad datosPago = new DatosPagoEntidad();
            datosPago.setVendedor(vendedorGuardado);
            datosPago.setTipoCuenta(parseTipoCuenta(request.datosPago().tipoCuenta()));
            datosPago.setNumeroCuenta(request.datosPago().numeroCuenta().trim());
            datosPago.setBanco(request.datosPago().banco().trim());
            datosPago.setTitularCuenta(request.datosPago().titularCuenta().trim());
            datosPagoGuardados = datosPagoRepositorio.save(datosPago);
        }

        usuario.setRol(RolUsuario.VENDEDOR);
        usuarioRepositorioJpa.save(usuario);

        return new VendedorResponse(
                vendedorGuardado.getId(),
                usuario.getId(),
                usuario.getRol().name(),
                vendedorGuardado.getNombreCompletoORazonSocial(),
                vendedorGuardado.getTipoDocumento().name(),
                vendedorGuardado.getNumeroDocumento(),
                vendedorGuardado.getPais(),
                vendedorGuardado.getTelefono(),
                vendedorGuardado.getCorreo(),
                vendedorGuardado.getNombreComercial(),
                vendedorGuardado.getAceptaTerminos(),
                datosPagoGuardados != null
                        ? new DatosPagoResponse(
                        datosPagoGuardados.getTipoCuenta().name(),
                        datosPagoGuardados.getNumeroCuenta(),
                        datosPagoGuardados.getBanco(),
                        datosPagoGuardados.getTitularCuenta())
                        : null
        );
    }

    private void validarTelefono(String telefono) {
        String digits = telefono.replaceAll("\\D", "");
        if (digits.length() < 7 || digits.length() > 15) {
            throw new BadRequestException("El telefono no tiene un formato valido");
        }
    }

    private TipoDocumentoVendedor parseTipoDocumento(String tipoDocumento) {
        try {
            return TipoDocumentoVendedor.valueOf(tipoDocumento.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El tipo de documento no es valido");
        }
    }

    private TipoCuentaPago parseTipoCuenta(String tipoCuenta) {
        try {
            return TipoCuentaPago.valueOf(tipoCuenta.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El tipo de cuenta no es valido");
        }
    }
}

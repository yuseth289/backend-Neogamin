package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.infraestructura.ConsultaBusquedaProducto;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class InterpretadorConsultaProducto {

    private static final Pattern ESPACIOS = Pattern.compile("\\s+");
    private static final Pattern CARACTERES_NO_ALFANUM = Pattern.compile("[^\\p{L}\\p{Nd}\\s-]");
    private static final Set<String> STOPWORDS = Set.of(
            "de", "del", "la", "las", "el", "los", "para", "con", "sin", "y", "o", "en", "por", "un", "una", "unos", "unas"
    );
    private static final Set<String> INTENCION_BARATO = Set.of(
            "barato", "barata", "baratos", "baratas", "economico", "economica", "economicos", "economicas"
    );
    private static final Set<String> INTENCION_PREMIUM = Set.of(
            "premium", "alta", "alto", "gama", "highend", "tope", "pro"
    );
    private static final Set<String> INTENCION_OFERTA = Set.of(
            "oferta", "ofertas", "descuento", "descuentos", "promo", "promos", "promocion", "promociones", "rebaja", "rebajas"
    );
    private static final Map<String, List<String>> MARCAS = Map.of(
            "samsung", List.of("samsung"),
            "iphone", List.of("iphone", "apple"),
            "sony", List.of("sony"),
            "anker", List.of("anker"),
            "xiaomi", List.of("xiaomi", "redmi"),
            "hp", List.of("hp"),
            "lenovo", List.of("lenovo"),
            "asus", List.of("asus")
    );
    private static final Map<String, List<String>> ATRIBUTOS = Map.of(
            "bluetooth", List.of("bluetooth", "inalambrico"),
            "usb c", List.of("usb c", "usb-c", "tipo c", "type c"),
            "ram", List.of("ram", "8gb ram", "16gb ram", "32gb ram"),
            "gb", List.of("64gb", "128gb", "256gb", "512gb", "1tb")
    );
    private static final Map<String, List<String>> SINONIMOS = Map.of(
            "portatil", List.of("portatil", "laptop", "notebook"),
            "celular", List.of("celular", "telefono", "smartphone"),
            "celulares", List.of("celulares", "telefono", "smartphone"),
            "audifonos", List.of("audifonos", "auriculares", "headphones"),
            "cargador", List.of("cargador", "charger"),
            "nevera", List.of("nevera", "refrigerador")
    );
    private static final Map<String, List<String>> TIPOS_PRODUCTO = Map.of(
            "smartphones", List.of("celular", "celulares", "telefono", "telefonos", "smartphone", "smartphones", "movil", "moviles", "android"),
            "computadores", List.of("computador", "computadores", "pc", "laptop", "portatil", "portatiles", "notebook"),
            "audifonos", List.of("audifono", "audifonos", "auriculares", "headphones"),
            "cargadores", List.of("cargador", "cargadores", "charger"),
            "electrodomesticos", List.of("nevera", "refrigerador", "lavadora", "microondas")
    );

    public ConsultaProductoInterpretada interpretar(String textoOriginal) {
        var textoNormalizado = normalizarTextoBusqueda(textoOriginal);
        var textoAscii = aAscii(textoNormalizado);

        LinkedHashSet<String> terminos = new LinkedHashSet<>();
        LinkedHashSet<String> aliasTipoProducto = new LinkedHashSet<>();
        LinkedHashSet<String> marcasDetectadas = new LinkedHashSet<>();
        LinkedHashSet<String> atributosDetectados = new LinkedHashSet<>();

        detectarSinonimos(textoAscii, terminos);
        detectarTipoProducto(textoAscii, terminos, aliasTipoProducto);
        detectarMarcas(textoAscii, terminos, marcasDetectadas);
        detectarAtributos(textoAscii, terminos, atributosDetectados);
        agregarTokensLibres(textoNormalizado, terminos);

        if (terminos.isEmpty() && !detectarIntencionOferta(textoAscii)) {
            terminos.add(textoNormalizado);
        }

        return new ConsultaProductoInterpretada(
                textoOriginal.trim(),
                textoNormalizado,
                new ArrayList<>(terminos),
                new ArrayList<>(aliasTipoProducto),
                detectarIntencionOferta(textoAscii),
                new ArrayList<>(marcasDetectadas),
                new ArrayList<>(atributosDetectados),
                detectarIntencionPrecio(textoAscii)
        );
    }

    private void detectarSinonimos(String textoAscii, LinkedHashSet<String> terminos) {
        SINONIMOS.forEach((disparador, equivalencias) -> {
            if (textoAscii.contains(disparador)) {
                terminos.addAll(equivalencias);
            }
        });
    }

    private void detectarTipoProducto(String textoAscii, LinkedHashSet<String> terminos, LinkedHashSet<String> aliasTipoProducto) {
        TIPOS_PRODUCTO.forEach((tipoCanonico, alias) -> {
            if (alias.stream().anyMatch(textoAscii::contains)) {
                aliasTipoProducto.add(tipoCanonico);
                aliasTipoProducto.addAll(alias);
                terminos.addAll(alias);
            }
        });
    }

    private void detectarMarcas(String textoAscii, LinkedHashSet<String> terminos, LinkedHashSet<String> marcasDetectadas) {
        MARCAS.forEach((marcaCanonica, equivalencias) -> {
            if (equivalencias.stream().anyMatch(textoAscii::contains)) {
                marcasDetectadas.add(marcaCanonica);
                terminos.addAll(equivalencias);
            }
        });
    }

    private void detectarAtributos(String textoAscii, LinkedHashSet<String> terminos, LinkedHashSet<String> atributosDetectados) {
        ATRIBUTOS.forEach((atributoCanonico, equivalencias) -> {
            if (equivalencias.stream().anyMatch(textoAscii::contains)) {
                atributosDetectados.add(atributoCanonico);
                terminos.addAll(equivalencias);
            }
        });
    }

    private void agregarTokensLibres(String textoNormalizado, LinkedHashSet<String> terminos) {
        for (String token : textoNormalizado.split(" ")) {
            var tokenAscii = aAscii(token);
            if (tokenAscii.isBlank() || STOPWORDS.contains(tokenAscii) || INTENCION_BARATO.contains(tokenAscii)
                    || INTENCION_PREMIUM.contains(tokenAscii) || INTENCION_OFERTA.contains(tokenAscii)) {
                continue;
            }
            terminos.add(token);
        }
    }

    private boolean detectarIntencionOferta(String textoAscii) {
        return INTENCION_OFERTA.stream().anyMatch(textoAscii::contains);
    }

    private ConsultaBusquedaProducto.IntencionPrecio detectarIntencionPrecio(String textoAscii) {
        boolean barato = INTENCION_BARATO.stream().anyMatch(textoAscii::contains);
        boolean premium = textoAscii.contains("gama alta") || INTENCION_PREMIUM.stream().anyMatch(textoAscii::contains);

        if (barato && !premium) {
            return ConsultaBusquedaProducto.IntencionPrecio.BAJO;
        }
        if (premium && !barato) {
            return ConsultaBusquedaProducto.IntencionPrecio.ALTO;
        }
        return ConsultaBusquedaProducto.IntencionPrecio.NEUTRO;
    }

    private String normalizarTextoBusqueda(String texto) {
        var limpio = CARACTERES_NO_ALFANUM.matcher(texto.toLowerCase(Locale.ROOT)).replaceAll(" ");
        return ESPACIOS.matcher(limpio).replaceAll(" ").trim();
    }

    private String aAscii(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}

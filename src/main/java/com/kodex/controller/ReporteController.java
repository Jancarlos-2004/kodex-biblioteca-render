package com.kodex.controller;

import com.kodex.model.EstadoMulta;
import com.kodex.model.EstadoPrestamo;
import com.kodex.model.Libro;
import com.kodex.model.Multa;
import com.kodex.model.Prestamo;
import com.kodex.repository.MultaRepository;
import com.kodex.repository.PrestamoRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador de Reportes Gerenciales y Estadísticas (SDD-18, SDD-19).
 * Accesible para ADMIN y GERENTE.
 *
 * Los agregados se calculan en memoria a partir de los repositorios para
 * evitar incompatibilidades de Hibernate 6 con COALESCE(SUM(...), 0.0) en PostgreSQL.
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;

    public ReporteController(PrestamoRepository prestamoRepository,
                             MultaRepository multaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
    }

    @GetMapping("/prestamos")
    public String reportePrestamos(@RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                   @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                   @RequestParam(value = "estado", required = false) EstadoPrestamo estado,
                                   Model model) {
        if (desde == null) {
            desde = LocalDate.now().minusMonths(1);
        }
        if (hasta == null) {
            hasta = LocalDate.now();
        }

        List<Prestamo> prestamos = prestamoRepository.filtrarPrestamos(null, estado, desde, hasta);

        // Ranking de los 5 libros más prestados (calculado sobre todo el historial).
        Map<Libro, Long> conteo = prestamoRepository.findAll().stream()
                .filter(p -> p.getEjemplar() != null && p.getEjemplar().getLibro() != null)
                .collect(Collectors.groupingBy(p -> p.getEjemplar().getLibro(), Collectors.counting()));

        List<Object[]> rankingLibros = conteo.entrySet().stream()
                .sorted(Map.Entry.<Libro, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> new Object[]{
                        e.getKey().getIdLibro(),
                        e.getKey().getTitulo(),
                        e.getKey().getAutor(),
                        e.getValue()
                })
                .collect(Collectors.toList());

        model.addAttribute("prestamos", prestamos);
        model.addAttribute("rankingLibros", rankingLibros);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoPrestamo.values());
        model.addAttribute("totalPrestamosPeriodo", prestamos.size());

        return "reportes/prestamos";
    }

    @GetMapping("/multas")
    public String reporteMultas(@RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                Model model) {
        if (desde == null) {
            desde = LocalDate.now().minusMonths(1);
        }
        if (hasta == null) {
            hasta = LocalDate.now();
        }

        List<Multa> todas = multaRepository.findAll();

        List<Multa> multasPagadas = todas.stream()
                .filter(m -> m.getEstado() == EstadoMulta.PAGADA)
                .filter(m -> m.getFechaPago() != null)
                .filter(m -> !m.getFechaPago().isBefore(desde) && !m.getFechaPago().isAfter(hasta))
                .collect(Collectors.toList());

        BigDecimal recaudoPeriodo = sumar(multasPagadas);
        BigDecimal recaudoHistorico = sumar(todas.stream()
                .filter(m -> m.getEstado() == EstadoMulta.PAGADA)
                .collect(Collectors.toList()));
        BigDecimal pendienteHistorico = sumar(todas.stream()
                .filter(m -> m.getEstado() == EstadoMulta.PENDIENTE)
                .collect(Collectors.toList()));

        model.addAttribute("multas", multasPagadas);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("recaudoPeriodo", recaudoPeriodo);
        model.addAttribute("recaudoHistorico", recaudoHistorico);
        model.addAttribute("pendienteHistorico", pendienteHistorico);
        model.addAttribute("totalMultasPagadasPeriodo", multasPagadas.size());

        return "reportes/multas";
    }

    private BigDecimal sumar(List<Multa> multas) {
        return multas.stream()
                .map(Multa::getValor)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

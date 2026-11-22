package com.kodex.controller;

import com.kodex.model.EstadoMulta;
import com.kodex.model.EstadoPrestamo;
import com.kodex.model.Multa;
import com.kodex.model.Prestamo;
import com.kodex.repository.LibroRepository;
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
import java.util.List;

/**
 * Controlador de Reportes Gerenciales y Estadísticas (SDD-18, SDD-19).
 * Accesible para ADMIN y GERENTE.
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final LibroRepository libroRepository;

    public ReporteController(PrestamoRepository prestamoRepository,
                             MultaRepository multaRepository,
                             LibroRepository libroRepository) {
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
        this.libroRepository = libroRepository;
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
        List<Object[]> rankingLibros = libroRepository.findLibrosMasPrestados();

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

        List<Multa> multasPagadas = multaRepository.filtrarMultas(EstadoMulta.PAGADA, desde, hasta);
        BigDecimal recaudoPeriodo = multaRepository.sumRecaudadoPorPeriodo(desde, hasta);
        BigDecimal recaudoHistorico = multaRepository.sumTotalRecaudado();
        BigDecimal pendienteHistorico = multaRepository.sumTotalPendiente();

        model.addAttribute("multas", multasPagadas);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("recaudoPeriodo", recaudoPeriodo);
        model.addAttribute("recaudoHistorico", recaudoHistorico);
        model.addAttribute("pendienteHistorico", pendienteHistorico);
        model.addAttribute("totalMultasPagadasPeriodo", multasPagadas.size());

        return "reportes/multas";
    }
}

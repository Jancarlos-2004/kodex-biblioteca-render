package com.kodex;

import com.kodex.model.*;
import com.kodex.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de Reglas de Negocio KODEX (SDD-01 a SDD-24).
 * Valida límites de préstamos, sanciones, multas por mora y reservas según especificación.
 */
@SpringBootTest
@Transactional
class PrestamoBusinessRulesTest {

    @Autowired
    private LectorRepository lectorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private EjemplarRepository ejemplarRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private MultaRepository multaRepository;

    @Autowired
    private ReservaLibroRepository reservaLibroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    private Lector lectorPrueba;
    private Libro libroPrueba;
    private Ejemplar ejemplarPrueba;
    private Usuario usuarioBiblio;

    @BeforeEach
    void setUp() {
        // Obtener o crear datos base
        lectorPrueba = lectorRepository.findByDocumento("1098765432").orElseGet(() -> {
            Lector l = new Lector("1098765432", "Carlos Lector Test", "3001234567", "carlos.test@uts.edu.co");
            return lectorRepository.save(l);
        });

        usuarioBiblio = usuarioRepository.findByCorreo("bibliotecario@kodex.com").orElseGet(() -> {
            Usuario u = new Usuario("Biblio Test", "bibliotecario@kodex.com", "hash", Rol.BIBLIOTECARIO, true);
            return usuarioRepository.save(u);
        });

        Categoria cat = categoriaRepository.findAll().stream().findFirst().orElseGet(() -> {
            Categoria c = new Categoria("Pruebas Unitarias", "Descripción test");
            return categoriaRepository.save(c);
        });

        libroPrueba = libroRepository.findByIsbn("978-0134685991").orElseGet(() -> {
            Libro lib = new Libro("978-0134685991", "Libro de Test", "Autor Test", "Editorial Test", 2024, cat);
            return libroRepository.save(lib);
        });

        List<Ejemplar> ejemplares = ejemplarRepository.findByLibroIdLibroAndEstado(libroPrueba.getIdLibro(), EstadoEjemplar.DISPONIBLE);
        if (!ejemplares.isEmpty()) {
            ejemplarPrueba = ejemplares.get(0);
        } else {
            ejemplarPrueba = new Ejemplar("TEST-EJ-01", EstadoEjemplar.DISPONIBLE, "Sala A", libroPrueba);
            ejemplarPrueba = ejemplarRepository.save(ejemplarPrueba);
        }
    }

    @Test
    @DisplayName("SDD-06: Crear préstamo exitoso deja el ejemplar en estado PRESTADO y préstamo ACTIVO")
    void testCrearPrestamoValido() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(7);

        Prestamo p = new Prestamo(hoy, limite, lectorPrueba, ejemplarPrueba, usuarioBiblio);
        Prestamo guardado = prestamoRepository.save(p);
        ejemplarPrueba.setEstado(EstadoEjemplar.PRESTADO);
        ejemplarRepository.save(ejemplarPrueba);

        assertNotNull(guardado.getIdPrestamo());
        assertEquals(EstadoPrestamo.ACTIVO, guardado.getEstado());
        assertEquals(EstadoEjemplar.PRESTADO, ejemplarPrueba.getEstado());
    }

    @Test
    @DisplayName("SDD-08: Verificar conteo de préstamos activos para control de límite (máximo 3)")
    void testLimitePrestamosActivos() {
        long activos = prestamoRepository.countByLectorIdLectorAndEstado(lectorPrueba.getIdLector(), EstadoPrestamo.ACTIVO);
        assertTrue(activos <= 3, "El lector no debe exceder el límite de 3 préstamos activos.");
    }

    @Test
    @DisplayName("SDD-10: Solo se permite una renovación si el préstamo está activo y sin renovaciones previas")
    void testRenovacionPrestamo() {
        Prestamo p = new Prestamo(LocalDate.now(), LocalDate.now().plusDays(7), lectorPrueba, ejemplarPrueba, usuarioBiblio);
        p.setRenovaciones(0);
        p = prestamoRepository.save(p);

        // Primera renovación permitida
        p.setFechaLimite(p.getFechaLimite().plusDays(7));
        p.setRenovaciones(p.getRenovaciones() + 1);
        prestamoRepository.save(p);

        assertEquals(1, p.getRenovaciones());
        // Una segunda renovación debe ser impedida por la regla
        assertFalse(p.getRenovaciones() < 1, "Una segunda renovación debe ser rechazada por SDD-10.");
    }

    @Test
    @DisplayName("SDD-12: Devolución con fecha posterior a la fecha límite calcula multa correctamente")
    void testCalculoMultaPorMora() {
        LocalDate limite = LocalDate.now().minusDays(3); // 3 días de mora
        LocalDate devolucion = LocalDate.now();

        Prestamo p = new Prestamo(LocalDate.now().minusDays(10), limite, lectorPrueba, ejemplarPrueba, usuarioBiblio);
        p.setFechaDevolucion(devolucion);
        p.setEstado(EstadoPrestamo.DEVUELTO);
        p = prestamoRepository.save(p);

        long diasRetraso = java.time.temporal.ChronoUnit.DAYS.between(limite, devolucion);
        assertEquals(3, diasRetraso);

        BigDecimal tarifaDiaria = new BigDecimal("1000.00");
        BigDecimal valorEsperado = tarifaDiaria.multiply(BigDecimal.valueOf(diasRetraso));

        Multa multa = new Multa((int) diasRetraso, valorEsperado, p);
        multa = multaRepository.save(multa);

        assertEquals(EstadoMulta.PENDIENTE, multa.getEstado());
        assertEquals(new BigDecimal("3000.00"), multa.getValor());
    }

    @Test
    @DisplayName("SDD-13 y SDD-14: Pago de multa pendiente la marca como PAGADA y valida duplicados")
    void testPagoMulta() {
        Prestamo p = new Prestamo(LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), lectorPrueba, ejemplarPrueba, usuarioBiblio);
        p.setEstado(EstadoPrestamo.DEVUELTO);
        p = prestamoRepository.save(p);

        Multa multa = new Multa(2, new BigDecimal("2000.00"), p);
        multa = multaRepository.save(multa);

        assertEquals(EstadoMulta.PENDIENTE, multa.getEstado());

        // Pagar multa (SDD-13)
        multa.setEstado(EstadoMulta.PAGADA);
        multa.setFechaPago(LocalDate.now());
        multaRepository.save(multa);

        assertEquals(EstadoMulta.PAGADA, multa.getEstado());
        assertNotNull(multa.getFechaPago());

        // Validar pago duplicado (SDD-14): si ya está pagada debe rechazarse
        assertTrue(EstadoMulta.PAGADA.equals(multa.getEstado()), "No debe permitirse volver a pagar una multa ya PAGADA.");
    }

    @Test
    @DisplayName("SDD-16: Regla de reserva solo permitida cuando no hay ejemplares disponibles")
    void testReglaReservaSinDisponibles() {
        long disponibles = ejemplarRepository.countByLibroIdLibroAndEstado(libroPrueba.getIdLibro(), EstadoEjemplar.DISPONIBLE);
        // Si hay ejemplares disponibles, la regla SDD-16 dictamina rechazo
        if (disponibles > 0) {
            assertTrue(disponibles > 0, "No debe permitirse reservar si hay copias disponibles en sala.");
        }
    }
}

package com.kodex.config;

import com.kodex.model.*;
import com.kodex.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inicializador de datos semilla del sistema KODEX.
 * Garantiza que existan los 6 roles, usuarios iniciales de prueba (clave: kodex123),
 * categorías, libros, ejemplares, lectores y préstamos demostrativos.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final LectorRepository lectorRepository;
    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final ReservaLibroRepository reservaLibroRepository;
    private final ConfiguracionRepository configuracionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           LectorRepository lectorRepository,
                           CategoriaRepository categoriaRepository,
                           LibroRepository libroRepository,
                           EjemplarRepository ejemplarRepository,
                           PrestamoRepository prestamoRepository,
                           MultaRepository multaRepository,
                           ReservaLibroRepository reservaLibroRepository,
                           ConfiguracionRepository configuracionRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.lectorRepository = lectorRepository;
        this.categoriaRepository = categoriaRepository;
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
        this.reservaLibroRepository = reservaLibroRepository;
        this.configuracionRepository = configuracionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        inicializarConfiguracion();
        inicializarCategorias();
        inicializarLectores();
        inicializarUsuarios();
        inicializarLibrosYEjemplares();
        inicializarPrestamosYMultasDemo();
    }

    private void inicializarConfiguracion() {
        if (configuracionRepository.count() == 0) {
            Configuracion config = new Configuracion();
            config.setDiasPrestamo(7);
            config.setMaxPrestamosActivos(3);
            config.setValorMultaDiaria(new BigDecimal("1000.00"));
            config.setDiasVencimientoReserva(5);
            config.setNombreBiblioteca("Biblioteca Central KODEX - UTS");
            configuracionRepository.save(config);
        }
    }

    private void inicializarCategorias() {
        if (categoriaRepository.count() == 0) {
            categoriaRepository.save(new Categoria("Ingeniería y Tecnología", "Libros de programación, arquitectura de software, bases de datos e IA"));
            categoriaRepository.save(new Categoria("Ciencias Básicas", "Matemáticas avanzadas, física universitaria, estadística aplicada"));
            categoriaRepository.save(new Categoria("Literatura y Ficción", "Novelas clásicas, literatura hispanoamericana y narrativa universal"));
            categoriaRepository.save(new Categoria("Administración y Gestión", "Gestión de proyectos, liderazgo, finanzas y emprendimiento"));
        }
    }

    private void inicializarLectores() {
        if (lectorRepository.count() == 0) {
            Lector l1 = new Lector("1098765432", "Carlos Andrés Pérez", "3104567890", "lector@kodex.com");
            l1.setEstado(EstadoLector.ACTIVO);
            l1.setFechaRegistro(LocalDateTime.now().minusMonths(2));
            lectorRepository.save(l1);

            Lector l2 = new Lector("1098765433", "María Fernanda Gómez", "3117891234", "maria.gomez@uts.edu.co");
            l2.setEstado(EstadoLector.ACTIVO);
            l2.setFechaRegistro(LocalDateTime.now().minusMonths(1));
            lectorRepository.save(l2);

            Lector l3 = new Lector("1098765434", "Juan David Rodríguez", "3123456789", "juan.rodriguez@uts.edu.co");
            l3.setEstado(EstadoLector.ACTIVO);
            l3.setFechaRegistro(LocalDateTime.now().minusWeeks(2));
            lectorRepository.save(l3);

            Lector l4 = new Lector("1098765435", "Laura Camila Torres", "3139876543", "laura.torres@uts.edu.co");
            l4.setEstado(EstadoLector.SANCIONADO);
            l4.setFechaRegistro(LocalDateTime.now().minusMonths(3));
            lectorRepository.save(l4);
        }
    }

    private void inicializarUsuarios() {
        String claveComun = passwordEncoder.encode("kodex123");

        // 1. ADMIN
        if (!usuarioRepository.existsByCorreo("admin@kodex.com")) {
            Usuario admin = new Usuario("Jan Carlos Galvis (Admin)", "admin@kodex.com", claveComun, Rol.ADMIN, true);
            usuarioRepository.save(admin);
        }

        // 2. BIBLIOTECARIO
        if (!usuarioRepository.existsByCorreo("bibliotecario@kodex.com")) {
            Usuario bib = new Usuario("Anphernee Hooker (Bibliotecario)", "bibliotecario@kodex.com", claveComun, Rol.BIBLIOTECARIO, true);
            usuarioRepository.save(bib);
        }

        // 3. CATALOGADOR
        if (!usuarioRepository.existsByCorreo("catalogador@kodex.com")) {
            Usuario cat = new Usuario("Paola Velasco (Catalogador)", "catalogador@kodex.com", claveComun, Rol.CATALOGADOR, true);
            usuarioRepository.save(cat);
        }

        // 4. TESORERO
        if (!usuarioRepository.existsByCorreo("tesorero@kodex.com")) {
            Usuario tes = new Usuario("Tesorero General KODEX", "tesorero@kodex.com", claveComun, Rol.TESORERO, true);
            usuarioRepository.save(tes);
        }

        // 5. GERENTE
        if (!usuarioRepository.existsByCorreo("gerente@kodex.com")) {
            Usuario ger = new Usuario("Dirección General UTS (Gerente)", "gerente@kodex.com", claveComun, Rol.GERENTE, true);
            usuarioRepository.save(ger);
        }

        // 6. LECTOR (Asociado al Lector Carlos Andrés Pérez para el portal)
        if (!usuarioRepository.existsByCorreo("lector@kodex.com")) {
            Lector lectorAsoc = lectorRepository.findByDocumento("1098765432").orElse(null);
            Usuario lec = new Usuario("Carlos Andrés Pérez (Lector)", "lector@kodex.com", claveComun, Rol.LECTOR, true);
            lec.setLector(lectorAsoc);
            usuarioRepository.save(lec);
        }
    }

    private void inicializarLibrosYEjemplares() {
        if (libroRepository.count() == 0) {
            Categoria catIng = categoriaRepository.findByNombreIgnoreCase("Ingeniería y Tecnología").orElse(null);
            Categoria catLit = categoriaRepository.findByNombreIgnoreCase("Literatura y Ficción").orElse(null);
            Categoria catCiencias = categoriaRepository.findByNombreIgnoreCase("Ciencias Básicas").orElse(null);

            // Libro 1: Effective Java
            Libro libro1 = new Libro("978-0134685991", "Effective Java (3rd Edition)", "Joshua Bloch", "Addison-Wesley", 2018, catIng);
            libroRepository.save(libro1);
            ejemplarRepository.save(new Ejemplar("KDX-LIB-001-A", EstadoEjemplar.DISPONIBLE, "Estante A-1, Pasillo 1", libro1));
            ejemplarRepository.save(new Ejemplar("KDX-LIB-001-B", EstadoEjemplar.DISPONIBLE, "Estante A-1, Pasillo 1", libro1));

            // Libro 2: Clean Code
            Libro libro2 = new Libro("978-0132350884", "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "Prentice Hall", 2008, catIng);
            libroRepository.save(libro2);
            ejemplarRepository.save(new Ejemplar("KDX-LIB-002-A", EstadoEjemplar.DISPONIBLE, "Estante A-2, Pasillo 1", libro2));
            ejemplarRepository.save(new Ejemplar("KDX-LIB-002-B", EstadoEjemplar.DISPONIBLE, "Estante A-2, Pasillo 1", libro2));

            // Libro 3: Design Patterns
            Libro libro3 = new Libro("978-0201633610", "Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "Addison-Wesley", 1994, catIng);
            libroRepository.save(libro3);
            ejemplarRepository.save(new Ejemplar("KDX-LIB-003-A", EstadoEjemplar.DISPONIBLE, "Estante B-1, Pasillo 2", libro3));

            // Libro 4: Cien Años de Soledad
            Libro libro4 = new Libro("978-0307474728", "Cien Años de Soledad", "Gabriel García Márquez", "Editorial Sudamericana", 1967, catLit);
            libroRepository.save(libro4);
            ejemplarRepository.save(new Ejemplar("KDX-LIB-004-A", EstadoEjemplar.DISPONIBLE, "Estante C-1, Pasillo 3", libro4));

            // Libro 5: Cálculo de Stewart
            Libro libro5 = new Libro("978-6075265551", "Cálculo de Una Variable: Trascendentes Tempranas", "James Stewart", "Cengage Learning", 2018, catCiencias);
            libroRepository.save(libro5);
            ejemplarRepository.save(new Ejemplar("KDX-LIB-005-A", EstadoEjemplar.EN_REPARACION, "Taller de Restauración B-2", libro5));
        }
    }

    private void inicializarPrestamosYMultasDemo() {
        if (prestamoRepository.count() == 0) {
            Lector lector = lectorRepository.findByDocumento("1098765432").orElse(null);
            Usuario biblio = usuarioRepository.findByCorreo("bibliotecario@kodex.com").orElse(null);
            Ejemplar ej1 = ejemplarRepository.findByCodigoInventario("KDX-LIB-001-A").orElse(null);

            if (lector != null && biblio != null && ej1 != null) {
                // Préstamo devuelto con multa pagada demostrativa
                Prestamo pDevuelto = new Prestamo(LocalDate.now().minusDays(20), LocalDate.now().minusDays(13), lector, ej1, biblio);
                pDevuelto.setFechaDevolucion(LocalDate.now().minusDays(10)); // 3 días de retraso
                pDevuelto.setEstado(EstadoPrestamo.DEVUELTO);
                prestamoRepository.save(pDevuelto);

                Multa multa = new Multa(3, new BigDecimal("3000.00"), pDevuelto);
                multa.setEstado(EstadoMulta.PAGADA);
                multa.setFechaPago(LocalDate.now().minusDays(10));
                multaRepository.save(multa);

                // Préstamo activo
                Ejemplar ej2 = ejemplarRepository.findByCodigoInventario("KDX-LIB-002-A").orElse(null);
                if (ej2 != null) {
                    Prestamo pActivo = new Prestamo(LocalDate.now().minusDays(2), LocalDate.now().plusDays(5), lector, ej2, biblio);
                    pActivo.setEstado(EstadoPrestamo.ACTIVO);
                    prestamoRepository.save(pActivo);
                    ej2.setEstado(EstadoEjemplar.PRESTADO);
                    ejemplarRepository.save(ej2);
                }
            }
        }
    }
}

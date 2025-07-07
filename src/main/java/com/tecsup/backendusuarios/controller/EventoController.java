package com.tecsup.backendusuarios.controller;

import com.tecsup.backendusuarios.model.Evento;
import com.tecsup.backendusuarios.model.User;
import com.tecsup.backendusuarios.model.Carrera;
import com.tecsup.backendusuarios.repository.EventoRepository;
import com.tecsup.backendusuarios.repository.UserRepository;
import com.tecsup.backendusuarios.repository.CarreraRepository;
import com.tecsup.backendusuarios.security.CurrentUser;
import com.tecsup.backendusuarios.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // Importar Collectors

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    // --- AÑADIR ESTE MÉTODO HELPER ---
    private String cleanImagePath(String imagePath) {
        if (imagePath != null) {
            // Si la ruta contiene la URL base de uploads, la eliminamos para dejar solo el nombre del archivo.
            // Esto es útil si la imagen se guarda con la URL completa por alguna razón,
            // o si el frontend envía la URL completa al backend.
            if (imagePath.startsWith("http://localhost:8080/uploads/")) {
                return imagePath.replace("http://localhost:8080/uploads/", "");
            }
            // Si la ruta es solo el nombre del archivo, o tiene un prefijo "/uploads/", lo dejamos.
            // Para asegurar, eliminamos "/uploads/" si está al principio, solo queremos el nombre del archivo.
            if (imagePath.startsWith("/uploads/")) {
                return imagePath.replace("/uploads/", "");
            }
        }
        return imagePath; // Devuelve el nombre del archivo o null
    }

    @GetMapping
    public List<Evento> getAllEventos() {
        return eventoRepository.findAll().stream()
                .peek(evento -> evento.setImagen(cleanImagePath(evento.getImagen()))) // <--- Aplicar limpieza
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evento> getEventoById(@PathVariable Long id) {
        return eventoRepository.findById(id)
                .map(evento -> {
                    evento.setImagen(cleanImagePath(evento.getImagen())); // <--- Aplicar limpieza
                    return ResponseEntity.ok(evento);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createEvento(@RequestBody Evento evento, @CurrentUser UserPrincipal currentUser) {
        System.out.println("DEBUG: Valor de imagen recibido en el backend: " + evento.getImagen());

        // Limpiar la imagen antes de guardar en la DB si el frontend envió la URL completa
        evento.setImagen(cleanImagePath(evento.getImagen())); // <--- Aplicar limpieza al guardar

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        evento.setAutor(user);
        return ResponseEntity.ok(eventoRepository.save(evento));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Evento> updateEvento(@PathVariable Long id, @RequestBody Evento eventoDetails,
                                               @CurrentUser UserPrincipal currentUser) {
        return eventoRepository.findById(id)
                .map(evento -> {
                    if (!evento.getAutor().getId().equals(currentUser.getId())) {
                        throw new RuntimeException("No tienes permiso para editar este evento");
                    }
                    if (eventoDetails.getTitulo() != null) evento.setTitulo(eventoDetails.getTitulo());
                    if (eventoDetails.getDescripcion() != null) evento.setDescripcion(eventoDetails.getDescripcion());
                    if (eventoDetails.getFechaInicio() != null) evento.setFechaInicio(eventoDetails.getFechaInicio());
                    if (eventoDetails.getFechaFin() != null) evento.setFechaFin(eventoDetails.getFechaFin());
                    if (eventoDetails.getUbicacion() != null) evento.setUbicacion(eventoDetails.getUbicacion());

                    // Limpiar la imagen antes de guardar en la DB si el frontend envió la URL completa
                    if (eventoDetails.getImagen() != null) {
                        evento.setImagen(cleanImagePath(eventoDetails.getImagen())); // <--- Aplicar limpieza al actualizar
                    }

                    if (eventoDetails.getCarrera() != null) evento.setCarrera(eventoDetails.getCarrera());
                    return ResponseEntity.ok(eventoRepository.save(evento));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteEvento(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        return eventoRepository.findById(id)
                .map(evento -> {
                    if (!evento.getAutor().getId().equals(currentUser.getId())) {
                        return ResponseEntity.badRequest().body("No tienes permiso para eliminar este evento");
                    }
                    eventoRepository.delete(evento);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/autor/{userId}")
    public List<Evento> getEventosByAutor(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        // También aplica limpieza si estos métodos se usan para mostrar en el frontend
        return eventoRepository.findByAutor(user).stream()
                .peek(evento -> evento.setImagen(cleanImagePath(evento.getImagen())))
                .collect(Collectors.toList());
    }

    @GetMapping("/carrera/{carreraId}")
    public List<Evento> getEventosByCarrera(@PathVariable Long carreraId) {
        Carrera carrera = carreraRepository.findById(carreraId)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));
        // También aplica limpieza si estos métodos se usan para mostrar en el frontend
        return eventoRepository.findByCarrera(carrera).stream()
                .peek(evento -> evento.setImagen(cleanImagePath(evento.getImagen())))
                .collect(Collectors.toList());
    }

    @GetMapping("/fechas")
    public List<Evento> getEventosByFecha(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {
        return eventoRepository.findByFechaInicioBetween(inicio, fin).stream()
                .peek(evento -> evento.setImagen(cleanImagePath(evento.getImagen())))
                .collect(Collectors.toList());
    }
}
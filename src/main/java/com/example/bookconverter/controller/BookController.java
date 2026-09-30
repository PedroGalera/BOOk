package com.example.bookconverter.controller;

import com.example.bookconverter.model.Capitulo;
import com.example.bookconverter.service.PreserTextoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api") 
public class BookController {

    
    private final PreserTextoService preserTextoService;

    
    public BookController(PreserTextoService preserTextoService) {
        this.preserTextoService = preserTextoService;
    }

    @PostMapping("/convert")
    public ResponseEntity<String> procesarLibro(@RequestParam("file") MultipartFile file) {
        // Validamos que no nos manden una petición vacía
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: El archivo está vacío o no se envió.");
        }

        try {
            
            List<Capitulo> capitulos = preserTextoService.praserEpub(file);
            
            
            String respuesta = "¡Procesamiento exitoso!\n";
            respuesta += "Se extrajeron " + capitulos.size() + " capítulos de lectura.\n";
            
            if (!capitulos.isEmpty()) {
                respuesta += "Muestra del primer capítulo: [" + capitulos.get(0).getTitulo() + "]";
            }
            
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Fallo en el servidor: " + e.getMessage());
        }
    }
}
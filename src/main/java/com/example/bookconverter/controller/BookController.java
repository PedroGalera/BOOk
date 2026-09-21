package com.example.bookconverter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class BookController {

    @PostMapping("/convert")
    public ResponseEntity<String> receiveBook(@RequestParam("file") MultipartFile file) {
        System.out.println("--- NUEVA PETICIÓN ---");
        System.out.println("Endpoint alcanzado. Archivo recibido: " + file.getOriginalFilename());
        
        return ResponseEntity.ok("El archivo " + file.getOriginalFilename() + " llegó al backend exitosamente.");
    }
}
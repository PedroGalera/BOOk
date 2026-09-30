package com.example.bookconverter.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import com.example.bookconverter.model.Capitulo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.springframework.web.multipart.MultipartFile;


@Service 

public class PreserTextoService {


    public List<Capitulo> praserEpub (MultipartFile file){
        List<Capitulo> capitulos = new ArrayList<>();
        Map<String, String> archivosEnMemoria = new HashMap<>();
        String rutaOpf = null;
        
        try(ZipInputStream zis = new ZipInputStream(file.getInputStream())){
            ZipEntry entry;

            while((entry = zis.getNextEntry()) != null){

                String nombreArchivo = entry.getName().toLowerCase();

                if (nombreArchivo.endsWith(".html") || nombreArchivo.endsWith(".xhtml") || nombreArchivo.endsWith(".opf") || nombreArchivo.endsWith(".ncx")) {
                    
                    String contenido = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    archivosEnMemoria.put(nombreArchivo, contenido);
                }
                if (nombreArchivo.endsWith(".opf")) {
                    rutaOpf = nombreArchivo;
                }


                zis.closeEntry();
            }
            if(rutaOpf != null){
                String contenidoOpf = archivosEnMemoria.get(rutaOpf);
                Document docOpf = Jsoup.parse(contenidoOpf, org.jsoup.parser.Parser.xmlParser());
                
                // 1. Detectamos en qué carpeta secreta está escondido el .opf
                String carpetaBase = "";
                int ultimoSlash = rutaOpf.lastIndexOf('/');
                if (ultimoSlash != -1) {
                    carpetaBase = rutaOpf.substring(0, ultimoSlash + 1);
                }

                Map<String, String> manifiesto = new HashMap<>();
                // 2. Usamos getElementsByTag que es inmune a los formatos raros de XML
                for (Element item : docOpf.getElementsByTag("item")) {
                    manifiesto.put(item.attr("id"), item.attr("href"));
                }
                
                for (Element itemref : docOpf.getElementsByTag("itemref")) {
                    String idCapitulo = itemref.attr("idref");
                    String nombreArchivoHtml = manifiesto.get(idCapitulo);
                    
                    if (nombreArchivoHtml != null) {
                        // 3. Ensamblamos la ruta exacta (Ej: "oebps/" + "capitulo1.html")
                        String rutaCompleta = (carpetaBase + nombreArchivoHtml).toLowerCase();
                        String contenidoHtml = archivosEnMemoria.get(rutaCompleta);
                
                        if (contenidoHtml != null) {
                            Document docHtml = Jsoup.parse(contenidoHtml);
                            String titulo = docHtml.title();

                            if (titulo == null || titulo.trim().isEmpty()) {
                                titulo = "Capítulo " + (capitulos.size() + 1);
                            }
                            Element body = docHtml.body();
                            if (body != null) {
                                String textoLimpio = body.text();

                                if (!textoLimpio.trim().isEmpty()) {
                                    capitulos.add(new Capitulo(titulo, textoLimpio));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error procesando el EPUB: " + e.getMessage());
        }
        return capitulos;

    }
}
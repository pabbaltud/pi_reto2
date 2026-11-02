package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class App {
    
    private static final String archivito = "cartelera.txt";
    
    private static void extraerCarteleraYGuardar(int cantidad) {
        String urlCartelera = "https://www.filmaffinity.com/es/cat_new_th_es.html";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivito))) {
            System.out.println("Conectando a la cartelera...");
            
            // Busque en internet como hacer para conectarme
            Document docPrincipal = Jsoup.connect(urlCartelera)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            Elements enlacesPeliculas = docPrincipal.select(".movie-card .mc-title a, .movie-card-content a, a[href*='/es/film']");
            
            if (enlacesPeliculas.isEmpty()) {
                System.out.println("No se detectaron enlaces con los selectores por defecto. Intentando selectores genéricos...");
                enlacesPeliculas = docPrincipal.select("a[href*='/es/film']");
            }
            
            Set<String> urlsProcesadas = new HashSet<>();
            int peliculasProcesadas = 0;
            
            for (Element enlace : enlacesPeliculas) {
                String urlFicha = enlace.attr("abs:href");
                
                if (urlFicha.isEmpty() || !urlFicha.contains("/es/film")) {
                    continue;
                }
                
                if (urlsProcesadas.contains(urlFicha)) {
                    continue;
                }
                
                try {
                    Document docFicha = Jsoup.connect(urlFicha)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                            .timeout(5000)
                            .get();
                    
                    String titulo = docFicha.select("#main-title span").text().trim();
                    if (titulo.isEmpty()) {
                        titulo = docFicha.select("h1").text().trim();
                    }
                    
                    String ano = docFicha.select("dt:contains(Año) + dd").text().trim();
                    String pais = docFicha.select("dt:contains(País) + dd").text().trim();
                    String director = docFicha.select("dt:contains(Dirección) + dd").text().trim();
                    String sinopsis = docFicha.select("dt:contains(Sinopsis) + dd").text().trim();
                    
                    if (titulo.isEmpty()) continue;
                    if (ano.isEmpty()) ano = "No disponible";
                    if (pais.isEmpty()) pais = "No disponible";
                    if (director.isEmpty()) director = "No disponible";
                    if (sinopsis.isEmpty()) sinopsis = "Sin sinopsis disponible.";
                    
                    urlsProcesadas.add(urlFicha);
                    
                    peliculasProcesadas++;
                    writer.write("Película #" + peliculasProcesadas + "\n");
                    writer.write("Título: " + titulo + "\n");
                    writer.write("Año: " + ano + "\n");
                    writer.write("País: " + pais + "\n");
                    writer.write("Director: " + director + "\n");
                    writer.write("Sinopsis: " + sinopsis + "\n");
                    writer.write("==================================================\n");
                    
                    System.out.println("Procesada (" + peliculasProcesadas + "): " + titulo);
                    
                    Thread.sleep(500);
                    
                    if (peliculasProcesadas >= cantidad) {
                        break;
                    }
                    
                } catch (Exception e) {
                    System.err.println("No se pudo extraer la información de: " + urlFicha);
                }
            }
            
            System.out.println("\n¡Fichero '" + archivito + "' generado correctamente sin duplicados!");
            
        } catch (IOException e) {
            System.err.println("Error general al conectar con FilmAffinity: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== TURBO SCRAPING 3000 ===");
        
        Scanner escaner = new Scanner(System.in);
        System.out.println("¿Cuantas peliculas quieres extraer? (Introduce solamente números): ");
        int cantidad = escaner.nextInt();
        escaner.close();
        
        extraerCarteleraYGuardar(cantidad);
    }
}

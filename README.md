# TreePlayer

Reproductor de música Android desarrollado como proyecto para 2º DAM.

## Características

- Lista de canciones con RecyclerView
- Barra de reproducción inferior
- Pantalla de reproducción con portada y controles
- Búsqueda de portadas mediante Deezer API
- Búsqueda de letras mediante Genius API
- Sistema de favoritos persistente
- Soporte multiidioma (español/inglés)

## Tecnologías

- Java 21
- Android SDK
- MediaPlayer
- RecyclerView + CardView
- Glide (carga de imágenes)
- Gson (persistencia JSON)
- jsoup (scraping de letras)
- Deezer API
- Genius API

## Configuración

1. El token de Genius API ya está incluido en `Config.java`
2. Ejecutar en dispositivo/emulador con API mínima 21+

## Música incluida

Álbum "[HELLMODE](https://www.quoteunquoterecords.com/qur116.htm)" de Jeff Rosenstock (10 canciones en raw/)

## APIs utilizadas

- **Deezer**: Búsqueda de portadas de álbumes
- **Genius**: Búsqueda y scraping de letras  
  
  

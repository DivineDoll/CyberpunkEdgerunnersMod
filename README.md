# Edgerunners (mod fan, Fabric 1.20.1)

Mod no oficial inspirado en *Cyberpunk: Edgerunners* (temporada 1). No está afiliado con los creadores de la serie; todo el código y las texturas son originales.

## Contenido

- **Sandevistan**: se lleva en el slot del pecho (columna mecánica 3D visible en tu espalda). Pulsa la tecla V (configurable en Controles; en móvil asígnala a un botón del launcher) para activarlo: los mobs y entidades a menos de 20 bloques (menos jugadores y tus propios proyectiles) solo avanzan 1 de cada 20 ticks. Estela de imágenes fantasma cian-verdes y pantalla teñida de verde. Deja cyberpsicosis y tiene 30 s de enfriamiento.
- **Hojas mantis**: hojas largas y finas con modelo 3D, montadas en el antebrazo, que dejan sangrando (Wither corto) al objetivo. Llevas una hoja en cada brazo: sostén el ítem en una mano y deja la otra vacía. En primera persona se ven los dos brazos con tu skin. Animaciones: la hoja se despliega en "V" al equiparla (con un brazo articulado entre la carcasa y la hoja), **clic derecho** la pliega o la despliega, hace un tajo al golpear y los LED cian y las líneas rojas laten.
- **Inyector de stim**: regeneración rápida y absorción. Un solo uso.
- **Cyberpsicosis** (efecto): más daño de ataque, pero te desgasta poco a poco. Nunca te baja de 1 corazón.

Todo está en la pestaña creativa **Edgerunners** y tiene receta de crafteo.

## Compilar el .jar

**Con GitHub Actions (desde el teléfono):** sube el proyecto a un repositorio y entra a *Actions → build → Run workflow*. Al terminar, descarga el artefacto `edgerunners-jar`.

**En local:** necesitas Java 17 y Gradle 8.5.

```bash
gradle build
```

El .jar queda en `build/libs/edgerunners-1.0.0.jar`. Instálalo en la carpeta `mods` junto con Fabric Loader y Fabric API para Minecraft 1.20.1.

## Estructura

- `src/main/java/com/divinedoll/edgerunners/` — código (items, efecto, registro)
- `src/main/resources/assets/edgerunners/` — texturas, modelos y traducciones (en, es)
- `src/main/resources/data/edgerunners/recipes/` — recetas

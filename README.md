# Edgerunners (mod fan, Fabric 1.20.1)

Mod no oficial inspirado en *Cyberpunk: Edgerunners* (temporada 1). No está afiliado con los creadores de la serie; todo el código y las texturas son originales.

## Contenido

- **Sandevistan**: clic derecho para moverte a toda velocidad mientras el tiempo se ralentiza a tu alrededor: los mobs y entidades a menos de 8 bloques (menos jugadores y tus propios proyectiles) solo avanzan 1 de cada 4 ticks. Dejas una estela de imágenes fantasma cian-verdes que se desvanecen y la pantalla se tiñe de verde. Te deja con cyberpsicosis un rato y tiene 30 s de enfriamiento.
- **Hojas mantis**: hojas largas y finas con modelo 3D, montadas en el antebrazo, que dejan sangrando (Wither corto) al objetivo. Llevas una hoja en cada brazo: sostén el ítem en una mano y deja la otra vacía. En primera persona se ven los dos brazos con tu skin.
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

# AngelFX 1.2.0

Mod visual **exclusivamente cliente** para Minecraft Java 1.21.11 + Fabric.

## Funciones de esta primera versión

- Aura orbital alrededor del jugador.
- Tres paletas de partículas: Celestial, Soul y Arcane.
- Estela de partículas al caminar o correr.
- Dos atajos configurables desde Opciones → Controles → Asignación de teclas.
- No añade paquetes de red ni modifica el mundo o el servidor.

## Teclas predeterminadas

| Tecla | Acción |
|---|---|
| `G` | Activar/desactivar AngelFX |
| `H` | Cambiar entre Celestial, Soul y Arcane |
| `M` | Abrir el menú de AngelFX |
| `?` | ? |

Puedes cambiar las teclas desde los controles de Minecraft. Los efectos son locales: otros jugadores no los verán.

## Requisitos

- Minecraft Java 1.21.11
- Fabric Loader 0.18.1 o posterior compatible con 1.21.11
- Fabric API 0.141.1+1.21.11
- Java 21
- Gradle 9.2 o posterior para compilar con el Loom 1.14

## Compilar

En Windows, abre una terminal dentro de esta carpeta y ejecuta:

```powershell
gradle build
```

En Linux/macOS:

```bash
gradle build
```

Gradle descargará Loom, Minecraft, mappings y Fabric API. El JAR remapeado se generará en `build/libs/angelfx-1.0.0.jar`.

Para abrir un cliente de desarrollo:

```bash
gradle runClient
```

> Este paquete contiene el proyecto fuente. No incluye un JAR precompilado: el entorno de creación no dispone de acceso a Maven/Gradle para descargar las dependencias y ejecutar la compilación aquí.

## Estructura

```text
AngelFX/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── src/main/java/com/angelfx/AngelFXClient.java
└── src/main/resources/
    ├── fabric.mod.json
    └── assets/angelfx/lang/
        ├── en_us.json
        └── es_es.json
```

## Licencia

MIT. Consulta `LICENSE`.


## Compilar desde Android con GitHub Actions

1. Extrae este ZIP en el teléfono.
2. En GitHub, crea un repositorio nuevo y vacío llamado `AngelFX`.
3. En el repositorio, usa **Add file → Upload files** y sube el CONTENIDO de esta carpeta (incluida la carpeta oculta `.github`). No subas el ZIP como único archivo.
4. Confirma con **Commit changes**.
5. Abre la pestaña **Actions**, selecciona **Build AngelFX** y pulsa **Run workflow → Run workflow**. También puede iniciarse automáticamente al subir los archivos.
6. Cuando termine con una marca verde, abre la ejecución y descarga el artefacto **AngelFX-1.0.0** de la sección **Artifacts**.
7. Extrae el ZIP descargado y copia `angelfx-1.0.0.jar` a `.minecraft/mods` en PojavLauncher.

El proyecto requiere Java 21 para compilar y Fabric API compatible con Minecraft 1.21.11 en el juego.

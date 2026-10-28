# Fiao — F1 a F6 funcionando

Segunda entrega: la app deja de ser un armazón y empieza a guardar
datos de verdad. Fecha: 28 de octubre de 2026.

---

## 1. Lo mismo de la vez pasada

No tengo SDK de Android aquí, así que **nada de esto está compilado**.
Esta entrega toca Gradle, que es donde más fácil fallan las versiones.
La sección 6 lista qué hacer con cada error probable.

Antes de descomprimir:

```
git add .
git commit -m "Estado antes de F1-F6"
```

---

## 2. Cambios en Gradle (hazlos primero)

### 2.1 El plugin de KSP

Room genera código al compilar, y para eso necesita KSP. **La versión de
KSP tiene que coincidir con tu versión de Kotlin**, que es 2.2.10.

En `gradle/libs.versions.toml`, en `[versions]`:

```toml
ksp = "2.2.10-2.0.2"
room = "2.7.1"
lifecycle = "2.8.7"
```

En `[libraries]`:

```toml
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
```

En `[plugins]`:

```toml
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### 2.2 Aplicar el plugin

En el `build.gradle.kts` **de la raíz** (no el de `app`), dentro del
bloque `plugins`:

```kotlin
alias(libs.plugins.ksp) apply false
```

En `app/build.gradle.kts`, dentro de su bloque `plugins`:

```kotlin
alias(libs.plugins.ksp)
```

### 2.3 Las dependencias

En `app/build.gradle.kts`, dentro de `dependencies`:

```kotlin
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)

implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.androidx.lifecycle.runtime.compose)
```

Fíjate en que el compilador de Room va con `ksp(...)` y no con
`implementation(...)`. Si se pone mal, Room no genera nada y el error
dice que no encuentra la clase `FiaoDatabase_Impl`.

### 2.4 Sube la versión de lifecycle que ya tenías

Tu catálogo trae `lifecycleRuntimeKtx = "2.6.1"`, que es vieja. Déjala
apuntando a la misma versión nueva para que no haya dos versiones de
lifecycle peleando:

```toml
lifecycleRuntimeKtx = "2.8.7"
```

Sincroniza antes de seguir. **Si falla aquí, no descomprimas nada
todavía**: mándame el error.

---

## 3. Un cambio a mano en el AndroidManifest

Abre `app/src/main/AndroidManifest.xml` y en la etiqueta
`<application>` agrega esta línea:

```xml
android:name=".FiaoApplication"
```

Queda algo así:

```xml
<application
    android:name=".FiaoApplication"
    android:allowBackup="true"
    ...
```

**Esto no es opcional.** Sin esa línea la app compila perfecto y
revienta al abrir, porque el contenedor de dependencias nunca se crea.
Es de los errores más confusos de Android, así que vale la pena que
sepas reconocerlo: `ClassCastException` apuntando a `Application`.

---

## 4. Qué archivos llegan

### Nuevos

| Archivo | Qué hace |
|---|---|
| `FiaoApplication.kt` | Crea el contenedor de dependencias al arrancar. |
| `di/ContenedorApp.kt` | Arma la base de datos y los repositorios. |
| `domain/rules/Reglas.kt` | Las reglas R1 a R6 como funciones puras. |
| `domain/repository/Repositorios.kt` | Los contratos de acceso a datos. |
| `data/local/Entidades.kt` | Las 4 tablas. |
| `data/local/Daos.kt` | Las consultas SQL. |
| `data/local/Convertidores.kt` | LocalDateTime a milisegundos y vuelta. |
| `data/local/FiaoDatabase.kt` | La base de datos. |
| `data/Mapeadores.kt` | Traducción entre tablas y dominio. |
| `data/RepositoriosRoom.kt` | Las implementaciones con Room. |
| `ui/FabricaViewModels.kt` | Enseña a Android a construir los ViewModel. |
| `ui/inicio/InicioViewModel.kt` | F2, F4. |
| `ui/movimientos/MovimientosViewModel.kt` | F2, F4, F5. |
| `ui/movimientos/MovimientoFormViewModel.kt` | F1, F3. |
| `ui/clientes/ClientesViewModel.kt` | Lista de clientes. |
| `ui/clientes/ClienteFormViewModel.kt` | F6. |
| `ui/clientes/ClienteDetalleViewModel.kt` | Detalle con datos reales. |
| `test/.../ReglasTest.kt` | 18 pruebas de las reglas. |

### Reemplazados

| Archivo | Por qué |
|---|---|
| `ui/navegacion/FiaoNavHost.kt` | Ahora conecta ViewModels, no datos falsos. |
| `ui/movimientos/MovimientosScreen.kt` | Le entra el calendario (F5). |
| `ui/movimientos/MovimientoFormScreen.kt` | Usa el UiState del ViewModel. |
| `ui/clientes/ClienteFormScreen.kt` | Usa el UiState del ViewModel. |

`DatosDemo.kt` se queda: lo usan las previews y el resumen semanal,
que sigue siendo F13 pendiente.

---

## 5. Cómo probarlo

La base de datos arranca vacía, así que lo primero que verás son los
estados vacíos. Eso es correcto, no es un error.

1. Toca **Registrar**, escribe un monto y una descripción, y guarda.
   Debe aparecer en la lista y el balance de arriba debe cambiar solo.
2. Registra un gasto y comprueba que el balance resta.
3. **Cierra la app por completo y vuelve a abrirla.** Los movimientos
   siguen ahí: eso es Room funcionando.
4. **Rota el celular con el formulario a medio llenar.** Ya no se
   borra, porque el estado vive en el ViewModel. Compáralo con lo que
   pasaba antes.
5. Toca un movimiento de la lista para editarlo, cambia el monto y
   guarda. Luego bórralo.
6. Entra al calendario (icono arriba a la derecha en Movimientos) y
   cambia de día. Debe quedar vacío, porque solo hay datos de hoy.
7. Crea un cliente. Aparece en la lista con saldo cero y "Al día".
8. Intenta guardar un movimiento sin monto: debe salir el error bajo
   el campo.

### Correr los tests

Clic derecho sobre `ReglasTest.kt` y Run, o en la terminal:

```
./gradlew test
```

Deben pasar los 18.

---

## 6. Si algo falla

**Error de versión de KSP.** El mensaje dice textualmente cuál espera.
Copia esa versión al catálogo y vuelve a sincronizar.

**`FiaoDatabase_Impl` no existe.** El compilador de Room no corrió:
revisa que la línea sea `ksp(libs.androidx.room.compiler)` y no
`implementation(...)`.

**La app compila pero se cierra al abrir.** Falta
`android:name=".FiaoApplication"` en el manifest.

**`collectAsStateWithLifecycle` no se resuelve.** Falta
`lifecycle-runtime-compose`.

**`viewModel()` no se resuelve.** Falta `lifecycle-viewmodel-compose`.

**Room se queja de una consulta.** Los errores de Room son de los
mejores que hay: dicen la columna exacta y la línea del SQL.

---

## 7. Lo que quedó pendiente

| Falta | Día |
|---|---|
| F7 y F8: registrar fiado y abono | 9 y 10 |
| F11: historial (la pantalla existe, faltan los datos) | 10 |
| F12: recordatorio por WhatsApp | 10 |
| F13: resumen semanal con datos reales | 11 |
| Tests de los ViewModel | 11 |

---

## 8. Preguntas para la sesión de análisis

Súmalas a las 12 del documento anterior.

**Sobre arquitectura**

1. ¿Por qué `MovimientoRepository` es una interfaz en `domain` y su
   implementación está en `data`? ¿Qué se gana?
2. ¿Por qué hay `MovimientoEntity` y `Movimiento` por separado, si
   tienen los mismos campos?
3. Si cambiáramos Room por Supabase, ¿qué archivos habría que tocar?

**Sobre ViewModel y estado**

4. ¿Por qué ahora el formulario no se borra al rotar, si antes sí?
5. ¿Qué hace `stateIn` y por qué lleva `WhileSubscribed(5000)`?
6. ¿Qué diferencia hay entre `collectAsState()` y
   `collectAsStateWithLifecycle()`?
7. En `MovimientosViewModel`, ¿por qué `flatMapLatest` y no `map`?

**Sobre Room y corrutinas**

8. ¿Por qué los métodos que devuelven `Flow` no son `suspend` y los
   que escriben sí?
9. ¿Qué pasaría si subiéramos una columna nueva a una tabla sin
   cambiar `version = 1`?
10. ¿Por qué el dinero se guarda como `Long` y la fecha como
    milisegundos?

**Sobre las reglas**

11. ¿Por qué la regla R6 se verifica dos veces en
    `ClienteFormViewModel`?
12. ¿Por qué `calcularResumenDia` no recibe los fiados?
13. ¿Por qué los tests de las reglas no necesitan emulador?

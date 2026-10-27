# Fiao — armazón de la interfaz

Documento de los archivos generados, para la sesión de análisis.
Fecha: 26 de octubre de 2026.

---

## 1. Antes de nada: esto no está compilado

No tengo un SDK de Android aquí, así que **no pude compilar ni ejecutar
este código**. Está escrito con APIs estables de Compose y Material 3,
pero es muy probable que aparezcan errores de compilación por versiones:
un `import` que cambió de paquete, un componente que se renombró.

Eso no es un problema, es parte del trabajo. Compila, copia el error y
lo resolvemos. Los sospechosos más comunes están en la sección 7.

---

## 2. Cómo instalarlo

El zip respeta la estructura de carpetas del proyecto. Descomprímelo
**sobre** `C:\Users\willi\AndroidStudioProjects\Fiao` y acepta
reemplazar.

Reemplaza tres archivos que ya existían: `MainActivity.kt`,
`ui/theme/Color.kt` y `ui/theme/Theme.kt`. Como tienes todo en Git, si
algo sale mal recuperas la versión anterior con `git checkout`.

Haz el commit **antes** de descomprimir, para que el diff quede limpio
y puedas ver exactamente qué cambió.

### Falta una dependencia

La navegación entre pantallas necesita una librería que el proyecto
todavía no tiene.

En `gradle/libs.versions.toml`, agrega en `[versions]`:

```toml
navigationCompose = "2.8.5"
```

y en `[libraries]`:

```toml
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
```

Después, en `app/build.gradle.kts`, dentro del bloque `dependencies`:

```kotlin
implementation(libs.androidx.navigation.compose)
```

Sincroniza el proyecto. Si la versión 2.8.5 da conflicto con tu versión
de Compose, Android Studio te sugerirá la correcta.

---

## 3. Qué archivo hace qué

### Tema y estilo

| Archivo | Qué hace |
|---|---|
| `ui/theme/Color.kt` | Todos los colores de la app, con su nombre. |
| `ui/theme/ColoresFiao.kt` | Los colores que Material no tiene: venta, gasto, fiado. |
| `ui/theme/Type.kt` | Escala tipográfica y los tres estilos de dinero. |
| `ui/theme/Theme.kt` | Arma el tema claro y el oscuro. |

### Dominio

| Archivo | Qué hace |
|---|---|
| `domain/model/Modelos.kt` | `Movimiento`, `Cliente`, `Fiado`, `Abono` y tipos de apoyo. |
| `domain/model/DatosDemo.kt` | Datos falsos para ver las pantallas sin base de datos. |

### Utilidades

| Archivo | Qué hace |
|---|---|
| `util/Moneda.kt` | Formatea pesos colombianos: `45000` → `$ 45.000`. |

### Componentes reutilizables

| Archivo | Qué hace |
|---|---|
| `componentes/MontoTexto.kt` | Un monto con su color y su signo. |
| `componentes/FilaMovimiento.kt` | Una línea del libro del día, más el separador. |
| `componentes/FilaCliente.kt` | Una línea de la lista de clientes, con saldo. |
| `componentes/CampoMonto.kt` | Campo numérico de dinero y campo de texto. |
| `componentes/SelectorTipo.kt` | Los dos chips de venta y gasto. |
| `componentes/EstadoVacio.kt` | Mensaje cuando una lista está vacía. |
| `componentes/BarraSuperior.kt` | La barra de título con botón de volver. |
| `componentes/TarjetaPorCobrar.kt` | La tarjeta ocre del total fiado. |

### Pantallas

| Archivo | Qué hace |
|---|---|
| `ui/inicio/InicioScreen.kt` | Balance del día, por cobrar y movimientos. |
| `ui/movimientos/MovimientosScreen.kt` | El libro de un día completo. |
| `ui/movimientos/MovimientoFormScreen.kt` | Crear o editar una venta o gasto. |
| `ui/clientes/ClientesScreen.kt` | Quién debe, con búsqueda. |
| `ui/clientes/ClienteFormScreen.kt` | Crear o editar un cliente. |
| `ui/clientes/ClienteDetalleScreen.kt` | Saldo, historial y acciones. |
| `ui/resumen/ResumenScreen.kt` | Gráfica de barras de 7 días. |

### Navegación y entrada

| Archivo | Qué hace |
|---|---|
| `ui/navegacion/Rutas.kt` | Los nombres de las rutas, centralizados. |
| `ui/navegacion/FiaoNavHost.kt` | Conecta las pantallas entre sí. |
| `MainActivity.kt` | Arranca el tema y la navegación. |
| `res/values/strings.xml` | El nombre de la app. |

---

## 4. Decisiones visuales

**La plantilla de Android Studio viene en morado.** Ese morado es la
señal más clara de que alguien no tocó el tema. Lo primero fue
cambiarlo.

**La paleta sale de las fachadas del Caribe colombiano:** teal,
terracota y ocre. No es el verde brillante genérico de "app de dinero".

**Tres colores cargan información, no adorno:**

- verde teal → venta, entra plata
- terracota → gasto, sale plata
- ocre → fiado, plata prometida que todavía no entra

**Los colores no se eligieron a ojo.** El par verde/rojo que uno usaría
por instinto (`#1C7A3E` y `#B23A1F`) lo pasé por un validador de
contraste y daltonismo, y **falló**: para deuteranopía la diferencia era
de 5.0 cuando el mínimo aceptable es 8. La deuteranopía afecta a
alrededor del 8% de los hombres, así que casi uno de cada doce tenderos
vería la venta y el gasto casi del mismo color.

Los valores finales (`#157F63`, `#9C3B16`, `#B08200`) pasan en visión
normal, deuteranopía y protanopía, en tema claro y oscuro. Este es un
buen tema para la entrevista: muestra que verificaste algo en vez de
confiar en el gusto.

**Aun así, el color nunca va solo.** El monto lleva signo (+/-), el
historial dice "Fiado" o "Abono", la tarjeta dice "Por cobrar". Quien no
distinga los colores entiende igual.

**Cifras tabulares.** Los tres estilos de dinero en `Type.kt` llevan
`fontFeatureSettings = "tnum"`, que hace que todos los dígitos ocupen el
mismo ancho. Sin eso, una columna de montos queda desalineada porque el
1 es más angosto que el 8.

**Tarjeta o fila, según el significado.** Las tarjetas se usan para
objetos que están aparte; las listas van con separadores finos. La plata
fiada es una tarjeta porque es otra clase de dinero: no está en la caja,
está en la calle. Los movimientos son filas porque son líneas de un
mismo libro. Convertir todo en tarjetas idénticas es el error más común
y hace que nada destaque.

**Sin dynamic color.** Material You tomaría los colores del fondo de
pantalla del usuario y reemplazaría la paleta, con lo que venta y fiado
podrían volverse indistinguibles. Cuando el color carga información, no
se delega al fondo de pantalla. La plantilla lo trae activado; aquí se
quitó a propósito.

**Botones de 56dp y chips de 48dp.** La app se usa de pie, con una mano,
atendiendo a alguien.

---

## 5. Decisiones de arquitectura

**Ninguna pantalla busca sus datos.** Todas los reciben por parámetro.
Eso se llama *state hoisting* y es lo que permite:

- verlas en el panel de preview sin base de datos,
- probarlas sin emulador,
- cambiar el origen de los datos sin tocar la pantalla.

Cuando llegue el ViewModel el día 6, lo único que cambia es quién le
pasa los datos a la pantalla.

**El dominio no sabe de Android.** `Modelos.kt` son data class puras.
Esa separación es lo que después deja probar las reglas de negocio con
JUnit normal.

**El dinero es `Long` en pesos enteros, nunca `Double`.** Un `Double` no
representa 0,1 de forma exacta y el error se acumula al sumar: la caja
no cuadraría por unos pesos. Pregunta clásica de entrevista.

**`EntradaHistorial` es una `sealed class`.** El historial mezcla fiados
y abonos, y solo esos dos. Al recorrerla con `when`, el compilador
obliga a cubrir los dos casos y no hace falta `else`. Si mañana se
agrega un tercer tipo, el código deja de compilar justo donde hay que
actualizarlo.

**Las rutas son constantes en un solo archivo.** Una ruta mal escrita
como cadena suelta compila igual y falla en ejecución; como constante,
el error sale al compilar.

**La gráfica está dibujada con `Canvas`, sin librerías.** Para dos
series y siete días no se justifica una dependencia. Y debajo va la
misma información en tabla: una gráfica se lee rápido pero es imprecisa,
y quien use lector de pantalla no la ve.

**`java.time` sin desugaring.** `LocalDateTime` existe desde API 26, que
es justo el `minSdk` del proyecto. Si fuera menor, habría que activar
*core library desugaring* en Gradle.

---

## 6. Lo que todavía NO está

Esto es un armazón: se ve y se navega, pero no guarda nada.

| Falta | Llega el día |
|---|---|
| ViewModel y `UiState` real | 6 |
| Room y persistencia | 7 |
| Validaciones de verdad | 6 a 8 |
| Selector de fecha | 8 |
| Registrar fiado y abono | 9 y 10 |
| Recordatorio por WhatsApp | 10 |
| Tests | 9 a 11 |

Dentro del código hay comentarios `/* ... día N */` en los lugares donde
falta conectar algo.

---

## 7. Si algo no compila

Por orden de probabilidad:

1. **Falta la dependencia de navegación.** Si el error menciona
   `NavHost`, `rememberNavController` o `androidx.navigation`, es la
   sección 2.
2. **`HorizontalDivider` o nombres de Material 3.** Material 3 renombró
   varios componentes entre versiones. Si marca error, Android Studio
   suele ofrecer el nombre correcto con Alt+Enter.
3. **Iconos.** Usé solo los del paquete básico (`Add`, `Edit`, `Delete`,
   `Search`, `Phone`, `DateRange`, `ArrowBack`). Si alguno no aparece,
   falta `material-icons-extended` o cambió de ruta.
4. **`Icons.AutoMirrored`.** En versiones nuevas `ArrowBack` se movió
   ahí. Si tu versión es anterior, usa `Icons.Default.ArrowBack`.
5. **El paquete.** Todos los archivos declaran
   `package com.williammarrugo.fiao`. Si tu proyecto quedó con otro
   nombre, hay que ajustarlo.

Mándame el error tal cual aparece.

---

## 8. Preguntas para la sesión de análisis

Estas son del tipo que te pueden hacer en la entrevista sobre tu propio
código. Respóndelas con tus palabras, mirando los archivos.

**Sobre Compose**

1. ¿Por qué `InicioScreen` recibe los datos por parámetro en lugar de
   buscarlos ella misma?
2. ¿Qué hace `@Preview` y por qué casi todos los componentes tienen una?
3. En `MovimientoFormScreen`, ¿por qué el diálogo de confirmación usa
   `rememberSaveable` y no `remember`? ¿Qué pasaría al rotar?
4. ¿Qué significa que `BarraSuperior` reciba `acciones` como
   `@Composable () -> Unit`?

**Sobre Kotlin**

5. ¿Por qué `EntradaHistorial` es `sealed` y qué gana el `when` de
   `FilaHistorial` con eso?
6. En `Modelos.kt`, `ResumenDia.balance` es un `val` con `get()`.
   ¿En qué se diferencia de guardarlo como propiedad normal?
7. ¿Por qué `Cliente.telefono` es `String?` y `Cliente.nombre` no?

**Sobre arquitectura**

8. Si mañana los datos vinieran de un servidor en vez de Room,
   ¿qué archivos habría que tocar y cuáles no?
9. ¿Por qué el dominio no importa nada de `androidx`?

**Sobre diseño**

10. ¿Por qué "Por cobrar" es una tarjeta y los movimientos son filas?
11. ¿Qué pasaría si dejáramos el dynamic color activado?
12. ¿Por qué el monto lleva signo además de color?

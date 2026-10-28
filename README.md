# Fiao

App Android para que un microempresario lleve la caja del día y las
cuentas de lo que le fían.

Nació de una observación simple: en la tienda de barrio la caja se
cuadra con un cuaderno, y los fiados se apuntan en otra hoja que casi
nunca cuadra con la primera. Fiao junta las dos cosas y responde de un
vistazo las dos preguntas que importan: **cuánto hice hoy** y **quién me
debe**.

\---

## Capturas



|Inicio|Movimientos|Clientes|
|-|-|-|
|!\[Inicio](docs/MainView.jpeg)|!\[Movimientos](docs/RegistroEntrada.jpeg)|!\[Clientes](docs/PestañaClientes.jpeg)|

\---

## Qué hace

|Estado|Función|
|-|-|
|Listo|Registrar ventas y gastos, con validación|
|Listo|Ver los movimientos del día y el balance|
|Listo|Editar y eliminar movimientos|
|Listo|Consultar cualquier día con un calendario|
|Listo|Crear, editar y eliminar clientes|
|Listo|Persistencia local: los datos sobreviven al cierre|
|Pendiente|Registrar fiados y abonos|
|Pendiente|Recordatorio de cobro por WhatsApp|
|Pendiente|Resumen semanal con datos reales|

\---

## Stack

* **Kotlin** y **Jetpack Compose** con Material 3
* **Room** para persistencia local
* **Coroutines** y **Flow** para datos reactivos
* **Navigation Compose**
* **JUnit** para las pruebas del dominio
* SDK mínimo 26, sin dependencias de servicios externos

\---

## Arquitectura

Tres capas, con las dependencias apuntando siempre hacia adentro.

```
ui/          Pantallas (Compose) y ViewModels
   |         Solo dibujan y reportan acciones.
   v
domain/      Modelos, reglas de negocio e interfaces de repositorio.
   ^         No importa nada de Android ni de Room.
   |
data/        Room: entidades, DAOs y la implementación de los
             repositorios que el dominio declara.
```

La decisión central es que **`domain` no conoce a `data`**. Los
repositorios se declaran como interfaces en el dominio y se implementan
con Room en la capa de datos. El contenedor de dependencias es el único
sitio de la app que sabe que los datos vienen de SQLite.

Eso tiene dos consecuencias prácticas:

* Las reglas de negocio se prueban con JUnit en milisegundos, sin
emulador, porque son funciones puras.
* Cambiar el origen de los datos (un backend, otra base) implica
escribir otra implementación de las mismas interfaces, sin tocar
ViewModels ni pantallas.

### Flujo de datos

Unidireccional. Room expone `Flow`, el ViewModel los combina en un
`UiState` único y lo publica como `StateFlow`; la pantalla lo observa
con `collectAsStateWithLifecycle` y manda las acciones de vuelta al
ViewModel. Nadie llama a "recargar": al escribir en la base de datos,
la pantalla se redibuja sola.

El estado vive en el ViewModel, así que sobrevive a los cambios de
orientación.

### Reglas de negocio

Están en `domain/rules/Reglas.kt`, aisladas y con pruebas:

1. Todo monto es un entero en pesos y mayor que cero.
2. El balance del día es ventas más abonos, menos gastos.
3. Un fiado no entra al balance: solo aumenta lo que está por cobrar.
4. El saldo de un cliente es lo fiado menos lo abonado, nunca negativo.
5. No se puede abonar más de lo que se debe.
6. No se elimina un cliente con saldo pendiente.

El dinero se guarda como `Long` en pesos enteros, nunca como `Double`:
un `Double` no representa 0,1 de forma exacta y el error se acumula al
sumar, con lo que la caja no cuadraría.

\---

## Decisiones de diseño

**Tres colores cargan información**, no decoración: verde para ventas,
terracota para gastos y ocre para fiados. Los valores no se eligieron a
ojo: se validaron contra deuteranopía y protanopía, porque el par
verde/rojo que uno usaría por instinto resulta casi indistinguible para
cerca del 8% de los hombres. Aun así, el color nunca va solo: cada
monto lleva signo y cada fila lleva su etiqueta.

**Sin dynamic color.** Material You tomaría los colores del fondo de
pantalla del usuario y podría volver indistinguibles venta y fiado.
Cuando el color carga información, no se delega al fondo de pantalla.

**Cifras tabulares** en todos los montos, para que las columnas de
dinero queden alineadas.

**Las tarjetas significan "objeto aparte"; las listas llevan
separadores.** Lo por cobrar es una tarjeta porque es otra clase de
dinero: no está en la caja, está en la calle.

\---

## Cómo compilarlo

Requiere Android Studio reciente y un dispositivo o emulador con
Android 8.0 (API 26) o superior.

```bash
git clone https://github.com/WillMarr19/Fiao.git
cd Fiao
./gradlew assembleDebug
```

O ábrelo en Android Studio y pulsa Run. No hace falta configurar claves
ni cuentas: la app funciona sin conexión y sin servicios externos.

### Pruebas

```bash
./gradlew test
```

\---

## Uso de IA

Este proyecto se desarrolló con asistencia de IA (Claude) para parte de
la implementación. El alcance, el modelo de dominio, las reglas de
negocio y las decisiones de producto son míos, y reviso y entiendo todo
el código del repositorio.

\---

## Qué sigue

* Registro de fiados y abonos
* Recordatorio de cobro por WhatsApp
* Resumen semanal con datos reales
* Pruebas de los ViewModels
* Sincronización opcional con un backend

\---

## Autor

**William E. Marrugo Ruiz** — Cartagena de Indias, Colombia
[GitHub](https://github.com/WillMarr19)


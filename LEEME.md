# Mis Finanzas — app Android nativa

App nativa en **Kotlin + Jetpack Compose (Material 3)**. Sin HTML ni WebView.
Los datos se guardan en una base **SQLite** dentro del teléfono y funciona sin internet.

## Qué hace
- **Inicio**: balance del mes, ingresos, gastos, tasa de ahorro (meta 20 %), en qué se va tu dinero y gráfico de los últimos 6 meses.
- **Movimientos**: lista del mes agrupada por día, con filtros Todos / Gastos / Ingresos. Toca uno para editarlo o eliminarlo.
- **Presupuestos**: límite mensual por categoría con barra de avance y alertas ("Cerca del límite", "Excedido"). Toca una categoría para cambiar su límite.
- **Nuevo**: monto, tipo, categoría, fecha (calendario) y detalle.
- Menú ⋮: exportar respaldo CSV (WhatsApp, Drive, correo), cargar datos de ejemplo, borrar movimientos.
- Modo claro y oscuro automático.

## Obtener el APK sin instalar nada (GitHub)
1. Crea un repositorio en GitHub (puede ser privado).
2. Sube todo el contenido de esta carpeta, incluida la carpeta oculta `.github`.
3. Pestaña **Actions** → se ejecuta "Construir APK" (5–8 min la primera vez).
4. Entra a la ejecución terminada → **Artifacts** → descarga `MisFinanzas-apk` (zip con el `.apk`).
5. Pásalo al celular, ábrelo y permite "Instalar apps desconocidas".

## Con Android Studio
File → Open → esta carpeta → espera la sincronización → Build → Build App Bundle(s) / APK(s) → Build APK(s).
También puedes conectar el celular por USB y darle ▶ Run.

## Estructura
```
app/src/main/java/pe/misfinanzas/app/
├── MainActivity.kt              Punto de entrada
├── data/
│   ├── Modelos.kt               Movimiento, Tipo, categorías y presupuestos iniciales
│   ├── FinanzasDb.kt            Tablas SQLite
│   ├── Repositorio.kt           Lecturas y escrituras
│   └── DatosEjemplo.kt          6 meses de datos ficticios
└── ui/
    ├── AppFinanzas.kt           Barra superior, navegación inferior, menú
    ├── FinanzasViewModel.kt     Estado de la app
    ├── Componentes.kt           Selector de mes, tarjetas, filas
    ├── Formato.kt               Soles y fechas en español (Perú)
    ├── theme/Tema.kt            Colores y tipografía
    └── pantallas/               Inicio, Movimientos, Presupuestos, Formulario
```
Para cambiar categorías o presupuestos iniciales edita `data/Modelos.kt`.

## Importante
Desinstalar la app borra los datos. Exporta el respaldo CSV de vez en cuando.

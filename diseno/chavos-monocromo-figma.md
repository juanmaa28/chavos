# Chavos · Estilo Monocromo — Especificación para replicar en Figma

Documento de entrada para un agente de IA con acceso a Figma por MCP (herramienta `use_figma` / Plugin API). Describe **todo** lo necesario para reconstruir en Figma las 7 pantallas del estilo Monocromo de la app Chavos: tokens, tipografía, íconos, componentes y pantallas, con medidas exactas y textos finales.

Fuente de verdad: `diseno/monocromo.html` y las capturas en `diseno/png/monocromo/`. Si algo de este documento contradice una captura, manda la captura.

---

## 0. Instrucciones para el agente

1. **Antes de escribir en Figma**, carga la guía del MCP (skill `figma-use` o el recurso `skill://figma/figma-use/SKILL.md`). Sin ella se repiten errores conocidos.
2. **Presupuesto de llamadas.** La cuenta del usuario tiene asiento *View* en plan *Starter*: el MCP permite unas **20 llamadas al mes**. Agrupa el trabajo en pocas llamadas grandes y planifica así:
   | # | Llamada | Contenido |
   |---|---|---|
   | 1 | `create_new_file` | Archivo nuevo (no cuenta para el límite) |
   | 2 | `use_figma` | Páginas + variables de color + estilos de texto y efectos |
   | 3 | `use_figma` | Íconos como componentes (sección 6) |
   | 4 | `use_figma` | Componentes base (sección 7.1 a 7.10) |
   | 5 | `use_figma` | Componentes compuestos (sección 7.11 a 7.22) |
   | 6–12 | `use_figma` | Una pantalla por llamada (sección 8) |
   | 13 | `get_screenshot` | Verificación visual de la página Pantallas |
3. **Fuente.** Todo el sistema usa **Schibsted Grotesk**. Carga cada estilo con `figma.loadFontAsync` antes de crear o editar texto. Verifica los nombres exactos de los estilos con `figma.listAvailableFontsAsync()` (se esperan `Regular`, `Medium`, `SemiBold`, `Bold`).
4. **Contenedores sin relleno.** Un frame nuevo trae relleno blanco por defecto. En todo frame que solo agrupa (filas, columnas, wrappers de auto layout) pon `fills = []`. Olvidarlo produce cajas blancas sueltas sobre el fondo gris: ya pasó una vez en este proyecto.
5. **Auto layout en todo.** Usa `figma.createAutoLayout()` para cualquier contenedor con hijos relacionados. Añade el hijo con `appendChild` **antes** de asignar `layoutSizingHorizontal = 'FILL'`.
6. **Cifras.** No actives números tabulares: con Schibsted Grotesk el punto de miles queda con espacios anchos (`$1 . 250 . 000`). Usa los números proporcionales por defecto.
7. **Signo menos.** Los gastos usan el carácter `−` (U+2212), no el guion `-`.
8. **Idioma.** Todos los textos van en español con tildes y signos de apertura (`¿`). Copia los textos de este documento tal cual.
9. **Devuelve IDs.** Cada llamada debe retornar los IDs de lo que crea, para referenciarlos en la siguiente.

---

## 1. Qué se va a construir

App de finanzas personales para Android, estilo **Monocromo**: blanco, negro y grises en la tradición del diseño suizo. El color aparece solo donde informa algo: ingresos (verde), gastos (rojo apagado) y validaciones.

| Pantalla | Frame | Tiene barra de pestañas |
|---|---|---|
| Login | `01 Login` | No |
| Registro | `02 Registro` | No |
| Inicio | `03 Inicio` | Sí, pestaña Inicio activa |
| Registrar (hoja sobre Inicio) | `04 Registrar` | Sí, bajo el velo |
| Informe | `05 Informe` | No (pantalla apilada) |
| Metas | `06 Metas` | Sí, pestaña Metas activa |
| Ajustes | `07 Ajustes` | Sí, pestaña Ajustes activa |

Principios que el resultado debe respetar:
- La tarjeta de saldo es el **único bloque negro grande** de la app.
- Íconos de categoría **siempre en gris**; se distinguen por forma y etiqueta, nunca por color.
- Sin sombras en tarjetas: la separación sale de un borde de 1 px y del contraste gris/blanco.
- Botón principal negro; botón secundario solo con contorno.

---

## 2. Configuración del archivo

- **Páginas:** `Fundamentos` (colores, tipografía, íconos), `Componentes`, `Pantallas`.
- **Frames de pantalla:** 390 × 844 px (iPhone 14 / Android equivalente), fondo `color/bg/app`, `clipsContent = true`, sin radio de esquina.
- Colocar las 7 pantallas en una fila con 80 px de separación, empezando en (0, 0), en el orden de la tabla de la sección 1.
- **Margen lateral de pantalla:** 20 px a izquierda y derecha.
- Opcional, solo para presentación: duplicar cada pantalla dentro de un marco de 390 × 844 con radio 44 y sombra `0 30 60 -22 rgba(0,0,0,0.35)`.

---

## 3. Color

Crear una colección de variables **`Chavos`** con un modo **`Claro`**. Asignar `scopes` explícitos (no dejar `ALL_SCOPES`).

### 3.1 Colores base

| Variable | Hex | Uso | Scopes |
|---|---|---|---|
| `color/bg/app` | `#EDEDED` | Fondo de todas las pantallas | FRAME_FILL |
| `color/surface/card` | `#FFFFFF` | Tarjetas, barra de pestañas, hoja inferior | FRAME_FILL, SHAPE_FILL |
| `color/surface/muted` | `#F1F1F1` | Fondo de íconos de categoría y botón cerrar | FRAME_FILL, SHAPE_FILL |
| `color/ink/primary` | `#000000` | Texto principal, botón principal, tarjeta de saldo, FAB, avatar | TEXT_FILL, FRAME_FILL, SHAPE_FILL, STROKE_COLOR |
| `color/ink/secondary` | `#4D4D4D` | Texto secundario, glifos de íconos de categoría | TEXT_FILL, SHAPE_FILL, STROKE_COLOR |
| `color/ink/tertiary` | `#8A8A8A` | Etiquetas, subtítulos, fechas, valores de ajustes | TEXT_FILL, STROKE_COLOR |
| `color/ink/on-dark-muted` | `#9A9A9A` | Etiquetas dentro de la tarjeta negra; pestañas inactivas | TEXT_FILL, STROKE_COLOR |
| `color/ink/inverse` | `#FFFFFF` | Texto sobre negro | TEXT_FILL, STROKE_COLOR |
| `color/icon/chevron` | `#B0B0B0` | Chevrones de filas | STROKE_COLOR |

### 3.2 Líneas y pistas

| Variable | Hex | Uso |
|---|---|---|
| `color/line/card` | `#E3E3E3` | Borde 1 px de tarjetas y botones de ícono |
| `color/line/hair` | `#ECECEC` | Divisor 1 px entre filas dentro de una tarjeta |
| `color/line/tabbar` | `#E0E0E0` | Borde superior de la barra de pestañas |
| `color/line/input` | `#CFCFCF` | Subrayado de campos y borde del botón secundario |
| `color/track/default` | `#E6E6E6` | Pista de barras de progreso sobre blanco |
| `color/track/on-bg` | `#DADADA` | Pista de progreso sobre el fondo gris (resumen de Metas) |
| `color/track/step` | `#D0D0D0` | Segmento inactivo del indicador de pasos |
| `color/track/strength` | `#D6D6D6` | Segmento vacío del medidor de contraseña; interruptor apagado |
| `color/chart/bar` | `#C8C8C8` | Barras de meses anteriores |
| `color/chart/average` | `#9A9A9A` | Línea punteada del promedio |
| `color/sheet/grabber` | `#D4D4D4` | Asa de la hoja inferior |

### 3.3 Semánticos

| Variable | Hex | Uso |
|---|---|---|
| `color/semantic/positive` | `#2E6B45` | Montos de ingreso, check de validación |
| `color/semantic/negative` | `#9E2A2B` | Montos de gasto, texto e ícono de "Cerrar sesión" |
| `color/semantic/negative-tint` | `#F5E7E7` | Fondo del ícono de "Cerrar sesión" |
| `color/overlay/scrim` | `#000000` al 45 % | Velo detrás de la hoja inferior |
| `color/hero/divider` | `#FFFFFF` al 16 % | Línea divisoria dentro de la tarjeta de saldo |

### 3.4 Paleta para gráficas por categoría (reservada)

No se usa en estas 7 pantallas, pero forma parte del sistema: es la única paleta permitida para gráficas que distinguen categorías por color. Está validada para daltonismo (ΔE 14,6 entre vecinos) y visión normal (ΔE 22,7). **Respetar el orden.**

| Orden | Variable | Hex |
|---|---|---|
| 1 | `color/chart/cat-1` | `#3B8D5D` |
| 2 | `color/chart/cat-2` | `#A280C8` |
| 3 | `color/chart/cat-3` | `#255691` |
| 4 | `color/chart/cat-4` | `#CFA354` |
| 5 | `color/chart/cat-5` | `#984646` |

---

## 4. Tipografía

**Familia única:** Schibsted Grotesk (Google Fonts). Crear un estilo de texto por fila. En Figma el interlineado va en px y el espaciado entre letras en **porcentaje** (`-0.045em` = `-4.5 %`).

| Estilo de texto | Tamaño | Peso | Interlineado | Letras | Dónde se usa |
|---|---|---|---|---|---|
| `Display/Hero 48` | 48 | Bold | 48 px | −4.5 % | Monto de la tarjeta de saldo |
| `Display/Login 46` | 46 | Bold | 45 px | −5 % | "Tus finanzas, en calma." |
| `Display/Number 40` | 40 | Bold | 40 px | −4.5 % | Cifra principal de Informe y Metas |
| `Title/Page 34` | 34 | Bold | 35 px | −4.5 % | "Crea tu cuenta" |
| `Title/Large 30` | 30 | Bold | Auto | −4 % | Títulos "Metas" y "Ajustes" |
| `Title/Greeting 26` | 26 | Bold | Auto | −3.5 % | "Hola, Juan" |
| `Title/Sheet 24` | 24 | Bold | Auto | −3 % | "Registrar" |
| `Title/Wordmark 20` | 20 | Bold | Auto | −2 % | "Chavos" junto al logo |
| `Heading/Section 18` | 18 | Bold | Auto | −2 % | "Recientes"; porcentaje de cada meta |
| `Heading/Sub 16` | 16 | Bold | Auto | −1 % | "Por categoría" |
| `Heading/Card 15` | 15 | Bold | Auto | 0 | "Últimos seis meses", "Informe" en barra superior |
| `Body/Row title 16` | 16 | SemiBold | Auto | −1 % | Opciones de la hoja, nombre en tarjeta de perfil |
| `Body/Row title 15` | 15 | SemiBold | Auto | −1 % | Títulos de fila, etiquetas de ajustes, montos de lista |
| `Body/Value 16` | 16 | SemiBold | Auto | −1 % | Ingresos y gastos dentro de la tarjeta de saldo |
| `Body/Input 16` | 16 | Regular | Auto | 0 | Texto dentro de campos |
| `Body/Lead 15` | 15 | Regular | 150 % | 0 | Subtítulos de Login y Registro |
| `Body/Button 15.5` | 15.5 | SemiBold | Auto | 0 | Texto de botones |
| `Body/Category 14` | 14 | SemiBold | Auto | −1 % | Nombre y monto por categoría en Informe |
| `Body/Month 14` | 14 | Bold | Auto | 0 | "Septiembre 2026" |
| `Body/Footer 14` | 14 | Regular | Auto | 0 | "¿Aún no tienes cuenta?…" |
| `Body/Setting value 14` | 14 | Regular | Auto | 0 | "COP $", "20:00", "CSV" |
| `Body/Link 13.5` | 13.5 | SemiBold | Auto | 0 | "¿Olvidaste tu contraseña?" (subrayado) |
| `Body/Small 13.5` | 13.5 | Regular | 150 % | 0 | Texto de términos |
| `Body/Note 13` | 13 | Regular | Auto | 0 | Comparaciones y resúmenes bajo cifras grandes |
| `Body/Link 13` | 13 | SemiBold | Auto | 0 | "Ver todos" |
| `Label/Field 12.5` | 12.5 | SemiBold | Auto | 0 | Etiquetas de campos y de grupos de ajustes |
| `Label/Meta 12.5` | 12.5 | Medium | Auto | 0 | Fecha, "Gastaste", "Llevas ahorrado", etiqueta de la tarjeta de saldo |
| `Label/Caption 12` | 12 | Regular | Auto | 0 | Subtítulo de fila ("Alimentación, hoy") |
| `Label/Percent 12` | 12 | SemiBold | Auto | 0 | Porcentaje por categoría; medidor de contraseña |
| `Label/Badge 12` | 12 | Bold | Auto | 0 | Número del distintivo |
| `Label/Hero split 11.5` | 11.5 | Medium | Auto | 0 | "Ingresos" y "Gastos" dentro de la tarjeta negra |
| `Label/Chart 11.5` | 11.5 | Bold | Auto | 0 | Etiqueta directa "$1,25 M" |
| `Label/Chart axis 11` | 11 | Medium | Auto | 0 | Meses del eje (el mes actual en Bold y negro) |
| `Label/Tab 10.5` | 10.5 | SemiBold | Auto | 0 | Etiquetas de la barra de pestañas |
| `System/Status 14` | 14 | SemiBold | Auto | 0 | Hora de la barra de estado |

---

## 5. Espaciado, radios, bordes y efectos

### 5.1 Radios

| Token | Valor | Elementos |
|---|---|---|
| `radius/hero` | 12 | Tarjeta de saldo |
| `radius/card` | 10 | Tarjetas blancas |
| `radius/sheet-top` | 18 | Esquinas superiores de la hoja inferior |
| `radius/control` | 8 | Botones, botón de ícono, avatar, íconos de categoría 34–40 px |
| `radius/control-lg` | 10 | FAB, íconos de categoría de 46 px |
| `radius/checkbox` | 5 | Casilla |
| `radius/pill` | 999 | Interruptor, distintivo, asa, botón cerrar (círculo) |
| `radius/bar` | 4 (solo arriba) | Barras de la gráfica |
| `radius/track` | 3 (6 px de alto) · 4 (8 px de alto) | Barras de progreso |

### 5.2 Bordes
- Tarjetas y botón de ícono: 1 px interior, `color/line/card`.
- Divisor entre filas: línea superior de 1 px, `color/line/hair`, en cada fila excepto la primera.
- Campo de texto: solo borde inferior de 1.5 px, `color/line/input`; activo: `color/ink/primary`.
- Botón secundario: 1.5 px interior, `color/line/input`.

### 5.3 Efectos
- `Effect/Toggle knob`: sombra `0 1 3 0 rgba(0,0,0,0.25)`.
- No hay otras sombras en la interfaz.

### 5.4 Medidas fijas del sistema

| Elemento | Medida |
|---|---|
| Barra de estado | 390 × 46; hora a 30 px del borde izquierdo; íconos a 26 px del borde derecho, separados 6 px |
| Indicador de inicio | 134 × 5, radio 3, `#000000` al 30 %, centrado, a 8 px del borde inferior |
| Barra de pestañas | 390 × 78 |
| Barra superior (Informe, Registro) | 390 × 52, relleno lateral 20 |
| Área táctil mínima | 44 × 44 |

---

## 6. Iconografía

Reglas: retícula de 24 × 24, trazo **1.7 px** (salvo que se indique otro), puntas y uniones **redondeadas**, sin relleno. Tamaños usados: 13, 14, 16, 18, 20, 22, 24. El color del trazo se asigna por instancia.

Crear cada ícono con `figma.createNodeFromSvg()` usando esta plantilla y convertirlo en componente `Icon/<nombre>`:

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#000000" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">RUTAS</svg>
```

| Componente | RUTAS |
|---|---|
| `Icon/home` | `<path d="M3 10.2 12 3l9 7.2"/><path d="M5.5 9.3V20h13V9.3"/><path d="M9.8 20v-5.6h4.4V20"/>` |
| `Icon/list` | `<path d="M8 6h12M8 12h12M8 18h12"/><circle cx="4" cy="6" r="1.1"/><circle cx="4" cy="12" r="1.1"/><circle cx="4" cy="18" r="1.1"/>` |
| `Icon/target` | `<circle cx="12" cy="12" r="8.4"/><circle cx="12" cy="12" r="4.4"/><circle cx="12" cy="12" r="1"/>` |
| `Icon/gear` | `<circle cx="12" cy="12" r="3.1"/><path d="M19.3 14.2a1.5 1.5 0 0 0 .3 1.7l.1.1a1.8 1.8 0 1 1-2.6 2.6l-.1-.1a1.5 1.5 0 0 0-2.6 1.1v.3a1.8 1.8 0 1 1-3.6 0v-.2a1.5 1.5 0 0 0-2.7-1.1l-.1.1a1.8 1.8 0 1 1-2.6-2.6l.1-.1a1.5 1.5 0 0 0-1.1-2.6h-.3a1.8 1.8 0 0 1 0-3.6h.2A1.5 1.5 0 0 0 5.4 7l-.1-.1a1.8 1.8 0 1 1 2.6-2.6l.1.1a1.5 1.5 0 0 0 1.7.3h.1A1.5 1.5 0 0 0 10.7 3.3V3a1.8 1.8 0 1 1 3.6 0v.2a1.5 1.5 0 0 0 2.6 1.1l.1-.1a1.8 1.8 0 1 1 2.6 2.6l-.1.1a1.5 1.5 0 0 0 1.1 2.6h.3a1.8 1.8 0 0 1 0 3.6h-.2a1.5 1.5 0 0 0-1.4.9z"/>` |
| `Icon/plus` | `<path d="M12 5v14M5 12h14"/>` |
| `Icon/x` | `<path d="m6 6 12 12M18 6 6 18"/>` |
| `Icon/chevron-right` | `<path d="m9 5 7 7-7 7"/>` |
| `Icon/chevron-left` | `<path d="m15 5-7 7 7 7"/>` |
| `Icon/arrow-up-right` | `<path d="M7 17 17 7M9 7h8v8"/>` |
| `Icon/arrow-down-left` | `<path d="M17 7 7 17M15 17H7V9"/>` |
| `Icon/cart` | `<path d="M3 4h2.2l2 11.2h10.3l2-8.2H6.4"/><circle cx="9" cy="19.2" r="1.3"/><circle cx="17" cy="19.2" r="1.3"/>` |
| `Icon/bus` | `<rect x="4" y="4" width="16" height="13" rx="3"/><path d="M4 11h16"/><circle cx="8" cy="14.4" r="1"/><circle cx="16" cy="14.4" r="1"/><path d="M7 17v2.4M17 17v2.4"/>` |
| `Icon/play` | `<rect x="3" y="5" width="18" height="14" rx="3.2"/><path d="m10.5 9.6 4.4 2.4-4.4 2.4z"/>` |
| `Icon/heart` | `<path d="M12 19.5s-7.2-4.3-7.2-9A3.9 3.9 0 0 1 12 8.2a3.9 3.9 0 0 1 7.2 2.3c0 4.7-7.2 9-7.2 9z"/>` |
| `Icon/wallet` | `<path d="M4 7.5A2.5 2.5 0 0 1 6.5 5h11A1.5 1.5 0 0 1 19 6.5V8"/><rect x="4" y="7.5" width="16" height="11.5" rx="2.5"/><circle cx="16" cy="13.2" r="1.2"/>` |
| `Icon/more` | `<circle cx="5" cy="12" r="1.3"/><circle cx="12" cy="12" r="1.3"/><circle cx="19" cy="12" r="1.3"/>` |
| `Icon/send` | `<path d="M21 3 3 10.5l7 2.5 2.5 7z"/><path d="m10 13 4-4"/>` |
| `Icon/shield` | `<path d="M12 3.4 5 6v6c0 4.2 2.9 7.3 7 8.6 4.1-1.3 7-4.4 7-8.6V6z"/>` |
| `Icon/laptop` | `<rect x="4.5" y="5" width="15" height="10.5" rx="1.6"/><path d="M2.5 19h19"/>` |
| `Icon/bell` | `<path d="M18 16.2V11a6 6 0 1 0-12 0v5.2L4.4 18.4h15.2z"/><path d="M10.2 21.2a2 2 0 0 0 3.6 0"/>` |
| `Icon/coin` | `<circle cx="12" cy="12" r="8.4"/><path d="M14.6 9.4c-.5-.8-1.5-1.3-2.6-1.3-1.5 0-2.6.8-2.6 2 0 2.7 5.4 1.3 5.4 4 0 1.2-1.2 2-2.8 2-1.2 0-2.2-.5-2.7-1.4M12 6.6v1.5M12 15.9v1.5"/>` |
| `Icon/moon` | `<path d="M20 14.2A8.2 8.2 0 0 1 9.8 4 8.4 8.4 0 1 0 20 14.2z"/>` |
| `Icon/calendar` | `<rect x="3.6" y="5" width="16.8" height="15" rx="2.6"/><path d="M3.6 9.6h16.8M8.4 3.4v3.2M15.6 3.4v3.2"/>` |
| `Icon/fingerprint` | `<path d="M6.2 11a5.8 5.8 0 0 1 11.6 0v2.2"/><path d="M9 11.4a3 3 0 0 1 6 0v3.4a6 6 0 0 1-.7 2.8"/><path d="M12 11.6v4.2a9 9 0 0 1-1 4.2"/><path d="M4.5 8.2A9 9 0 0 1 19.4 8"/><path d="M17.7 15.6a13 13 0 0 1-.7 4"/>` |
| `Icon/export` | `<path d="M12 4.5v11M7.5 9 12 4.5 16.5 9"/><path d="M5 14.5V19h14v-4.5"/>` |
| `Icon/logout` | `<path d="M14.5 4.5h-8A1.5 1.5 0 0 0 5 6v12a1.5 1.5 0 0 0 1.5 1.5h8"/><path d="m15.5 15.5 3.5-3.5-3.5-3.5M19 12h-9"/>` |
| `Icon/info` | `<circle cx="12" cy="12" r="8.6"/><path d="M12 11v5.2"/><circle cx="12" cy="7.8" r=".9" fill="#000000" stroke="none"/>` |
| `Icon/mail` | `<rect x="3" y="5.5" width="18" height="13" rx="2.4"/><path d="m3.8 7 8.2 6 8.2-6"/>` |
| `Icon/user` | `<circle cx="12" cy="8.4" r="3.8"/><path d="M4.8 20c.9-3.6 3.8-5.6 7.2-5.6s6.3 2 7.2 5.6"/>` |
| `Icon/eye` | `<path d="M2.8 12S6.4 5.8 12 5.8 21.2 12 21.2 12 17.6 18.2 12 18.2 2.8 12 2.8 12z"/><circle cx="12" cy="12" r="2.8"/>` |
| `Icon/check` | `<path d="m5.5 12.5 4 4 9-9"/>` |
| `Icon/wifi` | `<path d="M2.5 9.2a14 14 0 0 1 19 0M5.8 12.6a9.4 9.4 0 0 1 12.4 0M9 16a4.8 4.8 0 0 1 6 0"/><circle cx="12" cy="19.2" r=".9" fill="#000000" stroke="none"/>` |

**Batería** (barra de estado), 24 × 12, importar tal cual:
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="12" viewBox="0 0 24 12" fill="none"><rect x="1" y="1.5" width="18" height="9" rx="2.6" stroke="#000" stroke-width="1.2" opacity=".5"/><rect x="2.8" y="3.3" width="13" height="5.4" rx="1.5" fill="#000"/><path d="M21 5v2.4" stroke="#000" stroke-width="1.6" stroke-linecap="round" opacity=".5"/></svg>
```

**Logotipo** (tres monedas), 34 × 34, componente `Brand/Logo mark`:
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="34" height="34" viewBox="0 0 34 34" fill="none"><circle cx="11" cy="22" r="9" fill="#BDBDBD"/><circle cx="19" cy="15" r="9" fill="#000000"/><circle cx="24" cy="9" r="6.2" fill="#FFFFFF" stroke="#000000" stroke-width="1.6"/></svg>
```

---

## 7. Componentes

Todos en la página `Componentes`. Nombre = `Categoría/Nombre`; las variantes se agrupan con `combineAsVariants`. Cada componente con propiedades de texto e intercambio de ícono (INSTANCE_SWAP) donde se indique.

### 7.1 `System/Status bar`
Frame 390 × 46, auto layout horizontal, `space-between`, alineación vertical al centro, relleno izquierdo 30 y derecho 26, sin relleno de color.
- Texto "9:41" (`System/Status 14`, negro).
- Grupo derecho, auto layout horizontal, espacio 6: `Icon/wifi` 16 px + batería.

### 7.2 `System/Home indicator`
Rectángulo 134 × 5, radio 3, `#000000` al 30 %. En pantallas se posiciona absoluto, centrado, a 8 px del borde inferior y por encima de todo.

### 7.3 `Button` — variantes `Type=Primary | Secondary`
Auto layout horizontal, alto fijo 52, ancho FILL, contenido centrado, espacio 8, radio 8.
- **Primary:** relleno `color/ink/primary`; texto `Body/Button 15.5` blanco.
- **Secondary:** sin relleno; borde 1.5 px `color/line/input`; texto negro; ícono opcional de 20 px a la izquierda del texto.
- Propiedades: `Label` (texto), `Show icon` (booleano), `Icon` (intercambio).

### 7.4 `Icon button`
38 × 38, relleno blanco, borde 1 px `color/line/card`, radio 8, ícono de 20 px centrado (trazo 1.7, o 2 para `plus`).

### 7.5 `Avatar` — variantes `Size=38 | 46`
Cuadrado negro, radio 8, iniciales "JM" centradas en blanco: Bold 13.5 (38) o Bold 15 (46).

### 7.6 `Checkbox` (estado marcado)
20 × 20, radio 5, relleno negro, `Icon/check` 13 px con trazo 2.6 blanco centrado.

### 7.7 `Toggle` — variantes `State=On | Off`
44 × 26, radio 13. Perilla blanca 20 × 20 con `Effect/Toggle knob`, a 3 px del borde.
- **Off:** pista `color/track/strength` (#D6D6D6), perilla a la izquierda (x = 3).
- **On:** pista negra, perilla a la derecha (x = 21).

### 7.8 `Badge`
Auto layout horizontal, alto 22, ancho mínimo 22, relleno horizontal 7, radio 11, relleno negro, texto `Label/Badge 12` blanco centrado. Contenido de ejemplo: "2".

### 7.9 `Category icon` — variantes `Size=34 | 36 | 40 | 46` × `Tone=Muted | Dark | Danger`
Cuadrado con ícono centrado.
| Size | Radio | Ícono |
|---|---|---|
| 34 | 8 | 18 px |
| 36 | 8 | 18 px |
| 40 | 8 | 20 px |
| 46 | 10 | 22 px |

| Tone | Fondo | Trazo del ícono |
|---|---|---|
| Muted | `color/surface/muted` #F1F1F1 | `color/ink/secondary` #4D4D4D |
| Dark | `color/ink/primary` #000000 | blanco |
| Danger | `color/semantic/negative-tint` #F5E7E7 | `color/semantic/negative` #9E2A2B |

Propiedad `Icon` (intercambio).

### 7.10 `Text field` — variantes `State=Default | Active`
Auto layout vertical, espacio 9, ancho FILL.
1. Etiqueta: `Label/Field 12.5`, `color/ink/tertiary`.
2. Fila de entrada: auto layout horizontal, espacio 10, alineación al centro, alto mínimo 32, relleno inferior 10; **solo borde inferior** 1.5 px: `color/line/input` (Default) o `color/ink/primary` (Active).
   - Ícono inicial opcional 18 px, trazo `color/ink/on-dark-muted` (#9A9A9A).
   - Texto `Body/Input 16` negro, ancho FILL. Para contraseñas: puntos "••••••••" con espaciado entre letras 20 %.
   - Ícono final opcional 18 px: `Icon/eye` en #8A8A8A, o `Icon/check` trazo 2.2 en `color/semantic/positive`.

Propiedades: `Label`, `Value`, `Show leading icon`, `Leading icon`, `Show trailing icon`, `Trailing icon`.

### 7.11 `Tab bar` — variantes `Active=Inicio | Movimientos | Metas | Ajustes`
Frame 390 × 78, relleno blanco, borde superior 1 px `color/line/tabbar`, auto layout horizontal, relleno 11 arriba, 12 a los lados, 0 abajo, alineación arriba.
Cinco ranuras en este orden:
1. `Inicio` (`Icon/home`) · 2. `Movimientos` (`Icon/list`) · 3. **FAB** · 4. `Metas` (`Icon/target`) · 5. `Ajustes` (`Icon/gear`)
- Pestaña: auto layout vertical, espacio 4, centrada, ancho FILL; ícono 24; etiqueta `Label/Tab 10.5`.
  - Inactiva: ícono y texto `#9A9A9A`.
  - Activa: ícono y texto negros, y debajo un punto negro de 4 × 4.
- Ranura FAB: ancho fijo 62. FAB 52 × 52, radio 10, relleno negro, `Icon/plus` 24 px trazo 2 blanco. Desplazado 18 px hacia arriba (sobresale del borde superior de la barra): usar margen negativo o posición absoluta dentro de la ranura con y = −18.

### 7.12 `Top bar` — variantes `Type=Title | Step`
Frame 390 × 52, auto layout horizontal, `space-between`, alineación al centro, relleno lateral 20.
- **Title:** `Icon button` con `Icon/chevron-left` · texto centrado `Heading/Card 15` ("Informe") · espaciador vacío de 38 × 38.
- **Step:** `Icon button` con `Icon/chevron-left` · texto "Paso 1 de 2" (`Label/Field 12.5`, `color/ink/tertiary`).

### 7.13 `Section header`
Auto layout horizontal, `space-between`, alineación a la línea base, ancho FILL.
- Título `Heading/Section 18`.
- Enlace opcional: "Ver todos" (`Body/Link 13`, negro) + `Icon/chevron-right` 14 px trazo 2.2, espacio 1.

### 7.14 `Card`
Auto layout vertical, relleno blanco, borde 1 px `color/line/card`, radio 10, ancho FILL. El relleno interior depende del uso (se indica en cada pantalla).

### 7.15 `Transaction row`
Auto layout horizontal, espacio 12, relleno vertical 12, alineación al centro, ancho FILL; divisor superior 1 px `color/line/hair` salvo la primera fila de la tarjeta.
- `Category icon` Size=40, Tone=Muted.
- Columna (FILL): título `Body/Row title 15` + subtítulo `Label/Caption 12` en `color/ink/tertiary`, 2 px de separación.
- Monto `Body/Row title 15`, sin salto de línea: `color/semantic/negative` con "−" o `color/semantic/positive` con "+".

### 7.16 `Settings row` — variantes `Trailing=Chevron | Value | Toggle On | Toggle Off | Badge | None`
Auto layout horizontal, espacio 12, relleno vertical 9, alineación al centro, ancho FILL; divisor superior 1 px `color/line/hair` salvo la primera.
- `Category icon` Size=34 (Muted; Danger para "Cerrar sesión").
- Etiqueta `Body/Row title 15`, ancho FILL (roja `color/semantic/negative` en la variante de cerrar sesión).
- Final según variante:
  - Chevron: `Icon/chevron-right` 16 px `color/icon/chevron`.
  - Value: texto `Body/Setting value 14` `color/ink/tertiary` + chevron.
  - Toggle On / Off: componente `Toggle`.
  - Badge: `Badge` + chevron.
  - None: nada.

### 7.17 `Balance card` (tarjeta de saldo)
Auto layout vertical, relleno negro, radio 12, relleno 20 arriba, 20 a los lados, 18 abajo, ancho FILL.
1. Fila superior (`space-between`, centro): "Saldo de septiembre" (`Label/Meta 12.5`, #9A9A9A) · enlace "Informe" (SemiBold 12.5 blanco) + `Icon/chevron-right` 14 px trazo 2.2 blanco, espacio 2.
2. Monto "$1.250.000" (`Display/Hero 48`, blanco), margen superior 14 e inferior 22.
3. Fila dividida: borde superior 1 px blanco al 16 %, relleno superior 14, dos columnas iguales (FILL):
   - Etiqueta `Label/Hero split 11.5` #9A9A9A + valor `Body/Value 16` blanco, 4 px de separación.
   - Columna 1: "Ingresos" / "$2.500.000". Columna 2: "Gastos" / "$1.250.000".

### 7.18 `Bottom sheet`
Frame 390 de ancho, altura según contenido (HUG), relleno blanco, radios superiores 18 e inferiores 0, auto layout vertical, relleno 10 arriba, 20 a los lados, 40 abajo.
1. Asa: 38 × 5, radio 3, `color/sheet/grabber`, centrada, margen inferior 16.
2. Encabezado (`space-between`, centro, margen inferior 8): título `Title/Sheet 24` · botón cerrar: círculo 32 × 32 `color/surface/muted` con `Icon/x` 16 px trazo 2.2 `color/ink/secondary`.
3. Filas de opción: auto layout horizontal, espacio 14, relleno vertical 15, divisor superior `color/line/hair` salvo la primera.
   - `Category icon` Size=46 (Dark en la primera opción, Muted en las demás), ícono 22.
   - Columna: título `Body/Row title 16` + subtítulo `Label/Caption 12`.
   - `Icon/chevron-right` 18 px `color/icon/chevron`.

### 7.19 `Progress track` — variantes `Height=6 | 8`
Pista FILL de ancho con radio 3 (6 px) o 4 (8 px) y relleno `color/track/default`; barra interior negra del mismo radio con ancho = porcentaje. Sobre fondo gris usar pista `color/track/on-bg`.

### 7.20 `Monthly bar chart`
Contenido de la tarjeta "Últimos seis meses" (ver pantalla Informe). Área de 96 px de alto:
- 6 barras de igual ancho, 12 px de separación, esquinas superiores de radio 4, ancladas a la base.
- Alturas de izquierda a derecha: 68, 78, 61, 87, 78, 96. Las 5 primeras `color/chart/bar`; la última negra.
- Línea punteada horizontal a 80 px desde la base, 1.5 px, trazo `color/chart/average`, guion 4 y espacio 4, a todo el ancho.
- Etiqueta directa "$1,25 M" (`Label/Chart 11.5`, negra), alineada a la derecha, 4 px sobre la última barra.
- Eje X debajo (margen 8, separación 12): "abr", "may", "jun", "jul", "ago", "sep"; `Label/Chart axis 11` en `color/ink/tertiary`; "sep" en Bold negro.

### 7.21 `Category row` (Informe)
Auto layout horizontal, espacio 12, relleno vertical 10, alineación al centro; divisor superior salvo la primera.
- `Category icon` Size=36.
- Columna FILL: fila `space-between` con nombre y monto (`Body/Category 14`), margen inferior 7; debajo `Progress track` Height=6.
- Porcentaje: ancho fijo 34, alineado a la derecha, `Label/Percent 12` `color/ink/tertiary`.

### 7.22 `Goal card`
`Card` con relleno 16.
1. Fila (espacio 12, centro, margen inferior 16): `Category icon` Size=40 · columna FILL con título `Body/Row title 15` y subtítulo `Label/Caption 12` · porcentaje `Heading/Section 18`.
2. `Progress track` Height=8.
3. Pie (`space-between`, margen superior 11, 13 px): a la izquierda monto en Bold negro + " de $X" en `color/ink/tertiary`; a la derecha texto en `color/ink/tertiary`.

### 7.23 `Password strength`
Auto layout horizontal, espacio 10, centro. Tres segmentos FILL de 4 px de alto, radio 2, separados 4 (dos en `color/ink/secondary` #4D4D4D, uno en `color/track/strength`) + texto "Contraseña segura" (`Label/Percent 12`, `color/ink/secondary`).

### 7.24 `Step indicator`
Auto layout horizontal, espacio 6, ancho FILL. Dos segmentos FILL de 3 px de alto, radio 2: el primero negro, el segundo `color/track/step`.

---

## 8. Pantallas

Todas en la página `Pantallas`: frame 390 × 844, relleno `color/bg/app`, auto layout vertical, espacio 0, sin relleno interior salvo que se indique. Orden de hijos de arriba abajo. "px 20" significa relleno izquierdo y derecho de 20.

### 8.1 `01 Login`
1. `Status bar`.
2. **Cuerpo** — auto layout vertical, `space-between`, alto FILL, px 20, relleno superior 22 e inferior 34, sin relleno de color.
   - **Bloque superior** (vertical):
     - Fila logo: `Brand/Logo mark` 34 + "Chavos" (`Title/Wordmark 20`), espacio 10.
     - "Tus finanzas,⏎en calma." (`Display/Login 46`), margen superior 64. El salto de línea va después de la coma.
     - "Registra lo que gastas, ahorra para tus metas y entiende tu mes de un vistazo." (`Body/Lead 15`, `color/ink/secondary`), ancho máximo 290, margen superior 16.
   - **Bloque inferior** (vertical, espacio 22):
     - `Text field` Default: etiqueta "Correo electrónico", `Icon/mail` inicial, valor "juanm@upb.edu.co".
     - `Text field` Active: etiqueta "Contraseña", valor "••••••••", `Icon/eye` final.
     - Fila `space-between`, centro: [`Checkbox` + "Recordarme" (13.5 Medium, `color/ink/secondary`), espacio 9] · "¿Olvidaste tu contraseña?" (`Body/Link 13.5`, negro, subrayado).
     - Columna de botones (espacio 10, margen superior 4): `Button` Primary "Iniciar sesión" · `Button` Secondary con `Icon/fingerprint` "Entrar con huella".
     - "¿Aún no tienes cuenta? **Crear cuenta**" centrado (`Body/Footer 14`, `color/ink/secondary`; "Crear cuenta" en Bold negro).
3. `Home indicator` (absoluto).

### 8.2 `02 Registro`
1. `Status bar`.
2. `Top bar` Type=Step.
3. **Cuerpo** — vertical, `space-between`, alto FILL, px 20, relleno superior 14 e inferior 34.
   - **Bloque superior**:
     - `Step indicator`.
     - "Crea tu cuenta" (`Title/Page 34`), margen superior 26.
     - "Empieza a ordenar tu dinero hoy mismo." (`Body/Lead 15`, `color/ink/secondary`), margen superior 10.
     - Columna de campos, espacio 20, margen superior 30:
       - `Text field` "Nombre completo", `Icon/user`, "Juan Manuel Arias".
       - `Text field` "Correo electrónico", `Icon/mail`, "juanm@upb.edu.co".
       - `Text field` Active "Contraseña", "••••••••••", `Icon/eye`; debajo, a 14 px, `Password strength`.
       - `Text field` "Confirmar contraseña", "••••••••••", `Icon/check` verde.
   - **Bloque inferior** (espacio 18):
     - Fila (espacio 10, alineación arriba): `Checkbox` + "Acepto los **términos de uso** y el tratamiento de mis datos." (`Body/Small 13.5`, `color/ink/secondary`; "términos de uso" en Bold negro subrayado).
     - `Button` Primary "Continuar".
     - "¿Ya tienes cuenta? **Inicia sesión**" centrado, mismo estilo que el pie de Login.
4. `Home indicator`.

### 8.3 `03 Inicio`
1. `Status bar`.
2. **Encabezado** — horizontal, `space-between`, alineación abajo, px 20, relleno superior 6 e inferior 20:
   - Columna: "Jueves 24 de septiembre" (`Label/Meta 12.5`, `color/ink/tertiary`) + "Hola, Juan" (`Title/Greeting 26`), 3 px de separación.
   - `Avatar` Size=38.
3. **Cuerpo** — vertical, espacio 30, px 20, alto FILL:
   - `Balance card`.
   - Bloque Recientes (vertical, espacio 12):
     - `Section header` "Recientes" con enlace "Ver todos".
     - `Card` con relleno 2 arriba y abajo, 16 a los lados, con 5 `Transaction row`:

       | Ícono | Título | Subtítulo | Monto | Color |
       |---|---|---|---|---|
       | cart | Mercado del mes | Alimentación, hoy | −$120.000 | negativo |
       | bus | Metro y buses | Transporte, hoy | −$35.000 | negativo |
       | wallet | Salario | Ingresos, ayer | +$2.500.000 | positivo |
       | play | Netflix | Suscripciones, 21 sep | −$35.000 | negativo |
       | heart | Farmacia | Salud, 20 sep | −$60.000 | negativo |
4. `Tab bar` Active=Inicio.
5. `Home indicator`.

### 8.4 `04 Registrar`
1. Instancia completa de la pantalla Inicio (o duplicado de `03 Inicio`) como fondo.
2. Velo: rectángulo 390 × 844 en (0, 0), `color/overlay/scrim` (negro al 45 %), por encima de todo lo anterior.
3. `Bottom sheet` anclado abajo (y = 844 − alto de la hoja), título "Registrar", con 3 opciones:

   | Ícono | Tono | Título | Subtítulo |
   |---|---|---|---|
   | arrow-up-right | Dark | Gasto | Compras, pagos y suscripciones |
   | arrow-down-left | Muted | Ingreso | Salario, ventas o reembolsos |
   | target | Muted | Aporte a una meta | Suma dinero a tus ahorros |
4. `Home indicator` por encima de la hoja.

En el prototipo: el FAB de la barra de pestañas abre esta hoja con animación *Move in* desde abajo, curva *Ease out*, 300 ms, y superpone el velo. El botón cerrar y un toque en el velo la cierran con *Move out* hacia abajo.

### 8.5 `05 Informe`
1. `Status bar`.
2. `Top bar` Type=Title, texto "Informe".
3. **Cuerpo** — vertical, espacio 18, px 20, relleno superior 6:
   - Selector de mes: fila `space-between`, centro: `Icon/chevron-left` 16 (`color/ink/tertiary`) · "Septiembre 2026" (`Body/Month 14`) · `Icon/chevron-right` 16.
   - Resumen (vertical): "Gastaste" (`Label/Meta 12.5`, `color/ink/tertiary`) · "$1.250.000" (`Display/Number 40`), margen superior 6 · "14 % por encima de tu promedio de seis meses" (`Body/Note 13`, `color/ink/secondary`), margen superior 9.
   - `Card` con relleno 16 arriba, 16 a los lados, 14 abajo:
     - Fila `space-between`, línea base, margen inferior 22: "Últimos seis meses" (`Heading/Card 15`) · "Promedio $1,09 M" (11.5 Medium, `color/ink/tertiary`).
     - `Monthly bar chart`.
   - Bloque por categoría (vertical, espacio 10):
     - "Por categoría" (`Heading/Sub 16`).
     - `Card` con relleno 2 arriba y abajo, 16 a los lados, con 6 `Category row`:

       | Ícono | Nombre | Monto | Barra | % |
       |---|---|---|---|---|
       | cart | Alimentación | $425.000 | 100 % | 34 % |
       | bus | Transporte | $262.500 | 61.8 % | 21 % |
       | target | Aportes a metas | $200.000 | 47.1 % | 16 % |
       | play | Ocio y suscripciones | $175.000 | 41.2 % | 14 % |
       | heart | Salud | $125.000 | 29.4 % | 10 % |
       | more | Otros | $62.500 | 14.7 % | 5 % |

       La columna "Barra" es el ancho de la barra negra relativo a la categoría más grande; la columna "%" es la parte del gasto total que se muestra como texto.
4. `Home indicator`. Esta pantalla no lleva barra de pestañas.

### 8.6 `06 Metas`
1. `Status bar`.
2. **Encabezado** — horizontal, `space-between`, alineación abajo, px 20, relleno superior 6 e inferior 22: "Metas" (`Title/Large 30`) · `Icon button` con `Icon/plus` (trazo 2).
3. **Cuerpo** — vertical, espacio 14, px 20:
   - Resumen (vertical, relleno inferior 10): "Llevas ahorrado" (`Label/Meta 12.5`, `color/ink/tertiary`) · "$5.050.000" (`Display/Number 40`), margen superior 6 · "de $9.700.000 entre tres metas" (`Body/Note 13`, `color/ink/secondary`), margen superior 9 e inferior 14 · `Progress track` Height=8 con pista `color/track/on-bg` al 52 %.
   - 3 `Goal card`:

     | Ícono | Título | Subtítulo | % | Barra | Ahorrado | Objetivo | Derecha |
     |---|---|---|---|---|---|---|---|
     | send | Viaje a Cartagena | Para diciembre de 2026 | 62 % | 62 % | $3.100.000 | de $5.000.000 | $475.000 al mes |
     | shield | Fondo de emergencia | Sin fecha límite | 50 % | 50 % | $750.000 | de $1.500.000 | Aporte libre |
     | laptop | Portátil nuevo | Para marzo de 2027 | 38 % | 37.5 % | $1.200.000 | de $3.200.000 | $333.000 al mes |
4. `Tab bar` Active=Metas.
5. `Home indicator`.

### 8.7 `07 Ajustes`
1. `Status bar`.
2. Título "Ajustes" (`Title/Large 30`), px 20, relleno superior 6 e inferior 18.
3. **Cuerpo** — vertical, espacio 16, px 20:
   - Tarjeta de perfil: `Card` horizontal, relleno 14 arriba y abajo, 16 a los lados, espacio 13, centro: `Avatar` Size=46 · columna FILL con "Juan Manuel Arias" (`Body/Row title 16`) y "juanm@upb.edu.co" (`Label/Caption 12`, `color/ink/tertiary`) · `Icon/chevron-right` 16 `color/icon/chevron`.
   - Grupo "Preferencias": etiqueta (`Label/Field 12.5`, `color/ink/tertiary`, margen izquierdo 2, inferior 8) + `Card` con relleno lateral 16 y 0 vertical:
     | Ícono | Etiqueta | Final |
     |---|---|---|
     | bell | Notificaciones | Badge "2" + chevron |
     | coin | Moneda | Value "COP $" |
     | moon | Tema oscuro | Toggle Off |
     | calendar | Recordatorio diario | Value "20:00" |
   - Grupo "Seguridad y datos":
     | Ícono | Etiqueta | Final |
     |---|---|---|
     | fingerprint | Desbloqueo con huella | Toggle On |
     | export | Exportar movimientos | Value "CSV" |
   - Tarjeta sin etiqueta:
     | Ícono | Etiqueta | Final |
     |---|---|---|
     | info | Créditos | Chevron |
     | logout (tono Danger) | Cerrar sesión (texto rojo) | None |
4. `Tab bar` Active=Ajustes.
5. `Home indicator`.

### 8.8 `08 Movimientos` (listado)
1. `Status bar`.
2. Título "Movimientos" (`Title/Page 34`), px 20, relleno superior 6 e inferior 18.
3. **Cuerpo** — vertical, espacio 16, px 20:
   - Selector de mes igual al de `05 Informe`, con "Septiembre 2026".
   - `Card` de resumen, horizontal, relleno 16, tres columnas FILL iguales: etiqueta (`Label/Meta 12.5`, `color/ink/tertiary`) y valor (`Body/Value 16`) debajo con 4 px de separación: "Ingresos" $2.500.000 · "Gastos" $1.250.000 · "Balance" $1.250.000.
   - Fila de filtros (espacio 8): chips "Todos" (relleno negro, texto blanco), "Ingresos" y "Gastos" (relleno `color/bg/surface`, borde `color/line/card`, texto negro). Chip: alto 36, radio 10, px 16, texto 14 SemiBold.
   - Grupos por día (vertical, espacio 8 dentro del grupo y 18 entre grupos). Cada grupo: fila `space-between` con el día (`Label/Field 13`, `color/ink/secondary`) y el total del día (12.5 Medium, `color/ink/tertiary`) · `Card` con relleno 2 arriba y abajo, 16 a los lados, con sus `Transaction row`, subtítulo "Categoría, hora":

     | Día | Total | Filas |
     |---|---|---|
     | Hoy | −$155.000 | cart · Mercado del mes · Alimentación, 10:24 · −$120.000 / bus · Metro y buses · Transporte, 08:10 · −$35.000 |
     | Ayer | +$2.500.000 | wallet · Salario · Salario, 09:00 · +$2.500.000 |
     | Lunes 21 de septiembre | −$35.000 | play · Netflix · Ocio y suscripciones, 20:15 · −$35.000 |
4. `Tab bar` Active=Movimientos.
5. `Home indicator`.

### 8.9 `09 Detalle`
1. `Status bar`.
2. `Top bar` Type=Title, texto "Detalle".
3. **Cuerpo** — vertical, alto FILL, px 20, relleno superior 14:
   - Columna centrada: `Category icon` Size=46 Tone=Muted con `Icon/heart` · "−$60.000" (`Display/Number 40`, `color/negative`), margen superior 18 · "Farmacia" (`Body/Row title 16`), margen superior 10 · "Domingo 20 de septiembre, 10:24" (`Label/Meta 12.5`, `color/ink/tertiary`), margen superior 4.
   - `Card` con relleno lateral 16, margen superior 26, con tres filas de dato (alto 48, `space-between`, etiqueta `Label/Field 13` `color/ink/tertiary` a la izquierda y valor `Body/Value 15` a la derecha, separador entre filas): "Tipo" Gasto · "Categoría" Salud · "Fecha" 20 de septiembre de 2026. Si el movimiento es un aporte, se agrega la fila "Meta" con el nombre de la meta.
   - Abajo del todo (relleno inferior 34), fila de dos botones FILL con espacio 12: `Button` Secondary con `Icon/pencil` "Editar" · `Button` Secondary con `Icon/trash` "Eliminar" (texto e ícono `color/negative`).
4. `Home indicator`. Sin barra de pestañas.

### 8.10 `10 Formulario`
1. `Status bar`.
2. `Top bar` Type=Title con `Icon/x` en lugar de la flecha, texto "Nuevo gasto".
3. **Cuerpo** — vertical, alto FILL, px 20:
   - Control segmentado: `Card` de alto 48, relleno 4, dos opciones FILL: "Gasto" (relleno negro, texto blanco, radio 10) e "Ingreso" (texto negro, sin relleno).
   - "Monto" (`Label/Meta 12.5`, `color/ink/tertiary`), centrado, margen superior 24 · "$28.500" (`Display/Amount 44`; el "$" en 22 Bold `color/ink/tertiary`), centrado · "Pesos colombianos" (`Label/Caption 12`, `color/ink/tertiary`), centrado.
   - "Categoría" (`Label/Field 12.5`, `color/ink/tertiary`), margen superior 28 · cuadrícula de 4 columnas y 2 filas, espacio vertical 14: cada celda es un cuadro de 50 × 50, radio 12, con el ícono, y el nombre debajo (`Label/Category 12`). La seleccionada va con cuadro negro, ícono blanco y texto negro; las demás con cuadro `color/bg/muted` y texto `color/ink/tertiary`. Orden: Alimentos (cart, seleccionada), Transporte (bus), Ocio (play), Salud (heart), Hogar (home), Estudio (book), Metas (target), Otros (more).
   - `Card` con una `Settings row` `calendar` "Fecha" con Value "24 sep 2026" y chevron, margen superior 24.
   - `Text field` "Nota" con el valor "Taxi al aeropuerto", margen superior 22.
   - `Button` Primary "Guardar gasto" anclado abajo, relleno inferior 34.
4. `Home indicator`. Sin barra de pestañas.

Variantes de la misma pantalla: con "Ingreso" seleccionado el título es "Nuevo ingreso" y las categorías son Salario, Ventas, Regalos y Otros; desde "Aporte a una meta" se oculta el segmentado y la cuadrícula, y aparece "Meta" con chips de las metas.

### 8.11 `11 Créditos`
1. `Status bar`.
2. `Top bar` Type=Title, texto "Créditos".
3. **Cuerpo** — vertical, espacio 24, px 20, relleno superior 10:
   - Fila (espacio 16, centro): `Avatar` de 64 × 64 con "JM" · columna con "Juan Manuel Arias" (`Title/Greeting 26`) e "Ingeniería de Sistemas" (`Body/Lead 15`, `color/ink/secondary`).
   - `Card` con relleno lateral 16 y cuatro filas (alto 48, separador entre filas): ícono 16 `color/ink/tertiary` + etiqueta (`Label/Field 13`, `color/ink/tertiary`) a la izquierda, valor (`Body/Value 15`) a la derecha: book "Universidad" Universidad Pontificia Bolivariana · laptop "Curso" Aplicaciones Móviles · calendar "Periodo" 2026-2 · target "Entrega" 3 · App nativa.
   - "Construido con" (`Label/Field 12.5`, `color/ink/tertiary`) y debajo chips en flujo (espacio 8): Kotlin, Jetpack Compose, Room, DataStore, Material 3, Android Studio.
   - Pie centrado: `Brand/Logo mark` 22 + "Chavos" (`Title/Wordmark 20`) · "Versión 1.0.0" y "Hecho con Kotlin en Medellín, Colombia" (`Label/Caption 12`, `color/ink/tertiary`).
4. `Home indicator`. Sin barra de pestañas.

---

## 9. Detalles que se pierden con facilidad

- La etiqueta "Informe" con chevron dentro de la tarjeta de saldo indica que la tarjeta se puede tocar: lleva a `05 Informe`.
- Las notificaciones **no** tienen campana en el inicio: viven en Ajustes con su distintivo.
- En Registro el botón dice **Continuar**, no "Crear cuenta", porque es el paso 1 de 2.
- El único color de Registro es el check verde de "Confirmar contraseña".
- El medidor de contraseña usa gris oscuro (#4D4D4D), no negro, para no confundirse con el subrayado activo del campo.
- La primera opción de la hoja Registrar (Gasto) va con ícono negro porque es la acción más frecuente.
- Montos alineados a la derecha, sin cortes de línea.
- Separadores entre filas solo dentro de las tarjetas, nunca sobre el fondo gris.

---

## 10. Verificación final

Tomar una captura de la página `Pantallas` y comparar con `diseno/png/monocromo/*.png`. Comprobar:
- [ ] Ningún contenedor de agrupación tiene relleno blanco no intencional.
- [ ] Todos los textos usan Schibsted Grotesk con el estilo de texto asignado (ningún texto en Inter o Roboto).
- [ ] Los montos no tienen espacios alrededor de los puntos.
- [ ] Los gastos usan "−" (U+2212) y el color negativo; los ingresos "+" y el color positivo.
- [ ] Ningún texto se corta ni se sale de su tarjeta; las filas de lista terminan antes de la barra de pestañas.
- [ ] El FAB sobresale 18 px por encima de la barra de pestañas.
- [ ] Las pestañas activas coinciden con la pantalla (Inicio, Metas, Ajustes).
- [ ] Todos los colores vienen de variables y todos los textos de estilos de texto (sin valores sueltos).
- [ ] Las tildes y los signos `¿` están presentes.

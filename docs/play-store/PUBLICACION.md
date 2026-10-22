# Publicación en Google Play

Todo lo necesario para subir Chavos a Google Play Console. Los textos se pueden copiar tal cual.

## 1. Archivo para subir

```bash
./gradlew :app:bundleRelease
```

Genera `app/build/outputs/bundle/release/app-release.aab` (unos 5,6 MB), firmado con la llave de subida.

La llave **no está en el repositorio**: está en `firma/chavos-upload.jks` y su contraseña en `keystore.properties`,
ambos en la carpeta del proyecto del computador donde se creó. Guarda una copia de los dos archivos:
sin ellos no se pueden publicar actualizaciones con la misma llave (Google permite pedir un cambio de llave de subida, pero tarda).
Si el archivo `keystore.properties` no existe, el proyecto compila igual, pero el AAB de release sale sin firmar.

Antes de cada actualización hay que subir `versionCode` y `versionName` en `app/build.gradle.kts`.

## 2. Ficha de Play Store

| Campo | Valor |
|---|---|
| Nombre de la app | Chavos: finanzas personales |
| Idioma predeterminado | Español (Latinoamérica) – es-419 |
| Tipo | App |
| Gratis o de pago | Gratis |
| Categoría | Finanzas |
| Etiquetas | Presupuesto, Finanzas personales, Ahorro |
| Sitio web | https://github.com/juanmaa28/chavos |
| Política de privacidad | https://github.com/juanmaa28/chavos/blob/main/PRIVACIDAD.md |

**Descripción breve** (máximo 80 caracteres):

> Registra gastos e ingresos, ahorra para tus metas y entiende tu mes.

**Descripción completa:**

> Chavos te ayuda a ordenar tu dinero sin complicaciones.
>
> Registra en segundos cada gasto e ingreso, con su categoría, fecha y una nota. En la pantalla de inicio ves tu saldo del mes y tus últimos movimientos, y en el listado los encuentras agrupados por día, con filtros por tipo.
>
> Crea metas de ahorro, como un viaje, un portátil o un fondo de emergencia. Chavos te dice cuánto llevas, qué porcentaje te falta y cuánto separar cada mes para llegar a tiempo.
>
> El informe mensual te muestra cuánto gastaste, cómo vas frente a tu promedio de los últimos seis meses y en qué categorías se va tu dinero.
>
> Privada por diseño: tus datos se guardan solo en tu teléfono. Chavos no usa internet, no tiene publicidad y no comparte tu información. Puedes proteger la app con tu huella y exportar tus movimientos a un archivo CSV cuando quieras.
>
> También incluye tema oscuro y montos en pesos colombianos.
>
> Chavos es un proyecto académico del curso Aplicaciones Móviles de la Universidad Pontificia Bolivariana.

## 3. Recursos gráficos

| Recurso | Archivo |
|---|---|
| Ícono (512 × 512) | `docs/play-store/icono-512.png` |
| Gráfico de funciones (1024 × 500) | `docs/play-store/grafico-funciones-1024x500.png` |
| Capturas de teléfono (8, de 1200 × 2400) | `docs/play-store/capturas/` |

## 4. Contenido de la app (respuestas para Play Console)

- **Acceso a la app:** algunas funciones requieren iniciar sesión. Dar la cuenta de demostración que está en el README; la cuenta se crea sola al instalar la app.
- **Anuncios:** la app no contiene anuncios.
- **Clasificación de contenido:** categoría "Utilidad, productividad, comunicación u otra"; responder "No" a todo (no hay violencia, contenido sexual, apuestas, compras ni interacción entre usuarios).
- **Público objetivo:** 18 años o más.
- **Seguridad de los datos:** "¿La app recopila o comparte datos del usuario?" → **No**. Los datos se guardan solo en el dispositivo y la app no tiene permiso de internet, así que no se transmiten fuera del teléfono.
- **App de noticias / de salud / gubernamental / préstamos:** No.
- **Funciones financieras:** la app no ofrece préstamos, pagos, inversiones ni criptoactivos; solo lleva un registro personal de gastos.

## 5. Pasos en Play Console

1. Crear la app con el nombre y el idioma de la tabla anterior.
2. Completar "Contenido de la app" con las respuestas de la sección 4.
3. Completar la ficha de Play Store con los textos y gráficos.
4. En **Prueba interna** (o la pista que indique el profesor), crear una versión, aceptar la firma de apps de Google Play y subir `app-release.aab`.
5. Revisar y enviar a revisión.

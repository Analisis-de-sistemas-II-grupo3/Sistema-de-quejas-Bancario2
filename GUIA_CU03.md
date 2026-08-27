# Guía rápida de CU03: Adjuntar evidencia

```text
Pantalla HTML → ControladorEvidencias → ServicioEvidencias → AlmacenAdjuntos → PostgreSQL/Neon
```

## Clases principales

- `ControladorEvidencias`: recibe el botón **Adjuntar archivos** y muestra la pantalla.
- `ServicioEvidencias`: contiene las reglas: permisos, formato y tamaño.
- `AlmacenAdjuntos`: valida y guarda una copia local temporal dentro de `uploads/`.
- `EvidenciaCaso`: guarda los datos de un archivo que ya fue cargado.
- `RepositorioCasosPostgres`: guarda el contenido del archivo, sus datos y la bitácora en PostgreSQL/Neon.
- `RegistradorCasos`: conecta la lógica sencilla del caso con el repositorio de datos.

## Reglas implementadas

1. Debe existir un caso con el folio indicado.
2. Un Cliente solo puede adjuntar evidencia a su propio caso.
3. Un Agente de Atención puede adjuntar evidencia a un caso.
4. Se pueden seleccionar varios archivos a la vez.
5. Se aceptan PDF, JPG, JPEG y PNG.
6. El límite es 2 MB para Cliente y 10 MB para Agente de Atención.
7. Si un archivo no es válido, ninguno se guarda.
8. La evidencia, su contenido y su auditoría quedan almacenados en la tabla `documento_adjunto` y `bitacora_caso`.
9. La descarga verifica de nuevo que el usuario tenga permiso sobre el caso.

## Cómo probarlo

1. Ejecuta el proyecto en NetBeans.
2. Inicia sesión como `ana.lopez`.
3. Registra un caso.
4. En la pantalla de éxito usa **Adjuntar más evidencia**.
5. Elige uno o varios archivos válidos, dentro del límite de 2 MB.
6. Al enviarlos deben aparecer en la lista de adjuntos y el botón **Descargar** debe recuperar el mismo archivo.

> Para conectar Neon en una nueva laptop, copia `src/main/resources/application-local.properties.example` como `application-local.properties` y coloca allí la contraseña. Ese archivo real no se sube a GitHub.

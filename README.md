# Sistema de Quejas Bancario — CU00, CU01, CU02 y CU03

Aplicación web con Java, Spring Boot y Thymeleaf. Implementa CU00 V1.0, CU01 V1.3, CU02 V1.4 y CU03 V1.2.

## CU03 - Adjuntar Evidencia al Caso

- Permite adjuntar uno o varios archivos desde un caso recién registrado.
- Solo el Cliente dueño del caso o un Agente de Atención pueden cargar evidencia.
- Formatos: PDF, JPG, JPEG y PNG.
- Tamaño máximo por archivo: 2 MB para Cliente y 10 MB para Agente de Atención.
- Guarda en PostgreSQL/Neon el archivo, sus metadatos y el evento de carga en bitácora.
- Permite descargar cada evidencia con una comprobación de permisos.
- La explicación sencilla de las clases está en `GUIA_CU03.md`.

## CU00 - Portal / Página de Inicio

- La ruta pública `/` es el punto de entrada del sistema y muestra la identidad institucional, explicación del servicio y los cuatro tipos de caso.
- Ofrece accesos a `/login` y a `/consulta-estado`; este último queda preparado para el futuro CU de consulta pública por folio.
- La identidad institucional cumple RN22: nombre, siglas y colores se cambian en `src/main/resources/application.properties`, mediante las propiedades `institucion.*`, sin editar Java.

## CU01 - Reglas implementadas

- Acceso multiplataforma vía navegador (sin app nativa), con usuario y contraseña.
- 6 actores: Cliente/Denunciante, Agente de Atención, Supervisor, Administrador, Auditor (RN01), más el propio Sistema.
- Contraseñas almacenadas cifradas (hash BCrypt), nunca en texto plano.
- FA01: credenciales inválidas muestra un mensaje genérico y regresa al formulario.
- Redirección a la bandeja principal según el rol (flujo básico, paso 7):
  - Cliente/Denunciante → `/casos/nuevo` (Mis Casos, ya implementado por CU02).
  - Agente de Atención → `/bandeja/agente` (placeholder, pendiente de su CU).
  - Supervisor → `/bandeja/supervisor` (placeholder).
  - Administrador → `/admin` (placeholder).
  - Auditor → `/auditoria` (placeholder).
- FA02: cierre de sesión manual vía `/logout`, con bitácora simulada.
- FA03: cierre de sesión automático por inactividad, configurable en `application.properties` (`server.servlet.session.timeout`), registrado en bitácora al expirar.
- `ProveedorSesionActual` (puerto que CU02 esperaba) ahora lo implementa `ProveedorSesionActualHttp`, leyendo la sesión HTTP real; el stub anterior fue eliminado.
- Las rutas `/casos/**`, `/bandeja/**`, `/admin/**` y `/auditoria/**` quedan protegidas por sesión activa. Ya **no** se puede entrar directo a `/casos/nuevo` sin iniciar sesión primero.

### Usuarios de prueba (solo desarrollo local, ver `ConfiguracionUsuarios`)

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| ana.lopez | Cliente#2026 | Cliente |
|

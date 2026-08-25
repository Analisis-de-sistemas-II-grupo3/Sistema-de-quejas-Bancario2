# Sistema de Quejas Bancario — CU00, CU01 y CU02

Aplicación web local con Java, Spring Boot y Thymeleaf. Implementa CU00 V1.0, CU01 V1.3 y CU02 V1.4; los datos viven temporalmente en memoria.

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
| carlos.perez | Agente#2026 | Agente de Atención |
| sofia.ruiz | Supervisor#2026 | Supervisor |
| admin | Admin#2026 | Administrador |
| auditor1 | Auditor#2026 | Auditor |

## CU02 - Reglas implementadas

- Solo un usuario autenticado con rol `Cliente` puede registrar casos.
- El Cliente debe tener al menos una cuenta activa.
- No existe registro anónimo, incluso para una Denuncia.
- Tipos disponibles: Queja, Reclamo, Denuncia y Sugerencia.
- Producto o servicio seleccionado desde un catálogo bancario cerrado.
- Campos obligatorios: tipo, producto/servicio, sucursal/canal, fecha del hecho y descripción (máximo 2,000 caracteres).
- Evidencia opcional: PDF/JPG/JPEG/PNG, máximo 2 MB; se guarda localmente en `uploads/`.
- Folio anual por tipo, por ejemplo `Q-00001-2026`.
- Estado inicial `RECIBIDO`; si hay Agente de Atención disponible se asigna aleatoriamente y pasa a `ASIGNADO`.
- Bitácora y notificaciones simuladas en memoria para validar el flujo de creación y asignación.

## Dependencias pendientes de otros casos de uso

- **CU00 Portal:** navegación desde la página de inicio.
- **CU Asignar Caso a Responsable:** reemplazará el asignador local por el proceso definitivo.
- **CU Registrar Bitácora** y **CU Enviar Notificaciones:** reemplazarán los registros/notificaciones simulados de CU01 y CU02.
- **CU Bandeja de Agente / Supervisor / Panel Administrativo / Panel de Auditoría:** reemplazarán los placeholders creados por CU01.
- **Base de datos:** sustituirá usuarios, catálogo, clientes, cuentas, agentes, casos y folios en memoria.

Se puede iniciar desde `http://localhost:8080/` o ingresar directamente a `http://localhost:8080/login`.

## Abrir y ejecutar en NetBeans

1. Abre NetBeans y selecciona **File > Open Project**.
2. Elige la carpeta `Sistema-de-quejas-Bancario2` que contiene `pom.xml`.
3. Configura JDK 17 o superior si NetBeans lo solicita.
4. Clic derecho en el proyecto → **Run**.
5. Abre [http://localhost:8080/](http://localhost:8080/) e ingresa con alguno de los usuarios de prueba.

## Pruebas locales

Ejecuta `PruebasCU01.java` y `PruebasCU02.java` dentro de **Source Packages**. Deben mostrar `Pruebas CU01 V1.3 aprobadas.` y `Pruebas CU02 V1.4 aprobadas.`

Nota: este código no se compiló con Maven en su generación (sin red/Maven disponibles); se revisó manualmente. Ejecuta `mvn compile` o **Run** en NetBeans antes de darlo por bueno.

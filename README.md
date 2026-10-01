# Sistema de Quejas Bancario

Proyecto de **Análisis** (CU-01 a CU-17), Reglas de Negocio y
el modelo de Base de Datos ya definidos.

- **Backend:** Java 17 + Spring Boot 3 + Spring Security (JWT) + JPA/Hibernate + PostgreSQL (Neon)
- **Frontend:** HTML + JavaScript (vanilla) + Bootstrap 5, un panel por rol
- **Base de datos:** PostgreSQL en Neon 

---

## 1. Estructura del proyecto

```
SistemaQuejasBancario/
├── backend/                   Proyecto Maven Spring Boot
│   ├── database/
│   │   ├── extensions.sql     <- EJECUTA ESTO contra tu base en Neon
│   │   └── postgres_schema.sql (script completo de referencia, opcional)
│   ├── src/main/java/com/umg/quejasbancario/
│   │   ├── entity/            Entidades JPA (14 tablas + extensiones)
│   │   ├── repository/        Repositorios Spring Data JPA
│   │   ├── service/           Lógica de negocio (1 servicio por CU aprox.)
│   │   ├── controller/        Endpoints REST
│   │   ├── security/          JWT, filtros, UserDetails
│   │   ├── config/            Seguridad, CORS, propiedades
│   │   ├── exception/         Manejo centralizado de errores
│   │   └── scheduler/         Reintento de asignación automática (RF17)
│   └── pom.xml
└── frontend/                  Sitio estático (HTML/JS/Bootstrap)
    ├── index.html             Login
    ├── cliente/                Panel del Cliente
    ├── agente/                 Panel del Agente de Atención
    ├── supervisor/              Panel del Supervisor
    ├── administrador/          Panel del Administrador
    └── auditor/                Panel del Auditor
```

---

## 2. Importante: cambios necesarios en tu base de datos de Neon

Tu base de datos ya fue creada siguiendo el documento **"09_Base_de_Datos"**
(14 tablas). Sin embargo, al implementar los Casos de Uso a detalle,
aparecieron algunos datos que los Casos de Uso piden capturar/guardar pero
que la tabla original no contemplaba con un campo dedicado. En vez de
inventar información, se documentan aquí como **extensiones explícitas**:

| Tabla | Columna agregada | Motivo |
|---|---|---|
| `usuario` | `sesion_activa BOOLEAN` | RN06: un Agente solo recibe casos si "su estado de sesión se encuentra marcado como Activo" |
| `usuario` | `bloqueado_hasta TIMESTAMP` | Bloqueo temporal tras intentos fallidos de inicio de sesión (CU-01, FA02) |
| `caso` | `nombre_cliente_caso`, `identificacion_cliente`, `correo_contacto`, `telefono_contacto` | Datos de contacto capturados al registrar el caso (CU-02 / RN04) |
| `caso` | `detalle_resolucion TEXT` | El Agente debe registrar el detalle de la solución (CU-08) |
| `caso` | `solicitudes_reasignacion_usadas INT` | Control de RN07 (máximo 2 solicitudes de reasignación por caso) |
| `solicitud_reasignacion` | `motivo_rechazo TEXT` | El Supervisor ingresa un motivo al rechazar (CU-06, flujo alterno) |
| — | Nueva tabla `password_reset_token` | Requerida para "Recuperar Contraseña" (CU-01, numeral 2.3.3) |

**Para aplicar estos cambios**, abre el **SQL Editor de Neon** (o conéctate
con `psql`) y ejecuta el archivo:

```
backend/database/extensions.sql
```

Es seguro volver a ejecutarlo las veces que quieras (usa `IF NOT EXISTS` /
`ON CONFLICT`). También incluye, comentados, los triggers de inmutabilidad
de bitácora en PL/pgSQL (equivalentes a los que ya definiste en MySQL) y
los `INSERT` de catálogo (roles, tipos de caso, parámetro de límite de
casos por agente) por si tu base aún no los tiene cargados.

Si en algún momento quieres crear la base **desde cero** en un proyecto
nuevo de Neon, usa `backend/database/postgres_schema.sql`, que es el
script íntegro ya adaptado a sintaxis PostgreSQL.

---

## 3. Configurar y ejecutar el backend

### 3.1 Requisitos
- JDK 17+
- Maven 3.9+ (o usa tu IDE, IntelliJ/Eclipse/VS Code con soporte Maven)
- Tu connection string de Neon

### 3.2 Configuración

Edita `backend/src/main/resources/application.yml`, o mejor, define
variables de entorno (más seguro, no quedan credenciales en el código):

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_URL` | Cadena de conexión JDBC de Neon | `jdbc:postgresql://ep-xxxx.neon.tech/quejas_bancario?sslmode=require` |
| `DB_USERNAME` | Usuario de Neon | `neondb_owner` |
| `DB_PASSWORD` | Contraseña de Neon | `********` |
| `JWT_SECRET` | Cadena aleatoria larga (≥32 caracteres) para firmar los tokens | `genera-una-clave-larga-y-aleatoria` |
| `MAIL_HOST` / `MAIL_PORT` | Servidor SMTP | `smtp.gmail.com` / `587` |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Credenciales SMTP (con Gmail, usa una **contraseña de aplicación**, no la contraseña normal de la cuenta) | |
| `MAIL_FROM` | Correo remitente | `no-responder@quejasbancario.com` |
| `CORS_ORIGINS` | Orígenes permitidos para el frontend | `http://127.0.0.1:5500` |
| `FRONTEND_RESET_URL` | URL del `reset-password.html` de tu frontend | `http://127.0.0.1:5500/reset-password.html` |

Puedes definirlas como variables de entorno del sistema operativo, o
crear un archivo `application-local.yml` (ya excluido en `.gitignore`) y
ejecutar con `--spring.profiles.active=local`, o simplemente editar los
valores por defecto directamente en `application.yml` para pruebas
locales rápidas.

### 3.3 Crear el usuario Administrador inicial

La tabla `usuario` no admite contraseñas en texto plano (se guarda un
hash BCrypt). Genera uno con la utilidad incluida:

```bash
cd backend
mvn exec:java -Dexec.mainClass="com.umg.quejasbancario.util.PasswordHashGenerator" -Dexec.args="TuContrasenaSegura"
```

Esto imprime un hash BCrypt en consola. Cópialo y complétalo en el
`INSERT` comentado que está al final de `database/extensions.sql`
(o `postgres_schema.sql`), y ejecútalo contra tu base en Neon.

### 3.4 Ejecutar

```bash
cd backend
mvn spring-boot:run
```

El backend queda disponible en `http://localhost:8080`. Documentación
interactiva de la API (Swagger) en `http://localhost:8080/swagger-ui.html`.

---

## 4. Ejecutar el frontend

El frontend es HTML/JS estático — no necesita build ni Node.js. Basta con
servirlo con cualquier servidor estático, por ejemplo:

- **VS Code:** extensión "Live Server" → clic derecho en `frontend/index.html` → "Open with Live Server" (por defecto usa el puerto `5500`, que ya coincide con el `CORS_ORIGINS` de ejemplo).
- **Python:** `cd frontend && python -m http.server 5500`

Si tu backend corre en una URL distinta a `http://localhost:8080/api`,
edita la constante al inicio de `frontend/js/api.js`:

```js
const API_BASE_URL = window.API_BASE_URL || "http://localhost:8080/api";
```

Al iniciar sesión, cada usuario es redirigido automáticamente a su panel
según su rol (Cliente, Agente, Supervisor, Administrador o Auditor).

---

## 5. Mapeo de Casos de Uso → módulos implementados

| Caso de Uso | Backend | Frontend |
|---|---|---|
| CU-01 Gestión de Acceso y Sesión | `AuthController` / `AuthService` | `index.html`, `forgot-password.html`, `reset-password.html` |
| CU-02 Registrar Caso | `CasoController#registrarCaso` | `cliente/dashboard.html` (sección Registrar) |
| CU-03 Consultar Estado de Caso | `CasoController#misCasos/detalleMiCaso` | `cliente/dashboard.html` (Mis Casos) |
| CU-04 Asignar Caso Automáticamente | `AsignacionService` + `AsignacionScheduler` | (proceso automático, sin UI) |
| CU-05 Solicitar Reasignación | `ReasignacionController#solicitar` | `agente/dashboard.html` |
| CU-06 Aprobar/Rechazar Reasignación | `ReasignacionController` | `supervisor/dashboard.html` |
| CU-07 Iniciar Atención | `AtencionService#iniciarAtencion` | `agente/dashboard.html` |
| CU-08 Resolver Caso | `AtencionService#resolverCaso` | `agente/dashboard.html` |
| CU-09 Cerrar Caso | `AtencionService#cerrarCaso` | `agente/dashboard.html` |
| CU-10 Buscar y Filtrar Casos | `CasoController#buscarCasos` | `supervisor/dashboard.html` |
| CU-11 Enviar Notificación | `NotificacionService` (6 plantillas de RN09) | (correo electrónico) |
| CU-12 Reporte de Casos | `ReporteController#reporteCasos` | `supervisor/` y `administrador/dashboard.html` |
| CU-13 Reporte de Auditoría | `ReporteController#reporteAuditoria` | `auditor/dashboard.html` |
| CU-14 Gestionar Usuarios | `UsuarioController` | `administrador/dashboard.html` |
| CU-15 Gestionar Catálogos | `CatalogoController` | `administrador/dashboard.html` |
| CU-16 Configurar Parámetros | `ParametroController` | `administrador/dashboard.html` |
| CU-17 Consultar Bitácoras | `BitacoraController` | `auditor/dashboard.html` |

---

## 6. Notas y limitaciones conocidas

- El "cierre de sesión" con JWT es *stateless*: el backend registra el
  evento y marca `sesion_activa = false`, pero el token en sí sigue
  siendo técnicamente válido hasta que expira (8 horas por defecto). Para
  un entorno productivo real se recomendaría una lista de tokens
  revocados o tokens de vida más corta con refresh token.
- Los documentos adjuntos se guardan en el sistema de archivos local del
  servidor (`./uploads` por defecto, configurable con `UPLOADS_DIR`). En
  un despliegue en la nube conviene moverlos a un bucket (S3, etc.).
- El envío de correo usa SMTP real (`spring-boot-starter-mail`); si las
  credenciales no están configuradas o el envío falla, el sistema
  **no bloquea** el flujo de negocio: registra el error en
  `BITACORA_CORREO` y continúa (tal como especifica CU-11, FA01).
- Los clientes (rol *Cliente*) no se auto-registran: los crea el
  Administrador desde CU-14, igual que a los demás roles.

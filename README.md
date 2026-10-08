# Sistema de Gestión de Quejas, Reclamos, Denuncias y Sugerencias — Banco Aurora

Implementación del sistema a partir de los casos de uso (CU-00 Portal a CU-17 Consultar Bitácoras), las Reglas de
Negocio (RN01–RN17) y el modelo de base de datos definidos en la fase de análisis.

- **Backend:** Java 17 · Spring Boot 3.3 · Spring Security (JWT) · JPA/Hibernate · PostgreSQL (Neon)
- **Frontend:** HTML · JavaScript (vanilla) · Bootstrap 5, un panel por rol
- **Documentación de la API:** Swagger UI (springdoc-openapi)

---

## Contenido

1. [Descripción y objetivos](#1-descripción-y-objetivos)
2. [Arquitectura y estructura del proyecto](#2-arquitectura-y-estructura-del-proyecto)
3. [Mapeo de casos de uso](#3-mapeo-de-casos-de-uso)
4. [Instalación y ejecución](#4-instalación-y-ejecución)

---

## 1. Descripción y objetivos

El sistema es el canal oficial de una institución bancaria para que sus cuentahabientes registren y den seguimiento a
**quejas, reclamos, denuncias y sugerencias**, y para que el personal interno las atienda, las resuelva y deje
constancia auditable de cada paso.

**Objetivo general:** automatizar el ciclo de vida de un caso, desde su registro hasta su cierre, con trazabilidad completa.

**Objetivos específicos** *(ajústalos a los de tu documento de análisis)*:

- Permitir al cliente registrar un caso con evidencia adjunta y consultar su estado.
- Asignar los casos de forma automática y aleatoria entre los agentes disponibles (RN06).
- Controlar el flujo de estados del caso y las solicitudes de reasignación (RN07, RN08).
- Notificar por correo cada evento relevante (RN09).
- Registrar toda acción en bitácoras inmutables para auditoría (RN15).
- Permitir adaptar el sistema a otra institución bancaria solo cambiando la configuración (RN17).

## 2. Arquitectura y estructura del proyecto

Arquitectura cliente-servidor en capas. El frontend estático consume la API REST del backend; el backend aplica
seguridad, reglas de negocio y persistencia.

```
 ┌────────────────────┐   HTTP / JSON + JWT   ┌──────────────────────────────────────────┐   JDBC   ┌──────────────┐
 │ Frontend estático  │ ────────────────────► │ Backend Spring Boot                      │ ───────► │ PostgreSQL   │
 │ HTML + JS + BS5    │ ◄──────────────────── │ controller → service → repository (JPA)  │ ◄─────── │ (Neon)       │
 └────────────────────┘                       │ security (JWT) · scheduler · exception   │          └──────────────┘
                                              └──────────────────────┬───────────────────┘
                                                                     │ SMTP
                                                                     ▼
                                                              Correo electrónico
```

```
SistemaQuejasBancario/
├── backend/                       Proyecto Maven Spring Boot
│   ├── .env.example               Plantilla de variables de entorno
│   ├── src/main/java/com/umg/quejasbancario/
│   │   ├── entity/                Entidades JPA y enumeraciones
│   │   ├── repository/            Repositorios Spring Data JPA
│   │   ├── service/               Lógica de negocio
│   │   ├── controller/            Endpoints REST
│   │   ├── dto/                   Objetos de petición y respuesta
│   │   ├── validation/            Validaciones propias (DPI/NIT, teléfono)
│   │   ├── security/              JWT, filtros y límite de consultas públicas
│   │   ├── config/                Seguridad, CORS y propiedades
│   │   ├── exception/             Manejo centralizado de errores
│   │   └── scheduler/             Reintento de asignación automática (RF17)
│   └── pom.xml
└── frontend/                      Sitio estático
    ├── index.html                 Portal / Página de inicio (CU-00)
    ├── login.html                 Inicio de sesión (CU-01)
    ├── consultar-caso.html        Consulta pública del estado de un caso
    ├── css/ · js/ · img/          Estilos, lógica compartida y logotipo
    └── cliente/ agente/ supervisor/ administrador/ auditor/    Un panel por rol
```

## 3. Mapeo de casos de uso

| CU | Nombre | Backend | Frontend |
|---|---|---|---|
| CU-00 | Portal / Página de Inicio | `PortalController` / `PortalService` | `index.html`, `consultar-caso.html` |
| CU-01 | Gestión de Acceso y Sesión | `AuthController` / `AuthService` | `login.html`, `forgot-password.html`, `reset-password.html` |
| CU-02 | Registrar Caso | `CasoController#registrarCaso` | `cliente/dashboard.html` |
| CU-03 | Consultar Estado de Caso | `CasoController#misCasos` y `PortalController#estadoCaso` (público) | `cliente/dashboard.html`, `consultar-caso.html` |
| CU-04 | Asignar Caso Automáticamente | `AsignacionService` + `AsignacionScheduler` | (proceso automático) |
| CU-05 | Solicitar Reasignación | `ReasignacionController#solicitar` | `agente/dashboard.html` |
| CU-06 | Aprobar/Rechazar Reasignación | `ReasignacionController` | `supervisor/dashboard.html` |
| CU-07 | Iniciar Atención | `AtencionService#iniciarAtencion` | `agente/dashboard.html` |
| CU-08 | Resolver Caso | `AtencionService#resolverCaso` | `agente/dashboard.html` |
| CU-09 | Cerrar Caso | `AtencionService#cerrarCaso` | `agente/dashboard.html` |
| CU-10 | Buscar y Filtrar Casos | `CasoController#buscarCasos` | `supervisor/dashboard.html` |
| CU-11 | Enviar Notificación | `NotificacionService` (6 plantillas de RN09) | (correo electrónico) |
| CU-12 | Reporte de Casos | `ReporteController#reporteCasos` | `supervisor/`, `administrador/` |
| CU-13 | Reporte de Auditoría | `ReporteController#reporteAuditoria` | `auditor/dashboard.html` |
| CU-14 | Gestionar Usuarios | `UsuarioController` | `administrador/dashboard.html` |
| CU-15 | Gestionar Catálogos | `CatalogoController` | `administrador/dashboard.html` |
| CU-16 | Configurar Parámetros | `ParametroController` | `administrador/dashboard.html` |
| CU-17 | Consultar Bitácoras | `BitacoraController` | `auditor/dashboard.html` |

## 4. Instalación y ejecución

### 4.1 Requisitos

- JDK 17 o superior
- Maven 3.9+ (o un IDE con soporte Maven, como IntelliJ)
- Una base de datos PostgreSQL en Neon con SSL

### 4.2 Variables de entorno

Copia `backend/.env.example` a `backend/.env`, complétalo y cárgalo (o defínelo en
*Run → Edit Configurations → Environment variables* de IntelliJ). **No subas `.env` a git.**

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_URL` | Sí | URL JDBC de Neon, con `?sslmode=require` |
| `DB_USERNAME` / `DB_PASSWORD` | Sí | Credenciales de la base de datos |
| `JWT_SECRET` | Sí | Secreto largo y aleatorio (`openssl rand -base64 48`) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | No | SMTP; con Gmail usa una contraseña de aplicación. Sin ellas no se envían correos |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_FROM` | No | Servidor y remitente |
| `CORS_ORIGINS` | No | Orígenes permitidos del frontend (ej. `http://localhost:5500`) |
| `FRONTEND_RESET_URL` | No | URL de `reset-password.html` |
| `RATE_LIMIT_MAX` / `RATE_LIMIT_VENTANA_SEG` | No | Límite de consultas públicas (10 por 60 s) |
| `FORWARD_HEADERS_STRATEGY` | No | `framework` solo detrás de un proxy de confianza |

### 4.3 Administrador inicial

Las contraseñas se guardan con BCrypt. Genera el hash con la utilidad incluida:

```bash
cd backend
mvn exec:java -Dexec.mainClass="com.umg.quejasbancario.util.PasswordHashGenerator" -Dexec.args="TuContrasenaSegura"
```

Copia el hash en el `INSERT` comentado al final de `database/extensions.sql` y ejecútalo en Neon.

### 4.4 Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Queda en `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

### 4.5 Frontend

Es estático: no necesita build ni Node.js. Sírvelo en el puerto 5500:

- **IntelliJ / VS Code:** *Live Server* sobre `frontend/index.html`.
- **Python:** `cd frontend && python -m http.server 5500`

Abre `http://localhost:5500`. Si el backend corre en otra URL, cambia `API_BASE_URL` al inicio de `frontend/js/api.js`.
Al iniciar sesión, cada usuario va automáticamente a su panel según su rol.

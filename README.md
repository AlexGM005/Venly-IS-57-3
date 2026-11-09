# Venly IS-57-3 · Release 1

Proyecto académico **Venly v2.0**: aplicación de cine con frontend (Vue 3) y backend (Spring Boot) separados, y persistencia en un archivo JSON local.

Release 1 cubre 4 historias de usuario:

| HU | Descripción |
|----|-------------|
| HU1 | Autenticación: registro (Cliente / Gerente / Jefe) e inicio de sesión |
| HU2 | Menú principal según rol, cabecera con sesión y cierre de sesión |
| HU3 | Perfiles: consultar y editar el propio perfil; directorio de perfiles para Gerente/Jefe |
| HU4 | Películas: catálogo para todos; alta y edición (con póster) para Gerente/Jefe |

## 1) Stack

- **Frontend:** Vue 3 + Vite + TypeScript (`<script setup>`), Vue Router, Tailwind CSS + `@tailwindcss/forms`, `fetch` tipado.
- **Backend:** Java 21 + Spring Boot 4.1.2 (MVC REST), Bean Validation, `spring-security-crypto` (solo BCrypt).
- **Persistencia:** `data/Base.json` con Jackson. Sin bases de datos externas.

## 2) Estructura

```text
venly/
├── data/Base.json
├── backend/springboot backend/src/main/java/com/venly/backend/
│   ├── VenlyApplication.java
│   ├── config/        AppConfig (CORS) · SecurityProperties · OrganizationKeyProvider
│   ├── controller/    AuthController · ProfileController · MovieController
│   ├── service/       AuthService · ProfileService · MovieService (interfaces)
│   │   └── impl/      AuthServiceImpl · ProfileServiceImpl · MovieServiceImpl
│   │                  ProfileMapper · RequesterResolver (resolución de sesión y permisos)
│   ├── repository/    JsonDataStore (lectura/escritura atómica con ReentrantLock)
│   │                  ProfileRepository → JsonProfileRepository
│   │                  MovieRepository   → JsonMovieRepository
│   │                  RoomRepository    → JsonRoomRepository
│   ├── model/         Profile · Role · Movie · Room
│   ├── dto/           CreateProfileRequest · UpdateProfileRequest · LoginRequest · ProfileResponse
│   │                  CreateMovieRequest · UpdateMovieRequest · MovieResponse
│   │                  ApiErrorResponse · ValidationPatterns
│   └── exception/     BusinessException · GlobalExceptionHandler
└── frontend/src/
    ├── App.vue · main.ts · assets/main.css
    ├── composables/useSession.ts
    ├── types/         profile.ts · movie.ts
    ├── services/      httpClient.ts · authService.ts · profileService.ts · movieService.ts
    ├── utils/         validators.ts
    ├── components/    AppHeader · LoadingSpinner · ProfileCard · RoleBadge · BaseModal · MovieCard
    ├── router/index.ts
    └── views/         LoginView · MainMenuView · MyProfileView · ProfileDirectoryView
                       MovieCatalogView · MovieDetailView · MovieFormView
```

### Arquitectura en capas

- **Controller:** solo recibe la petición HTTP y delega en el Service.
- **Service (interfaz + impl):** toda la lógica de negocio, validaciones y reglas de rol.
- **Repository (interfaz + impl):** abstrae la persistencia; los repositorios delegan en `JsonDataStore`.
- **JsonDataStore:** acceso concurrente seguro con `ReentrantLock` y escritura atómica (archivo temporal + `ATOMIC_MOVE`).

## 3) Datos (`data/Base.json`)

```json
{
  "Peliculas": [],
  "Perfiles": [],
  "Salas": [ { "id": 1, "nombre": "Sala 1", "capacidad": 120 }, "… hasta Sala 5" ]
}
```

Si `Salas` está vacío al arrancar, el backend crea las 5 salas por defecto.

## 4) Sesión simulada

- No hay JWT. Un login o registro correcto devuelve el perfil completo, sin el hash de contraseña.
- El frontend guarda el perfil en `localStorage` (`venly.session`) y envía la cabecera **`X-Perfil-Id`** en cada petición.
- Si la cabecera falta → `401`. Si el perfil de la cabecera no existe → `401`, y el frontend cierra la sesión.

## 5) Clave de organización (Gerente / Jefe)

- Es una sola clave compartida. Valor de prueba: **`Venly2024`** (`venly.security.organization-key-default` en `application.yml`).
- `OrganizationKeyProvider` calcula su hash BCrypt **una sola vez** al arrancar. Si existe la variable de entorno `VENLY_ORG_KEY_HASH`, usa ese hash en su lugar.
- En el registro, al elegir Gerente o Jefe aparece el campo de la clave con la pista "🔑 Clave de prueba: Venly2024".

## 6) API REST

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/api/auth/register` | Público | Registro. `CLIENTE` por defecto; `GERENTE`/`JEFE` requieren `claveOrganizacion` (incorrecta → 403). Correo o cédula duplicados → 409 |
| POST | `/api/auth/login` | Público | `correo` + `contrasena`. Credenciales inválidas → 401 |
| GET | `/api/perfiles/me` | Sesión | Perfil propio |
| PUT | `/api/perfiles/me` | Sesión | Edita solo `nombre`, `telefono` y `correo` (correo único → 409) |
| GET | `/api/perfiles` | Gerente/Jefe | Listado de perfiles (Cliente → 403) |
| GET | `/api/perfiles/{id}` | Gerente/Jefe o el propio | Detalle de perfil |
| GET | `/api/peliculas` | Todos | Catálogo |
| GET | `/api/peliculas/{id}` | Todos | Detalle (inexistente → 404) |
| GET | `/api/peliculas/salas` | Todos | Las 5 salas |
| POST | `/api/peliculas` | Gerente/Jefe | Crear película (Cliente → 403) |
| PUT | `/api/peliculas/{id}` | Gerente/Jefe | Editar película (`quitarImagen: true` elimina el póster) |

### Reglas de validación (mensajes en español)

- **Nombre:** solo letras y espacios.
- **Cédula:** 6 a 12 dígitos.
- **Teléfono:** 10 a 15 dígitos.
- **Contraseña:** mínimo 6 caracteres, con letras y números. Se guarda como hash BCrypt.
- **Película:**
  - `titulo`, `sinopsis`, `clasificacion` (G, PG, PG-13, R, NC-17) son obligatorios.
  - `horario` tiene el formato `HH:mm - HH:mm`, y el inicio debe ser distinto del fin.
  - `duracion` admite `2h 30min`, `2h` o `90min`.
  - `salaId` debe existir.
  - Imagen opcional, solo **JPG/PNG** (se verifican los bytes reales), máximo **10 MB**, en Base64 (se acepta prefijo `data:`).

### Formato de error

```json
{ "codigo": 400, "mensaje": "Error de validación", "errores": { "campo": "mensaje" } }
```

## 7) Rutas del frontend

| Ruta | Acceso |
|------|--------|
| `/login` | Solo sin sesión (pestañas "Iniciar Sesión" / "Registrarse") |
| `/menu` | Sesión. Cliente: Mi Perfil + Catálogo · Gerente/Jefe: Gestión de Perfiles + Gestión de Películas |
| `/perfil` | Sesión: tarjeta de perfil + modal "Editar Perfil" |
| `/perfiles` | Gerente/Jefe: tabla con búsqueda por nombre o cédula y detalle |
| `/peliculas` | Sesión: catálogo en tarjetas (Gerente/Jefe ven "Añadir película" y "Editar") |
| `/peliculas/:id` | Sesión: detalle |
| `/peliculas/nueva`, `/peliculas/:id/editar` | Gerente/Jefe: formulario con vista previa del póster y selector de sala |

Sin sesión, cualquier ruta protegida redirige a `/login`. Con sesión, `/login` redirige a `/menu`.

## 8) Arranque en localhost

### Backend (puerto 8080)

```bash
cd "backend/springboot backend"
chmod +x mvnw
./mvnw spring-boot:run
```

### Frontend (puerto 5173)

```bash
cd frontend
npm install
npm run dev
```

Vite redirige `/api` → `http://localhost:8080`. El backend además permite CORS para `http://localhost:5173`.

# Venly IS-57-3 · Release 1

Proyecto académico **Venly v2.0** con arquitectura separada en frontend, backend y data.

## 1) Estructura del proyecto

```text
venly/
├── backend/
│   └── springboot backend/
│       ├── pom.xml
│       ├── mvnw
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/venly/backend/
│       │   │   │   ├── VenlyApplication.java
│       │   │   │   ├── config/
│       │   │   │   │   ├── AppConfig.java
│       │   │   │   │   └── SecurityProperties.java
│       │   │   │   ├── controller/
│       │   │   │   │   └── ProfileController.java
│       │   │   │   ├── dto/
│       │   │   │   │   ├── ApiErrorResponse.java
│       │   │   │   │   ├── CreateProfileRequest.java
│       │   │   │   │   └── ProfileResponse.java
│       │   │   │   ├── exception/
│       │   │   │   │   ├── BusinessException.java
│       │   │   │   │   └── GlobalExceptionHandler.java
│       │   │   │   ├── model/
│       │   │   │   │   ├── Profile.java
│       │   │   │   │   ├── Role.java
│       │   │   │   │   └── Room.java
│       │   │   │   ├── repository/
│       │   │   │   │   ├── JsonProfileRepository.java
│       │   │   │   │   └── ProfileRepository.java
│       │   │   │   └── service/
│       │   │   │       ├── ProfileService.java
│       │   │   │       └── impl/ProfileServiceImpl.java
│       │   │   └── resources/
│       │   │       └── application.yml
│       │   └── test/java/com/venly/backend/VenlyApplicationTests.java
├── frontend/
│   ├── package.json
│   ├── tailwind.config.cjs
│   ├── postcss.config.cjs
│   └── src/
│       ├── assets/main.css
│       ├── components/AppHeader.vue
│       ├── router/index.ts
│       ├── services/
│       │   ├── httpClient.ts
│       │   └── profileService.ts
│       ├── types/profile.ts
│       ├── views/
│       │   ├── ConsultProfileView.vue
│       │   └── CreateProfileView.vue
│       ├── App.vue
│       └── main.ts
└── data/
    └── Base.json
```

## 2) Stack técnico aplicado

- **Frontend:** Vue 3 + Vite + TypeScript + Vue Router + Tailwind CSS.
- **Backend:** Java 21 + Spring Boot 4.1.2 (MVC REST).
- **Persistencia:** archivo local `data/Base.json` con Jackson (sin base de datos externa).

> Nota de versión de Spring Boot: en este entorno **sí resolvió correctamente `4.1.2`** en Maven, por lo que se mantuvo la versión solicitada.

## 3) Configuración de datos

Archivo: `data/Base.json`

Incluye las tres colecciones:

- `Peliculas`: arreglo inicial vacío.
- `Perfiles`: arreglo inicial vacío.
- `Salas`: 5 salas por defecto (`Sala 1` a `Sala 5`) con `id`, `nombre` y `capacidad`.

El backend escribe el archivo de forma segura con reemplazo atómico para reducir riesgo de corrupción.

## 4) Flujo de perfiles implementado

### Reglas de negocio

- Validaciones de servidor (Bean Validation):
  - `nombre` obligatorio.
  - `cédula` obligatoria (6 a 12 dígitos).
  - `correo` obligatorio y formato válido.
  - `teléfono` obligatorio (10 a 15 dígitos).
  - `contraseña` obligatoria, mínimo 6 caracteres, con letras y números.
- Roles permitidos: `CLIENTE`, `GERENTE`, `JEFE`.
- Si el rol es `GERENTE` o `JEFE`, se exige clave de organización.
- La clave de organización se valida contra hash BCrypt configurado.
- La contraseña del perfil **siempre** se persiste hasheada con BCrypt.
- Se rechazan duplicados por correo y cédula (`409 Conflict`).

### Endpoints

- `POST /api/perfiles` → crea perfil.
- `GET /api/perfiles/{id}` → consulta perfil propio, validando cabecera `X-Perfil-Id`.
- `GET /api/perfiles` → listado de perfiles, sólo permitido para rol `JEFE` (identificado por `X-Perfil-Id`).

### Errores JSON consistentes

Respuestas con estructura:

```json
{
  "codigo": 400,
  "mensaje": "Error de validación",
  "errores": {
    "campo": "mensaje"
  }
}
```

## 5) Frontend implementado

- Vista **Crear Perfil** (`/crear-perfil`) con:
  - formulario tipado con `<script setup lang="ts">`;
  - selector de rol;
  - campo de clave de organización condicional (`GERENTE`/`JEFE`);
  - validación cliente alineada con servidor;
  - estados de carga, éxito y error en español;
  - consumo de API mediante cliente HTTP tipado.
- Vista **Consultar Perfil** (`/consultar-perfil`) con:
  - uso de `X-Perfil-Id`;
  - render de datos del perfil;
  - botón **Editar Perfil** visible pero sin acción (deshabilitado).

## 6) CORS

Habilitado en backend para `http://localhost:5173` sobre rutas `/api/**`.

## 7) Variables y seguridad

`application.yml` incluye:

- `venly.data.file`: ruta del JSON (`../../data/Base.json`).
- `venly.security.organization-key-hash`: hash BCrypt de clave organizacional, configurable vía variable de entorno `VENLY_ORG_KEY_HASH`.

Clave de referencia usada para pruebas locales: `CLAVE_ORG_VENLY` (no persistida en texto plano, sólo su hash).

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

Con esto, el frontend consume el backend en localhost sin bloqueos de CORS.

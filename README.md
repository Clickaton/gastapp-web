# gastAPP Web

Aplicación web para gestión de gastos (Spring Boot 3 + Thymeleaf).

## Requisitos

- **Java 17+**
- **Maven 3.8+** (o usar el wrapper `mvnw` / `mvnw.cmd` si está disponible)

## Cómo ejecutar la aplicación

1. **Abrir una terminal** en la carpeta del proyecto (`gastapp-web-backend`).

2. **Arrancar en modo desarrollo** (H2 en memoria, datos de prueba al iniciar):

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   En Windows, si tenés Maven instalado:

   ```bash
   mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

   O desde IntelliJ: Run `GastappWebApplication` con el perfil activo `dev`.

3. **Abrir el navegador** en:

   ```
   http://localhost:8080
   ```

   Serás redirigido a **Login**. Si ya estás logueado, irás al **Dashboard**.

## Usuario de prueba

En modo **dev** se crea automáticamente un usuario de prueba al iniciar la app:

| Campo         | Valor               |
|--------------|---------------------|
| **Email**    | `test@gastapp.com`  |
| **Contraseña** | `test123`        |

Pasos: ir a **http://localhost:8080/login**, ingresar esos datos y hacer clic en **Entrar**.

## Flujo recomendado para probar

1. **Login** → `test@gastapp.com` / `test123`
2. **Dashboard** → Ver total del mes y últimos gastos (en dev ya hay 3 de prueba).
3. **Gastos** → Listar, crear, editar y eliminar gastos.
4. **Categorías** → Listar, crear, editar y eliminar categorías (primero crear categorías para usarlas en gastos).

Las rutas `/dashboard`, `/expenses` y `/categories` están protegidas: sin login se redirige a `/login`.

## Otras URLs

- **Registro**: http://localhost:8080/register  
- **Consola H2** (solo perfil `dev`): http://localhost:8080/h2-console  
  - JDBC URL: `jdbc:h2:mem:gastapp`  
  - User: `sa`  
  - Password: *(vacío)*

## Estructura

- `controller.web` – Controladores Thymeleaf (auth, dashboard, gastos, categorías).
- `service` – Lógica de negocio (siempre filtrada por usuario logueado).
- `model` – Entidades JPA (User, Category, Expense).
- `repository` – Spring Data JPA.
- `config` – Security, DataLoader, Thymeleaf (requestURI para navbar).

Ver [SECURITY.md](SECURITY.md) para diseño multi-usuario y seguridad.

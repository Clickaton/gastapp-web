# Seguridad multi-usuario en gastAPP Web

## Objetivo

Que el **Usuario A** nunca pueda ver ni editar datos del **Usuario B**, aunque conozca IDs de recursos (gastos, categorías).

---

## 1. Spring Security: quién está logueado

- **Login**: usar formulario (Thymeleaf) o JWT (React). Al autenticarse, Spring Security guarda el usuario en `SecurityContextHolder`.
- **Obtener usuario actual** en cualquier capa:

```java
// En servicio o controlador
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth != null && auth.getPrincipal() instanceof UserDetails) {
    String email = ((UserDetails) auth.getPrincipal()).getUsername();
    UUID userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
    // usar siempre este userId en repositorios
}
```

- **Regla**: nunca usar `expenseRepository.findAll()` ni `expenseRepository.findById(id)` sin filtrar por usuario. Siempre usar `findByUserId(...)` o `findByIdAndUserId(id, userId)`.

---

## 2. Capa de servicio (patrón “siempre userId”)

- Todos los métodos que toquen datos sensibles reciben **userId** (o lo obtienen de un `CurrentUserService` que lee de `SecurityContext`).
- Ejemplo:

```java
// Bien: siempre se filtra por usuario
expenseRepository.findByUserIdOrderByFechaDesc(userId);
expenseRepository.findByIdAndUserId(expenseId, userId);
expenseRepository.existsByIdAndUserId(expenseId, userId);

// Mal: no filtrar por usuario
expenseRepository.findAll();
expenseRepository.findById(expenseId);
```

- El controlador no debe recibir `userId` desde el cliente (salvo header de prueba en dev). En producción, el `userId` sale solo de `Authentication` / `CurrentUserService`.

---

## 3. Query rewriting con Hibernate @Filter (opcional “toque senior”)

Para que **nunca** se escape un `findAll()` sin filtro de usuario, se puede hacer que Hibernate inyecte siempre `WHERE user_id = :currentUserId` en las entidades `Expense` y `Category`:

1. **Entidad** (ej. `Expense`):

```java
@Entity
@Table(name = "expenses")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "userId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "user_id = :userId")
public class Expense extends BaseAuditableEntity {
    // ...
}
```

2. **Configuración del filtro** (antes de cada transacción que toque gastos/categorías):

```java
@Transactional
public List<Expense> findAllByUserId(UUID userId) {
    entityManager.unwrap(Session.class).enableFilter("tenantFilter").setParameter("userId", userId);
    return expenseRepository.findAll(); // ahora solo devuelve los del userId
}
```

3. **Cuidado**: hay que activar el filtro en **cada** punto de entrada (servicio) con el `userId` del usuario logueado. Si no se activa, no filtra. Por eso el patrón “siempre pasar userId a métodos del repositorio” suele ser más simple y explícito.

---

## 4. Resumen de buenas prácticas

| Capa        | Qué hacer |
|------------|-----------|
| Controlador | No confiar en IDs del cliente; obtener usuario de SecurityContext (o CurrentUserService). |
| Servicio    | Recibir o resolver `userId` y usarlo en **todas** las llamadas a repositorio. |
| Repositorio | Métodos por recurso y usuario: `findByIdAndUserId`, `findByUserId`, `existsByIdAndUserId`. No exponer `findAll()` sin filtro. |
| Producción | Desactivar header `X-User-Id`; usar solo sesión o JWT y `Authentication`. |

Con esto se evita que un usuario acceda o modifique datos de otro aunque conozca el ID del gasto o categoría.

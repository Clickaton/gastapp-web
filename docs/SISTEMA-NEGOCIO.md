# gastAPP Web — Sistema de negocio y alcance

Documentación del modelo de negocio que mantiene la aplicación, su alcance actual y su comportamiento en el tiempo.

---

## 1. Alcance actual del sistema

gastAPP Web hoy es un **registro de gastos por usuario**, con categorías propias y un dashboard que muestra el total del mes en curso.

| Qué hace hoy | Qué no hace hoy |
|--------------|------------------|
| Multi-usuario (cada usuario ve solo sus datos) | No hay **ingresos** ni concepto de "entrada de dinero" |
| CRUD de **gastos** (monto, fecha, categoría, descripción) | No hay **cuentas** ni saldos (ej. banco, efectivo) |
| **Categorías** personalizables por usuario (nombre, color, icono) | No hay **presupuesto** ni tope mensual |
| Dashboard con **total del mes actual** y últimos 10 gastos | No hay **cierre de mes** ni proceso automático al cambiar de mes |
| Listado de gastos ordenado por fecha (más recientes primero) | No hay **compensación** explícita (ingresos vs gastos) |

---

## 2. Modelo de datos (entidades)

### 2.1 User (Usuario)

- **ID**: UUID  
- **email**: único, para login  
- **passwordHash**: contraseña hasheada  
- **nombre**: nombre del usuario  
- **fecha_creacion**: auditoría  

Cada usuario tiene sus propias categorías y gastos. No hay roles ni administración de otros usuarios.

---

### 2.2 Category (Categoría)

- **ID**: UUID  
- **nombre**: ej. "Comida", "Transporte"  
- **icono**: identificador del icono (ej. clase Bootstrap Icons)  
- **color**: hexadecimal (ej. `#4CAF50`)  
- **user**: dueño de la categoría (Many-to-One)  
- **fecha_creacion**: auditoría  

Las categorías son por usuario. Un gasto siempre pertenece a una categoría (y a un usuario).

---

### 2.3 Expense (Gasto)

- **ID**: UUID  
- **monto**: valor del gasto (siempre positivo en el modelo actual)  
- **descripcion**: texto libre, opcional  
- **fecha**: día del gasto (`LocalDate`)  
- **category**: categoría del gasto (Many-to-One)  
- **user**: dueño del gasto (Many-to-One)  
- **fecha_creacion**: auditoría  

Un gasto es un **registro histórico**: una vez guardado, solo cambia si el usuario lo edita o borra. No tiene estado "pagado/pendiente" ni se vincula a ninguna cuenta ni saldo.

---

## 3. Comportamiento en el tiempo

### 3.1 Cómo se usa la fecha

- Cada gasto tiene una **fecha** (día).
- Los totales y reportes se calculan **por rango de fechas** (por ejemplo, todo un mes).
- No hay "mes contable" ni "período cerrado": cualquier día puede tener gastos y se pueden editar o borrar en cualquier momento.

### 3.2 Qué pasa cuando "finaliza el mes"

- **No hay ningún proceso automático** al cambiar de mes.
- Al abrir el dashboard en un mes nuevo:
  - El **total del mes** corresponde al mes actual (1 al último día del mes).
  - Si todavía no cargaste gastos en ese mes, el total es **0**.
- Los gastos de meses anteriores **siguen existiendo** y se pueden ver en el listado de gastos (ordenado por fecha descendente).
- No se "archivan", no se "cierran" ni se mueven: solo dejan de entrar en el total del mes actual porque su `fecha` ya no cae en ese rango.

Resumen: **el cambio de mes solo cambia el rango que usa el dashboard para sumar; no hay cierre ni acumulación automática de saldos.**

### 3.3 ¿Los gastos son acumulativos?

- **Como registros**: no. Cada gasto es un hecho aislado (monto + fecha + categoría). No hay "saldo acumulado" guardado en base de datos.
- **Como totales mostrados**: sí, en el sentido de que **el total del mes es la suma** de todos los gastos cuya `fecha` está en ese mes.

Ejemplo:

- Gastos: 100 (1/1), 50 (15/1), 200 (30/1).  
- Total enero = 100 + 50 + 200 = 350.  
- En febrero, el total de enero no se "acumula" a febrero: el total de febrero es solo la suma de los gastos con fecha en febrero.

No existe en el sistema actual:

- Saldo inicial de mes.  
- Saldo final de mes.  
- "Acumulado anual" ni total histórico en una sola pantalla (aunque se podría calcular sumando rangos).

---

## 4. Cómo se calculan los totales hoy

- **Dashboard — "Gastos del mes"**  
  - Rango: desde el día 1 del mes actual hasta el último día del mes actual.  
  - Cálculo: `SUM(monto)` de todos los gastos del usuario con `fecha` en ese rango.  
  - Si no hay gastos en ese rango, el resultado es **0**.

- **Listado de gastos**  
  - Muestra todos los gastos del usuario, ordenados por `fecha` descendente (más recientes primero).  
  - No hay total global en esa pantalla en el código actual; el único total por período es el del dashboard (mes actual).

---

## 5. Compensación de gastos (qué hay y qué falta)

### 5.1 Qué existe hoy

- Solo **gastos** (salida de dinero).
- Total del mes = suma de esos gastos en el mes.
- No hay concepto de "compensar" en el modelo ni en las pantallas.

### 5.2 Qué habría que tener para "compensar" gastos

Depende de qué quieras decir por "compensar":

| Interpretación | Qué implica | Estado actual |
|----------------|-------------|----------------|
| **Ingresos vs gastos** | Poder cargar ingresos y ver "Ingresos − Gastos" (o saldo del mes) | No hay entidad Ingreso ni pantalla de ingresos. |
| **Presupuesto mensual** | Definir un tope (ej. 50.000) y ver "te sobró / te faltó" | No hay entidad Presupuesto ni regla de negocio. |
| **Cuentas / saldo** | Efectivo, banco, etc., y que cada gasto reste de un saldo | No hay entidad Cuenta ni saldos. |
| **Pagos / deudas** | Marcar gastos como "pagados" o asociarlos a una deuda | No hay estados ni entidades para eso. |

Para avanzar habría que:

1. Definir **una** de estas líneas (o una combinación clara).  
2. Añadir las entidades y pantallas necesarias (por ejemplo Ingreso, o Presupuesto, o Cuenta).  
3. Definir reglas de cálculo (ej. "saldo del mes = ingresos del mes − gastos del mes").

---

## 6. Resumen ejecutivo

- **Alcance:** Registro de gastos por usuario, con categorías y total del mes actual.  
- **Tiempo:** La fecha del gasto define en qué mes entra; no hay cierre de mes ni proceso automático al cambiar de mes.  
- **Totales:** El total del mes es la **suma** de los montos de gastos con fecha en ese mes; no hay saldo acumulado ni ingresos.  
- **Compensación:** No está implementada; requiere definir si serán ingresos, presupuesto, cuentas o otro concepto, y luego extender modelo y pantallas.

Con esta base podés decidir cómo avanzar: por ejemplo, agregar **ingresos** y una vista "Ingresos − Gastos del mes", o **presupuesto mensual** y alertas cuando te pasás, o **cuentas** con saldo.

Si querés, el siguiente paso puede ser un documento corto de "Evolución sugerida" (por ejemplo: Fase 1 Ingresos, Fase 2 Presupuesto) con cambios concretos en entidades y pantallas.

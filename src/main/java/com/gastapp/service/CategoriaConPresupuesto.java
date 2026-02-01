package com.gastapp.service;

import com.gastapp.model.Category;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * DTO para mostrar categoría con su presupuesto del mes y gasto actual.
 */
@Value
@Builder
public class CategoriaConPresupuesto {
    Category category;
    BigDecimal montoPresupuestado;  // null si no hay presupuesto
    BigDecimal gastoActual;
    UUID budgetId;  // null si no hay presupuesto

    /** Porcentaje usado (0-100+), null si no hay presupuesto. */
    public Double getPorcentajeUsado() {
        if (montoPresupuestado == null || montoPresupuestado.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return gastoActual.divide(montoPresupuestado, 4, RoundingMode.HALF_UP).doubleValue() * 100;
    }

    public boolean isExcedido() {
        return montoPresupuestado != null && gastoActual.compareTo(montoPresupuestado) > 0;
    }
}

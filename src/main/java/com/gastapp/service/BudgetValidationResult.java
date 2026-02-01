package com.gastapp.service;

import java.math.BigDecimal;

/**
 * Resultado de validación de presupuesto al registrar un gasto.
 */
public record BudgetValidationResult(
    boolean presupuestoExcedido,
    BigDecimal montoPresupuestado,
    BigDecimal gastoActual,
    BigDecimal montoNuevo
) {
    public String getMensajeAviso() {
        if (!presupuestoExcedido) return null;
        return String.format("Aviso: Has superado el presupuesto de esta categoría para el mes (Límite: %s $, Gasto actual: %s $).",
            montoPresupuestado.toString(), gastoActual.toString());
    }
}

-- Schema inicial compatible con PostgreSQL (ejecutar manualmente o con flyway/liquibase en prod).
-- Con H2 en dev, Spring/JPA pueden crear el esquema; este archivo sirve como referencia y para prod.

-- Extensión para UUID (PostgreSQL)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Usuarios
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users (email);

-- Categorías (cada una pertenece a un usuario)
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    icono VARCHAR(50),
    color VARCHAR(20),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_categories_user_id ON categories (user_id);

-- Cuentas (efectivo, banco, mercado pago - con icono y color)
CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    saldo_inicial NUMERIC(19, 4) NOT NULL DEFAULT 0,
    icono VARCHAR(50),
    color VARCHAR(20),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_accounts_user_id ON accounts (user_id);

-- Ingresos (vinculados a una cuenta)
CREATE TABLE IF NOT EXISTS incomes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    account_id UUID NOT NULL REFERENCES accounts (id) ON DELETE RESTRICT,
    monto NUMERIC(19, 4) NOT NULL,
    descripcion VARCHAR(500),
    fecha DATE NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_incomes_user_id ON incomes (user_id);
CREATE INDEX IF NOT EXISTS ix_incomes_account_id ON incomes (account_id);
CREATE INDEX IF NOT EXISTS ix_incomes_fecha ON incomes (fecha);

-- Presupuestos (monto máximo por categoría por mes/año)
CREATE TABLE IF NOT EXISTS budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    mes INTEGER NOT NULL,
    anio INTEGER NOT NULL,
    monto_maximo NUMERIC(19, 4) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_budgets_user_id ON budgets (user_id);
CREATE INDEX IF NOT EXISTS ix_budgets_category_id ON budgets (category_id);
CREATE INDEX IF NOT EXISTS ix_budgets_mes_anio ON budgets (mes, anio);

-- Gastos (cada uno pertenece a un usuario, categoría y cuenta)
CREATE TABLE IF NOT EXISTS expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    account_id UUID NOT NULL REFERENCES accounts (id) ON DELETE RESTRICT,
    monto NUMERIC(19, 4) NOT NULL,
    descripcion VARCHAR(500),
    fecha DATE NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS ix_expenses_user_id ON expenses (user_id);
CREATE INDEX IF NOT EXISTS ix_expenses_category_id ON expenses (category_id);
CREATE INDEX IF NOT EXISTS ix_expenses_account_id ON expenses (account_id);
CREATE INDEX IF NOT EXISTS ix_expenses_fecha ON expenses (fecha);

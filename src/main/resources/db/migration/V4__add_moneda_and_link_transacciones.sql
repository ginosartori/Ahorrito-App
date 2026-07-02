-- Add relation from transactions to fixed expenses
ALTER TABLE transacciones ADD COLUMN gasto_fijo_id BIGINT;
ALTER TABLE transacciones ADD CONSTRAINT fk_transacciones_gasto_fijo FOREIGN KEY (gasto_fijo_id) REFERENCES gastos_fijos(id) ON DELETE SET NULL;
CREATE INDEX idx_transacciones_gasto_fijo_id ON transacciones(gasto_fijo_id);

-- Add currency code to fixed expenses
ALTER TABLE gastos_fijos ADD COLUMN moneda VARCHAR(3) NOT NULL DEFAULT 'ARS';

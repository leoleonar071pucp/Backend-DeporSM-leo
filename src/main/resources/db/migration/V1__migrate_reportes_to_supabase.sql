-- Migración para cambiar el almacenamiento de reportes de local a Supabase
-- Cambia la columna ruta_archivo por url_archivo

-- Agregar nueva columna url_archivo
ALTER TABLE reportes ADD COLUMN url_archivo VARCHAR(500);

-- Copiar datos existentes (opcional, si hay reportes existentes)
-- UPDATE reportes SET url_archivo = ruta_archivo WHERE ruta_archivo IS NOT NULL;

-- Eliminar la columna antigua ruta_archivo
ALTER TABLE reportes DROP COLUMN ruta_archivo;

-- Hacer la nueva columna NOT NULL
ALTER TABLE reportes MODIFY COLUMN url_archivo VARCHAR(500) NOT NULL;

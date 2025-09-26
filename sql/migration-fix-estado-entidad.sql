-- Limpia duplicados en estado_entidad manteniendo el último por (tipo_entidad,id_entidad)
START TRANSACTION;

CREATE TEMPORARY TABLE tmp_estado_entidad AS
SELECT t.*
FROM estado_entidad t
JOIN (
  SELECT tipo_entidad, id_entidad, MAX(fecha_cambio) AS max_fecha
  FROM estado_entidad
  GROUP BY tipo_entidad, id_entidad
) ult
ON ult.tipo_entidad = t.tipo_entidad AND ult.id_entidad = t.id_entidad AND ult.max_fecha = t.fecha_cambio;

DELETE FROM estado_entidad;

INSERT INTO estado_entidad (tipo_entidad, id_entidad, activo, fecha_cambio)
SELECT tipo_entidad, id_entidad, activo, fecha_cambio FROM tmp_estado_entidad;

DROP TEMPORARY TABLE tmp_estado_entidad;

-- Añade índice único si no existiera
SET @stmt = NULL;
SELECT IF(
  EXISTS(
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'estado_entidad' AND index_name = 'uk_estado'
  ),
  'SELECT 1',
  'ALTER TABLE estado_entidad ADD UNIQUE KEY uk_estado (tipo_entidad, id_entidad)'
) INTO @stmt;
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

COMMIT;

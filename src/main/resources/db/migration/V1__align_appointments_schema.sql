DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_duration_minutes'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_duration_in_minutes'
    ) THEN
        EXECUTE 'ALTER TABLE appointments RENAME COLUMN total_duration_minutes TO total_duration_in_minutes';
    ELSIF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_duration'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_duration_in_minutes'
    ) THEN
        EXECUTE 'ALTER TABLE appointments RENAME COLUMN total_duration TO total_duration_in_minutes';
    ELSIF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'totaldurationinminutes'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_duration_in_minutes'
    ) THEN
        EXECUTE 'ALTER TABLE appointments RENAME COLUMN totaldurationinminutes TO total_duration_in_minutes';
    ELSIF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'totalduration_minutes'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_duration_in_minutes'
    ) THEN
        EXECUTE 'ALTER TABLE appointments RENAME COLUMN totalduration_minutes TO total_duration_in_minutes';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_value'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_price'
    ) THEN
        EXECUTE 'ALTER TABLE appointments RENAME COLUMN total_value TO total_price';
    ELSIF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_amount'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_price'
    ) THEN
        EXECUTE 'ALTER TABLE appointments RENAME COLUMN total_amount TO total_price';
    ELSIF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'appointment_total'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_price'
    ) THEN
        EXECUTE 'ALTER TABLE appointments RENAME COLUMN appointment_total TO total_price';
    ELSIF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'totalprice'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'total_price'
    ) THEN
        EXECUTE 'ALTER TABLE appointments RENAME COLUMN totalprice TO total_price';
    END IF;
END $$;

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS total_duration_in_minutes integer;

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS total_price numeric(10,2);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'appointments'
          AND column_name = 'service_id'
          AND is_nullable = 'NO'
    ) THEN
        EXECUTE 'ALTER TABLE appointments ALTER COLUMN service_id DROP NOT NULL';
    END IF;
END $$;

UPDATE appointments
SET total_duration_in_minutes = CAST(EXTRACT(EPOCH FROM (end_time - start_time)) / 60 AS integer)
WHERE total_duration_in_minutes IS NULL
  AND start_time IS NOT NULL
  AND end_time IS NOT NULL;

UPDATE appointments
SET total_duration_in_minutes = 0
WHERE total_duration_in_minutes IS NULL;

UPDATE appointments a
SET total_price = totals.total_price
FROM (
    SELECT asi.appointment_id,
           COALESCE(SUM(asi.service_price_snapshot), 0) AS total_price
    FROM appointment_service_items asi
    GROUP BY asi.appointment_id
) totals
WHERE a.id = totals.appointment_id
  AND a.total_price IS NULL;

UPDATE appointments
SET total_price = 0
WHERE total_price IS NULL;

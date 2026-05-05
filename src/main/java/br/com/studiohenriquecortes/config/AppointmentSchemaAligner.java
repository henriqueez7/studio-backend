package br.com.studiohenriquecortes.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Locale;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppointmentSchemaAligner {

    private static final String TABLE_NAME = "appointments";
    private static final String SERVICE_ID_COLUMN = "service_id";
    private static final String TOTAL_DURATION_COLUMN = "total_duration_in_minutes";
    private static final String TOTAL_PRICE_COLUMN = "total_price";
    private static final String APPOINTMENT_SERVICE_ITEMS_TABLE = "appointment_service_items";
    private static final List<String> LEGACY_DURATION_COLUMNS = List.of(
            "total_duration_minutes",
            "total_duration",
            "totaldurationinminutes",
            "totalduration_minutes"
    );
    private static final List<String> LEGACY_PRICE_COLUMNS = List.of(
            "total_value",
            "total_amount",
            "appointment_total",
            "totalprice"
    );

    private final JdbcTemplate jdbcTemplate;

    @Bean
    public ApplicationRunner alignAppointmentSchema() {
        return args -> {
            if (!tableExists(TABLE_NAME)) {
                return;
            }

            alignLegacyServiceIdColumn();
            alignTotalDurationColumn();
            alignTotalPriceColumn();
        };
    }

    private void alignLegacyServiceIdColumn() {
        if (!columnExists(TABLE_NAME, SERVICE_ID_COLUMN)) {
            return;
        }

        if (isColumnNullable(TABLE_NAME, SERVICE_ID_COLUMN)) {
            return;
        }

        jdbcTemplate.execute(
                "ALTER TABLE " + TABLE_NAME
                        + " ALTER COLUMN " + SERVICE_ID_COLUMN + " DROP NOT NULL"
        );
        log.info("Restricao NOT NULL removida da coluna {} na tabela {}", SERVICE_ID_COLUMN, TABLE_NAME);
    }

    private void alignTotalDurationColumn() {
        if (columnExists(TABLE_NAME, TOTAL_DURATION_COLUMN)) {
            fillMissingTotalDurationValues();
            return;
        }

        for (String legacyColumn : LEGACY_DURATION_COLUMNS) {
            if (columnExists(TABLE_NAME, legacyColumn)) {
                jdbcTemplate.execute(
                        "ALTER TABLE " + TABLE_NAME
                                + " RENAME COLUMN " + legacyColumn
                                + " TO " + TOTAL_DURATION_COLUMN
                );
                log.info("Coluna {} renomeada para {} na tabela {}", legacyColumn, TOTAL_DURATION_COLUMN, TABLE_NAME);
                fillMissingTotalDurationValues();
                return;
            }
        }

        jdbcTemplate.execute(
                "ALTER TABLE " + TABLE_NAME
                        + " ADD COLUMN " + TOTAL_DURATION_COLUMN + " integer"
        );
        fillMissingTotalDurationValues();
        log.info("Coluna {} criada e preenchida na tabela {}", TOTAL_DURATION_COLUMN, TABLE_NAME);
    }

    private void fillMissingTotalDurationValues() {
        if (!columnExists(TABLE_NAME, "start_time") || !columnExists(TABLE_NAME, "end_time")) {
            log.warn("Colunas start_time/end_time nao encontradas em {}. Preenchimento de {} ignorado.", TABLE_NAME, TOTAL_DURATION_COLUMN);
            return;
        }

        jdbcTemplate.execute(
                "UPDATE " + TABLE_NAME + " "
                        + "SET " + TOTAL_DURATION_COLUMN + " = "
                        + "CAST(EXTRACT(EPOCH FROM (end_time - start_time)) / 60 AS integer) "
                        + "WHERE " + TOTAL_DURATION_COLUMN + " IS NULL "
                        + "AND start_time IS NOT NULL "
                        + "AND end_time IS NOT NULL"
        );

        jdbcTemplate.execute(
                "UPDATE " + TABLE_NAME + " "
                        + "SET " + TOTAL_DURATION_COLUMN + " = 0 "
                        + "WHERE " + TOTAL_DURATION_COLUMN + " IS NULL"
        );
    }

    private void alignTotalPriceColumn() {
        if (columnExists(TABLE_NAME, TOTAL_PRICE_COLUMN)) {
            fillMissingTotalPriceValues();
            return;
        }

        for (String legacyColumn : LEGACY_PRICE_COLUMNS) {
            if (columnExists(TABLE_NAME, legacyColumn)) {
                jdbcTemplate.execute(
                        "ALTER TABLE " + TABLE_NAME
                                + " RENAME COLUMN " + legacyColumn
                                + " TO " + TOTAL_PRICE_COLUMN
                );
                log.info("Coluna {} renomeada para {} na tabela {}", legacyColumn, TOTAL_PRICE_COLUMN, TABLE_NAME);
                fillMissingTotalPriceValues();
                return;
            }
        }

        jdbcTemplate.execute(
                "ALTER TABLE " + TABLE_NAME
                        + " ADD COLUMN " + TOTAL_PRICE_COLUMN + " numeric(10,2)"
        );
        fillMissingTotalPriceValues();
        log.info("Coluna {} criada e preenchida na tabela {}", TOTAL_PRICE_COLUMN, TABLE_NAME);
    }

    private void fillMissingTotalPriceValues() {
        if (tableExists(APPOINTMENT_SERVICE_ITEMS_TABLE)
                && columnExists(APPOINTMENT_SERVICE_ITEMS_TABLE, "appointment_id")
                && columnExists(APPOINTMENT_SERVICE_ITEMS_TABLE, "service_price_snapshot")) {
            jdbcTemplate.execute(
                    """
                    UPDATE appointments a
                       SET total_price = totals.total_price
                      FROM (
                            SELECT asi.appointment_id,
                                   COALESCE(SUM(asi.service_price_snapshot), 0) AS total_price
                              FROM appointment_service_items asi
                             GROUP BY asi.appointment_id
                           ) totals
                     WHERE a.id = totals.appointment_id
                       AND a.total_price IS NULL
                    """
            );
        } else {
            log.warn("Tabela/colunas de {} nao encontradas. Preenchimento agregado de {} sera ignorado.", APPOINTMENT_SERVICE_ITEMS_TABLE, TOTAL_PRICE_COLUMN);
        }

        jdbcTemplate.execute(
                "UPDATE " + TABLE_NAME + " "
                        + "SET " + TOTAL_PRICE_COLUMN + " = 0 "
                        + "WHERE " + TOTAL_PRICE_COLUMN + " IS NULL"
        );
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.tables
                where table_schema = current_schema()
                  and lower(table_name) = ?
                """,
                Integer.class,
                tableName.toLowerCase(Locale.ROOT)
        );

        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from information_schema.columns
                where table_schema = current_schema()
                  and lower(table_name) = ?
                  and lower(column_name) = ?
                """,
                Integer.class,
                tableName.toLowerCase(Locale.ROOT),
                columnName.toLowerCase(Locale.ROOT)
        );

        return count != null && count > 0;
    }

    private boolean isColumnNullable(String tableName, String columnName) {
        String isNullable = jdbcTemplate.queryForObject(
                """
                select is_nullable
                from information_schema.columns
                where table_schema = current_schema()
                  and lower(table_name) = ?
                  and lower(column_name) = ?
                """,
                String.class,
                tableName.toLowerCase(Locale.ROOT),
                columnName.toLowerCase(Locale.ROOT)
        );

        return "YES".equalsIgnoreCase(isNullable);
    }
}

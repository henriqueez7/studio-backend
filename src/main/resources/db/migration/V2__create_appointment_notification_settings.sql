CREATE TABLE IF NOT EXISTS appointment_notification_settings (
    id BIGSERIAL PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    message_template TEXT NOT NULL,
    address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO appointment_notification_settings (enabled, message_template, address)
SELECT
    FALSE,
    'Ola, {{clientName}}! Seu horario foi confirmado para {{date}} as {{time}}.' || E'\n\n' ||
    'Servicos: {{serviceNames}}' || E'\n' ||
    'Barbeiro: {{barberName}}' || E'\n' ||
    'Endereco: {{address}}' || E'\n' ||
    'Valor do atendimento: {{price}}' || E'\n\n' ||
    'Tolerancia para atraso: 10 minutos.' || E'\n' ||
    'Em caso de atraso sem aviso previo, sera cobrada uma taxa de 50% sobre o valor do atendimento.',
    ''
WHERE NOT EXISTS (
    SELECT 1
    FROM appointment_notification_settings
);

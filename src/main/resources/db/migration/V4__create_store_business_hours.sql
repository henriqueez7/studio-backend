create table if not exists store_business_hours (
    id bigserial primary key,
    day_of_week varchar(20) not null unique,
    start_time time not null,
    end_time time not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

insert into store_business_hours (day_of_week, start_time, end_time, active)
values
    ('MONDAY', '09:00:00', '20:00:00', false),
    ('TUESDAY', '09:00:00', '20:00:00', true),
    ('WEDNESDAY', '09:00:00', '20:00:00', true),
    ('THURSDAY', '09:00:00', '20:00:00', true),
    ('FRIDAY', '09:00:00', '20:00:00', true),
    ('SATURDAY', '09:00:00', '20:00:00', true),
    ('SUNDAY', '09:00:00', '20:00:00', false)
on conflict (day_of_week) do nothing;

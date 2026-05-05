CREATE TABLE IF NOT EXISTS schedule_blocks (
    id BIGSERIAL PRIMARY KEY,
    barber_id BIGINT NOT NULL,
    block_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    title VARCHAR(120) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_blocks_barber FOREIGN KEY (barber_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_schedule_blocks_barber_date ON schedule_blocks (barber_id, block_date);
CREATE INDEX IF NOT EXISTS idx_schedule_blocks_date ON schedule_blocks (block_date);

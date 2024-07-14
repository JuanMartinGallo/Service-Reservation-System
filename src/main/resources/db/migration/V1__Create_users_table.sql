CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    fullname VARCHAR(100),
    username VARCHAR(50) UNIQUE NOT NULL,
    country VARCHAR(50),
    password VARCHAR(100),
    roles VARCHAR(50),
    date_created TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'capacities') THEN
        CREATE TABLE capacities (
            id SERIAL PRIMARY KEY,
            amenity_type VARCHAR(50) UNIQUE NOT NULL,
            capacity INTEGER
        );
        RAISE NOTICE 'The table "capacities" was created.';
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS reservations (
    id SERIAL PRIMARY KEY,
    amenity_type VARCHAR(50),
    reservation_date DATE,
    start_time TIME,
    end_time TIME,
    username VARCHAR(50),
    user_id INTEGER,
    CONSTRAINT fk_amenity_type FOREIGN KEY (amenity_type) REFERENCES capacities (amenity_type) ON DELETE CASCADE,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
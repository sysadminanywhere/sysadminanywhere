CREATE TABLE inventory_schedule (
    id smallint PRIMARY KEY CHECK (id = 1),
    cron text NOT NULL,
    enabled boolean NOT NULL
);

CREATE TABLE patch_remediation (
    computer_id bigint PRIMARY KEY REFERENCES computers(id) ON DELETE CASCADE,
    owner varchar(255) NOT NULL CHECK (length(trim(owner)) > 0),
    due_date date NOT NULL,
    assigned_at timestamp with time zone NOT NULL,
    verified_at timestamp with time zone
);

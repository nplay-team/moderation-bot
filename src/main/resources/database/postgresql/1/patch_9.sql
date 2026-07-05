ALTER TABLE slowmode_channels
    ADD created_at timestamp NOT NULL DEFAULT now();

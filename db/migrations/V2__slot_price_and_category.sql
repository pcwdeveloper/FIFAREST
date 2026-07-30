-- Adds per-slot pricing and time-of-day category, needed for bulk month slot generation.
--
-- `slots.price` — per-slot price, set explicitly by both manual "Add slot" and bulk
--   generation. NULL on legacy rows created before this change; application code falls
--   back to `courts.price_per_slot` when NULL, so backfilling is optional.
-- `slots.category` — which owner-defined time-of-day bucket (MORNING/AFTERNOON/EVENING/
--   NIGHT) a bulk-generated slot belongs to. NULL for manually-added slots.
--
-- Safe to run more than once (MySQL 8.0.29+ required for "ADD COLUMN IF NOT EXISTS";
-- on an older server, drop the "IF NOT EXISTS" clauses and only run this once).

ALTER TABLE slots
    ADD COLUMN price DECIMAL(10, 2) NULL,
    ADD COLUMN category VARCHAR(20) NULL;

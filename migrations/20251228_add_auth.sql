CREATE DATABASE IF NOT EXISTS recipes_db;
USE recipes_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

ALTER TABLE recipe
    ADD COLUMN IF NOT EXISTS owner_user_id INT NULL,
    ADD COLUMN IF NOT EXISTS visibility VARCHAR(10) NOT NULL DEFAULT 'PRIVATE';

ALTER TABLE recipe
    ADD CONSTRAINT IF NOT EXISTS fk_recipe_owner
        FOREIGN KEY (owner_user_id) REFERENCES users(id)
        ON DELETE SET NULL;


UPDATE recipe SET visibility = 'PUBLIC' WHERE visibility IS NULL OR visibility = '';

CREATE INDEX IF NOT EXISTS idx_recipe_owner ON recipe(owner_user_id);



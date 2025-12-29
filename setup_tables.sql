USE recipes_db;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Create recipe table
CREATE TABLE IF NOT EXISTS recipe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    preparation_time INT,
    cooking_time INT,
    servings INT,
    difficulty VARCHAR(50),
    category VARCHAR(100),
    owner_user_id INT NULL,
    visibility VARCHAR(10) NOT NULL DEFAULT 'PRIVATE',
    CONSTRAINT fk_recipe_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_visibility CHECK (visibility IN ('PUBLIC','PRIVATE'))
);

-- Create ingredient table
CREATE TABLE IF NOT EXISTS ingredient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity DECIMAL(10,2),
    unit VARCHAR(50),
    notes TEXT,
    FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
);

-- Create instruction table
CREATE TABLE IF NOT EXISTS instruction (
    recipe_id INT NOT NULL,
    step_number INT NOT NULL,
    text TEXT NOT NULL,
    PRIMARY KEY (recipe_id, step_number),
    FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
);

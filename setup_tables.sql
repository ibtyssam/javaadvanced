USE recipes_db;

-- Create recipe table
CREATE TABLE IF NOT EXISTS recipe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    preparation_time INT,
    cooking_time INT,
    servings INT,
    difficulty VARCHAR(50),
    category VARCHAR(100)
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

INSERT INTO category (name, display_order) VALUES
    ('Polievky', 1), ('Hlavné jedlá', 2), ('Nápoje', 3);

INSERT INTO menu_item (category_id, name, description, price)
    SELECT c.id, v.name, v.description, v.price
    FROM (VALUES
        ('Polievky', 'Gulášová polievka', 'hovädzí guláš, chlieb', 5.60),
        ('Polievky', 'Cesnaková polievka', 'so syrom a krutónmi', 4.50),
        ('Hlavné jedlá', 'Bryndzové halušky', 'so slaninou', 8.50),
        ('Hlavné jedlá', 'Pizza Margherita', 'paradajková omáčka, mozzarella', 9.00),
        ('Nápoje', 'Kofola 0,5 l', 'čapovaná', 2.20),
        ('Nápoje', 'Vinea 0,3 l', 'biela', 1.00)
    ) AS v(category, name, description, price)
        JOIN category c ON c.name = v.category;

INSERT INTO ingredient (name) VALUES
    ('hovädzie mäso'), ('cibuľa'), ('chlieb'), ('cesnak'), ('syr'), ('krutóny'),
    ('halušky'), ('bryndza'), ('slanina'), ('kyslá smotana'),
    ('cesto'), ('paradajková omáčka'), ('mozzarella'), ('šampiňóny');

INSERT INTO menu_item_ingredient (menu_item_id, ingredient_id, type, removable, extra_price)
    SELECT m.id, i.id, v.type, v.removable, v.extra_price
    FROM (VALUES
        ('Gulášová polievka', 'hovädzie mäso', 'BASE', false, NULL::numeric),
        ('Gulášová polievka', 'cibuľa', 'BASE', true, NULL),
        ('Gulášová polievka', 'chlieb', 'BASE', true, NULL),
        ('Cesnaková polievka', 'cesnak', 'BASE', false, NULL),
        ('Cesnaková polievka', 'syr', 'BASE', true, NULL),
        ('Cesnaková polievka', 'krutóny', 'BASE', true, NULL),
        ('Bryndzové halušky', 'halušky', 'BASE', false, NULL),
        ('Bryndzové halušky', 'bryndza', 'BASE', false, NULL),
        ('Bryndzové halušky', 'slanina', 'BASE', true, NULL),
        ('Bryndzové halušky', 'kyslá smotana', 'EXTRA', false, 0.80),
        ('Pizza Margherita', 'cesto', 'BASE', false, NULL),
        ('Pizza Margherita', 'paradajková omáčka', 'BASE', false, NULL),
        ('Pizza Margherita', 'mozzarella', 'BASE', true, NULL),
        ('Pizza Margherita', 'slanina', 'EXTRA', false, 1.20),
        ('Pizza Margherita', 'šampiňóny', 'EXTRA', false, 0.90)
    ) AS v(item, ingredient, type, removable, extra_price)
        JOIN menu_item m ON m.name = v.item
        JOIN ingredient i ON i.name = v.ingredient;

INSERT INTO dining_table (label, token) VALUES
    ('1', 'dev-token-stol-1'),
    ('2', 'dev-token-stol-2'),
    ('3', 'dev-token-stol-3');
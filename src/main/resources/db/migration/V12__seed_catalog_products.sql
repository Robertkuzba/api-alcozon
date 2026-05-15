-- Katalog produktów z folderu Informacje do bazy danych
-- Uruchamiane raz przez Flyway (V12)

UPDATE products SET is_active = FALSE WHERE name = 'Demo Vodka 500ml';

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wódka Ostoya', 'Wódka Ostoya 40% 0,5L', 'vodka', 54.99, 500, 40.0, '/products/vodka-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wódka Polska Pan Tadeusz', 'Wódka Polska Pan Tadeusz 40% 500ml', 'vodka', 62.49, 500, 40.0, '/products/vodka-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wódka Wyborowa', 'Wódka Wyborowa 40% 700ml', 'vodka', 69.99, 700, 40.0, '/products/vodka-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Belvedere Vodka w Kartoniku', 'Belvedere Vodka w Kartoniku | 40% | 700 ml', 'vodka', 77.49, 700, 40.0, '/products/vodka-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wódka Ogiński', 'Wódka Ogiński 500ml 40%', 'vodka', 84.99, 500, 40.0, '/products/vodka-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Finladnia', 'Finladnia 40% może mieć 500ml lub 700ml', 'vodka', 92.49, 500, 40.0, '/products/vodka-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Czarna Olcha wódka kraftowa z żyta', 'Czarna Olcha wódka kraftowa z żyta 40% 500ml', 'vodka', 99.99, 500, 40.0, '/products/vodka-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wódka Biały Bocian', 'Wódka Biały Bocian może mieć 500ml lub 700ml 40%', 'vodka', 54.99, 500, 40.0, '/products/vodka-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wódka J.A. Baczewski', 'Wódka J.A. Baczewski 700ml 40%', 'vodka', 62.49, 700, 40.0, '/products/vodka-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Soplica', 'Soplica 40% 500ml lub 1l', 'vodka', 69.99, 500, 40.0, '/products/vodka-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wódka Stumbras', 'Wódka Stumbras ma 500 lub 700ml 40%', 'vodka', 77.49, 700, 40.0, '/products/vodka-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Absolut Vodka', 'Absolut Vodka 40% ma 500, 700 lub 1000ml', 'vodka', 84.99, 1000, 40.0, '/products/vodka-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Aberlour 12', 'Aberlour 12 40% 700ml', 'whisky', 149.99, 700, 40.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Ardbeg 10 y.o. Single Malt', 'Ardbeg 10 y.o. Single Malt 46% 700ml', 'whisky', 197.49, 700, 46.0, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Ardbeg 8', 'Ardbeg 8 50.8% 700ml', 'whisky', 204.99, 700, 50.8, '/products/whisky-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Auchentoshan 12', 'Auchentoshan 12 40% 700ml', 'whisky', 172.49, 700, 40.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Auchentoshan Three Wood', 'Auchentoshan Three Wood 43% 700ml', 'whisky', 179.99, 700, 43.0, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Ballantine''s Brasil', 'Ballantine''s Brasil 30% 700ml', 'whisky', 187.49, 700, 30.0, '/products/whisky-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Ballantine''s Finest', 'Ballantine''s Finest 40% 500ml, 700ml, 1000ml', 'whisky', 194.99, 500, 40.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Balvenie 12', 'Balvenie 12 40% 700ml', 'whisky', 149.99, 700, 40.0, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Macallan Rare Cask 2023', 'Macallan Rare Cask 2023 43% 700ml', 'whisky', 157.49, 700, 43.0, '/products/whisky-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Macallan 18 Double Cask', 'Macallan 18 Double Cask 43% 700ml', 'whisky', 164.99, 700, 43.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bushmills Black Bush', 'Bushmills Black Bush 40% 700ml', 'whisky', 172.49, 700, 40.0, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bushmills Original', 'Bushmills Original 40% 500ml, 700ml, 1000ml, 1750ml', 'whisky', 179.99, 500, 40.0, '/products/whisky-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Tullamore Dew', 'Tullamore Dew 40% 700ml, 1000ml', 'whisky', 187.49, 700, 40.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bulleit Bourbon Frontier Whiskey', 'Bulleit Bourbon Frontier Whiskey 45% 700ml', 'whisky', 234.99, 700, 45.0, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bulleit Rye Burbon', 'Bulleit Rye Burbon 45% 700ml', 'whisky', 189.99, 700, 45.0, '/products/whisky-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Gentleman Jack Tennessee Whiskey', 'Gentleman Jack Tennessee Whiskey 40% 700ml, 1000ml', 'whisky', 157.49, 700, 40.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Jack Daniel''s Single Barrel', 'Jack Daniel''s Single Barrel 45% 700ml', 'whisky', 204.99, 700, 45.0, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Jack Daniel''s Tennessee Fire', 'Jack Daniel''s Tennessee Fire 35% 700ml', 'whisky', 172.49, 700, 35.0, '/products/whisky-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Jack Daniel''s Tennessee Honey', 'Jack Daniel''s Tennessee Honey 500ml, 700ml 35%', 'whisky', 179.99, 500, 35.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Jack Daniel''s Tennessee Whiskey', 'Jack Daniel''s Tennessee Whiskey 40% 500ml, 700ml, 1000ml', 'whisky', 187.49, 500, 40.0, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Knob Creek', 'Knob Creek 50% 700ml', 'whisky', 234.99, 700, 50.0, '/products/whisky-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Hibiki Suntory Whisky', 'Hibiki Suntory Whisky 43% 700ml', 'whisky', 149.99, 700, 43.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Nikka Whisky From The Barrel', 'Nikka Whisky From The Barrel 51,4% 500ml', 'whisky', 197.49, 500, 51.4, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Tenjaku Blended Japanese Whisky', 'Tenjaku Blended Japanese Whisky 40% 500ml, 700ml', 'whisky', 164.99, 500, 40.0, '/products/whisky-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Tenjaku Whisky Pure Malt', 'Tenjaku Whisky Pure Malt 43% 500ml, 700ml', 'whisky', 172.49, 500, 43.0, '/products/whisky-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('The Chita whisky', 'The Chita whisky 43% 700ml', 'whisky', 179.99, 700, 43.0, '/products/whisky-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Chardonnay 2022 białe wytrawne', 'Chardonnay 2022 12% 750ml białe wytrawne', 'wine', 69.99, 750, 12.0, '/products/wine-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Grzaniec Benedyktyński Klasyczny', 'Grzaniec Benedyktyński Klasyczny 13% 1 litr', 'wine', 77.49, 1000, 13.0, '/products/wine-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Pet-Nat brut wino białe', 'Pet-Nat brut wino białe 750ml 13%', 'wine', 84.99, 750, 13.0, '/products/wine-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Pet-Nat brut wino czerwone', 'Pet-Nat brut wino czerwone 750ml 13%', 'wine', 92.49, 750, 13.0, '/products/wine-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Winiarnia Zamojska Wino Gruszkowe Musujące', 'Winiarnia Zamojska Wino Gruszkowe Musujące 7.5% 750ml', 'wine', 99.99, 750, 7.5, '/products/wine-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wino CIN&CIN Free Sauvignon Blanc Ambra', 'Wino CIN&CIN Free Sauvignon Blanc 750ml Ambra', 'wine', 107.49, 750, NULL, '/products/wine-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wino Jagodowe Słodkie', 'Wino Jagodowe Słodkie 13% 750 ml', 'wine', 114.99, 750, 13.0, '/products/wine-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wino Mirabelka Słodkie', 'Wino Mirabelka Słodkie 12% 750ml', 'wine', 69.99, 750, 12.0, '/products/wine-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wino Musujące Rosé Reserva różowe, musujące, z certyfikowanych polskich owoców odmiany Pinot Noir, extra brut', 'Wino Musujące Rosé Reserva 12.5% 750ml różowe, musujące, 100% z certyfikowanych polskich owoców odmiany Pinot Noir, extra brut', 'wine', 77.49, 750, 12.5, '/products/wine-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Wino z Aronii Ekologiczne Wytrawne', 'Wino z Aronii Ekologiczne Wytrawne 13% 750ml', 'wine', 84.99, 750, 13.0, '/products/wine-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Zachowickie półsłodkie czerwone czerwone gronowe', 'Zachowickie półsłodkie czerwone 12.5% 750ml czerwone gronowe', 'wine', 92.49, 750, 12.5, '/products/wine-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('BERNARD ŚWIĄTECZNY LAGER', 'BERNARD ŚWIĄTECZNY LAGER 5% 500ml', 'beer', 4.49, 500, 5.0, '/products/beer-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Cieszyn Wheat', 'Cieszyn Wheat 5.4% 330ml', 'beer', 4.84, 330, 5.4, '/products/beer-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('HEINEKEN Piwo zawartość alkoholu:', 'HEINEKEN Piwo 500 ml zawartość alkoholu: 5%', 'beer', 5.19, 500, 5.0, '/products/beer-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Karmi Classic Piwo ciemne bezalkoholowe', 'Karmi Classic Piwo ciemne bezalkoholowe 500 ml', 'beer', 5.54, 500, NULL, '/products/beer-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('OKOCIM Piwo bezalkoholowe z lemoniadą limonka z miętą', 'OKOCIM Piwo bezalkoholowe z lemoniadą limonka z miętą 500ml', 'beer', 5.89, 500, NULL, '/products/beer-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Piwo bezalkoholowe kraftowe - piwa 0 procent - Piwo rzemieślnicze mazurskie', 'Piwo bezalkoholowe kraftowe - piwa 0 procent - Piwo rzemieślnicze mazurskie 500ml', 'beer', 4.49, 500, NULL, '/products/beer-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('PIWO BROWAR JABŁONOWO NA MIODZIE GRYCZANYM BUT.BZ.', 'PIWO BROWAR JABŁONOWO NA MIODZIE GRYCZANYM 5,2% 0,5L BUT.BZ.', 'beer', 4.84, 500, 5.2, '/products/beer-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Piwo czeskie bezalkoholowe Non Alcoholic max alk obj - Paropramen', 'Piwo czeskie bezalkoholowe Non Alcoholic max alk 0,5% obj 500ml - Paropramen', 'beer', 5.19, 500, 0.5, '/products/beer-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Piwo Lech free ananas butelka', 'Piwo Lech free 0,0% ananas butelka , 0,330 litr', 'beer', 5.54, 330, 0.0, '/products/beer-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('PIWO ZATECKI SVETLY LEZAK', 'PIWO ZATECKI SVETLY LEZAK 0,5L 5%', 'beer', 5.89, 500, 5.0, '/products/beer-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('TATRA JASNE PEŁNE', 'TATRA JASNE PEŁNE 500ML 5.6%', 'beer', 4.49, 500, 5.6, '/products/beer-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Warka Radler Mix piwa i lemoniady cytryna', 'Warka Radler Mix piwa 0 % i lemoniady cytryna 500 ml', 'beer', 4.84, 500, 0.0, '/products/beer-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Jagermeister', 'Jagermeister 500ml 35%', 'liqueur', 59.99, 500, 35.0, '/products/liqueur-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Aperol', 'Aperol 700ml 11%', 'liqueur', 67.49, 700, 11.0, '/products/liqueur-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Baileys', 'Baileys 17% 700ml lub 1000ml', 'liqueur', 74.99, 700, 17.0, '/products/liqueur-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Carolans Irish Cream', 'Carolans Irish Cream 700ml 17%', 'liqueur', 82.49, 700, 17.0, '/products/liqueur-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Cointreau - Francuski likier pomarańczowy', 'Cointreau - Francuski likier pomarańczowy 40% 700ml', 'liqueur', 89.99, 700, 40.0, '/products/liqueur-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Disaronno', 'Disaronno 28% 700ml', 'liqueur', 97.49, 700, 28.0, '/products/liqueur-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Drambuie', 'Drambuie 40% 700ml', 'liqueur', 104.99, 700, 40.0, '/products/liqueur-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Jakob Haberfeld Pigwówka', 'Jakob Haberfeld Pigwówka 35% 500ml', 'liqueur', 59.99, 500, 35.0, '/products/liqueur-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Kahlua likier kawowy', 'Kahlua likier kawowy 16% 700ml', 'liqueur', 67.49, 700, 16.0, '/products/liqueur-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Likier kokosowy Malibu 500, 700', 'Likier kokosowy Malibu 500, 700, 1000ml 18%', 'liqueur', 74.99, 1000, 18.0, '/products/liqueur-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Passoa 500', 'Passoa 14,9% 500 lub 700ml', 'liqueur', 82.49, 700, 14.9, '/products/liqueur-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Sheridans', 'Sheridans 700ml 15.5%', 'liqueur', 89.99, 700, 15.5, '/products/liqueur-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bacardi Carta Blanca', 'Bacardi Carta Blanca 37.5% 700ml', 'rum', 79.99, 700, 37.5, '/products/rum-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bacardi Carta Negra', 'Bacardi Carta Negra 37.5% 700ml', 'rum', 87.49, 700, 37.5, '/products/rum-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bacardi Carta Oro', 'Bacardi Carta Oro 37.5% 700ml', 'rum', 94.99, 700, 37.5, '/products/rum-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Botucal Reserva Exclusiva', 'Botucal Reserva Exclusiva 40% 700ml', 'rum', 102.49, 700, 40.0, '/products/rum-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Botucal rum', 'Botucal rum 40% 700ml', 'rum', 109.99, 700, 40.0, '/products/rum-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bumbu XO', 'Bumbu XO 40% 700ml', 'rum', 117.49, 700, 40.0, '/products/rum-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Captain Morgan Dark Rum', 'Captain Morgan Dark Rum 40% 700ml lub 1000ml', 'rum', 124.99, 700, 40.0, '/products/rum-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Captain Morgan Spiced Gold', 'Captain Morgan Spiced Gold 35% 700ml', 'rum', 79.99, 700, 35.0, '/products/rum-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Dictador 12', 'Dictador 12 40% 700ml', 'rum', 87.49, 700, 40.0, '/products/rum-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Don Papa Masskara', 'Don Papa Masskara 40% 700ml', 'rum', 94.99, 700, 40.0, '/products/rum-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Eminente Ron De Cuba', 'Eminente Ron De Cuba 41.3% 700ml', 'rum', 102.49, 700, 41.3, '/products/rum-mockup-2.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Bumbu', 'Bumbu 40% 700ml', 'rum', 109.99, 700, 40.0, '/products/rum-mockup-3.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

INSERT INTO products (name, description, category, price, volume_ml, abv, image_url, is_active, created_at, updated_at)
VALUES ('Rum Kraken', 'Rum Kraken 40% 700ml', 'rum', 117.49, 700, 40.0, '/products/rum-mockup-1.png', TRUE, NOW(), NOW());
INSERT INTO product_stock (product_id, quantity, warehouse_zone)
VALUES (currval(pg_get_serial_sequence('products', 'id')), 50, 'A1');

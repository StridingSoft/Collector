--
-- Файл сгенерирован с помощью SQLiteStudio v3.2.1 в Чт дек 27 18:26:38 2018
--
-- Использованная кодировка текста: UTF-8
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Таблица: collections
CREATE TABLE collections (_collectionId INTEGER, _collectionCreatorId INTEGER, _collectionName TEXT, _collectionDescription TEXT, _collectionCreateDate TEXT);

-- Таблица: items
CREATE TABLE items (_itemId INTEGER, _itemSectionId INTEGER, _itemName TEXT, _itemDescription TEXT, _itemImagePath TEXT);
INSERT INTO items (_itemId, _itemSectionId, _itemName, _itemDescription, _itemImagePath) VALUES (1, 0, 'Overlord-02', 'Overlord-02', 'C:\ANDROID\AndroidProjects\Collector\app\src\main\imagesDb\Overlord\Overlord-02.jpg');
INSERT INTO items (_itemId, _itemSectionId, _itemName, _itemDescription, _itemImagePath) VALUES (0, 0, 'Overlord-01', 'Overlord-01', 'C:\ANDROID\AndroidProjects\Collector\app\src\main\imagesDb\Overlord\Overlord-01.jpg');

-- Таблица: sections
CREATE TABLE sections (_sectionId INTEGER, _sectionCollectionId INTEGER, _sectionName TEXT, _sectionDescription TEXT);
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (13, 0, 'Форма голоса', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (12, 0, 'Твоё имя', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (11, 0, 'Скунс и Оцелот', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (10, 0, 'Сад изящных слов', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (9, 0, 'Невеста чародея', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (8, 0, 'Летние войны', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (7, 0, 'Девочка из Чужеземья', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (6, 0, 'Двойняшки Фуро', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (5, 0, 'Гигантомахия', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (4, 0, 'Волчица и пряности', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (3, 0, 'Волколуние', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (2, 0, 'To Your Eternity', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (1, 0, 'Overlord', 'description');
INSERT INTO sections (_sectionId, _sectionCollectionId, _sectionName, _sectionDescription) VALUES (0, 0, 'SAO', 'description');

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;

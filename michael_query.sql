-- Smallest Transaction Per Day
SELECT * FROM transaction WHERE cost = (SELECT MIN(cost) FROM transaction) ORDER BY time ASC;

-- Inventory items per menu item (dish)
SELECT dish.name COUNT(dishinventory.fk_dish)
FROM dish
JOIN dishinventory
ON dishinventory.fk_dish = dish.dish_id
GROUP BY dish.name;

-- Number of times each Dish was ordered (over the entire database)
SELECT dish.name, COUNT(*)
FROM transactiondish
JOIN dish on transactiondish.FK_dish = dish.dish_id
GROUP BY dish.name;





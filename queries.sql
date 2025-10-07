-- Bao based income 1
SELECT SUM(cost) from transaction WHERE fk_customer=14 OR fk_customer=15;

-- Employee with most profit 2
SELECT fk_employee, SUM(cost) from transaction GROUP BY fk_employee ORDER BY sum DESC;

-- Worst Day Best Dish 3
CREATE TEMPORARY VIEW lowestDay AS SELECT time::date AS day, SUM(cost) AS day_sum FROM transaction GROUP BY day ORDER BY day_sum ASC LIMIT 1;
CREATE TEMPORARY VIEW lowestDayTransaction AS SELECT t.transaction_id, l.day FROM transaction t JOIN lowestday l ON t.time::date = l.day;
SELECT td.fk_dish, COUNT(*) AS usage_count FROM transactiondish td JOIN lowestDayTransaction ldt ON td.fk_transaction = ldt.transaction_id GROUP BY td.fk_dish ORDER BY usage_count DESC LIMIT 1;
DROP view lowestDay,lowestDayTransaction;

-- Customer that spent the most 4
SELECT fk_customer, SUM(cost)
FROM transaction
GROUP BY fk_customer
ORDER BY SUM DESC;

-- Employee transaction count 5
SELECT fk_employee, COUNT(transaction_id)
FROM transaction
GROUP BY fk_employee;

--Transaction per day 6
SELECT time::date AS day, COUNT(*) FROM transaction GROUP BY day ORDER by day;

--Money made per day 7
SELECT time::date AS day, SUM(cost) FROM transaction GROUP BY day ORDER by day;

--Best Week 8
SELECT time::date AS week, SUM(cost) AS week_sum FROM transaction GROUP BY week ORDER BY week_sum DESC LIMIT 1;

--52 Weeks of sales history: select count of orders grouped by week 9
SELECT DATE_TRUNC('week', time) AS weeks, COUNT(*) FROM transaction GROUP BY weeks ORDER BY weeks;

--Realistic sales history: select count of orders, sum of order total grouped by hour 10
SELECT time::date AS hour, SUM(cost) AS orders_sum, COUNT(*) AS orders_count FROM transaction GROUP BY hour ORDER by hour;

--Peak 2 days 11
SELECT DATE_TRUNC('day', time) AS date, SUM(cost) as day_sum FROM transaction GROUP BY date ORDER BY day_sum DESC LIMIT 2;
OR
SELECT time::date AS day, SUM(cost) AS day_sum FROM transaction GROUP BY day ORDER BY day_sum DESC LIMIT 2;
	
--Worst Day 12
SELECT date, day_sum FROM (
	SELECT DATE(time) AS date, SUM(cost) AS day_sum
	FROM transaction 
	GROUP BY DATE(time)
	) AS day_sums
ORDER BY day_sum DESC LIMIT 1;

--Worst Week 13
SELECT date, week_sum FROM (
	SELECT DATE_TRUNC('week', time) AS date, SUM(cost) AS week_sum
	FROM transaction 
	GROUP BY DATE_TRUNC('week', time)
	) AS week_sums
ORDER BY week_sum DESC LIMIT 1;

-- Smallest Transaction Per Day 14
SELECT * FROM transaction WHERE cost = (SELECT MIN(cost) FROM transaction) ORDER BY time ASC;

-- Inventory items per menu item (dish) 15
SELECT dish.name, COUNT(dishinventory.fk_dish) AS ingredient_count
FROM dish
JOIN dishinventory
ON dishinventory.fk_dish = dish.dish_id
GROUP BY dish.name;

-- Number of times each Dish was ordered (over the entire database) 16
SELECT dish.name, COUNT(*)
FROM transactiondish
JOIN dish on transactiondish.FK_dish = dish.dish_id
GROUP BY dish.name;

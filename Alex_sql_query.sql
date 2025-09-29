-- Bao based income
SELECT SUM(cost) from transaction WHERE fk_customer=14 OR fk_customer=15;

-- Employee with most profit
SELECT fk_employee, SUM(cost) from transaction GROUP BY fk_employee ORDER BY sum DESC;

-- Worst Day Best Dish
CREATE TEMPORARY VIEW lowestDay AS SELECT time::date AS day, SUM(cost) AS day_sum FROM transaction GROUP BY day ORDER BY day_sum ASC LIMIT 1;
CREATE TEMPORARY VIEW lowestDayTransaction AS SELECT t.transaction_id, l.day FROM transaction t JOIN lowestday l ON t.time::date = l.day;
SELECT td.fk_dish, COUNT(*) AS usage_count FROM transactiondish td JOIN lowestDayTransaction ldt ON td.fk_transaction = ldt.transaction_id GROUP BY td.fk_dish ORDER BY usage_count DESC LIMIT 1;
DROP view lowestDay,lowestDayTransaction;

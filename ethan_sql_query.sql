--52 Weeks of sales history: select count of orders grouped by week
SELECT DATE_TRUNC('week', time) AS weeks, COUNT(*) FROM transaction GROUP BY weeks ORDER BY weeks;

--Realistic sales history: select count of orders, sum of order total grouped by hour
SELECT time::date AS hour, SUM(cost) AS orders_sum, COUNT(*) AS orders_count FROM transaction GROUP BY hour ORDER by hour;

--Peak 2 days
SELECT DATE_TRUNC('day', time) AS date, SUM(cost) as day_sum FROM transaction GROUP BY date ORDER BY day_sum DESC LIMIT 2;
OR
SELECT time::date AS day, SUM(cost) AS day_sum FROM transaction GROUP BY day ORDER BY day_sum DESC LIMIT 2;
	
--Worst Day
SELECT date, day_sum FROM (
	SELECT DATE(time) AS date, SUM(cost) AS day_sum
	FROM transaction 
	GROUP BY DATE(time)
	) AS day_sums
ORDER BY day_sum DESC LIMIT 1;

--Worst Week
SELECT date, week_sum FROM (
	SELECT DATE_TRUNC('week', time) AS date, SUM(cost) AS week_sum
	FROM transaction 
	GROUP BY DATE_TRUNC('week', time)
	) AS week_sums
ORDER BY week_sum DESC LIMIT 1;

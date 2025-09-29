Transaction per day
SELECT time::date AS day, COUNT(*) FROM transaction GROUP BY day ORDER by day;

Money made per day
SELECT time::date AS day, SUM(cost) FROM transaction GROUP BY day ORDER by day;

Best Week
SELECT time::date AS week, SUM(cost) AS week_sum FROM transaction GROUP BY week ORDER BY week_sum DESC LIMIT 1;


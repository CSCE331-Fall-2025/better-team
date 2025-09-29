-- Customer that spent the most
SELECT fk_customer, SUM(cost)
FROM transaction
GROUP BY fk_customer
ORDER BY SUM DESC;

-- Employee transaction count
SELECT fk_employee, COUNT(transaction_id)
FROM transaction
GROUP BY fk_employee;

-- Read-only checks. Review exceptions against actual business documents; this script does not change balances.
USE bee_farm;
-- 1. Batch balances below zero, or above the originally bottled quantity.
SELECT p.id, p.product_batch_no, p.product_name, p.bottle_count,
       COALESCE(s.balance,0) AS batch_balance
FROM biz_product_batch p
LEFT JOIN (SELECT batch_id,SUM(CASE WHEN biz_type IN (2,4) THEN -ABS(quantity) ELSE ABS(quantity) END) balance
           FROM biz_stock_record GROUP BY batch_id) s ON s.batch_id=p.id
WHERE p.deleted=0 AND (COALESCE(s.balance,0)<0 OR COALESCE(s.balance,0)>p.bottle_count);
-- 2. Inventory ledger does not match all stock movements.
SELECT i.id,i.item_name,i.spec,i.stock,COALESCE(s.balance,0) AS movement_balance
FROM biz_inventory i
LEFT JOIN (SELECT item_id,SUM(CASE WHEN biz_type IN (2,4) THEN -ABS(quantity) ELSE ABS(quantity) END) balance
           FROM biz_stock_record GROUP BY item_id) s ON s.item_id=i.id
WHERE i.deleted=0 AND i.stock<>COALESCE(s.balance,0);
-- 3. Historical negative after-balances: reconcile timestamps and source documents.
SELECT id,item_id,batch_id,biz_type,quantity,stock_after,create_time FROM biz_stock_record WHERE stock_after<0;
-- 4. Sales dated before the referenced product was produced.
SELECT o.outbound_no,o.outbound_date,p.product_batch_no,p.produce_date
FROM biz_outbound o JOIN biz_outbound_item oi ON oi.outbound_id=o.id
JOIN biz_product_batch p ON p.id=oi.product_batch_id
WHERE o.deleted=0 AND o.status=1 AND o.outbound_date<p.produce_date;

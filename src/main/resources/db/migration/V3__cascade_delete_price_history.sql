ALTER TABLE price_history
DROP CONSTRAINT price_history_product_id_fkey,
    ADD CONSTRAINT price_history_product_id_fkey
        FOREIGN KEY (product_id)
        REFERENCES product(id)
        ON DELETE CASCADE;
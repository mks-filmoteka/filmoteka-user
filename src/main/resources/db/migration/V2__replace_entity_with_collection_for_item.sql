DROP TABLE IF EXISTS film_list_item;

CREATE TABLE list_item
(
    film_list_id BIGINT NOT NULL,
    film_id      BIGINT NOT NULL,

    CONSTRAINT pk_list_item PRIMARY KEY (film_list_id, film_id),

    CONSTRAINT list_item_film_list_fk FOREIGN KEY (film_list_id) REFERENCES film_list (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_list_item_film_id ON list_item (film_id);

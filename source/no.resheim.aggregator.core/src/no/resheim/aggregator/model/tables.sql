CREATE TABLE categories (
		uuid CHAR(36) NOT NULL,
		parent_uuid CHAR(36),
        title VARCHAR(32) NOT NULL,
		marks VARCHAR(64) NOT NULL		
	);

CREATE TABLE feeds (
        uuid CHAR(36) NOT NULL,
		parent_uuid CHAR(36) NOT NULL,
		title VARCHAR(32) NOT NULL,
		user_title VARCHAR(32) NOT NULL,
		url VARCHAR(256) NOT NULL,
		marks VARCHAR(64) NOT NULL,		
		archiving VARCHAR(32) NOT NULL,			
		archiving_items INTEGER NOT NULL,
		archiving_days INTEGER NOT NULL,
		update_interval INTEGER NOT NULL,
		update_period VARCHAR(32) NOT NULL,
		last_update BIGINT NOT NULL,
		description LONG VARCHAR,
		link VARCHAR(256),
		webmaster VARCHAR(64),
		editor VARCHAR(64),
		copyright VARCHAR(64),
		feed_type VARCHAR(32) 
	);

CREATE TABLE articles (
        uuid CHAR(36) NOT NULL,
		parent_uuid CHAR(36) NOT NULL,
		feed_uuid CHAR(36) NOT NULL,
		guid VARCHAR(256) NOT NULL,
		title VARCHAR(128) NOT NULL,
		url VARCHAR(128) NOT NULL,
		marks VARCHAR(64) NOT NULL,		
		is_read INT NOT NULL,
	    publication_date BIGINT NOT NULL,
		read_date BIGINT NOT NULL,
		added_date BIGINT NOT NULL,
		description LONG VARCHAR,
		creator VARCHAR(128)
	);

/* Selection, tree browsing */
CREATE INDEX categories_parent ON categories (parent_uuid,uuid);
CREATE INDEX feeds_parent ON feeds (parent_uuid,uuid);
CREATE INDEX articles_parent ON articles (parent_uuid,uuid);

/* Unique index */
CREATE INDEX categories_id ON categories (uuid);
CREATE INDEX feeds_id ON feeds (uuid);
CREATE INDEX articles_id ON articles (uuid);

/* Deletion/cleaning up */
CREATE INDEX articles_feed ON articles (feed_uuid, uuid);
CREATE INDEX articles_added ON articles (feed_uuid, added_date DESC, uuid);
CREATE INDEX articles_published ON articles (feed_uuid, publication_date DESC, uuid);


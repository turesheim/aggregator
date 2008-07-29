CREATE TABLE folders (
		uuid CHAR(36) NOT NULL PRIMARY KEY,
		parent_uuid CHAR(36),
		ordering INTEGER NOT NULL,
		hidden INT NOT NULL,
        title VARCHAR(256) NOT NULL,
		marks VARCHAR(64) NOT NULL,
		FOREIGN KEY (parent_uuid) references folders (uuid) ON DELETE CASCADE
	);

CREATE TABLE feeds (
        uuid CHAR(36) NOT NULL PRIMARY KEY,
		parent_uuid CHAR(36) NOT NULL,
		ordering INTEGER NOT NULL,
		title VARCHAR(256) NOT NULL,
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
		feed_type VARCHAR(32),
		hidden INT NOT NULL,
		username VARCHAR(64),
		password VARCHAR(64),
		threaded INT,
		FOREIGN KEY (parent_uuid) references folders (uuid) ON DELETE CASCADE
	);

CREATE TABLE articles (
        uuid CHAR(36) NOT NULL PRIMARY KEY,
		parent_uuid CHAR(36) NOT NULL,
		ordering INTEGER NOT NULL,
		feed_uuid CHAR(36) NOT NULL,
		guid VARCHAR(256) NOT NULL,
		title VARCHAR(256) NOT NULL,
		url VARCHAR(128) NOT NULL,
		marks VARCHAR(64) NOT NULL,		
		is_read INT NOT NULL,
	    publication_date BIGINT NOT NULL,
		read_date BIGINT NOT NULL,
		added_date BIGINT NOT NULL,
		description LONG VARCHAR,
		creator VARCHAR(128),
		FOREIGN KEY (parent_uuid) references feeds (uuid) ON DELETE CASCADE
	);


/* Selection*/
CREATE INDEX folders_parent ON folders (parent_uuid,uuid);
CREATE INDEX feeds_parent ON feeds (parent_uuid,uuid);
CREATE INDEX feeds_url ON feeds (url,uuid);
CREATE INDEX articles_parent ON articles (parent_uuid,uuid);
CREATE INDEX articles_guid ON articles (guid,uuid);

/* Virtual tree browsing */
CREATE INDEX folders_tree ON folders (parent_uuid,ordering,uuid);
CREATE INDEX feeds_tree ON feeds (parent_uuid,ordering,uuid);
CREATE INDEX articles_tree ON articles (parent_uuid,ordering,uuid);

/* Unique index */
CREATE INDEX folders_id ON folders (uuid);
CREATE INDEX feeds_id ON feeds (uuid);
CREATE INDEX articles_id ON articles (uuid);

/* Finding read articles */
CREATE INDEX articles_old ON articles (feed_uuid,is_read);

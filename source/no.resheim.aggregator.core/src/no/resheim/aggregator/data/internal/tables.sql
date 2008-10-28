CREATE TABLE folders (
		uuid CHAR(36) NOT NULL PRIMARY KEY,
		parent_uuid CHAR(36),
		ordering INTEGER NOT NULL,
		feed_uuid CHAR(36),
		hidden INT NOT NULL,
        title VARCHAR(256) NOT NULL,
		marking VARCHAR(32) NOT NULL,
		flags VARCHAR(128) NOT NULL,
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
		marking VARCHAR(32) NOT NULL,		
		flags VARCHAR(128) NOT NULL,
		is_read INT NOT NULL,
	    publication_date BIGINT NOT NULL,
		read_date BIGINT NOT NULL,
		added_date BIGINT NOT NULL,
		description LONG VARCHAR,
		creator VARCHAR(128),
		media_player_url VARCHAR(128),
		media_enclosure_url VARCHAR(128),
		media_enclosure_duration VARCHAR(128),
		media_enclosure_type VARCHAR(128),
		FOREIGN KEY (parent_uuid) references folders (uuid) ON DELETE CASCADE
	);

CREATE TABLE feeds (
        uuid CHAR(36) NOT NULL PRIMARY KEY,
		location CHAR(36) NOT NULL,
		title VARCHAR(256) NOT NULL,
		url VARCHAR(256) NOT NULL,
		archiving VARCHAR(32) NOT NULL,			
		archiving_items INTEGER NOT NULL,
		archiving_days INTEGER NOT NULL,
		update_interval INTEGER NOT NULL,
		update_period VARCHAR(32) NOT NULL,
		last_update BIGINT NOT NULL,
		description LONG VARCHAR,
		link VARCHAR(256),
		webmaster VARCHAR(256),
		editor VARCHAR(256),
		copyright VARCHAR(256),
		feed_type VARCHAR(32),
		hidden INT NOT NULL,
		anonymous_access INT NOT NULL,
		image_data VARCHAR(10240)
	);

/* Selection*/
CREATE INDEX folders_parent ON folders (parent_uuid,uuid);
CREATE INDEX feeds_url ON feeds (url,uuid);
CREATE INDEX articles_parent ON articles (parent_uuid,uuid);
CREATE INDEX articles_guid ON articles (guid,uuid);

/* Virtual tree browsing */
CREATE INDEX folders_tree ON folders (parent_uuid,ordering,uuid);
CREATE INDEX articles_tree ON articles (parent_uuid,ordering,uuid);

/* Finding read articles */
CREATE INDEX articles_old ON articles (feed_uuid,is_read);

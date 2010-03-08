/* Labels are used to group articles */
CREATE TABLE labels (
		/* The labelr identifier */
		uuid CHAR(36) NOT NULL PRIMARY KEY,
		/* The label parent identifier */
		parent_uuid CHAR(36),
		/* The title of the label */
        title VARCHAR(256) NOT NULL
);

/* Folders hold other folders and articles */
CREATE TABLE folders (
		/* The folder identifier */
		uuid CHAR(36) NOT NULL PRIMARY KEY,
		/* The folder parent */
		parent_uuid CHAR(36),
		subscription_uuid CHAR(36),
		hidden INT NOT NULL,
		/* The title of the folder */
        title VARCHAR(256) NOT NULL,
		/* System flags */
		flags VARCHAR(128) NOT NULL,
		FOREIGN KEY (parent_uuid) references folders (uuid) ON DELETE CASCADE
	);

CREATE TABLE articles (
		/* Unique identifier */
        uuid CHAR(36) NOT NULL PRIMARY KEY,
        /* Unique identifier of the parent node */
		parent_uuid CHAR(36) NOT NULL,
		/** Unique identifier of the feed creating the item */
		subscription_uuid CHAR(36) NOT NULL,
		/* Globally unique identifier */
		guid VARCHAR(256) NOT NULL,
		/* Title of the item */
		title VARCHAR(256) NOT NULL,
		/* URL of the original publication */
		url VARCHAR(256) NOT NULL,
		/* System flags */
		flags VARCHAR(128) NOT NULL,
		/* Comma separated list of labels */
		labels VARCHAR(256) NOT NULL,
		/* Whether or not the article has been read */
		is_read INT NOT NULL,
		/* Publication date */
	    publication_date BIGINT NOT NULL,
	    /* The date the article was read */
		read_date BIGINT NOT NULL,
		/* Date when added to the collection */
		added_date BIGINT NOT NULL,
		/* The article content */
		description CLOB,
		/* The name of the author ? */
		creator VARCHAR(128),
		/* ? */
		media_player VARCHAR(128),
		/* Date of the last (local) change */
		last_changed BIGINT NOT NULL,
		/* Whether or not the item is starred */
		starred INT NOT NULL,
		FOREIGN KEY (parent_uuid) references folders (uuid) ON DELETE CASCADE
	);

/* Holds media:content and enclosure elements 
	ordering
*/
CREATE TABLE media_content (
		ordering INT NOT NULL,
		/* The article the content belongs to */
		article_uuid CHAR(36) NOT NULL,
		/* URL of the content */
		content_url VARCHAR(128),
		/* URL of the thumbnail */
		thumbnail_url VARCHAR(128),
		/* Content MIME type */
		content_type VARCHAR(128),
		filesize BIGINT,
		medium VARCHAR(32),
		is_default INT NOT NULL,
		expression VARCHAR(32),
		bitrate INT,
		framerate INT,
		samplingrate INT,
		channels INT, 
		duration INT,
		height VARCHAR(32),
		width VARCHAR(32),
		lang VARCHAR(32),
		player_url VARCHAR(128),		
		/* Reference the article */
		FOREIGN KEY (article_uuid) references articles (uuid) ON DELETE CASCADE
	);

/* Holds article notes */
CREATE TABLE notes (
		/* The id of the note */
		uuid CHAR(36) NOT NULL PRIMARY KEY,
		/* The id of the article */
		article_uuid CHAR(36),
		/* The note text */
		notes CLOB,
		/* Reference the article */
		FOREIGN KEY (article_uuid) references articles (uuid) ON DELETE CASCADE
	);

CREATE TABLE subscriptions (
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
		keep_unread INT NOT NULL,
		image_data VARCHAR(16384),
		/* The synchronizer to use (extension) */
		synchronizer VARCHAR(128)
	);

/** A filter with a title */	
CREATE TABLE filters (
		uuid CHAR(36) NOT NULL PRIMARY KEY,
		title VARCHAR(256) NOT NULL,
		match_all INT NOT NULL,
		manual INT NOT NULL
	);

/** A criteria for the filter */  
CREATE TABLE filter_criteria (
		uuid CHAR(36) NOT NULL PRIMARY KEY,
		filter_uuid CHAR(36) NOT NULL,
		field VARCHAR(32),
		operator VARCHAR(32),
		value_match VARCHAR(128),
		FOREIGN KEY (filter_uuid) references filters (uuid) ON DELETE CASCADE
	);
	
/* Ties filters and folders together */	
CREATE TABLE filter_folders (
		folder_uuid CHAR(36) NOT NULL,
		filter_uuid CHAR(36) NOT NULL,
		FOREIGN KEY (folder_uuid) references folders (uuid) ON DELETE CASCADE,
		FOREIGN KEY (filter_uuid) references filters (uuid) ON DELETE CASCADE
	);

/* A filter action to perform */	
CREATE TABLE filter_action (
		uuid CHAR(36) NOT NULL PRIMARY KEY,
		filter_uuid CHAR(36) NOT NULL,
		operator VARCHAR(32),
		operand VARCHAR(32),
		FOREIGN KEY (filter_uuid) references filters (uuid) ON DELETE CASCADE
	);

CREATE INDEX labels on labels (title);
/* Selection*/
CREATE INDEX feeds_url ON subscriptions (url, uuid);
/* Article list display per date */
CREATE UNIQUE INDEX articles ON articles (parent_uuid, publication_date DESC, uuid);
/* Folder tree browsing */
CREATE INDEX folders ON folders (parent_uuid, title);

CREATE UNIQUE INDEX media_content ON media_content (ordering, article_uuid);
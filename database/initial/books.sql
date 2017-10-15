-- TAG
DROP TABLE IF EXISTS `tag`;


CREATE TABLE `tag` (
  `tag_id`  int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL UNIQUE,
  `data` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `tag` WRITE;

INSERT INTO `tag` VALUES (1, "sci-fi", "Science Fiction & Fantasy" );
INSERT INTO `tag` VALUES (2, "e-book", "Electronic books" );

UNLOCK TABLES;



-- User Book

DROP TABLE IF EXISTS `userbook`;

CREATE TABLE `userbook` (
  `user_book_id`  int(11) NOT NULL AUTO_INCREMENT,
  `user_id`  int(11) NOT NULL,
  `book_id`  int(11) NOT NULL,
  `rating` tinyint(1) DEFAULT NULL,
  `data` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`user_book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE userbook ADD CONSTRAINT unique_book UNIQUE(user_id, book_id);


-- TAG MAP
DROP TABLE IF EXISTS `tagmapping`;
 

CREATE TABLE `tagmapping` (
  `user_book_id`  int(11) NOT NULL,
  `tag_id`  int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




-- USER

DROP TABLE IF EXISTS `user`;


CREATE TABLE `user` (
  `user_id`  int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL UNIQUE,
  `user_group` varchar(255) DEFAULT NULL,
  `data` varchar(2048) DEFAULT NULL,
  `password` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE user ADD CONSTRAINT unique_user UNIQUE(name);

LOCK TABLES `user` WRITE;

-- Insert initial admin/admin user

INSERT INTO `user` VALUES (1, 'admin', 'admin', '', 'ZEiRIzMMip7aN7nRSbLZLRaCUzIjIWwc');

UNLOCK TABLES;



--
-- Table structure for table `author`
--


DROP TABLE IF EXISTS `author`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `author` (
  `author_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL UNIQUE,
  `birth_date` varchar(50) DEFAULT NULL,
  `subjects` varchar(2000) DEFAULT NULL,
  `image_small` varchar(250) DEFAULT NULL,
  `image_medium` varchar(250) DEFAULT NULL,
  `image_large` varchar(250) DEFAULT NULL,
  `ol_key` char(100) DEFAULT NULL,
  PRIMARY KEY (`author_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

ALTER TABLE author ADD CONSTRAINT unique_author UNIQUE(name);


/*!40101 SET character_set_client = @saved_cs_client */;


 

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `book` (
  `book_id` int(11) NOT NULL AUTO_INCREMENT,
  `author_id` int(11) NOT NULL DEFAULT '0',
  `year` char(11) DEFAULT NULL,
  `title` char(100) DEFAULT NULL,
  `isbn` varchar(1200) DEFAULT NULL,
  `subjects` varchar(2000) DEFAULT NULL,
  `ol_works` char(100) DEFAULT NULL,
  `description` varchar(3000) DEFAULT NULL,
  `image_small` varchar(1000) DEFAULT NULL,
  `image_medium` varchar(1000) DEFAULT NULL,
  `image_large` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`book_id`)
) ENGINE=MyISAM AUTO_INCREMENT=808 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

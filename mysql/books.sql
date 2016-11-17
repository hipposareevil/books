DROP TABLE IF EXISTS `author`;

CREATE TABLE `author` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL,
  `image_url` varchar(400) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE (name)
) ENGINE=MyISAM AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;


LOCK TABLES `author` WRITE;


INSERT INTO `author` VALUES (1, 'Isaac Asimov', ''),(2,'J.R.R. Tolkien',''),(3,'Piers Anthony',''),(4,'Neil Gaiman','');

UNLOCK TABLES;

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
  `data` varchar(2048) DEFAULT NULL,
  `password` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

ALTER TABLE user ADD CONSTRAINT unique_user UNIQUE(name);

LOCK TABLES `user` WRITE;

-- Insert initial admin/admin user

INSERT INTO `user` VALUES (1, 'admin', '', 'ZEiRIzMMip7aN7nRSbLZLRaCUzIjIWwc');

UNLOCK TABLES;



--
-- Table structure for table `author`
--


DROP TABLE IF EXISTS `author`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `author` (
  `author_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(40) DEFAULT NULL,
  `image_url` varchar(400) DEFAULT NULL,
  `searched_image` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`author_id`)
) ENGINE=MyISAM AUTO_INCREMENT=365 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `author`
--

LOCK TABLES `author` WRITE;
/*!40000 ALTER TABLE `author` DISABLE KEYS */;
INSERT INTO `author` VALUES (1,'Isaac Asimov','1.jpg',1),(2,'J.R.R. Tolkien','2.jpg',1),(4,'Piers Anthony','4.jpg',1),(6,'Neil Gaiman','genericAvatar.png',1),(7,'Carl Sagan','genericAvatar.png',0),(8,'Philip K Dick','8.jpg',1),(14,'Paul Parker','genericAvatar.png',0),(15,'William Gibson','genericAvatar.png',0),(16,'Bruce Campbell','16.jpg',1),(17,'Dan Brown','genericAvatar.png',0),(18,'Douglas Adams','genericAvatar.png',0),(19,'Stephen King','19.jpg',1),(20,'Michael Strong','genericAvatar.png',0),(21,'John Lofty','genericAvatar.png',0),(22,'Bob Burns','genericAvatar.png',0),(23,'Darran Wells','genericAvatar.png',0),(24,'Clyde Soles','genericAvatar.png',0),(25,'Andy Selters','genericAvatar.png',0),(26,'Bjorn Kjellstrom','genericAvatar.png',0),(27,'Alastair Reynolds','genericAvatar.png',0),(28,'Brian Greene','genericAvatar.png',0),(29,'Charles Mann','genericAvatar.png',0),(30,'David Fromkin','genericAvatar.png',0),(31,'Michael Pollan','genericAvatar.png',0),(32,'Burton Malkiel','genericAvatar.png',0),(33,'Richard Feynman','33.jpg',1),(34,'Tony Horwitz','genericAvatar.png',0),(35,'Jared Diamond','genericAvatar.png',0),(36,'S.M. Stirling','genericAvatar.png',0),(37,'Ray Bradbury','genericAvatar.png',0),(38,'Susanna Clarke','38.jpg',1),(39,'Jeanne Duprau','genericAvatar.png',0),(41,'Peter Nelson','genericAvatar.png',0),(42,'Maurice Barkley','genericAvatar.png',0),(43,'Philip Varney','genericAvatar.png',0),(44,'Ben Bova','genericAvatar.png',0),(45,'Arthur Clarke','genericAvatar.png',0),(46,'Robert Asprin','46.jpg',1),(48,'Coffee Table','genericAvatar.png',0),(49,'Berke Breathed','genericAvatar.png',0),(50,'Colors Staff','genericAvatar.png',0),(51,'D. Plowden','genericAvatar.png',0),(52,'James Startt','genericAvatar.png',0),(53,'Martin Kemp','genericAvatar.png',0),(55,'Lena Lencek','genericAvatar.png',0),(56,'Jon Krakauer','genericAvatar.png',0),(69,'R.A. Salvatore','69.jpg',1),(59,'Darby Conley','genericAvatar.png',0),(60,'Art Wolfe','genericAvatar.png',0),(61,'Steve McCurry','genericAvatar.png',0),(62,'Shel Silverstein','genericAvatar.png',0),(63,'Carol Berg','63.jpg',1),(64,'Gary Gygax','64.jpg',1),(68,'Orson Scott Card','genericAvatar.png',0),(70,'David Brin','genericAvatar.png',0),(71,'Greg Bear','genericAvatar.png',0),(72,'Poul Anderson','genericAvatar.png',0),(73,'Martin Caidin','genericAvatar.png',0),(74,'Jack Chalker','genericAvatar.png',0),(75,'Michael Capobianco','genericAvatar.png',0),(76,'C.J. Cherryh','genericAvatar.png',0),(77,'Gordon Dickson','genericAvatar.png',0),(78,'David Drake','genericAvatar.png',0),(79,'Robert Forward','genericAvatar.png',0),(83,'Ward Hawkins','genericAvatar.png',0),(81,'Lester Rey','genericAvatar.png',0),(82,'Alan Foster','genericAvatar.png',0),(84,'Lyndon Hardy','genericAvatar.png',0),(85,'Richard Matheson','genericAvatar.png',0),(86,'C.S. Friedman','86.jpg',1),(87,'Michael Mccollum','genericAvatar.png',0),(88,'C.S. Lewis','88.jpg',1),(89,'Ursula K Le Guin','89.jpg',1),(90,'Robert Heinlein','90.jpg',1),(91,'Joel Rosenberg','genericAvatar.png',0),(92,'Kim Robinson','genericAvatar.png',0),(93,'Larry Niven','genericAvatar.png',0),(95,'Carl Sherrell','genericAvatar.png',0),(96,'Robert Silverberg','96.jpg',1),(97,'Dan Simmons','genericAvatar.png',0),(98,'John Steakley','genericAvatar.png',0),(99,'Christopher Stasheff','genericAvatar.png',0),(100,'Roger Zelazny','genericAvatar.png',0),(101,'Margaret Weis','101.jpg',1),(102,'Erik Larson','genericAvatar.png',0),(103,'Charles Frazier','genericAvatar.png',0),(104,'Mark Kurlansky','genericAvatar.png',0),(105,'Cormac McCarthy','genericAvatar.png',0),(106,'George Martin','genericAvatar.png',0),(107,'Joe Haldeman','107.jpg',1),(108,'Robert Reed','genericAvatar.png',0),(109,'Darrell Schweitzer','genericAvatar.png',0),(110,'Lawrence Watt-Evans','genericAvatar.png',0),(111,'Patrick Tilley','genericAvatar.png',0),(112,'Fred Saberhagen','genericAvatar.png',0),(113,'Allen Steele','genericAvatar.png',0),(114,'Martha Wells','genericAvatar.png',0),(115,'Madeleine LEngle','genericAvatar.png',0),(116,'David Zindell','genericAvatar.png',0),(117,'Dave Wolverton','genericAvatar.png',0),(118,'Jack Williamson','genericAvatar.png',0),(119,'University Chicago','genericAvatar.png',0),(120,'Philip Herbst','genericAvatar.png',0),(121,'Frederick Forsyth','genericAvatar.png',0),(122,'Bill Bryson','122.jpg',1),(123,'Jackie Chan','genericAvatar.png',0),(124,'Anthony Swofford','genericAvatar.png',0),(125,'Dr. Seuss','genericAvatar.png',0),(126,'Mark Twain','genericAvatar.png',0),(127,'John Irving','genericAvatar.png',0),(128,'Clifford Stoll','genericAvatar.png',0),(129,'Edward Abbey','129.jpg',1),(130,'Apuleius','genericAvatar.png',0),(131,' Plautus','genericAvatar.png',0),(132,' Juvenal','genericAvatar.png',0),(133,'Titus Livius','genericAvatar.png',0),(134,' Virgil','genericAvatar.png',0),(135,'Richard Kieckhefer','genericAvatar.png',0),(136,'Charles Leland','genericAvatar.png',0),(137,'Richard Cavendish','genericAvatar.png',0),(138,' Lucretius','genericAvatar.png',0),(139,' Tacitus','genericAvatar.png',0),(140,' Seneca','genericAvatar.png',0),(141,'Karl Christ','genericAvatar.png',0),(142,'Georg Luck','genericAvatar.png',0),(144,'Mark Twight','genericAvatar.png',0),(145,'Joe Tasker','genericAvatar.png',0),(146,'William Sullivan','genericAvatar.png',0),(147,'Art Boericke','genericAvatar.png',0),(148,'Robert Stern','genericAvatar.png',0),(149,'Tom Till','genericAvatar.png',0),(150,'Steve Terrill','genericAvatar.png',0),(151,'Yann Arthus-Bertrand','genericAvatar.png',0),(152,'James Holloway','genericAvatar.png',0),(153,'Andy Goldsworthy','genericAvatar.png',0),(154,'Maurice Sendak','genericAvatar.png',0),(155,'Arnold Lobel','genericAvatar.png',0),(156,'Olivier Follmi','genericAvatar.png',0),(157,'Leah Bendavid-Val','genericAvatar.png',0),(158,'Eleanor Harz','genericAvatar.png',0),(159,'Jim Lovell','genericAvatar.png',0),(160,'Tom Wolfe','genericAvatar.png',0),(161,'James Michener','genericAvatar.png',0),(162,'Milton Thompson','genericAvatar.png',0),(163,'Craig Ryan','genericAvatar.png',0),(164,'Alan Shepard','genericAvatar.png',0),(165,'Tom Grimm','genericAvatar.png',0),(166,'John Hedgecoe','genericAvatar.png',0),(167,'David Logan','genericAvatar.png',0),(168,'John Schaefer','genericAvatar.png',0),(169,'Alan Dressler','genericAvatar.png',0),(170,'Richard Westfall','genericAvatar.png',0),(171,'Thomas Kuhn','genericAvatar.png',0),(172,'Galileo Galilei','172.jpg',1),(173,'Paula Korn','genericAvatar.png',0),(174,'Rene Descartes','genericAvatar.png',0),(175,'Timothy Ferris','genericAvatar.png',0),(176,'James Gleick','genericAvatar.png',0),(177,'Edwin Abbott','genericAvatar.png',0),(178,'Stephen Hawking','genericAvatar.png',0),(179,'Kip Thorne','genericAvatar.png',0),(180,'Stephen Ambrose','genericAvatar.png',0),(181,'Brian Skinner','genericAvatar.png',0),(182,'Kenneth Hamblin','genericAvatar.png',0),(183,'Bernard Pipkin','genericAvatar.png',0),(184,'Eric Chaisson','genericAvatar.png',0),(185,'William Kenneth','genericAvatar.png',0),(186,'Andrew Chaikin','genericAvatar.png',0),(187,'Tom Clancy','187.jpg',1),(188,'Richard Preston','genericAvatar.png',0),(189,'Michael Crichton','genericAvatar.png',0),(190,'Gary Weir','genericAvatar.png',0),(191,'Tom Mangold','genericAvatar.png',0),(192,'Orr Kelly','genericAvatar.png',0),(193,'Charles Henderson','genericAvatar.png',0),(194,'Danny Coulson','genericAvatar.png',0),(195,'Mark Bowden','genericAvatar.png',0),(196,'John Keegan','genericAvatar.png',0),(197,'Bao Ninh','genericAvatar.png',0),(198,'Robert McMahon','genericAvatar.png',0),(199,'David Halberstam','genericAvatar.png',0),(200,'William Hartmann','genericAvatar.png',0),(201,'Nigel Calder','genericAvatar.png',0),(202,'Ron Miller','genericAvatar.png',0),(203,'James Chiles','genericAvatar.png',0),(204,'Nathaniel Philbrick','genericAvatar.png',0),(205,'James Bradley','genericAvatar.png',0),(206,'Peter Jenkins','genericAvatar.png',0),(207,'Paul Theroux','genericAvatar.png',0),(208,'Linda Greenlaw','genericAvatar.png',0),(209,'Bob Holtel','genericAvatar.png',0),(210,'Leslie Croot','genericAvatar.png',0),(211,'Michael McCoy','genericAvatar.png',0),(212,'Steve Butterman','genericAvatar.png',0),(213,'Tom Kirkendall','genericAvatar.png',0),(214,'Richard Lovett','genericAvatar.png',0),(215,'Joe Kurmaskie','genericAvatar.png',0),(216,'Roff Smith','genericAvatar.png',0),(217,'Evelyn McDaniel','genericAvatar.png',0),(218,'Barbara Savage','genericAvatar.png',0),(219,'Donna Lynn','genericAvatar.png',0),(220,'Sebastian Junger','genericAvatar.png',0),(221,'Laura Hillenbrand','genericAvatar.png',0),(222,'Timothy Egan','genericAvatar.png',0),(223,'Lynne Truss','genericAvatar.png',0),(340,'Emil Henry','genericAvatar.png',0),(226,'Malcolm Gladwell','genericAvatar.png',0),(227,'Gary Kinder','genericAvatar.png',0),(228,'Marc Reisner','genericAvatar.png',0),(229,'Roy Moxham','genericAvatar.png',0),(230,'Larry Habegger','genericAvatar.png',0),(231,'Fred Beckey','231.jpg',1),(232,'Joe Hyams','genericAvatar.png',0),(233,'Jeff Thomas','genericAvatar.png',0),(234,'Gaston Rebuffat','genericAvatar.png',0),(235,'John Long','genericAvatar.png',0),(236,'Yvon Chouinard','genericAvatar.png',0),(237,'Roger Frison-Roche','genericAvatar.png',0),(238,'James Ullman','genericAvatar.png',0),(239,'Mountain Eering','genericAvatar.png',0),(240,'Eliot Porter','genericAvatar.png',0),(241,'Harvey Manning','genericAvatar.png',0),(242,'Philip Hyde','genericAvatar.png',0),(243,'Dave Bingham','genericAvatar.png',0),(244,'Laird Davis','genericAvatar.png',0),(245,'Michael Benge','genericAvatar.png',0),(246,'Greg Barnes','genericAvatar.png',0),(247,'Thomas Hornbein','genericAvatar.png',0),(248,'David Whitelaw','genericAvatar.png',0),(249,'Eric Bjornstad','genericAvatar.png',0),(250,'John Zilly','genericAvatar.png',0),(251,'The Mountaineers','genericAvatar.png',0),(252,'Jill Fredston','genericAvatar.png',0),(253,'Derrick Lewis','genericAvatar.png',0),(254,'Jonathan Thesenga','genericAvatar.png',0),(255,'Jerry Cinnamon','genericAvatar.png',0),(256,'Greg Child','256.jpg',1),(257,'Goran Kropp','genericAvatar.png',0),(258,'Clint Willis','genericAvatar.png',0),(260,'David Breashears','genericAvatar.png',0),(261,'Bob Drury','genericAvatar.png',0),(262,'Joe Simpson','genericAvatar.png',0),(263,'Reinhold Messner','genericAvatar.png',0),(264,'Art Davidson','genericAvatar.png',0),(265,'Alfred Lansing','genericAvatar.png',0),(266,'Andy Kirkpatrick','genericAvatar.png',0),(267,'Nicholas Clinch','genericAvatar.png',0),(269,'Ricky Jay','genericAvatar.png',0),(270,'Jostein Gaarder','genericAvatar.png',0),(272,'Stieg Larsson','genericAvatar.png',0),(274,'Ian Tregillis','genericAvatar.png',0),(275,'Bradford Washburn','genericAvatar.png',0),(277,'Steve House','genericAvatar.png',0),(278,'David Ben','genericAvatar.png',0),(279,'Paolo Bacigalupi','genericAvatar.png',0),(281,'John Scalzi','genericAvatar.png',0),(282,'J.G. Ballard','genericAvatar.png',0),(283,'Michael Punke','genericAvatar.png',0),(284,'Aron Ralston','genericAvatar.png',0),(285,'James Tabor','genericAvatar.png',0),(286,'Kate Milford','genericAvatar.png',0),(288,'Robert Kurson','genericAvatar.png',0),(289,'Ed Viesturs','genericAvatar.png',0),(290,'Andrew X Pham','genericAvatar.png',0),(291,'David Grann','genericAvatar.png',0),(292,'Stieg Larsson','genericAvatar.png',0),(303,'Randy Pausch','genericAvatar.png',0),(338,'Mark Jenkins','genericAvatar.png',0),(339,'Patrick Rothfuss','genericAvatar.png',0),(298,'Max Brooks','genericAvatar.png',0),(337,'Suzanne Collins','genericAvatar.png',0),(297,'Eoin Colfer','genericAvatar.png',0),(304,'Lee Child','304.jpg',1),(305,'Ben Aaronovitch','305.jpg',1),(299,'Ranulph Fiennes','genericAvatar.png',0),(301,'J. North Conway','genericAvatar.png',0),(336,'Daniel O\'Malley','genericAvatar.png',0),(306,'Mike Shevdon','genericAvatar.png',0),(341,'Stuart Lutz','genericAvatar.png',0),(342,'Charles Duhigg','genericAvatar.png',0),(302,'Jim Nelson','genericAvatar.png',0),(308,'Morley Safer','genericAvatar.png',0),(309,'Robert L. O\'Connell','genericAvatar.png',0),(310,'Ian Frazier','genericAvatar.png',0),(311,'Jack Kerouac','genericAvatar.png',0),(312,'Henry David Thoreau','genericAvatar.png',0),(313,'John Kennedy Toole','genericAvatar.png',0),(314,'Sam Harris','genericAvatar.png',0),(315,'Upton Sinclair','genericAvatar.png',0),(316,'Mary Stewart','genericAvatar.png',0),(317,'Robin McKinley','genericAvatar.png',0),(318,'Gene Wolfe','genericAvatar.png',0),(319,'Frederick Law Olmsted','genericAvatar.png',0),(320,'Seth Grahame-Smith','genericAvatar.png',0),(321,'Studs Terkel','genericAvatar.png',0),(322,'Vernor Vinge','genericAvatar.png',0),(323,'Hiroshi Sakurazaka','genericAvatar.png',0),(324,'Edgar Rice Burroughs','genericAvatar.png',0),(325,'Karen Darke','genericAvatar.png',0),(326,'Joe Abercrombie','genericAvatar.png',0),(327,'Andreas Krase','genericAvatar.png',0),(328,'Frederick P. Brooks','genericAvatar.png',0),(329,'Nathacha Appanah','genericAvatar.png',0),(330,'Jim Steinmeyer','genericAvatar.png',0),(331,'William Rosen','genericAvatar.png',0),(332,'Guillermo Del Toro','genericAvatar.png',0),(333,'Caitlin R. Kiernan','genericAvatar.png',0),(334,'Paul Greenberg','genericAvatar.png',0),(335,'Edwin G. Burrows','genericAvatar.png',0),(343,'Patricia A. McKillip','genericAvatar.png',0),(344,'Mike Cary','genericAvatar.png',0),(345,'Justin Cronin','genericAvatar.png',0),(346,'Ken MacLeod','genericAvatar.png',0),(347,'David Mitchell','genericAvatar.png',0),(348,'Tyler Hamilton','genericAvatar.png',0),(349,'Jo Nesbo','genericAvatar.png',0),(350,'James S.A. Corey','genericAvatar.png',0),(351,'Harold Gatty','genericAvatar.png',0),(352,'Robert M. Edsel','genericAvatar.png',0),(353,'Colin Fletcher','genericAvatar.png',0),(354,'R.P. Ruggiero','genericAvatar.png',0),(355,'Hakan Nesser','genericAvatar.png',0),(356,'Howard Mittelmark','genericAvatar.png',0),(357,'Christopher Priest','genericAvatar.png',0),(358,'Harry Harrison','genericAvatar.png',0),(359,'Lois McMaster Bujold','359.jpg',1),(360,'David Muench','genericAvatar.png',0),(361,'David Stiles','genericAvatar.png',0),(362,'Boris Strugatsky','genericAvatar.png',0),(363,'James Howard Kunstler','genericAvatar.png',0),(364,'Peter Heller','genericAvatar.png',0);

/*!40000 ALTER TABLE `author` ENABLE KEYS */;
UNLOCK TABLES;

 

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `book` (
  `book_id` int(11) NOT NULL AUTO_INCREMENT,
  `author_id` int(11) NOT NULL DEFAULT '0',
  `isbn` char(50) DEFAULT NULL,
  `year` char(11) DEFAULT NULL,
  `title` char(100) DEFAULT NULL,
  `image_url` varchar(400) DEFAULT NULL,
  `searched_image` tinyint(1) DEFAULT '0',
  `metadata` varchar(15000) DEFAULT NULL,
  `shelf_id` int(11) DEFAULT NULL,
  `read_flag` tinyint(1) DEFAULT NULL,
  `rating` int(11) NOT NULL DEFAULT '0',
  `description` varchar(15000) DEFAULT NULL,
  PRIMARY KEY (`book_id`)
) ENGINE=MyISAM AUTO_INCREMENT=808 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book`
--

LOCK TABLES `book` WRITE;
/*!40000 ALTER TABLE `book` DISABLE KEYS */;
INSERT INTO `book` VALUES (1,6,'0380973634','1997.','Neverwhere','1.jpg',1,NULL,3,1,5,NULL),(2,6,'1886778388','2002.','Adventures in the dream trade','2.jpg',1,NULL,3,1,3,NULL),(15,4,'034530196X','1982.','Juxtaposition','15.jpg',1,NULL,3,1,5,NULL),(4,6,'0380973650','2001.','American gods : a novel','4.jpg',1,NULL,3,1,5,NULL),(5,6,'006051518X','2005.','Anansi boys','5.jpg',1,NULL,3,NULL,0,NULL),(14,4,'0962371211','1990.','Hard sell','14.jpg',1,NULL,2,1,3,NULL);
/*!40000 ALTER TABLE `book` ENABLE KEYS */;
UNLOCK TABLES;

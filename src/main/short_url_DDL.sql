/*
####环境:生产环境
*/
CREATE TABLE `link` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `long_url` varchar(500) DEFAULT NULL COMMENT '长链接',
  `short_url` varchar(255) DEFAULT NULL COMMENT '短链接',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;


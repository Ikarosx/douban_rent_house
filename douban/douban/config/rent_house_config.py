groupUrls = [
    # 福田租房
    'https://www.douban.com/group/futianzufang/discussion?start=0&type=new',
    # 深圳租房
    'https://www.douban.com/group/szsh/discussion?start=0&type=new',
    'https://www.douban.com/group/637628/discussion?start=0&type=new',
    # 南山租房
    'https://www.douban.com/group/nanshanzufang/discussion?start=0&type=new',
    'https://www.douban.com/group/591624/discussion?start=0&type=new',
    # 罗湖租房
    # 'https://www.douban.com/group/592828/discussion?start=0&type=new',
    # 宝安租房1
    'https://www.douban.com/group/baoanzufang/',
    # 宝安租房2
    'https://www.douban.com/group/luobao1haoxian/'
]

text = '''
CREATE TABLE IF NOT EXISTS `subway_site` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `site_pic` varchar(255) DEFAULT NULL COMMENT '车站图片',
    `site_introduced` varchar(255) DEFAULT NULL COMMENT '车站介绍',
    `site_code` varchar(255) DEFAULT NULL COMMENT '车站编码',
    `line_name` varchar(255) DEFAULT NULL COMMENT '线路名称',
    `oper_code` varchar(255) DEFAULT NULL COMMENT '运营编码',
    `line_code` varchar(255) DEFAULT NULL COMMENT '线路编码',
    `site_name` varchar(255) DEFAULT NULL COMMENT '车站名称',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

'''
# INSERT INTO `subway_site` (`id`, `site_pic`, `site_introduced`, `site_code`, `line_name`, `oper_code`, `line_code`, `site_name`) VALUES (1, 'http://www.szmc.net/styles/index/zdWeb/images/0602_b.jpg', '车站位于深圳市福田区上步中路与红荔路交叉口南侧，沿上步中路南北向布置，该站为地下三层岛式车站。', '264012', '6', '1261010000', '264', '通新岭');
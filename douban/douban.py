import json
import logging
import time
from datetime import datetime, timedelta
from fake_useragent import UserAgent
import pymysql
import requests
from bs4 import BeautifulSoup
from lxml import etree

ua = UserAgent()
log = logging.getLogger(__name__)
log.addHandler(logging.StreamHandler())
log.setLevel(logging.DEBUG)

class douban_spider:
    def __init__(self):
        self.proxy = None
        self.group_urls = [
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
        self.init_sql_pool()

    def start_spider(self):
        self.proxy = self.get_proxy()
        log.info(f'开始爬取{self.group_urls}')
        for url in self.group_urls:
            self.parse_index_data(url)

    def get_proxy(self):
        response = requests.get(
            'http://api.xiequ.cn/VAD/GetIp.aspx?act=get&uid=51539&vkey=0F4A5129E8AD784DD0A85E7725F00B03&num=1&time=30&plat=1&re=0&type=0&so=1&ow=1&spl=1&addr=&db=1')
        text = response.text
        if '白名单' in text:
            raise Exception('代理需要白名单')
        log.info(f'获取代理ip:{text}')
        return 'http://' + text.split('\r\n')[0]

    # 解析列表数据
    def parse_index_data(self, url):
        try:
            response = requests.get(url, headers=self.get_common_header(), proxies={'http': self.proxy, 'https': self.proxy}, timeout=3)
        except Exception as e:
            log.warning('请求失败', url)
            self.proxy = self.get_proxy()
            return self.parse_index_data(url)
        result = response.text
        if not self.is_valid_result(response):
            self.proxy = self.get_proxy()
            return self.parse_index_data(url)
        etree_result = etree.HTML(result)
        rows = etree_result.xpath('//table[@class="olt"]//tr[not(@class="th")]')
        for row in rows:
            self.parse_detail_data(row)

        # 判断最后回复时间是否已经超过一天
        lastReplyTime = rows[-1].xpath('td[4]/text()')[0]
        # 将字符串转换为datetime对象
        last_reply_datetime = datetime.strptime(f'{datetime.now().year}-{lastReplyTime}:00', '%Y-%m-%d %H:%M:%S')
        # 获取当前时间
        now = datetime.now()
        # 计算时间差
        time_diff = now - last_reply_datetime
        # 判断是否在1天以前
        if time_diff > timedelta(hours=3):
            log.warning("超过3小时，不继续爬取")
            return
        nextPage = etree_result.xpath('//a[text()="后页>"]/@href')[0]
        if nextPage:
            self.parse_index_data(nextPage)

    # 解析详细数据
    def parse_detail_data(self, row):
        # 提取出一个url而不是列表
        topicHref = row.xpath('td[1]/a/@href')[0]
        # topicHref = row.xpath('td[1]/a/@href')
        topicTitle = row.xpath('td[1]/a/text()')[0]
        topicAuthorHref = row.xpath('td[2]/a/@href')[0]
        topicAuthor = row.xpath('string(td[2]/a)')
        topicReplyCount = row.xpath('string(td[3])')
        topicLastReplyTime = row.xpath('string(td[4])')
        topicItem = {}
        topicItem['url'] = topicHref
        topicItem['title'] = topicTitle.strip()
        topicItem['author'] = topicAuthor
        topicItem['reply'] = 0 if topicReplyCount == '' else int(topicReplyCount)
        topicItem['last_reply_time'] = f'{datetime.now().year}-{topicLastReplyTime}:00'
        url = topicHref.strip('/')
        topicItem['id'] = url.split('/')[-1]
        log.info(f'开始获取{topicItem["title"]}')
        try:
            response = requests.get(topicHref, headers=self.get_common_header(), proxies={'http': self.proxy, 'https': self.proxy}, timeout=3)
        except Exception as e:
            log.warning('请求失败', url)
            self.proxy = self.get_proxy()
            return self.parse_detail_data(row)
        result = response.text
        if not self.is_valid_result(response):
            self.proxy = self.get_proxy()
            return self.parse_detail_data(row)
        etree_result = etree.HTML(result)
        topicItem['title'] = etree_result.xpath('//div[@class="article"]//h1/text()')[0].strip()
        topicItem['create_topic_time'] = etree_result.xpath('//span[contains(@class, "color-green")]/text()')[0]
        # 不包含图片
        topicItem['content'] = ' '.join(
            etree_result.xpath('//div[@class="topic-content"]//*[not(self::script)]/text()')).strip()
        # 组合图片
        img_tag_list = [etree.tostring(img).decode('utf-8') for img in
                        etree_result.xpath('//div[@class="topic-content"]//img')]
        topicItem['content'] = topicItem['content'] + ' '.join(img_tag_list)
        topicItem['group_name'] = etree_result.xpath('//div[@class="group-item"]//div[@class="title"]//a//text()')[0]
        self.save_to_mysql(topicItem)
        log.info(f'获取{topicItem["title"]}成功')

    def get_common_header(self):
        return {
            'user-agent': ua.random
        }

    def is_valid_result(self, response):
        if response.status_code == 403 or response.status_code == 302:
            log.warning("状态码为%s, 此ip被封禁，疑似请求频率过高,删除代理，代理:%s，剩余代理：%s,请求url:%s" % (
                response.status_code, self.proxy, self.proxy, response.url))
            #  豆瓣翻太多页会302
            if response.status_code == 302:
                log.info(f"Location:{response.headers['Location'].decode()}")
                if 'https://www.douban.com/accounts/login' in response.headers['Location'].decode():
                    log.warning("豆瓣账号被封禁")
                    return False
            if '你没有权限访问这个页面' in response.text:
                log.warning("你没有权限访问这个页面")
                return False
            return False
        if 'sec.douban.com' in response.text:
            log.warning("此ip被封禁，疑似请求频率过高,重新请求, %s, %s" % (
                response.url, self.proxy))
            return False
        if response.status_code != 200:
            log.warning("状态码为%s, url为：%s，代理:%s" % (
                response.status_code, response.url, self.proxy))
            return False
        return True

    def init_sql_pool(self):
        self.con = pymysql.connect(
            host='ikarosx-mysql.mysql.rds.aliyuncs.com',
            port=3306,
            database='douban',
            charset='utf8mb4',
            user='douban',
            password='douban2024!'
        )

    def save_to_mysql(self, topicItem):
        with self.con.cursor() as cursor:
            count_sql = f'select count(1) from group_topic where id = {topicItem["id"]}'
            cursor.execute(count_sql)
            count = cursor.fetchone()[0]
            if count > 0:
                log.info(f'已经存在{topicItem["title"]}')
                return
            sql = 'insert into group_topic (id, url, title, author, reply, last_reply_time, create_topic_time, content, group_name) values (%s, %s, %s, %s, %s, %s, %s, %s, %s)'
            cursor.execute(sql, (
                topicItem['id'], topicItem['url'], topicItem['title'], topicItem['author'], int(topicItem['reply']),
                topicItem['last_reply_time'], topicItem['create_topic_time'], topicItem['content'], topicItem['group_name']))
        self.con.commit()


if __name__ == '__main__':
    douban_spider().start_spider()

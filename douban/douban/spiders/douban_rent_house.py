import logging
from datetime import datetime, timedelta

import scrapy
from sqlalchemy import exists
from sqlalchemy.orm import scoped_session
from .. import pipelines
from ..config.rent_house_config import groupUrls as houseGroupUrls
from ..items import TopicItem
from .. import items
class DoubanRentHouseSpider(scrapy.Spider):
    name = "douban_rent_house"
    allowed_domains = ["www.douban.com"]
    start_urls = houseGroupUrls

    def parse(self, response):
        rows = response.xpath('//table[@class="olt"]//tr[not(@class="th")]')
        for row in rows:
            yield from self.parseIndexData(row)

        # 判断最后回复时间是否已经超过一天
        lastReplyTime = rows[-1].xpath('td[4]/text()').get()
        # 将字符串转换为datetime对象
        last_reply_datetime = datetime.strptime(f'{datetime.now().year}-{lastReplyTime}:00', '%Y-%m-%d %H:%M:%S')
        # 获取当前时间
        now = datetime.now()
        # 计算时间差
        time_diff = now - last_reply_datetime
        # 判断是否在1天以前
        if time_diff > timedelta(hours=2):
            logging.warning("超过2小时，不继续爬取")
            return
        nextPage = response.xpath('//a[text()="后页>"]/@href').get()
        if nextPage:
            yield scrapy.Request(nextPage, callback=self.parse)


    # 解析首页数据
    def parseIndexData(self, row):
        topicHref = row.xpath('td[1]/a/@href').get()
        topicTitle = row.xpath('td[1]/a/text()').get()
        topicAuthorHref = row.xpath('td[2]/a/@href').get()
        topicAuthor = row.xpath('td[2]/a/text()').get()
        topicReplyCount = row.xpath('td[3]/text()').get()
        topicLastReplyTime = row.xpath('td[4]/text()').get()
        topicItem = TopicItem()
        topicItem['url'] = topicHref
        topicItem['title'] = topicTitle
        topicItem['author'] = topicAuthor
        topicItem['reply'] = topicReplyCount
        topicItem['last_reply_time'] = f'{datetime.now().year}-{topicLastReplyTime}:00'
        url = topicHref.strip('/')
        topicItem['id'] = url.split('/')[-1]
        # 查询数据库这个id是否存在
        # session = scoped_session(pipelines.sessionFactory)
        # topic = session.query(exists().where(items.TopicItemMysqlEntity.id == topicItem['id'])).scalar()
        # if topic:
        #     return
        yield scrapy.Request(topicHref, callback=self.parseTopicData, meta={'item': topicItem})

    # 解析帖子数据
    def parseTopicData(self, response):
        topicItem = response.meta['item']
        topicItem['title'] = response.xpath('//div[@class="article"]//h1/text()').get()
        topicItem['create_topic_time'] = response.xpath('//span[contains(@class, "color-green")]/text()').get()
        # 不包含图片
        topicItem['content'] = ' '.join(response.xpath('//div[@class="topic-content"]//*[not(self::script)]/text()').getall())
        # 组合图片
        topicItem['content'] = topicItem['content'] + ' '.join(response.xpath('//div[@class="topic-content"]//img').getall())
        topicItem['group_name'] = response.xpath('//div[@class="group-item"]//div[@class="title"]//a//text()').get()
        yield topicItem
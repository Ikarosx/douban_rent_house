# Define here the models for your scraped items
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/items.html

import scrapy
from sqlalchemy import Column, String, Integer, Text, Date
from sqlalchemy.orm import declarative_base

# 创建对象的基类:
Base = declarative_base()

class TopicItem(scrapy.Item):
    # define the fields for your item here like:
    id = scrapy.Field()
    title = scrapy.Field()
    author = scrapy.Field()
    url = scrapy.Field()
    reply = scrapy.Field()
    create_topic_time = scrapy.Field()
    content = scrapy.Field()
    group_name = scrapy.Field()
    last_reply_time = scrapy.Field()

class TopicItemMysqlEntity(Base):
    # define the fields for your item here like:
    __tablename__ = 'group_topic'
    id = Column(Integer, primary_key=True)
    title = Column(String(255))
    author = Column(String(50))
    group_name = Column(String(50))
    url = Column(String(255))
    reply = Column(Integer)
    create_topic_time = Column(Date)
    content = Column(Text)
    last_reply_time = Column(Date)
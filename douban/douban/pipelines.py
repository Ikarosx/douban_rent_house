# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html
import logging

# useful for handling different item types with a single interface
from itemadapter import ItemAdapter
from sqlalchemy.ext.asyncio import create_async_engine
from . import items
from sqlalchemy import Column, String, create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.orm import scoped_session
logger = logging.getLogger(__name__)

global sessionFactory

class DoubanPipeline:


    def __init__(self, mysql_uri):
        self.mysql_uri = mysql_uri
        self.itemCount = 0
    @classmethod
    def from_crawler(cls, crawler):
        return cls(
            # 设置在settings.py
            mysql_uri=crawler.settings.get('MYSQL_URI')
        )

    def open_spider(self, spider):
        # 连接数据库
        # 初始化数据库连接:
        logger.info("初始化数据库连接")
        engine = create_engine(self.mysql_uri,
            pool_size=32,  # 连接池大小
            pool_timeout=30,
            pool_recycle=3600)
        # 创建DBSession类型:
        global sessionFactory
        sessionFactory = sessionmaker(bind=engine)
        logger.info("初始化数据库连接完成")
    def close_spider(self, spider):
        logger.info("关闭爬虫")


    def process_item(self, item, spider):
        session = scoped_session(sessionFactory)
        self.itemCount = self.itemCount + 1
        logger.info(f"处理item:${self.itemCount}")
        if isinstance(item, items.TopicItem):
            entity = items.TopicItemMysqlEntity(**item)
        else:
            return item
        try:
            if entity.id:
                # 如果存在直接删除
                exists = session.query(type(entity)).filter(type(entity).id == entity.id).delete()
                session.commit()
            session.add(entity)
            session.commit()
        except Exception as e:
            spider.logger.error(e)
            session.rollback()
        session.remove()
        return item




class CleanPipeline:
    def process_item(self, item, spider):
        for key in item.keys():
            if item[key] is not None and isinstance(item[key], str):
                item[key] = item[key].strip()  # 去除字符串字段中的空格
        return item

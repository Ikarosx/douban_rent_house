import scrapy


class DoubanRentHouseSpider(scrapy.Spider):
    name = "douban_rent_house"
    allowed_domains = ["www.douban.com"]
    start_urls = ["http://www.douban.com/"]

    def parse(self, response):
        pass

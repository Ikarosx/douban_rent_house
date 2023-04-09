# Define here the models for your spider middleware
#
# See documentation in:
# https://docs.scrapy.org/en/latest/topics/spider-middleware.html
import asyncio
import json
import logging
import random
import string
import threading
import time
import http.cookies
from datetime import datetime

import requests
import requests.utils
import scrapy.core.downloader.handlers.http11 as handler
import scrapy.crawler
from scrapy import signals
from scrapy.core.downloader.tls import openssl_methods
from scrapy.downloadermiddlewares.retry import RetryMiddleware
from scrapy.exceptions import IgnoreRequest
from scrapy.utils.misc import create_instance, load_object
from txsocksx.http import SOCKS5Agent
from scrapy.settings.default_settings import  DOWNLOADER_CLIENTCONTEXTFACTORY,DOWNLOADER_CLIENT_TLS_METHOD
# from scrapy.settings import BaseSettings as settings
from scrapy.settings import Settings as settings
# from scrapy.settings import default_settings as settings
from twisted.internet.endpoints import TCP4ClientEndpoint
from scrapy.core.downloader.webclient import _parse
from twisted.internet import reactor, task
import aiohttp
# useful for handling different item types with a single interface
from scrapy.downloadermiddlewares.useragent import UserAgentMiddleware

logger = logging.getLogger(__name__)
# 存放代理
proxies = []

# cookie

class DouBanDownloaderMiddleware(object):
    # Not all methods need to be defined. If a method is not defined,
    # scrapy acts as if the downloader middleware does not modify the
    # passed objects.

    @classmethod
    def from_crawler(cls, crawler):
        # This method is used by Scrapy to create your spiders.
        s = cls()
        crawler.signals.connect(s.spider_opened, signal=signals.spider_opened)
        return s

    def process_request(self, request, spider):
        # Called for each request that goes through the downloader
        # middleware.
        # Must either:
        # - return None: continue processing this request
        # - or return a Response object
        # - or return a Request object
        # - or raise IgnoreRequest: process_exception() methods of
        #   installed downloader middleware will be called
        return None

    def dealRepeatRequests(self, request):
        if isinstance(request, scrapy.Request):
            return scrapy.Request(url=request.url, callback=request.callback, dont_filter=True, meta=request.meta)
        elif isinstance(request, scrapy.FormRequest):
            return scrapy.FormRequest(url=request.url, callback=request.callback, body=request.body, meta=request.meta,
                                      dont_filter=True)
        logger.warn("不属于任何request")
        logger.warn(type(request))
        raise IgnoreRequest

    def process_response(self, request, response, spider):
        # Called with the response returned from the downloader.
        # Must either;
        # - return a Response object
        # - return a Request object
        # - or raise IgnoreRequest
        # Called with the response returned from the downloader.
        return response

    def process_exception(self, request, exception, spider):
        # Called when a download handler or a process_request()
        # (from other downloader middleware) raises an exception.
        # Must either:
        # - return None: continue processing this exception
        # - return a Response object: stops process_exception() chain
        # - return a Request object: stops process_exception() chain
        spider.logger.error("请求出现异常, URL:%s" % (request.url))
        spider.logger.error(exception)
        if 'proxy' in request.meta:
            if request.meta['proxy'] in proxies:
                proxies.remove(request.meta['proxy'])
            del request.meta['proxy']
        return self.dealRepeatRequests(request)

    def spider_opened(self, spider):
        spider.logger.info('Spider opened: %s' % spider.name)


# 随机UA头
class RandomUserAgent(UserAgentMiddleware):
    def __init__(self, user_agent):
        self.user_agent = user_agent

    @classmethod
    def from_crawler(cls, crawler):
        return cls(
            user_agent=crawler.settings.get('USER_AGENT_LIST')
        )

    def process_request(self, request, spider):
        agent = random.choice(self.user_agent)
        request.headers['User-Agent'] = agent
        return None

# 豆瓣专属cookie处理
class CookieMiddleware(object):

    def __init__(self):
        self.cookie = None

    def process_request(self, request, spider):
        bid = ''.join(random.choice(string.ascii_letters + string.digits) for x in range(11))
        cookies = {
            'bid': bid,
        }
        request.cookie = cookies
        return None


# 代理



class ProxyMiddleware(object):
    def __init__(self):
        # 定义一个定时器任务，每 5 分钟执行一次
        self.lock = threading.Lock()

    async def process_request(self, request, spider):
        # 解决一开始的请求携带了代理
        # request.meta['proxy'] = None
        request.meta['proxy'] = await self.getOneProxy()
        # request.meta['proxy'] = 'socks5://127.0.0.1:10808'

    # 取出一个代理
    async def getOneProxy(self):
        with self.lock:
            if len(proxies) == 0:
                self.update_proxies()
            proxy = random.choice(proxies)
            proxies.remove(proxy)
            return proxy


    def dealRepeatRequests(self, request):
        if isinstance(request, scrapy.Request):
            return scrapy.Request(url=request.url, callback=request.callback, dont_filter=True, meta=request.meta)
        elif isinstance(request, scrapy.FormRequest):
            return scrapy.FormRequest(url=request.url, callback=request.callback, body=request.body, meta=request.meta,
                                      dont_filter=True)
        logger.warning("不属于任何request")
        logger.warning(type(request))
        raise IgnoreRequest

    def process_response(self, request, response, spider):
        if response.status == 403 or response.status == 302:
            spider.logger.warn("状态码为%s, 此ip被封禁，疑似请求频率过高,删除代理，代理:%s，剩余代理：%s,请求url:%s" % (
            response.status, request.meta['proxy'] if 'proxy' in request.meta else '', len(proxies), request.url))
            if 'proxy' in request.meta:
                if request.meta['proxy'] in proxies:
                    proxies.remove(request.meta['proxy'])
                del request.meta['proxy']
            #  豆瓣翻太多页会302
            if response.status == 302:
                spider.logger.info(f"Location:{response.headers['Location'].decode()}")
                if 'https://www.douban.com/accounts/login' in response.headers['Location'].decode():
                    spider.logger.warn("豆瓣账号被封禁")
                    raise IgnoreRequest
            if '你没有权限访问这个页面' in response.text:
                spider.logger.warn("你没有权限访问这个页面")
                raise IgnoreRequest
            return self.dealRepeatRequests(request)
        if 'sec.douban.com' in response.text:
            spider.logger.warn("此ip被封禁，疑似请求频率过高,重新请求, %s, %s" % (
                request.url, request.meta['proxy'] if 'proxy' in request.meta else ''))
            if 'proxy' in request.meta:
                if request.meta['proxy'] in proxies:
                    proxies.remove(request.meta['proxy'])
                del request.meta['proxy']
            return self.dealRepeatRequests(request)
        if response.status != 200:
            spider.logger.warn("状态码为%s, url为：%s，代理:%s" % (
                response.status, request.url, request.meta['proxy'] if 'proxy' in request.meta else ''))
            return self.dealRepeatRequests(request)
        if 'proxy' in request.meta:
            proxies.append(request.meta['proxy'])
            del request.meta['proxy']
        return response

    def update_proxies(self):
        logger.info("更新代理")
        if len(proxies) > 50:
            logger.info("代理池充足，无需更新")
            return
        response = requests.get('http://api.xiequ.cn/VAD/GetIp.aspx?act=get&uid=51539&vkey=0F4A5129E8AD784DD0A85E7725F00B03&num=40&time=30&plat=0&re=0&type=2&so=3&ow=1&spl=1&addr=&db=1')
        text = response.text
        data = json.loads(text)['data']
        for item in data:
            proxies.append(f'socks5://{item["IP"]}:{item["Port"]}')
        logger.info("更新代理完成")
        logger.info(proxies)





class TorScrapyAgent(handler.ScrapyAgent):
    _Agent = SOCKS5Agent

    def _get_agent(self, request, timeout):
        proxy = request.meta['proxy']
        if proxy:
            proxy_scheme, _, proxy_host, proxy_port, _ = _parse(proxy)
            proxy_scheme = str(proxy_scheme, 'utf-8')
            if proxy_scheme == 'socks5':
                endpoint = TCP4ClientEndpoint(reactor, proxy_host, proxy_port)
                self._sslMethod = openssl_methods[DOWNLOADER_CLIENT_TLS_METHOD]
                self._contextFactoryClass = load_object(DOWNLOADER_CLIENTCONTEXTFACTORY)
                self._contextFactory = create_instance(
                    objcls=self._contextFactoryClass,
                    settings=settings(),
                    crawler=None,
                    method=self._sslMethod,
                )
                return self._Agent(reactor, proxyEndpoint=endpoint, contextFactory = self._contextFactory)

        return super(TorScrapyAgent, self)._get_agent(request, timeout)

class TorHTTPDownloadHandler(handler.HTTP11DownloadHandler):
    def download_request(self, request, spider):
        agent = TorScrapyAgent(contextFactory=self._contextFactory, pool=self._pool,
                               maxsize=getattr(spider, 'download_maxsize', self._default_maxsize),
                               warnsize=getattr(spider, 'download_warnsize', self._default_warnsize))

        return agent.download_request(request)


class MyRetry(RetryMiddleware):
    """
    保存重试失败url
    """
    def process_exception(self, request, exception, spider):
        logging.debug("重试process_exception%s" % (request.meta['proxy'] if 'proxy' in request.meta else '无代理'))
        if 'proxy' in request.meta:
            if request.meta['proxy'] in proxies:
                proxies.remove(request.meta['proxy'])
            del request.meta['proxy']
        if (
            isinstance(exception, self.EXCEPTIONS_TO_RETRY)
            and not request.meta.get('dont_retry', False)
        ):
            return self._retry(request, exception, spider)


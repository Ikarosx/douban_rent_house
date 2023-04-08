from typing import List

from scrapy.core.downloader.webclient import _parse
from txsocksx.http import SOCKS5Agent
from twisted.internet import reactor
from twisted.internet.endpoints import TCP4ClientEndpoint
from scrapy.core.downloader.handlers.http11 import HTTP11DownloadHandler, ScrapyAgent
import random
from urllib.parse import urlsplit

# Ref https://txsocksx.readthedocs.io/en/latest/#txsocksx.http.SOCKS5Agent

import certifi, os

os.environ["SSL_CERT_FILE"] = certifi.where() # if not setted , you'll got an ERROR : certificate verify failed')] [<twisted.python.failure.Failure OpenSSL.SSL.Error: [('STORE routines', '', 'unregistered scheme')


class Socks5DownloadHandler(HTTP11DownloadHandler):

    def download_request(self, request, spider):
        """Return a deferred for the HTTP download"""
        settings = spider.settings
        agent = ScrapySocks5Agent(settings, contextFactory=self._contextFactory, pool=self._pool, crawler=self._crawler)
        return agent.download_request(request)


class ScrapySocks5Agent(ScrapyAgent):
    def __init__(self, settings, **kwargs):
        """
        init proxy pool
        """
        super(ScrapySocks5Agent, self).__init__(**kwargs)
        self.__proxy_file = settings['PROXY_FILE']

    def _get_agent(self, request, timeout):
        proxy = request.meta['proxy']
        if not proxy:
            # error
            return super(ScrapySocks5Agent, self)._get_agent(request, timeout)
        proxy_scheme, _, proxy_host, proxy_port, _ = _parse(proxy)
        proxy_scheme = str(proxy_scheme, 'utf-8')
        if proxy_scheme != 'socks5':
            return super(ScrapySocks5Agent, self)._get_agent(request, timeout)
        proxyEndpoint = TCP4ClientEndpoint(reactor, proxy_host.decode(), proxy_port)
        agent = SOCKS5Agent(reactor, proxyEndpoint=proxyEndpoint)
        return agent


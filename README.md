# douban_rent_house
豆瓣小组租房数据爬虫

## 项目介绍
本项目是一个豆瓣小组租房数据爬虫，爬取豆瓣小组租房数据，包括房源信息、房源图片等。
配套前后端进行筛选，前端flutter，后端SpringBoot

目前只爬取了深圳的几个小组  
目前只爬取了深圳的几个小组  
目前只爬取了深圳的几个小组  
目前只爬取了深圳的几个小组  

## 项目结构
- douban： 爬虫
- backend: 后端
- frontend: 前端

## 环境

- 数据库，douban.sql，数据库名，账号，密码都是douban
- flutter环境
- 爬虫环境
- java环境

## 爬虫
先有爬虫才有数据的获取，使用scrapy框架
### 代理
豆瓣是需要代理的，而且是**socks5代理**，我采用的是[携趣代理](https://www.xiequ.cn/index.html?d325395f)，不充钱的话每天也有1000个免费的可以用  

<img src="https://ikaros-picture.oss-cn-shenzhen.aliyuncs.com/picgo20230412233737.png"/>

修改middlewares.py里update_proxies方法的获取代理地址为你自己的  
如果使用其他的代理需要自己重写一下   
如果想去掉代理尝试，可以在settings.py注释掉DOWNLOADER_MIDDLEWARES里的ProxyMiddleware  

```shell
cd douban 
pip install -r requirements.txt
# 正常安装完成是可以看到显示 douban_rent_house
scrapy list
# 运行爬虫
scrapy crawl douban_rent_house
```

### 注意事项
- 代理，必须是socks5，http不行
- cookie，要加上中间件，随机生成bid即可
- 代理会在内存中维护一个代理池，不够了就请求新的，一个request拿走一个（加锁），用完了放回
- Scrapy[不支持](https://github.com/scrapy/scrapy/issues/747#issuecomment-1186730617)socks5代理，所以加上了[txsocksx](https://github.com/habnabit/txsocksx)  
- 爬取小组的配置在config/rent_house_config.py，可以自行添加，目前只加了几个  
- 一个月以前的帖子查看需要登录
- 爬虫只负责原始数据的抓取，剩下的放在后端处理
- 爬取频率需要自行在spiders/douban_rent_house.py的parse方法修改，默认是爬取前2小时内的数据，因为我本地运行了定时任务，频率建议不要太高    

## flutter前端

1. 安装flutter环境
2. 打开项目
3. 运行
4. 不是很熟悉，全部文件都在main.dart里  

## SpringBoot后端

常规启动

- 接口请求在controller/RentController
- 数据处理在job/DoubanRentHouseJob里，先会处理是否是求租，求租的只标记不做其他处理（包括价格，位置等的提取）

### 一些条件的判定



## 项目展示


<img src="https://ikaros-picture.oss-cn-shenzhen.aliyuncs.com/picgo1681314935038.png"/>

<img src="https://ikaros-picture.oss-cn-shenzhen.aliyuncs.com/picgo20230412235646.png"/>






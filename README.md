## 从零开始打造简易秒杀系统

该项目为基于SpringBoot的简易秒杀系统实战代码

### 对应教程

[【秒杀系统】零基础上手秒杀系统（一）：防止超卖](https://mp.weixin.qq.com/s?__biz=MzU1NTA0NTEwMg==&mid=2247484174&idx=1&sn=235af7ead49a7d33e7fab52e05d5021f&lang=zh_CN#rd)

[【秒杀系统】零基础上手秒杀系统（二）：令牌桶限流 + 再谈超卖](https://mp.weixin.qq.com/s?__biz=MzU1NTA0NTEwMg==&mid=2247484178&idx=1&sn=f4d8072b5408b08f983cae26a6ce1cf5&lang=zh_CN#rd)

[【秒杀系统】零基础上手秒杀系统（三）：抢购接口隐藏 + 单用户限制频率](https://mp.weixin.qq.com/s?__biz=MzU1NTA0NTEwMg==&mid=2247484184&idx=1&sn=8b878e9e730a6e4da27ed336c8201c92&lang=zh_CN#rd)

[【秒杀系统】零基础上手秒杀系统（四）：缓存与数据库双写问题的争议](https://mp.weixin.qq.com/s?__biz=MzU1NTA0NTEwMg==&mid=2247484200&idx=1&sn=6b6c7251ee83fe8ef9201373aafcffdd&chksm=fbdb1aa9ccac93bfe26655f89056b0d25b3a536f6b11148878fe96ffdf1d8349d44659cad784&token=841068032&lang=zh_CN#rd)

[【秒杀系统】零基础上手秒杀系统番外篇：阿里开源MySQL中间件Canal快速入门](https://mp.weixin.qq.com/s?__biz=MzU1NTA0NTEwMg==&mid=2247484273&idx=1&sn=7fec41a40e763df094c0dd675330808a&chksm=fbdb1af0ccac93e676c2a0c6aeb1ff3edfe43b30969a7c1bbe19ccf7270acd6e41e6812caf0d&lang=zh_CN#rd)

[【秒杀系统】零基础上手秒杀系统（五）：如何优雅的实现订单异步处理](https://mp.weixin.qq.com/s?__biz=MzU1NTA0NTEwMg==&mid=2247484580&idx=1&sn=5092e7d109b0f9bb9ed604fa03ac82cb&chksm=fbdb1d25ccac94338e39a6a36fab10dc36dd6a91a4e3cb0db1559cb82448865c4510b80a41ef&token=845103815&lang=zh_CN#rd)


### 项目使用简介

项目是SpringBoot工程，并且是父子工程，直接导入IDEA，Eclipse即可使用。

1. 导入miaosha.sql文件到你的MySQL数据库

2. 配置application.properties文件，修改为你的数据库链接地址

3. mvn clean install最外层的父工程（pom.xml）

4. 运行miaosha-web，在POSTMAN或者浏览器直接访问请求链接即可。

## 申明

本仓库代码遵守Apache License 2.0，文章遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。

洗稿，抄袭代码者，自重！


## About me

全网笔名：蛮三刀酱

唯一技术公众号：后端技术漫谈

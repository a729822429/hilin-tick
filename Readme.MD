# hilin-tick内网穿透
本项目取tick（嘀嗒），寓意水滴的声音，水滴石穿。  
**hilin-tick**是一款轻量级、高性能、功能强大的**内网穿透**代理服务器。支持TCP/UDP穿透，可支持任何**tcp、udp**上层协议（访问内网网站、本地支付接口调试、ssh访问、远程桌面，内网dns解析等等……）。  
**hilin-tick**为全异步架构，全局事件驱动架构，因此仅需要监听系统事件，就可以快速接入并二开。

## 特点
- 协议支持全面，兼容TCP/UDP和所有基于TCP、UDP的协议，例如http(s)、socks5、http代理...
- 纯JAVA开发，理论上全平台兼容(linux、windows、macos等)
- 通过管理页面配置和重启客户端
- https集成，支持将后端代理和web服务转成https，同时支持多证书
- 操作简单，全部配置都可以通过管理后台页面来更改
- 多用户支持

## 技术栈
- **JDK21** ： 当前最新的LTS版本
- **maven** ： 项目管理及自动构建工具
- **vertx-core** ： 网络框架，本项目用来创建网络客户端、服务器，并全局使用该框架提供的EventBus
- **vertx-web** ： 提供对外api，为什么不用springboot?用springboot还怎么吹牛批......
- **spring-boot/spring-cloud** ：这个只要是javaer应该都知道吧。本项目仅作为中间件的工具，mysql、redis等
- **mysql** ： 最流行的关系型数据库
- **redis** ： 最流行的基于内存的kv存储系统

## 项目说明


## 字段说明
- 所有通用变量存于```icu.hilin.tick.core.TickContant```
- **clientID** 每个注册用户对应一个clienID，该值全局唯一
- **channelID** 每个注册用户可以有多个通道，每个通道对应一个ID，全局唯一

## 事件说明
- **send-cmd-{clientID}** 向信令通道发送数据
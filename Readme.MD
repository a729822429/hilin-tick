# hilin-tick内网穿透(开发中)

[1]: https://img.shields.io/badge/license-Apache2.0-brightgreen.svg?style=plastic

[2]: /LICENSE

[3]: https://img.shields.io/badge/hilin-welcome-brightgreen.svg?style=plastic

[4]: https://hilin.icu

[5]: https://img.shields.io/badge/release-1.0.0-blue.svg?style=plastic

[6]: https://hilin.icu
[![license][1]][2]
[![Issue Welcome][3]][4]
[![Release Version][5]][6]  
本项目取tick（嘀嗒），寓意水滴的声音，水滴石穿。  
**hilin-tick**是一款轻量级、高性能、功能强大的**内网穿透**代理服务器。支持TCP/UDP穿透，可支持任何**tcp、udp**
上层协议（访问内网网站、本地支付接口调试、ssh访问、远程桌面，内网dns解析等等……）。  
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
- **vertx-web** ： 提供对外api，为什么不用springboot?用springboot还怎么吹nb......
- **spring-boot** ：这个只要是javaer应该都知道吧。本项目仅作为中间件的客户端工具使用，包括但不限于mysql、redis等
- **mysql** ： 最流行的关系型数据库
- **redis** ： 最流行的基于内存的kv存储系统

## 项目说明

## 字段说明

- 所有通用变量存于```icu.hilin.tick.core.TickContant```
- **clientID** 这个可以理解为一个注册用户的id，该值全局唯一
- **tunnelID** 每个注册用户可以有多个隧道，每个隧道对应一个ID，全局唯一
- **channelID** 通道id，每个隧道连接对应一个通道，一个通道对应一个通道id，全局唯一

## 事件说明

### 单个通道

- **cmd_server_{action}_{clientID}**：通知cmd服务端执行对应命令
- **cmd_client_{action}_{clientID}**：通知cmd客户端执行对应命令
- **tunnel_server_{action}_{tunnelID}**：通知隧道服务端执行对应命令
- **tunnel_client_{action}_{tunnelID}**：通知隧道客户端执行对应命令
- **channel_server_{action}_{channelID}**：通知通道服务端执行对应命令
- **channel_client_{action}_{channelID}**：通知通道客户端执行对应命令

### 某一范围所有通道

- **cmd_server_{action}_all**：通知所有指令服务端
- **cmd_client_{action}_all**：通知所有指令客户端
- **tunnel_server_{action}_all**：通知所有服务端隧道
- **tunnel_client_{action}_all**：通知所有客户端隧道
- **channel_server_{action}_all**：通知所有服务端通道
- **channel_client_{action}_all**：通知所有客户端通道

### 进度

- [x] tcp穿透
- [ ] http域名绑定
- [ ] udp穿透
- [ ] 管理页面

### mod介绍

mod-feature中都是实验性功能，未在项目中使用，稳定后才会考虑使用到项目中

#### mod-feature

- **hilin-tick-mod-event** 自研事件框架

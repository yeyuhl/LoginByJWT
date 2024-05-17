> 源项目：[SpringBoot 3 + Vue3 前后端分离项目模版，已实现登录注册，限流等功能](https://github.com/itbaima-study/SpringBoot-Vue-Template-Jwt/tree/main)
> 
> 通过这个项目快速了解前后端是如何交互的。

# 前后端分离登录校验模板

采用SpringBoot3 + Vue3编写的前后端分离模版项目，集成多种技术栈，使用JWT校验方案。

## 后端功能与技术点

用户注册、用户登录、重置密码等基础功能以及对应接口

- 采用Mybatis-Plus作为持久层框架，使用更便捷
- 采用Redis存储注册/重置操作验证码，带过期时间控制
- 采用RabbitMQ积压短信发送任务，再由监听器统一处理
- 采用SpringSecurity作为权限校验框架，手动整合Jwt校验方案
- 采用Redis进行IP地址限流处理，防刷接口
- 视图层对象和数据层对象分离，编写工具方法利用反射快速互相转换（可以直接用BeanUtils.copyProperties()）
- 错误和异常页面统一采用JSON格式返回，前端处理响应更统一
- 手动处理跨域，采用过滤器实现（跨域这一块只能设置'\*'，这让我很困惑，换成前端的IP地址就会报错）
- 使用Swagger作为接口文档自动生成，已自动配置登录相关接口
- 采用过滤器实现对所有请求自动生成雪花ID方便线上定位问题
- 针对于多环境进行处理，开发环境和生产环境采用不同的配置
- 日志中包含单次请求完整信息以及对应的雪花ID，支持文件记录
- 项目整体结构清晰，职责明确，注释全面，开箱即用

## 前端功能与技术点

用户注册、用户登录、重置密码等界面，以及一个简易的主页

- 采用Vue-Router作为路由
- 采用Axios作为异步请求框架
- 采用Element-Plus作为UI组件库
- 使用VueUse适配深色模式切换（不知道怎么使用，win10现在不像苹果有深浅色切换）
- 使用unplugin-auto-import按需引入，减少打包后体积

# 雪花算法

Snowflake 是 Twitter 开源的分布式 ID 生成算法。Snowflake 由 64 bit 的二进制数字组成，这 64bit 的二进制被分成了几部分，每一部分存储的数据都有特定的含义：

- **第 0 位**：符号位（标识正负），始终为 0，没有用，不用管。
- **第 1~41 位**：一共 41 位，用来表示时间戳，单位是毫秒，可以支撑 2 ^41 毫秒（约 69 年）。
- **第 42~52 位**：一共 10 位，一般来说，前 5 位表示机房 ID，后 5 位表示机器 ID（实际项目中可以根据实际情况调整）。这样就可以区分不同集群/机房的节点。
- **第 53~64 位**：一共 12 位，用来表示序列号。 序列号为自增值，代表单台机器每毫秒能够产生的最大 ID 数(2^12 = 4096),也就是说单台机器每毫秒最多可以生成 4096 个 唯一 ID。

一般来说时间戳是由当前时间戳减去业务开始的时间戳后得出，将以上拼凑起来就是一个唯一ID，可以应用在分布式ID中。

# 问题总结

在调用rabbitTemplate.convertAndSend(Const.MQ_MAIL,data);时记得把data填上，不然虽然不报错，但由于没有数据，listener处理后也不会发送邮件。

使用autowired时报错不一定是注入的问题，不用急急忙忙地换成resource，有可能是某个注入的对象所依赖的类还没写完。

前端传入的数据一定要检验，如果后端不做数据校验，比如email不去检验，rabbitmq会一直抛出异常，因为传入的参数有问题导致无法正常消费，而我又没有配置死信队列，这个消息只会停留在队列中，当完成好参数校验之后，最简单粗暴的就是将原队列删掉，不然错误消息会堆积在队列中无法消费还会继续抛出异常。
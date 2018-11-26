# Distributed Synchronize 分布式同步组件
1. 分布式锁.
2. 基于Netty长连接实现.
3. 自定义协议,内置等待队列,性能远超redis锁.
4. 服务下线立刻感知,无需等待超时.
5. 支持优雅关闭.
6. 规避Redis分布式锁缺陷: http://tech.dianwoda.com/2018/04/11/redisfen-bu-shi-suo-jin-hua-shi/


### 启动服务端
```
java -jar -server dsync-dominate-xxx.jar
```
启动jar可以在maven下载,或者下载源码 ``mvn package`` 获得.
### 分布式锁 DLock
1. 添加依赖
```xml
<dependency>
  <groupId>cn.cyejing</groupId>
  <artifactId>dsync-toolkit</artifactId>
</dependency>
```
2. 使用方法
```java
Config config = Config.config().host("localhost").port(4843); //配置服务端地址
DSync dSync = DSync.create(config);
DLock lock = dSync.getLock();

lock.lock("adder");
lock.unlock();
```

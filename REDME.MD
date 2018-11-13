# Distributed Synchronize 分布式同步组件

1. 启动服务端
```
java -jar -server dsync-dominate-xxx.jar
```
启动jar可以在maven下载,或者下载源码 ``mvn install`` 获得.
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

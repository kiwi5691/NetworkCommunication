## SubscribeReq  订购请求request
SubscribeReq 是个普通的POJO对象
- 实现了Serializable接口
- 自动生成默认的序列化ID

## SubscribeResp 应答消息response

## SubReqServer
```java
ch.pipeline()
                .addLast(
                 new ObjectDecoder(
                  1024 * 1024,
                  ClassResolvers
                  .weakCachingConcurrentResolver(this
                  .getClass()
                  .getClassLoader())));
                            
```
创建了一个新的ObjectDecoder。负责对实现Serializable的POJO对象进行解码，她有多个构造函数，支持不同的ClassResolver。
这里使用weakCachingConcurrentResolver创建线程安全的WeakReference对类加载器进行缓存，其支持多线程，当虚拟机内存不足时，会释缓存中的内存，防止内存泄漏。
为了防止异常码流和解码错位导致的内存溢出，这里将单个对象最大序列化后的字节数组长度设置为1m。

```java
ch.pipeline().addLast(new ObjectEncoder());
```
新增了一个ObjectEncoder，它可以在消息发送的时候自动将实现Serializable的POJO对象进行编码，因此用户无需亲自对对象进行手工序列化，
对象序列化和反序列化都由netty的对象编解码器搞定


## SubReqClient

- 创建客户端的时候将Netty对象解码器和编解码器添加到ChannelPipeline
- 链路被激活时，构造订购请求消息发送，为了检验Netty的java序列化功能是否支持TCP粘包/拆包。client一次发送10条订单
- client订购处理handler将接收到的订单响应打印。

```java
     ch.pipeline()
              .addLast(
                   new ObjectDecoder(
                              1024, ClassResolvers
                             .cacheDisabled(this.getClass()
                                      .getClassLoader())));
      ch.pipeline().addLast(new ObjectEncoder());
     ch.pipeline().addLast(new SubReqClientHandler());           }
                   
```

其中，禁止对类加载器进行缓存。它在基于OSGi的动态模块化编程中经常使用。由于OSGi的bundle可以进行热部署和热升级，当某个bundle升级后，它对应的类加载器也将一起升级。
所以在动态模块化编程中，很少对类加载器进行缓存，因为它随时可能变化。
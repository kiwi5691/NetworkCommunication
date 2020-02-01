# Netty笔记

`TCP/IP`
`网络`
`多线程`

# TCP

- [TCP](https://github.com/kiwi5691/NetworkCommunication/tree/master/linuxIO/TCP)

# Linux/Unix I/O

- [Linux/Unix网络IO五大模型](https://github.com/kiwi5691/NetworkCommunication/tree/master/linuxIO)

# JAVA I/O
#### 经典BIO

- [BIO通信模型图](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/BIO)

- [BIO流程代码](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/BIO/src/main/java/bio)

#### 伪异步BIO


- [伪异步IO模型图](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/FakeAsyncIO)

- [伪异步IO流程代码](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/FakeAsyncIO/src/main/java/fakeAsyncIO)

- [线程池使用](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/FakeAsyncIO/src/main/java/fakeAsyncIO)

#### NIO

- [NIO概念](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/NIO)

- [NIO代码和时序图](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/NIO/src/main/java/nio)

#### AIO

- [AIO概念](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/AIO)

- [AIO代码和分析](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/AIO/src/main/java/aio)

#### Reactor设计模式

- [AIO概念](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/AIO)

#### java.util.concurrent
IO网络编程涉及到了许多多线程编程，这里是java.util.concurrent包下顺序下来的阅读笔记目录

- [ReentrantLock重入锁|Condition](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/AIO/src/main/java/aio/ReentrantLock)

- [AQS同步器](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/AIO/src/main/java/aio/AbstractQueuedSynchronizer)

- [CountDownLatch|Semaphore|CyclicBarrier](https://github.com/kiwi5691/NetworkCommunication/tree/master/javaIO/AIO/src/main/java/aio/countDownLatch)


#### 四种IO模型对比

![avatar](../../images/4io.png)

#### Netty|NIO

- NIO服务端需要能够处理网络的闪断，客户端的重复接入，客户端的安全验证，消息的编解码，半包读写等。
- 如果出现生产环境问题，会导致跨节点的服务调用中断，严重的话导致整个集群环境不可用，需要重启服务器

`NIO采用了异步非阻塞编程模型，而且是一个I/O线程处理多条链路，调试以及跟踪麻烦`

# Netty

- [netty测试用例](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/testNetty)

- [未处理TCP粘包，拆包情况](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/pasteTCP)

- 解决TCP粘包
   1. [LineBasedFrameDecoder](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/pasteTCP/solvedPaste)
   2. [FixedLengthFrameDecoder](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/pasteTCP/FixedLengthFrameDecoder)
   3. [DelimiterBasedFrameDecoder](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/pasteTCP/delimiterBasedFrameDecoder)
  
- 主流编解码框架(序列化)

   [编解码技术](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/pasteTCP/decoder)
   1.  [基于java](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/pasteTCP/decoder/pojo)
   2. [Google Protobuf](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/pasteTCP/decoder/Protobuf)
   3. [Jboss Marshalling](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/pasteTCP/decoder/jboss)
   
- 多协议
    
   1. [http](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/protocol/fileServer)
   2. [http+xml](https://github.com/kiwi5691/NetworkCommunication/tree/master/netty/src/main/java/protocol/fileServer/xml)
   
   - WebSocket
   1. http协议的弊端
   2. webSocket使用
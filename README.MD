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

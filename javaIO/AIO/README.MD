# AIO编程

NIO2 引入了新的异步通道的概念，通过了异步文件通道和异步Socket通道的实现。

### 异步通道提供两种方式获取操作结果

- 通过java.util.concurrent.Future类表示异步的结果

- 在执行异步操作的时候闯入一个java.nio.channels

CompletionHandler接口实现类作为操作完成的回调

AIO对应UNIX的事件驱动I/O，它不需要用多路复用器(Selector)对注册通道进行轮询操作即可实现异步读写，简化了NIO的编程模型

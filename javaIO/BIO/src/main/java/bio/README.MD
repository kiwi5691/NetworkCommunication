# BIO流程
`Handler是一个Runnable`
`线程池由自己设定`
- 每当有一个新的客户端请求接入时
- 服务端必须创建一个新的线程处理新的客户端链路
- 一个线程处理一个客户端连接

- [客户端代码](https://github.com/kiwi5691/NetworkCommunication/blob/master/javaIO/BIO/src/main/java/bio/TimeClient.java)
- [Runnable代码](https://github.com/kiwi5691/NetworkCommunication/blob/master/javaIO/BIO/src/main/java/bio/TimeserverHandler.java)
- [服务端代码](https://github.com/kiwi5691/NetworkCommunication/blob/master/javaIO/BIO/src/main/java/bio/TimeServer.java)

### Handler由ThreadPoolExecutor处理无需等待，不会导致线程个数过于膨胀or内存溢出
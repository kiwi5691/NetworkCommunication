## FixedLengthFrameDecoder

固定长度解码器，他能按照指定的长度对消息进行自动解码

**利用FixedLengthFrameDecoder，无论一次获取多少数据报，他都会根据构造函数中设置的固定长度进行解码，如果是半包消息，FixedLengthFrameDecoder会缓存半包消息并等待下一个包到达后进行解码，直到读取到一个完整的包。**


## TELNET

- 先启动echoServer
- cmd -> telnet localhost 8080
- 输入，echoServer回显

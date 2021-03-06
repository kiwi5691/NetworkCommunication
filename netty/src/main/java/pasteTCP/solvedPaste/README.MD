# LineBasedFrameDecoder解决TCP粘包

- LineBasedFrameDecoder 

原理是依次遍历ByteBuf中的可读字节，判断是否有\n or \r\n。如果有就以此位置结束。从可读索引到结束位置区间的字节就成了一行。
它是以换行符为结束标志的解码器，支持携带结束符or不携带结束符两种解码方式，同时支持配置单行的最大长度。
如果连续读取到最大长度后依然没有发现换行符，就会抛出异常，同时忽略掉之前读取到的异常码流。

- StringDecoder

将收到的对象转换成字符串，然后继续调用handler。

StringDecoder+LineBasedFrameDecoder组合就是换行切换的文本解码器。被设计用来拆包和粘包。

## 如果发送的消息不是以换行符结束，netty提供了多种支持tcp粘包/拆包的解码器



# 分隔符和定长解码器的应用

TCP以流的方式进行数据传输，上层应用协议为了对消息进行区分，采用如下方式
1. 消息长度固定，累计读取到的长度总和为d定长LEN的报文后，就认为读取到了一个完整的信息。将计数器置位，重新开始读取下一个数据报。
2. 将回车换行符作为消息结束符作为消息结束符，如FTP协议。这种方式在文本协议中应用广泛。
3. 将特殊的分隔符作为消息的结束标志。回车换行符就是一种特色的结束分隔符。
4. 通过消息头中定义长度字段来标识消息的总长度。

Netty对上面4种应用做了统一抽象，提供了4种解码器来解决对应问题。

#### DelimiterBasedFrameDecoder

自动完成以分隔符作为码流结束标志的消息和解码。

#### FixedLengthFrameDecoder

自动完成对定长消息的解码。

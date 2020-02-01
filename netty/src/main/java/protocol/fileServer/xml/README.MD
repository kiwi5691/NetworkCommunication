# Http服务端场景

用户订购系统，client填写订单，通过http client向server发送订购请求，将请求消息放在http消息体中，以xml承载。

以http+xml通信，双方使用http1.1协议，连接类型为CLOSE方式，即双发交互完毕http server主动关闭链路，client也关闭链路。

# 流程
1. 构造订单请求消息，并将请求消息编码为HTTP+XML格式

Netty的HTTP协议栈提供构造HTTP请求消息的相关接口，但无法将普通的POJO对象转换为HTTP+XML的HTTP请求消息，需要自定义HTTP+XML格式的请求消息编码器。

2. HTTP客户端发起连接，通过HTTP协议栈发送HTTP请求消息

利用Netty的HTTP协议栈，可以支持HTTP链路建立和请求消息的发送，所以不需要额外的开发，直接重用Netty的能力即可。
3. HTTP服务端对HTTP+XML请求消息进行解码，解码成请求POJO

HTTP服务端需要将HTTP+XML格式的订购请求消息解码为订购请求POJO对象，同时获取HTTP请求消息头信息。利用Netty的HTTP协议栈服务端，可以完成HTTP请求消息的解码。如果消息体为xml格式，netty无法转换pojo。需要在netty协议栈的基础上扩展实现。
 
4. 服务端构造应答消息并编码，通过HTTP+XML方式返回客户端

服务端对订购消息出路后，重新将其封装为xml，通过http应答消息体携带给client，netty的http协议不支持直接将pojo对象的应答消息以xml方式发送，需要定制

5. 客户端对HTTP+XML响应消息进行解码，解码成响应POJO

http客户端需要将http+xml格式的应答消息解码为订购pojo对象，同时能够应答消息的http头信息，netty的协议栈不支持自动的消息解码
 
 
# XML绑定框架JiBX

使用 JiBX 的过程分成两个过程，一个是 binding compiler，另一个是 binding runtime。binding compiler 是一个前期准备过程，包括定义绑定定义文挡，定义与 XML 绑定在一起的 Java 对象，然后编译。在这个过程，JiBX 比其他项目在操作上要简单，不用定义 DTD 和 Schema，缺点是需要自己定义 Java 程序。binding runtime 是使用 binding compiler 编译好的 Java class 处理 XML 数据。

**在运行JiBX时候，它使用了一个技术XPP(Xml Pull Parsing)**

### 步骤
1. 绑定XML文件，就是映射XML文件与java对象直接的对应关系
2. 运行时，实现java实例与xml文件的互相转换。

### 概念
- Unmarshal 数据分解

将xml文件转换为java对象

- Marshal 数据编排

marshal则是将java对象编排成规范的xml文件。

JiBX在Marshal/Unmarshal上如此高效，是由于其使用了XPP技术，而不是使用基于树形（tree-based）方式，将整个文档写入内存，然后进行操作的DOM，也不是使用基于事件流(event stream)的SAX(Simple API for Xml)。
**XPP使用的是不断增加的数据流处理方式，同时允许在解析XML文件时中断**


### JiBX类库使用

```java
	//根据Order的CLass构造BindingFactory对象
    factory = BindingDirectory.getFactory(Order.class);
    //构造新的StringWriter对象
	writer = new StringWriter();
	//通过IBingFactory构造Marshallling上下文，最后通过MarshallingDocument将Order序列化为StringWriter
	IMarshallingContext mctx = factory.createMarshallingContext();
	mctx.setIndent(2);
	mctx.marshalDocument(order, CHARSET_NAME, null, writer);
	//通过StringWriter的toString方法可返回String类型的XML对象
	String xmlStr = writer.toString();
	writer.close();

```


### HTTP+XML编码框架

- HTTP+XML请求消息编码类

HTTP+XML协议将请求发送给客户端，对业务零入侵的话，需要协议层和应用层解耦。

eg：通过HTTP消息头携带业务自定义字段，应该允许业务利用Netty的HTTP协议栈接口自行构造私有的HTTP消息头

HTTP+XML的协议编码依旧采用ChannelPipeline中增加对应的编码handler类实现
1. HttpXmlRequestEncoder

```java
public class HttpXmlRequestEncoder extends
	AbstractHttpXmlEncoder<HttpXmlRequest> 
```
2. 父类AbstractHttpXmlEncoder

- 父类AbstractHttpXmlEncoder

```java
public abstract class AbstractHttpXmlEncoder<T> extends
	MessageToMessageEncoder<T> 
```
其包含两个成员变量FullHttpRequest和编码对象Object,用于实现和协议栈之间的解耦
-

- HTTP+XML请求消息解码类

HTTP服务端接收到了HTTP+XML请求消息后，需要从HTTP消息体中获取请求码流，通过JiBX框架对其反序列化，得到请求POJO对象，然后对结果进行封装，回调到业务handler对象。
业务得到的就是解码后的POJO和HTTP消息头

1. HttpXmlRequestDecoder

```java
public class HttpXmlRequestDecoder extends
	AbstractHttpXmlDecoder<FullHttpRequest>
```

构造函数其有两个参数，分别为
- 需要解码的对象的类型信息
- 是否打印HTTP消息体码流的码流开关，码流开关默认关闭

2. AbstractHttpXmlEncoder
```java
public abstract class AbstractHttpXmlEncoder<T> extends
```

- HTTP+XML响应消息编码类

对于响应，它将业务处理后的POJO对象丢给HTTP+XML协议栈，由基础的协议栈进行后续的处理。为了降低业务定制开发的难度，首先封装一个全新的HTTP XML应答对象。

1. HttpXmlResponse

它由两个成员对象 FullHttpResponse， Object ，Object就是业务需要发送的应答POJO对象

2. HttpXmlRequestEncoder 应答消息编码类

```java
public class HttpXmlRequestEncoder extends
	AbstractHttpXmlEncoder<HttpXmlRequest> 
``` 

3. HttpXmlResponseDecoder 应答消息解码类

```java
public class HttpXmlResponseDecoder extends
	AbstractHttpXmlDecoder<DefaultFullHttpResponse> 
```
客户端收到Http+xml应答消息后，对消息进行解码，获取HttpXmlResponse对象
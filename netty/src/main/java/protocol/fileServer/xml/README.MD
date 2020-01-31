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

- Unmarshal 数据分解
- Marshal 数据排便
 
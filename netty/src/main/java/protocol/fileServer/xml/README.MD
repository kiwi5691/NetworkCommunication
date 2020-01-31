# Http服务端场景

用户订购系统，client填写订单，通过http client向server发送订购请求，将请求消息放在http消息体中，以xml承载。

以http+xml通信，双方使用http1.1协议，连接类型为CLOSE方式，即双发交互完毕http server主动关闭链路，client也关闭链路。

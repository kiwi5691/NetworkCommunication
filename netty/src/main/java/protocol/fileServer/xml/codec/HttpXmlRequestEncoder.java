package protocol.fileServer.xml.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.net.InetAddress;
import java.util.List;

public class HttpXmlRequestEncoder extends
	AbstractHttpXmlEncoder<HttpXmlRequest> {

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpXmlRequest msg,
	    List<Object> out) throws Exception {
	//调用父类的encode0，将POJO对象ORDER实例序列化为XML字符串，随后封装为Netty的ByteBuf
    ByteBuf body = encode0(ctx, msg.getBody());
	FullHttpRequest request = msg.getRequest();
	//对消息头判断，如果业务自定义和定制了消息头，则使用业务的，否则构造新的HTTP消息头
	if (request == null) {
		//构造和设置默认的HTTP消息头，一般注重消息体本身，直接写死，如果要产品化可以做出XML配置文件。允许业务自定义配置，定制灵活性
	    request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
		    HttpMethod.GET, "/do", body);
	    HttpHeaders headers = request.headers();
	    headers.set(HttpHeaders.Names.HOST, InetAddress.getLocalHost()
		    .getHostAddress());
	    headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
	    headers.set(HttpHeaders.Names.ACCEPT_ENCODING,
		    HttpHeaders.Values.GZIP.toString() + ','
			    + HttpHeaders.Values.DEFLATE.toString());
	    headers.set(HttpHeaders.Names.ACCEPT_CHARSET,
		    "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
	    headers.set(HttpHeaders.Names.ACCEPT_LANGUAGE, "zh");
	    headers.set(HttpHeaders.Names.USER_AGENT,
		    "Netty xml Http Client side");
	    headers.set(HttpHeaders.Names.ACCEPT,
		    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	}
	//很重要，由于请求消息消息体不为空，也没使用Chunk方式
	//所以在HTTP消息头中设置消息体的长度Content-Length。完成消息体的XML序列化后将重新构造的HTTP请求消息加入到out中
	//由后续Netty的Http请求编码器继续对HTTP请求消息进行编码
	HttpHeaders.setContentLength(request, body.readableBytes());
	out.add(request);
    }

}

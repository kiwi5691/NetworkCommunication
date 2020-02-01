/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package protocol.fileServer.xml.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author Lilinfeng
 * @date 2014年3月1日
 * @version 1.0
 */
public class HttpXmlResponseEncoder extends
	AbstractHttpXmlEncoder<HttpXmlResponse> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.netty.handler.codec.MessageToMessageEncoder#encode(io.netty.channel
     * .ChannelHandlerContext, java.lang.Object, java.util.List)
     */
    protected void encode(ChannelHandlerContext ctx, HttpXmlResponse msg,
	    List<Object> out) throws Exception {
	ByteBuf body = encode0(ctx, msg.getResult());
	FullHttpResponse response = msg.getHttpResponse();
	//对应答消息进行判断，如果业务侧已经构造了HTTP应答消息，则利用业务已有的应答消息重新复制一个新的HTTP应答消息
	if (response == null) {
		//无法重用业务测自定义HTTP应答消息的主要原因，是因为Netty的DefaultFullHttpResponse没有提供动态设置消息体content的接口，
		// 只能在第一次构造的时候设置内容
	    response = new DefaultFullHttpResponse(HTTP_1_1, OK, body);
	} else {
	    response = new DefaultFullHttpResponse(msg.getHttpResponse()
		    .getProtocolVersion(), msg.getHttpResponse().getStatus(),
		    body);
	}
	//设置消息体内容格式为text/xml。然后在消息头中设置消息体的长度。
	response.headers().set(CONTENT_TYPE, "text/xml");
	setContentLength(response, body.readableBytes());
	//把编码后的DefaultFullHttpResponse对象添加到编码结果列表中，由后续Netty的Http编码类进行二次编码
	out.add(response);
    }
}

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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * @author Lilinfeng
 * @date 2014年3月1日
 * @version 1.0
 */
public abstract class AbstractHttpXmlDecoder<T> extends
	MessageToMessageDecoder<T> {

    private IBindingFactory factory;
    private StringReader reader;
    private Class<?> clazz;
    private boolean isPrint;
    private final static String CHARSET_NAME = "UTF-8";
    private final static Charset UTF_8 = Charset.forName(CHARSET_NAME);

    protected AbstractHttpXmlDecoder(Class<?> clazz) {
	this(clazz, false);
    }

    protected AbstractHttpXmlDecoder(Class<?> clazz, boolean isPrint) {
	this.clazz = clazz;
	this.isPrint = isPrint;
    }

    protected Object decode0(ChannelHandlerContext arg0, ByteBuf body)
	    throws Exception {
    	//从HTTP消息体中获取请求码流，然后通过JiBx类库将XML转换为POJO
	factory = BindingDirectory.getFactory(clazz);
	String content = body.toString(UTF_8);
	//最后根据码流开关决定是否打印消息体码流。增加码流开关是为了方便定位，在实际项目中需要打印到日志中
	if (isPrint)
	    System.out.println("The body is : " + content);
	reader = new StringReader(content);
	IUnmarshallingContext uctx = factory.createUnmarshallingContext();
	Object result = uctx.unmarshalDocument(reader);
	reader.close();
	reader = null;
	return result;
    }

    /**
     * Calls {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} to
     * forward to the next {@link ChannelHandler} in the {@link ChannelPipeline}
     * .
     * 
     * Sub-classes may override this method to change behavior.
     */
    @Skip
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	    throws Exception {
    	//如果解码发生异常，要判断StringReader是否已经关闭，如果没有关闭，则关闭输入流并通知JVM对其进行垃圾回收
	// 释放资源
	if (reader != null) {
	    reader.close();
	    reader = null;
	}
    }
}

package testNetty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;


/*
* 继承自ChannelHandlerAdapter，用于对网络事件进行读写操作
* 通常只需要关注channelRead和exceptionCaught方法
 */
public class TimeServerHandler extends ChannelHandlerAdapter{

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg)
            throws Exception{
        //将msg转换成了netty的ByteBufd对象
        ByteBuf buf = (ByteBuf) msg;
        //readableBytes获取缓冲区可读的字节数，根据可读的字节数创建byte数组
        byte[] req = new byte[buf.readableBytes()];
        //通过readBytes方法将缓冲区中的字节数组复制到新建的byte数字中
        buf.readBytes(req);
        //通过new Stringg构造函数获取请求消息。
        String body = new String(req,"UTF-8");
        System.out.println("The time server receive order:" + body);

        //通过ChannelHandlerContext的write方法异步发送应答消息给客户端
        ByteBuf resp = Unpooled.copiedBuffer(new Date().toString().getBytes());
        ctx.write(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception{
    //调用了ChannelHandlerContextd的flush方法。将消息发送队列中的消息写入到SocketChannel发送给对方
        //从性能，为了防止频繁地唤醒Selector进行消息发送。
        //netty的write并不直接写入SocketChannel，调用write只是把待发送的消息放到发送缓冲数组中，再通过flush方法
        //将发送缓冲区的消息全部写到SocketChannel中
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        //发生异常的时候，关闭ChannelHandlerContext，释放ChannelHandlerContext相关联的句柄等资源
        ctx.close();
    }
}
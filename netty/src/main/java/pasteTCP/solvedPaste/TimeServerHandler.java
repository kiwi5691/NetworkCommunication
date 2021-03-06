package pasteTCP.solvedPaste;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;


public class TimeServerHandler extends ChannelHandlerAdapter{
    private int counter;

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception{

        //不需要对请求消息进行编码。也不需要处理读半包问题
        String body = (String)msg;
        System.out.println("the time server receive order:"+body + ";the counter is:" + ++counter);
        String currentTime = "query time order".equalsIgnoreCase(body)? new Date().toString():"BAD ORDER";
        currentTime = currentTime + System.getProperty("line.separator");
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable throwable){
        ctx.close();
    }
}
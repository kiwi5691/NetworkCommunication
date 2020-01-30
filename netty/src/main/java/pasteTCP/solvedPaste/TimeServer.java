package pasteTCP.solvedPaste;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;


public class TimeServer {

    public void bind(int port) throws Exception {
        //配置服务端的NIO线程组
        /*
         *   创建了两个NioEventLoopGroup实例
         * NioEventLoopGroup是个线程组，其包含了一组NIO线程，用于网络事件的处理，实际为两个REACTOR线程组
         * 一组处理服务端接收客户端的连接
         * 另一组用于进行SocketChannel的网络读写
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            /*
             * ServerBootstrap是Netty启动NIO服务端的辅助启动类
             */
            ServerBootstrap b = new ServerBootstrap();
            //将两个NIO线程组当做入参传递到ServerBootstrap中
            b.group(bossGroup,workerGroup)
                    //创建的Channel为NioServerSocketChannel对应NIO的ServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    //配置NioServerSocketChannel的TCP参数，设置backlog为1024
                    .option(ChannelOption.SO_BACKLOG,1024)
                    //绑定IO事件的处理类ChildChanelHandler，类似于Reactor模式的handler类，处理网络IO事件，记录日志，对消息进行编解码
                    .childHandler(new ChildChanelHandler());
            //绑定端口，同步等待成功
            //调用其同步阻塞方法sync等待绑定操作完成
            //之后netty返回ChannelFuture，类似j.u.c的Future，用于异步操作的通知回调

            ChannelFuture f = b.bind(port).sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();

        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChanelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            //新增加了两个解码器
            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
            socketChannel.pipeline().addLast(new StringDecoder());
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) throws Exception{
        int port = 8081;
        if(args !=null && args.length > 0 ){
            try{

                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }

        new TimeServer().bind(port);
    }
}
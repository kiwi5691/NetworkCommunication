package aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;


public class ServerAio {

    int port;
    CountDownLatch latch;
    //首先创建一个异步的服务端通道AsynchronousServerSocketChannel
    AsynchronousServerSocketChannel asynServerSocketChannel;
    public ServerAio(int port){
        this.port = port;
        try {
            asynServerSocketChannel = AsynchronousServerSocketChannel.open();
            //然后调用AsynchronousServerSocketChannel的bind方法绑定监听端口，如果未被占用，绑定成功
            asynServerSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("The time server is start in port:"+port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        //初始化CountDownLatch对象，其作用是在完成一组正在执行的操作之前，允许当前的线程一直阻塞
        //这个demo让线程在此阻塞，防止服务端执行完成退出。实际项目不需要启动独立线程处理AsynchronousServerSocketChannel
        latch = new CountDownLatch(1);
        doAccept();
        try{
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    private void doAccept() {
        /*这里用于接收客户端的连接，由于是异步操作，
        * 可传递一个CompletionHandler<AsynchronousSocketChannel,? super A>类型的handler实例接收accept操作成功的通知消息。
        * 这里使用AcceptCompletionHandler实例作为handlerl来接收通知消息
        */
        asynServerSocketChannel.accept(this,new AcceptCompletionHandler());
    }

    public static void main(String[] args){
        //创建异步的时间服务器处理类，然后启动线程将ServerAio拉起
        ServerAio serverAio = new ServerAio(8080);
        serverAio.start();
    }
}
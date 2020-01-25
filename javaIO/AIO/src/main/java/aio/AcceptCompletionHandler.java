package aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/*
*CompletionHandler有两个方法
*     void completed(V result, A attachment);
*     void failed(Throwable exc, A attachment);
 */
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, ServerAio> {
    /*
    * 从attachment获取成员变量AsynchronousSocketChannel
    * 然后继续调用其accept方法
    * 这里的再次调用accept是因为，如果有新的客户端连接接入，系统将回调我们传入的CompletionHandler实例的completed方法
    * 表示新的客户端已经接入成功，因为一个AsynchronousSocketChannel可以接收成千上万的客户端
    * 所以需要继续调用accept方法，接收其他的客户端连接，最终形成一个循环，每当一个客户端连接成功后，再异步接收新的客户端连接
     */
    @Override
    public void completed(AsynchronousSocketChannel result, ServerAio attachment) {
        attachment.asynServerSocketChannel.accept(attachment,this);
     // 服务端接收客户端消息，创建新ByteBuffer，预分配1m缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        /*
        * 异步read参数
        *ByteBuffer dst 接收缓冲区，从异步channel中读取数据包
        * A attachment 异步channel携带的附件，通知回调的时候入参使用
        * CompletionHandler<Integer,? super A>   接收通知回调的业务handler
         */
        result.read(buffer,buffer,new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, ServerAio attachment) {

        exc.printStackTrace();
        attachment.latch.countDown();
    }
}
package aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;


//这里使用了大量内部匿名类如果常用函数式编程，应该好理解
public class AioClient implements CompletionHandler<Void, AioClient> {

    CountDownLatch latch;
    AsynchronousSocketChannel socketChannel;
    public void start(){
        try {
            socketChannel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        latch = new CountDownLatch(1);
        //这里的connect有两个参数
        //A attachment ：AsynchronousSocketChannel的附件，用于回调时作为入参被传递，调用者自定义
        //CompletionHandler<Void,? super A> handler  ：异步操作h回调通知接口，由调用者实现
        //这两个参数都使用AioClient类自身，因为它实现了CompletionHandler接口
        socketChannel.connect(new InetSocketAddress("127.0.0.1",8080),this,this);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//连接成功后的回调
    @Override
    public void completed(Void result, AioClient attachment) {
        byte[] bytes = "queryTimeOrder".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        socketChannel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if(attachment.hasRemaining()){
                    socketChannel.write(writeBuffer,writeBuffer,this);
                }else{
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    socketChannel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            byte[] bytes1 = new byte[attachment.remaining()];
                            attachment.get(bytes1);
                            String body  = new String(bytes1);
                            System.out.println("receive from server:"+body);
                            //让此线程执行完毕
                            latch.countDown();
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                //关闭链路
                                socketChannel.close();
                                //让此线程执行完毕
                                latch.countDown();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        });
    }

    @Override
    public void failed(Throwable exc, AioClient attachment) {
        exc.printStackTrace();
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        latch.countDown();
    }

    public static void main(String[] args){
        //通过独立的I/O线程创建异步服务器客户端
        //实际不需要独立的线程创建异步连接对象，因为底层都是通过JDK系统回调实现的
        AioClient client = new AioClient();
        client.start();
    }

}
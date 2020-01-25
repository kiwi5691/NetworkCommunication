package aio;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;


public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

    private AsynchronousSocketChannel channel;
    //构造方法，将AsynchronousSocketChannel通过参数传递到ReadCompletionHandler中当作成员变量来使用
    //主要用于半包消息和发送应答
    public ReadCompletionHandler(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
       //读取消息后的处理，先flip操作
        attachment.flip();
        byte[] body = new byte[attachment.remaining()];
        attachment.get(body);
        try{
            String req =  new String(body,"UTF-8");
            System.out.println("AIO server recive order:"+req);
            doWrite("AIO server recive order:"+req);
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    private void doWrite(String s) {
        if(StringUtils.isNotBlank(s)){
            byte[] bytes = s.getBytes();
            ByteBuffer writeByteBuffer = ByteBuffer.allocate(bytes.length);
            writeByteBuffer.put(bytes);
            writeByteBuffer.flip();
            //复制到缓冲区writeByteBuffer，最后调用AsynchronousSocketChannel的异步write方法，同read参数一样
            channel.write(writeByteBuffer, writeByteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                //这里直接实现write方法的异步接口回调CompletionHandler
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
             //如果有剩余的字节可写，则没发送完成，继续发送
                    if(attachment.hasRemaining()){
                        channel.write(attachment,attachment,this);
                    }
                }

                @Override
                //发生异常时，对异常Throwable进行判断，如果是I/O异常，就关闭链路，释放资源
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        exc.printStackTrace();
        try {
            this.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
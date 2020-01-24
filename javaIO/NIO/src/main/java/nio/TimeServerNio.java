package nio;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeServerNio {
    private  boolean stop;
    Selector selector = null;

    public static void main(String[] args){

        new TimeServerNio().start();
    }

    public void start(){
        ServerSocketChannel acceptorSvr = null;
        try {
            int port = 8080;
            //1.打开ServerSocketChannel，用于监听客户端的连接，它是所有客户端连接的父管道
            acceptorSvr = ServerSocketChannel.open();
            //2.绑定监听端口，设置连接为非阻塞模式
            acceptorSvr.configureBlocking(false);
            acceptorSvr.socket().bind(new InetSocketAddress("127.0.0.1",port),1024);
            //3.创建多路复用器
            selector = Selector.open();
            //4.将ServerSocketChannel注册到多路复用器Selector上。监听ACCEPT事件
            acceptorSvr.register(selector,SelectionKey.OP_ACCEPT);

            /**
             * 上方的行为构造方法，在构造方法中进行资源初始化，创建多路复用器Selector。ServerSocketChannel。对channel设置为异步非阻塞模式
             * 它的backlogs设置为1024
             * 系统资源初始化成功后，将ServerSocketChannel注册到Selector，监听SelectionKey.OP_ACCEPT操作位，
             * 如果失败（port bind used），则退出
             */

            System.out.println("Timeserver start!!");

            //5.多路复用器在无线循环体内轮询准备就绪的key
            SelectionKey key;
            while(!stop){

                selector.select(1000);
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey>  iterator = selectionKeySet.iterator();
                /*
                *休眠时间为1s，隔一秒被唤醒一次
                * 当处于就绪状态的Channel时，selector将返回就绪的Channel的selectionKeySet集合
                * 通过对就绪的Channel集合进行迭代，可以进行网络异步写操作
                 */
                while(iterator.hasNext()){
                    key = iterator.next();
                    iterator.remove();
                    try{
                        handleInput(key);
                    }catch (Exception e){
                        if(key != null){
                            key.cancel();
                            if(key.channel() !=null){
                                key.channel().close();
                            }
                        }
                    }
                }

            }
            if(selector !=null){
                try{
                    selector.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private  void handleInput(SelectionKey key) throws IOException{
        if(key.isValid()){
        //接受新接入的消息
            if(key.isAcceptable()){

                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                //6.多路复用器监听到有新客户接入，处理新的接入请求，完成TCP三次握手，建立物理链路
                SocketChannel sc = ssc.accept();
                //7.设置客户端链路为非阻塞模式
                sc.configureBlocking(false);
                //8.将新接入的客户端连接注册到多路复用器上，监听读操作，读取客户端发送的网络消息
                sc.register(selector,SelectionKey.OP_READ);
            /*
            * 处理新接入客户端请求消息
            * 根据SelectionKey的操作位进行判断即可获知网络事件的类型
            * 通过ServerSocketChannel的accept接收客户端的连接请求并创建SocketChannel实例
            * 上述操作后，相当于结束了TCP三次握手，TCP物理链路正式建立
            * 这里需要将新创建的SocketChannel设置为异步非阻塞，同时也可设置TCP参数（TCP接收和发送缓冲区的大小）
             */
            }
            if(key.isReadable()){
                SocketChannel sc = (SocketChannel)key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //9.读取客户端请求消息到缓冲区
                int readBytes = sc.read(readBuffer);
                if(readBytes>0){
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    //10.对消息解码
                    String body = new String(bytes,"UTF-8");
                    System.out.println("the time server receive order:"+body);
                    doWrite(sc,"the time server receive order:"+body);
                    /*
                    *读取客户端的请求消息，先创建一个ByteBuffer，由于无法事先知道客户端发送的码流大小。先开辟一个1k的缓冲区
                    * 然后调用SocketChannel的read方式读取请求码流。
                    * 这里的SocketChannel设置为异步非阻塞模式。因此它的read是非阻塞的。
                    * 使用返回值进行判断，看读取到的字节数，返回值：
                    * >0 读到了字节，对字节j进行编解码
                    * =0  没有读取到字节，属于正常场景，忽略
                    * =-1  链路已经关闭，需要关闭SocketChannel，释放资源
                     */
                    /*
                    * 对readBuffer 进行flip操作
                    * 将其缓冲区的limit设置为position，position设置为0，用于后续对缓冲区的读取进行操作。
                    * 然后根据缓冲区可读的字节个数创建字节数组
                    * 调用ByteBuffer的get操作将缓冲区可读的字节数字复制到新创建的字节数组
                    * 最后调用字符串的构造函数创建请求消息体并打印
                     */
                }else if(readBytes <0){
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    private void doWrite(SocketChannel sc, String response) throws IOException{
        if(StringUtils.isNotBlank(response)){
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put(response.getBytes());
            byteBuffer.flip();
            //11.调用SocketChannel的异步write接口，将消息异步发给客户端
            sc.write(byteBuffer);
            System.out.println("send to client:"+response);

            /*
            *调用ByteBuffer的put操作将字节数组复制到缓冲区，然后对缓冲区进行flip操作
            * 最后调用SocketChannel的write方法将缓冲区的字节数组发送出去。
            * 由于SocketChannel是异步非阻塞，并不会一次性把需要的字节发送完。
            * 此时会出现‘写半包’问题，我们需要组成写操作，不断轮询Selector将没有发送完的ByteBuffer发送完毕
            * 可使用ByteBuffer的hasRemaining()方法判断消息是否完成
             */


    //如果发送区TCP缓冲区满，会导致半包。此时需要注册监听操作位。循环写。直到整包消息写入TCP缓冲区
        }
    }


    public void stop(){
        this.stop = true;
    }


}

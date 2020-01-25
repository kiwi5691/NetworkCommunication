package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientNio {

    Selector selector = null;
    SocketChannel socketChannel = null;
    String host = "127.0.0.1";
    int port = 8080;
    boolean stop  = false;
    /*
    *构造函数用于初始化NIO的多路复用器和SocketChannel对象。
    * 创建SocketChannel之后，需要将其设置为异步非阻塞模式
    * 也可设置SocketChannel的TCP参数，如接收和发送的TCP缓冲区大小
     */
    public static void main(String[] args){
        TimeClientNio clientNio = new TimeClientNio();
        clientNio.init();
        clientNio.start();

    }

    public void init(){
        try {
            //5.创建多路复用器
            selector = Selector.open();
            //1.打开SocketChannel绑定客户端本地地址
            socketChannel = SocketChannel.open();
            //2.设置SocketChannel为非阻塞模式,可设置客户端的TCP参数
            socketChannel.configureBlocking(false);
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start(){
        try{
            doConnect();
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        /*
         *用于发送连接请求。如果是成功的，所以不需要做重连操作，所以在循环之前
         */

        try {

            /*
             *在循环体中轮询多路复用器Selector，当有就绪的Channel时，执行handleInput(key)方法
             */


            //7.多路复用器的无限循环体轮询准备就绪的Key
            while(!stop) {
                selector.select(1000);
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeySet.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    try {
                        System.out.println(key.toString());
                        handleInput(key);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if(key.isValid()){
            //判断是否连接成功，说明服务端已经返回ACK应答消息
            SocketChannel socketChannel = (SocketChannel) key.channel();
            //8.接收connect事件进行处理
            if(key.isConnectable()){
                //9.判断连接结果，如果成功，注册事件到多路复用器
                if(socketChannel.finishConnect()){
                    //10.注册事件到多路复用器
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    doWrite(socketChannel);
                }else{
                    System.out.println("connect error");
                    System.exit(1);
                }
            }
            if(key.isReadable()){
                /*
                *如果客户端接收到了服务端的应答消息，则SocketChannel是可读的。同Server。预分配1M缓冲区用于读取应答
                 */
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                //11.异步读取客户端请求消息到缓冲区
                int byteLength = socketChannel.read(byteBuffer);
                if(byteLength>0){
                    //12.对byteBuffer解码，如果有半包消息接收缓冲区Reset，继续读取后续报文，将对解码成功的消息打印
                    byteBuffer.flip();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    System.out.println("client received:"+ new String(bytes,"UTF-8"));
                    stop = true;
                }else if(byteLength<0){
                    //对端链路关闭
                    key.cancel();
                    socketChannel.close();
                }

            }
        }
    }

    private void doConnect() throws IOException {
        //如果直接连接成功，则注册到多路复用器上，发送请求消息，请应答


        //3.异步连接客户端
        //4.判断连接是否成功，if success。则直接注册读状态位到多路复用器中，如果不成功
        //异步连接，返回false说明客户端已经发送sync包，服务端没有返回ack包，物理链路还没有建立成功
        if(socketChannel.connect(new InetSocketAddress(host,port))){
            socketChannel.register(selector,SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else{
            //5.向多路复用器注册OP_CONNECT状态位，监听TCP的ack应答
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
            /*
            *当服务端返回TCP syn-ack消息后，Selector就能够轮询到这个SocketChannel连接就绪状态。
             */
        }
    }

    private void doWrite(SocketChannel socketChannel) throws IOException {
        /*
        *发送是异步的所以会存在‘半包写’问题。最后通过hasRemaining对发送结果进行判断
         */
        String command = "firstQuery";
        byte[] bytes = command.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
    //12.调用SocketChanneld的异步write接口，将消息异步发送给客户端
        socketChannel.write(byteBuffer);
        if(!byteBuffer.hasRemaining()){
            System.out.println("send to server success!");
        }
    }


}
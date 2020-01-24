package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by wangdecheng on 24/03/2018.
 */
public class TimeClientNio {

    Selector selector = null;
    SocketChannel socketChannel = null;
    String host = "127.0.0.1";
    int port = 8080;
    boolean stop  = false;

    public static void main(String[] args){
        TimeClientNio clientNio = new TimeClientNio();
        clientNio.init();
        clientNio.start();

    }

    public void init(){
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
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

        try {
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
            SocketChannel socketChannel = (SocketChannel) key.channel();
            if(key.isConnectable()){
                if(socketChannel.finishConnect()){
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    doWrite(socketChannel);
                }else{
                    System.out.println("connect error");
                    System.exit(1);
                }
            }
            if(key.isReadable()){
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                int byteLength = socketChannel.read(byteBuffer);
                if(byteLength>0){
                    byteBuffer.flip();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    System.out.println("client received:"+ new String(bytes,"UTF-8"));
                    stop = true;
                }else if(byteLength<0){
                    key.cancel();
                    socketChannel.close();
                }

            }
        }
    }

    private void doConnect() throws IOException {
        if(socketChannel.connect(new InetSocketAddress(host,port))){
            socketChannel.register(selector,SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else{
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel socketChannel) throws IOException {
        String command = "firstQuery";
        byte[] bytes = command.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        if(!byteBuffer.hasRemaining()){
            System.out.println("send to server success!");
        }
    }
}
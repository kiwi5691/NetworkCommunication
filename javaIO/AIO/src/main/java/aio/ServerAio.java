package aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;


public class ServerAio {

    int port;
    CountDownLatch latch;
    AsynchronousServerSocketChannel asynServerSocketChannel;
    public ServerAio(int port){
        this.port = port;
        try {
            asynServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynServerSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("The time server is start in port:"+port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        latch = new CountDownLatch(1);
        doAccept();
        try{
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    private void doAccept() {
        asynServerSocketChannel.accept(this,new AcceptCompletionHandler());
    }

    public static void main(String[] args){
        ServerAio serverAio = new ServerAio(8080);
        serverAio.start();
    }
}
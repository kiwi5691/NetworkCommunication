package bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeServer{

    public static void main(String[] args) throws IOException{
        int port = 8080;
        //根据传入参数监听端口，无参默认8080
        if(args != null && args.length >0 ){
            try{
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e){

            }
        }

        ServerSocket server = null;
        try{
            server = new ServerSocket(port);
            System.out.println("the time server is start in port :"+port);
            Socket socket = null;
            //阻塞ServerSocket的accept上面
            while (true){
                socket = server.accept();
                new Thread(new TimeserverHandler(socket)).start();
            }
        }finally {
            if(server!=null){
                System.out.println("the time server close");
                server.close();
                server = null;
            }
        }
    }
}
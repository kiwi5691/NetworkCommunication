import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeServer {

    public static void main(String[] args) throws IOException{
        int port = 8080;
        if(args != null && args.length >0 ){
            try{
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e){
//默认
            }
        }

        ServerSocket server = null;
        try{
            server = new ServerSocket(port);
            System.out.println("the time server is start in port :"+port);
            Socket socket = null;
            TimeServerHandlerExcuterPool pool = new TimeServerHandlerExcuterPool(10,10);
            while (true){
                socket = server.accept();
                pool.executeTask(new TimeserverHandler(socket));
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
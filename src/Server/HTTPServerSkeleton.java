package Server;

import Server.ServerWorker;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServerSkeleton {
    static final int PORT = 6789;

    public static void main(String[] args) throws IOException {
        
        ServerSocket serverConnect = new ServerSocket(PORT);
        System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
        FileOutputStream logFileOutputStream = new FileOutputStream(new File("src/Server/log.txt"));
        logFileOutputStream.close();

        while(true)
        {
            Socket socket = serverConnect.accept();
            new ServerWorker(socket);

        }
        
    }
    
}

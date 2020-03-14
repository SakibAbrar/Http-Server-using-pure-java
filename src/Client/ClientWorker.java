package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientWorker extends Thread{
    Socket socket;
    InputStream socketInputStream;
    OutputStream socketOutputStream;
    String fileName;
    static int BYTE_CHUNK_SIZE = 4096;

    public ClientWorker(Socket socket, String fileName) throws IOException {
        this.socket = socket;
        socketOutputStream = socket.getOutputStream();
        socketInputStream = socket.getInputStream();
        this.fileName = fileName;
        start();
    }

    public void closeConnection() throws IOException{
        socketInputStream.close();
        socketOutputStream.close();
        socket.close();
    }

    public void sendFileInBytes(File file) throws IOException{
        InputStream inputStream = new FileInputStream(file);

        int bytesRead = 0;
        byte[] outputByte=new byte[BYTE_CHUNK_SIZE];
        int len;

        while(( len = inputStream.read(outputByte, 0, BYTE_CHUNK_SIZE )) > 0 ) {
            socketOutputStream.write( outputByte, 0, len );
        }
        socketOutputStream.flush();
    }

    public void run(){
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));
            PrintWriter printWriter = new PrintWriter(socketOutputStream);
            String str = "UPLOAD ";

            if(fileName==null){
                closeConnection();
                return;
            }

            File uploadFile = new File(fileName);

            if(!uploadFile.exists()){
                System.out.println("This file doesn't exist! Please enter a valid one!");
                str = str + fileName + " FTP/1.1 NO\r\n";
                printWriter.write(str);
                printWriter.flush();
                closeConnection();
                return;
            }

            str = str + fileName + " FTP/1.1 OK\r\n";
            System.out.println(str);
            printWriter.write(str);
            printWriter.flush();
            String response = bufferedReader.readLine();
            System.out.println(response);
            System.out.println("Sending File...");
            sendFileInBytes(uploadFile);
            System.out.println("File Sent Successfully!");

            closeConnection();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

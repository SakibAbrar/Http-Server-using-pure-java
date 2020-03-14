package Client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class HTTPClient {
    public static void main(String[] args) throws IOException {
        while (true){
            System.out.println("Write any valid filename to upload it to the server root:");
            Scanner scanner = new Scanner(System.in);
            String fileName = scanner.nextLine();
            if(fileName==null)
                continue;
            Socket clientSocket = new Socket("127.0.0.1", 6789);
            new ClientWorker(clientSocket, fileName);
        }
    }
}

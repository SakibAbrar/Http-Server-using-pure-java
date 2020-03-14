package Server;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class ServerWorker extends Thread {
    static int requestNo = 0;
    Socket socket;
    InputStream socketInputStream;
    OutputStream socketOutputStream;
    FileOutputStream logFileOutputStream;
    static int BYTE_CHUNK_SIZE = 4096;

    public ServerWorker(Socket socket) throws IOException {
        this.socket = socket;
        this.socketOutputStream = socket.getOutputStream();
        this.socketInputStream = socket.getInputStream();
        logFileOutputStream = new FileOutputStream(new File("src/Server/log.txt"), true);
        start();
    }

    public static String write404Html(){
        return "\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "\t\t<link rel=\"icon\" href=\"data:,\">\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<div id=\"main\">\n" +
                "\t\t\t<div class=\"fof\">\n" +
                "\t\t\t\t<h1>Error 404</h1>\n" +
                "\t\t\t</div>\n" +
                "\t\t</div>\n" +
                "\t</body>\n" +
                "</html>";
    }

    public static String wrteIndexPageHtml() throws IOException{
        FileInputStream fis = new FileInputStream(new File("index.html"));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while(( line = br.readLine()) != null ) {
            sb.append( line );
            sb.append( '\n' );
        }
        return sb.toString();
    }

    public static String writeValidHtml(File path){
        String content = "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "\t\t<link rel=\"icon\" href=\"data:,\">\n" +
                "\t</head>\n" +
                "\t<body>";
        String relativePath = path.getAbsolutePath();
        relativePath = relativePath.substring(new File(".").getAbsolutePath().length()-1);
        for(String file : path.list()){
            content = content + "<a href=\"http://127.0.0.1:6789/" + relativePath + "/";
            if(new File(file).isDirectory())
                content = content + "<strong>" + file + "/<strong>" +"\">" + file + "</a><br>";
            else
                content = content + file +"\">" + file + "</a><br>";
        }


        content = content + "\t</body>\n" +
                "</html>";
        return content;
    }

    public String getURL(String request){
        if(request == null)
            return null;

        String url = request.split(" ")[1];

        return url;
    }

    public void closeConnection() throws IOException{
        socketInputStream.close();
        socketOutputStream.close();
        logFileOutputStream.close();
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

    public void receiveFileInBytes(String fileName) throws IOException{
        File file = new File(fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        int bytesRead = 0;
        byte[] outputByte=new byte[BYTE_CHUNK_SIZE];
        int len;
        while(( len = socketInputStream.read(outputByte, 0, BYTE_CHUNK_SIZE )) > 0 ) {
            fileOutputStream.write( outputByte, 0, len );
        }
        fileOutputStream.flush();
    }

    @Override
    public void run() {
        try {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));
            PrintWriter pr = new PrintWriter(socketOutputStream);

            String input = bufferedReader.readLine();
            /// null somehow
            if(input==null){
                closeConnection();
                return;
            }

            /// GET request
            if(input.startsWith("GET")){
                String URL = getURL(input);
                URL = URL.replaceAll("%20", " ");
                System.out.println(URL);
                String content = null;
                Byte[] dateChunk = null;
                boolean isDirectory = false;
                boolean valid = false;
                /// index.html
                if(URL.equals("/")){
                    System.out.println("200 OK Valid, URL Request!\n");
                    content = wrteIndexPageHtml();
                    valid = true;
                    isDirectory = true;
                }

                /// valid url
                else if(new File(URL.substring(1)).exists()){
                    System.out.println("200 OK Valid, URL Request!\n");
                    valid = true;
                    URL = URL.substring(1);
                    URL = URL.replaceAll("%20", " ");
                    File path = new File(URL);

                    if(path.isDirectory()){
                        content = writeValidHtml(path);
                        isDirectory = true;
                    }

                    else
                        isDirectory = false;

                }

                /// invalid url
                else{
                    System.out.println("404 Page Not Found, Invalid URL Request\n");
                    content = write404Html();
                    valid = false;
                }

                /// A valid URL
                if(valid){

                    /// It is a directory
                    if(isDirectory){
                        String request = "Request " + ++requestNo + ": \n" + input + "\n\n";
                        logFileOutputStream.write(request.getBytes());
                        String response = "HTTP/1.1 200 OK\r\n" + "Server: Java HTTP Server: 1.0\r\n"
                                + "Date: " + new Date() + "\r\n" + "Content-Type: text/html\r\n"
                                + "Content-Length: " + content.length() + "\r\n" + "\r\n" + "\n\n";
                        response = "Response: \n" + response;
                        logFileOutputStream.write(response.getBytes());
                        logFileOutputStream.flush();
                        pr.write("HTTP/1.1 200 OK\r\n");
                        pr.write("Server: Java HTTP Server: 1.0\r\n");
                        pr.write("Date: " + new Date() + "\r\n");
                        pr.write("Content-Type: text/html\r\n");
                        pr.write("Content-Length: " + content.length() + "\r\n");
                        pr.write("\r\n");
                        pr.write(content);
                        pr.flush();
                    }

                    /// It is a file
                    else{
                        File file = new File(URL);
                        String request = "Request " + ++requestNo + ": \n" + input + "\n\n";
                        logFileOutputStream.write(request.getBytes());
                        String response = "HTTP/1.1 200 OK\r\n" + "Server: Java HTTP Server: 1.0\r\n"
                                + "Date: " + new Date() + "\r\n" + "Content-Type: application/force-download\r\n"
                                + "Content-Length: " + file.length() + "\r\n" + "\r\n" + "\n\n";
                        response = "Response: \n" + response;
                        logFileOutputStream.write(response.getBytes());
                        logFileOutputStream.flush();
                        System.out.println(URL + " in files ");

                        pr.write("HTTP/1.1 200 OK\r\n");
                        pr.write("Server: Java HTTP Server: 1.0\r\n");
                        pr.write("Date: " + new Date() + "\r\n");
                        pr.write("Content-Type: application/force-download\r\n");
                        pr.write("Content-Length: " + file.length() + "\r\n");
                        pr.write("\r\n");
                        pr.flush();
                        sendFileInBytes(file);
                    }
                }

                /// Not a valid URL
                else{
                    String request = "Request " + ++requestNo + ": \n" + input + "\n\n";
                    logFileOutputStream.write(request.getBytes());
                    String response = "HTTP/1.1 404 Not Found\n" + "Server: Java HTTP Server: 1.0\r\n"
                            + "Date: " + new Date() + "\r\n" + "Content-Type: text/html\r\n"
                            + "Content-Length: " + content.length() + "\r\n" + "\r\n" + "\n\n";
                    response = "Response: \n" + response;
                    logFileOutputStream.write(response.getBytes());
                    logFileOutputStream.flush();
                    pr.write("HTTP/1.1 404 Not Found\r\n");
                    pr.write("Server: Java HTTP Server: 1.0\r\n");
                    pr.write("Date: " + new Date() + "\r\n");
                    pr.write("Content-Type: text/html\r\n");
                    pr.write("Content-Length: " + content.length() + "\r\n");
                    pr.write("\r\n");
                    pr.write(content);
                    pr.flush();
                }

            }

            else if(input.startsWith("UPLOAD")){
                if(input.endsWith("NO")){
                    System.out.println("Invalid UPLOAD request received!r\n");
                    closeConnection();
                    return;
                }
                pr.write("Valid UPLOAD Request Received!\r\n");
                pr.flush();
                System.out.println(input);
                String fileName = getURL(input);
                System.out.println("Valid Upload Request for" + fileName);
                receiveFileInBytes("root/" + fileName);
                System.out.println(fileName + " Recevied Successfully!");
                pr.flush();
            }

            closeConnection();
        }
        catch (Exception io){
            io.printStackTrace();
        }
    }
}

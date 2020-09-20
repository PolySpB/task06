import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

/**
 * Реализация сервера на сокетах.
 * На любой GET запрос сервер выдает список файлов и директорий в текущей директории.
 * На остальные запросы сервер выдает 404 ошибку.
 */

public class HttpServer {

    public static void main(String[] args) throws Throwable {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client accepted");
            new Thread(new SocketProcessor(socket)).start();
        }
    }

    /**
     * Обработчик запросов от сокета.
     */
    private static class SocketProcessor implements Runnable {

        private Socket socket;
        private InputStream is;
        private OutputStream os;

        private SocketProcessor(Socket socket) throws Throwable {
            this.socket = socket;
            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();
        }

        /**
         * Обработчик запросов считывает хэдеры. Если хедер содердит GET запрос,
         * выводим текущую директорию и список файлов в ней.
         * Если на сервер поступил не GET запрос, выдаем ошибку 404.
         */
        public void run() {
            try {
                if (readInputHeaders()) {
                    String directory = System.getProperty("user.dir");
                    writeResponse("Working Directory: ", directory);
                }
                else {
                    writeNotFoundResponse("Page Not Found");
                }

            } catch (Throwable t) {

            } finally {
                try {
                    socket.close();
                } catch (Throwable t) {

                }
            }
            System.out.println("Client processing finished");
        }

        private void writeResponse(String s, String directory) throws Throwable {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Date: " + new Date() + "\r\n" +
                    "Server: Apache/2.2.14\r\n" +
                    "Content-Type: text\r\n" +
                    "Content-Length:\r\n" +
                    "Connection: Closed\r\n\r\n";
            String directories = listDirectories(directory);
            String result = response + s + directories;
            os.write(result.getBytes());
            os.flush();
        }

        private String listDirectories(String directory) {
            File directoryFile = new File(directory);
            StringBuilder sb = new StringBuilder();
            sb.append(directory + "\n\n" + "Files:");
            for (File file : Objects.requireNonNull(directoryFile.listFiles())) {
                sb.append("\n" + file.getName());
            }
            return sb.toString();
        }

        private void writeNotFoundResponse(String s) throws Throwable {
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Date: " + new Date() + "\r\n" +
                    "Server: Apache/2.2.14\r\n" +
                    "Content-Type: text\r\n" +
                    "Content-Length:\r\n" +
                    "Connection: Closed\r\n\r\n";
            String result = response + s;
            os.write(result.getBytes());
            os.flush();
        }

        private boolean readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s = br.readLine();
            if (s == null || s.trim().length() == 0) {
                return false;
            }
            if (s.contains("GET")) {
                return true;
            }

            while (true) {
                s = br.readLine();
                if (s == null || s.trim().length() == 0) {
                    break;
                }
            }
            return false;
        }
    }
}



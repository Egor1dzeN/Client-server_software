import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;


public class Server {
    static ServerCommand server_thread;
    static ServerSocket server = null;
    static boolean work = true;

    public static void main(String[] args) {
        try {
            server = new ServerSocket(8081);
            String dir = System.getProperty("user.dir");
            String dir1 = dir + "\\chapters";

            // Создаем директорию (если она отсутствует), в которой будут храниться голосования
            File theDir = new File(dir1);
            if (!theDir.exists()) {
                Files.createDirectory(Path.of(dir1));
            }
            String dir2 = dir1 + "\\download";
            theDir = new File(dir2);
            if (!theDir.exists()) {
                Files.createDirectory(Path.of(dir2));
            }
            System.out.println("Сервер запущен!");
            server_thread = new ServerCommand();

            //Создаем поток для серверных команд
            Thread myThready = new Thread(server_thread);
            myThready.start();

            while (work) {
                try {
                    //Подключение клиента
                    Socket client = server.accept();
                    System.out.println("Подключился клиент!  " + client.getLocalAddress());

                    //Создаем поток для обработки клиентских команд
                    ClientCommand clientSock
                            = new ClientCommand(client);
                    new Thread(clientSock).start();
                } catch (SocketException e) {
                    break;
                }

            }
            System.out.println("Сервер выключился");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

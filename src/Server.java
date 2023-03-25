import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Server {
    private static Scanner scanner = new Scanner(System.in);
    static ServerCommand server_thread;
    static ServerSocket server = null;
    static boolean work = true;
    public static void delete(){
        String dir = System.getProperty("user.dir");
        String dir1 = dir+"\\local_copy_chapter";
        File file = new File(dir1);
        for(File file1:file.listFiles()){
            for(File file2:file1.listFiles())
                file2.delete();
            file1.delete();
        }
        //System.out.println("Ujnjd");
    }
    public static void save() throws IOException {
        String dir = System.getProperty("user.dir");
        String dir1 = dir+"\\chapters";
        File file = new File(dir1);
        String dir3 = dir+"\\local_copy_chapter";
        File theDir = new File(dir3);
        delete();
        for(File file1:file.listFiles()){
            String dir_copy = dir+"\\local_copy_chapter\\"+file1.getName();
            File file1_copy = new File(dir_copy);
            if(!file1_copy.exists()){
                //System.out.println(file1.getName());
                Files.createDirectory(Path.of(dir_copy));
                for(File file2:file1.listFiles()){
                    FileReader fr = new FileReader(file2);
                    BufferedReader reader = new BufferedReader(fr);
                    String line = reader.readLine();
                    List<String> lines = new ArrayList<>();
                    while (line != null) {
                        lines.add(line);
                        // считываем остальные строки в цикле
                        line = reader.readLine();
                    }
                    Path file_path = Paths.get("local_copy_chapter\\"+file1.getName()+"\\"+file2.getName());
                    Files.write(file_path,lines, StandardCharsets.UTF_8);
                }
            }
        }
        //System.out.println("Изменения приняты");
    }

    public static void main(String[] args) {
        try {
            server = new ServerSocket(8081);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String dir = System.getProperty("user.dir");
            String dir1 =dir+"\\chapters";
            File theDir = new File(dir1);
            if (!theDir.exists()){
                Files.createDirectory(Path.of(dir1));
            }
            String dir2 =dir1+ "\\download";
            theDir = new File(dir2);
            if (!theDir.exists()){
                Files.createDirectory(Path.of(dir2));
            }
            save();
            System.out.println("Сервер запущен!");
            server_thread = new ServerCommand();
            Thread myThready = new Thread(server_thread);
            myThready.start();

            while (work) {
                try {
                    Socket client = server.accept();
                    System.out.println("Подключился клиент!");
                    ClientCommand clientSock
                            = new ClientCommand(client);
                    new Thread(clientSock).start();
                }
                catch (SocketException e){
                    System.out.println("server is closed");
                    break;
                }

            }
            System.out.println("Сервер выключился");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

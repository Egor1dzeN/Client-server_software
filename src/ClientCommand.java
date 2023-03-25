import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ClientCommand implements Runnable {
    private final Socket clientSocket;
    private static String username = "";

    public ClientCommand(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // инициируем каналы общения в сокете, для сервера

            // канал записи в сокет следует инициализировать сначала канал чтения для избежания блокировки выполнения программы на ожидании заголовка в сокете
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            ObjectInputStream objInput=null;
            ObjectOutputStream objOutput = null;

            boolean isLogin = false;

            while (!clientSocket.isClosed()) {
                System.out.println(username);
                String entry = "";
                try {
                    entry = in.readUTF();
                }
                catch (SocketException e){
                    System.out.println("Собединение сброшено");
                    clientSocket.close();
                    break;
                }

                //System.out.println(entry);
                // инициализация проверки условия продолжения работы с клиентом
                // по этому сокету по кодовому слову - quit в любом регистре
                String words[] = entry.split(" ");
                System.out.println(words[0]+" "+words[1]);
                if (words[0].equals("exit"))
                    break;
                if (words[0].equals("login")) {
                     username= words[1].substring(3);
                    out.writeUTF("Вы успешно вошли, " + username);
                    isLogin = true;
                } else {
                    System.out.println(username);
                    if (!isLogin)
                        out.writeUTF("Вы не вошли в систему");
                    else if (words[0].equals("create") && words[1].equals("topic")) {
                        String topic = words[2].substring(3);

                        String dir = System.getProperty("user.dir");
                        dir += "\\chapters\\" + topic;
                        Files.createDirectory(Path.of(dir));
                        out.writeUTF("Раздел с именем " + topic + " создан");

                        System.out.println(words[0] + " " + words[1]);
                    } else if (words[0].equals("view") && words.length < 3) {
                        String dir = System.getProperty("user.dir");
                        dir += "\\chapters";
                        File dir4 = new File(dir); //path указывает на директорию
                        String ans = "";
                        for (File file : dir4.listFiles()) {
                            //if(file.isDirectory())
                            ans+= "Раздел - "+file.getName()+" ";
                            ans+=" Кол-во голосований - "+String.valueOf(file.listFiles().length)+"\n";
                        }
                        out.writeUTF(ans);
                    } else if (words[0].equals("view") && words.length >= 3) {
                        out.writeUTF("view");
                        String topic = words[1].substring(3);
                        String vote = words[2].substring(3);
                        String path_dir = "chapters\\"+topic+"\\"+vote+".txt";
                        File file = new File(path_dir);
                        FileReader fr = new FileReader(file);
                        BufferedReader reader = new BufferedReader(fr);
                        String line = reader.readLine();
                        List<String> lines = new ArrayList<>();
                        while (line != null) {
                            lines.add(line);
                            // считываем остальные строки в цикле
                            line = reader.readLine();
                        }
                        objOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                        objOutput.writeObject(lines);
                    } else if (words[0].equals("create") && words[1].equals("vote")) {
                        System.out.println(username);
                        System.out.println("asopodspaospodfpaod");
                        String topic = words[2].substring(3);
                        out.writeUTF("create");
                        System.out.println("peredal");
                        String name_vote = in.readUTF();
                        String topic_vote = in.readUTF();
                        int count_vote = Integer.parseInt(in.readUTF());
                        objInput = new ObjectInputStream(clientSocket.getInputStream());
                        List<String> list;
                        list = (List<String>) objInput.readObject();
                        List<String>lines = new ArrayList<>();
                        lines.add(topic_vote);
                        lines.addAll(list);
                        lines.add(username);
                        int i =1;
                        Path file =Paths.get("chapters\\"+topic+"\\"+name_vote+".txt");
                        Files.write(file,lines, StandardCharsets.UTF_8);
                        System.out.println(name_vote);
                        System.out.println(topic_vote);
                        System.out.println(count_vote);
                        out.writeUTF("Голосование успешно создано");
                    }
                    else if(words[0].equals("vote")){
                        out.writeUTF("vote");
                        String topic = words[1].substring(3);
                        String vote = words[2].substring(3);
                        String path_dir = "chapters\\"+topic+"\\"+vote+".txt";
                        File file = new File(path_dir);
                        FileReader fr = new FileReader(file);
                        BufferedReader reader = new BufferedReader(fr);
                        String line = reader.readLine();
                        List<String> lines = new ArrayList<>();
                        while (line != null) {
                            lines.add(line);
                            // считываем остальные строки в цикле
                            line = reader.readLine();
                        }
                        objOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                        objOutput.writeObject(lines);
                        String choice = in.readUTF();
                        System.out.println(choice);
                        System.out.println(lines.get(Integer.parseInt(choice)));
                        String wordss[] = lines.get(Integer.parseInt(choice)).split(" ");
                        System.out.println(wordss.length);
                        System.out.println(wordss[0]) ;System.out.println(wordss[1]);
                        int i =0;
                        //for(i = 0;i<wordss.length;++i);
                        //{
                        //    System.out.println(wordss[i]);
                        //}
                        int kol = Integer.parseInt(wordss[wordss.length-1])+1;
                        String str = "";
                        for(int j = 0;j<wordss.length-1;++j)
                        {
                            str += wordss[j]+" ";
                            //System.out.println(wordss[i]);
                        }
                        str+=kol;
                        lines.set(Integer.parseInt(choice),str);
                        Path file1 =Paths.get("chapters\\"+topic+"\\"+vote+".txt");
                        Files.write(file1,lines,StandardCharsets.UTF_8);
                    }else if(words[0].equals("delete")){
                        String topic = words[1].substring(3);
                        String vote = words[2].substring(3);
                        String dir = System.getProperty("user.dir");
                        dir="C:\\Users\\egorm\\IdeaProjects\\TestServer\\chapters\\test\\test1.txt";
                        System.out.println(dir);
                        File file = new File(dir);
                        if(file.delete()) {
                            System.out.println("Успешно удалено");
                        }else{
                            System.out.println("NMKLDSOD");
                        }
                        FileReader fr = new FileReader(file);
                        BufferedReader reader = new BufferedReader(fr);
                        String line = reader.readLine();
                        List<String> lines = new ArrayList<>();
                        while (line != null) {
                            lines.add(line);
                            // считываем остальные строки в цикле
                            line = reader.readLine();
                        }
                        System.out.println(username);
                        System.out.println(lines.get(lines.size()-1));
                        if(username.equals(lines.get(lines.size()-1))){
                            if(file.delete()) {
                                out.writeUTF("Успешно удалено");
                            }else{
                                out.writeUTF("Файл не найден");
                            }
                        }else{
                            out.writeUTF("Нет прав доступа");
                        }

                    }else if(words[0].equals("exit")){
                        out.writeUTF("До свидания");
                        clientSocket.close();
                    }else{
                        out.writeUTF("Неизвестный запрос");
                    }

                }
                out.flush();
            }
            in.close();
            out.close();

            // потом закрываем сокет общения с клиентом в нити моносервера
            clientSocket.close();

            //System.out.println("Closing connections & channels - DONE.");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}


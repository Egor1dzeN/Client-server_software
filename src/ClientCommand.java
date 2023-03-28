import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClientCommand implements Runnable {
    private final Socket clientSocket;

    public ClientCommand(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            ObjectInputStream objInput=null;
            ObjectOutputStream objOutput = null;
            String username = "";
            boolean isLogin = false;

            while (!Server.server.isClosed()) {
                String entry = "";
                try {
                    entry = in.readUTF();
                }
                catch (SocketException e){
                    System.out.println("Собединение сброшено");
                    clientSocket.close();
                    break;
                }
                catch (EOFException e){
                    System.out.println("Клиент отключился");
                    clientSocket.close();
                    break;
                }
                String words[] = entry.split(" ");
                if (words[0].equals("exit"))
                    break;
                if(words.length>1){
                if (words[0].equals("login") && words[1].startsWith("-u=")) {
                    username= words[1].substring(3);
                    out.writeUTF("Вы успешно вошли, " + username);
                    isLogin = true;
                } else {
                    if (!isLogin)
                        out.writeUTF("Вы не вошли в систему");
                    else if (words[0].equals("create") && words[1].equals("topic") && words[2].startsWith("-n=")) {
                        String topic = words[2].substring(3);
                        String dir = System.getProperty("user.dir");
                        dir += "\\chapters\\" + topic;
                        File file = new File(dir);
                        if (file.exists()) {
                            out.writeUTF("Такой раздел уже существует");
                            continue;
                        }
                        Files.createDirectory(Path.of(dir));
                        out.writeUTF("Раздел с именем " + topic + " создан");
                    } else if (words[0].equals("view") && words.length < 3 && words[1].startsWith("-t=")) {
                        String topic = words[1].substring(3);
                        String dir = System.getProperty("user.dir");
                        dir += "\\chapters";
                        File dir4 = new File(dir); //path указывает на директорию
                        String ans = "";
                        int count = 0;
                        for (File file : dir4.listFiles()) {
                            if (file.getName().equals(topic)) {
                                count++;
                                ans += "Раздел - " + file.getName() + ". ";
                                ans += " Кол-во голосований - " + file.listFiles().length;
                            }
                        }
                        if (count == 0)
                            ans = "Разделы с именем " + topic + " не найдены";
                        out.writeUTF(ans);
                    } else if (words[0].equals("view") && words.length >= 3 && words[1].startsWith("-t=") && words[2].startsWith("-v=")) {

                        String topic = words[1].substring(3);
                        String vote = words[2].substring(3);
                        String path_dir = "chapters\\" + topic + "\\" + vote + ".txt";
                        File file = new File(path_dir);
                        if (!file.exists()) {
                            out.writeUTF("Файл не найден");
                            continue;
                        }
                        out.writeUTF("view");
                        FileReader fr = new FileReader(file);
                        BufferedReader reader = new BufferedReader(fr);
                        String line = reader.readLine();
                        List<String> lines = new ArrayList<>();
                        while (line != null) {
                            lines.add(line);
                            line = reader.readLine();
                        }
                        objOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                        objOutput.writeObject(lines);
                        fr.close();
                        reader.close();
                    } else if (words[0].equals("create") && words[1].equals("vote") && words[2].startsWith("-t=")) {
                        String topic = words[2].substring(3);
                        String dir_path = System.getProperty("user.dir") + "\\chapters\\" + topic;
                        File file_topic = new File(dir_path);
                        if (!file_topic.exists()) {
                            out.writeUTF("Такой раздел не существует");
                            continue;
                        }
                        out.writeUTF("create");
                        String name_vote = in.readUTF();
                        String topic_vote = in.readUTF();
                        int count_vote = Integer.parseInt(in.readUTF());
                        objInput = new ObjectInputStream(clientSocket.getInputStream());
                        List<String> list;
                        list = (List<String>) objInput.readObject();
                        List<String> lines = new ArrayList<>();
                        lines.add(topic_vote);
                        lines.addAll(list);
                        lines.add(username);
                        Path file = Paths.get("chapters\\" + topic + "\\" + name_vote + ".txt");
                        Files.write(file, lines, StandardCharsets.UTF_8);
                        out.writeUTF("Голосование успешно создано");
                    } else if (words[0].equals("vote") && words[1].startsWith("-t=") && words[2].startsWith("-v")) {

                        String topic = words[1].substring(3);
                        String vote = words[2].substring(3);
                        String path_dir = "chapters\\" + topic + "\\" + vote + ".txt";
                        File file = new File(path_dir);
                        if (!file.exists()) {
                            out.writeUTF("FileNotFound");
                            continue;
                        }
                        out.writeUTF("vote");
                        FileReader fr = new FileReader(file);
                        BufferedReader reader = new BufferedReader(fr);
                        String line = reader.readLine();
                        List<String> lines = new ArrayList<>();
                        while (line != null) {
                            lines.add(line);
                            line = reader.readLine();
                        }
                        objOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                        objOutput.writeObject(lines);
                        String choice = in.readUTF();
                        String wordss[] = lines.get(Integer.parseInt(choice)).split(" ");
                        int kol = Integer.parseInt(wordss[wordss.length - 1]) + 1;
                        String str = "";
                        for (int j = 0; j < wordss.length - 1; ++j) {
                            str += wordss[j] + " ";
                        }
                        str += kol;
                        lines.set(Integer.parseInt(choice), str);
                        Path file1 = Paths.get("chapters\\" + topic + "\\" + vote + ".txt");
                        Files.write(file1, lines, StandardCharsets.UTF_8);
                        fr.close();
                        reader.close();
                    } else if (words[0].equals("delete") && words[1].startsWith("-t=") && words[2].startsWith("-v=")) {
                        String topic = words[1].substring(3);
                        String vote = words[2].substring(3);
                        String dir = System.getProperty("user.dir");
                        dir += "\\chapters\\" + topic + "\\" + vote + ".txt";
                        File file = new File(dir);
                        if (!file.exists()) {
                            out.writeUTF("Файл не найден");
                            continue;
                        }
                        FileReader fr = new FileReader(file);
                        BufferedReader reader = new BufferedReader(fr);
                        String line = reader.readLine();
                        List<String> lines = new ArrayList<>();
                        while (line != null) {
                            lines.add(line);
                            line = reader.readLine();
                        }
                        fr.close();
                        reader.close();
                        if (username.equals(lines.get(lines.size() - 1))) {
                            if (file.delete()) {
                                out.writeUTF("Успешно удалено");
                            } else {
                                out.writeUTF("Файл не найден");
                            }
                        } else {
                            out.writeUTF("Нет прав доступа");
                        }

                    } else if (words[0].equals("exit")) {
                        out.writeUTF("До свидания");
                        clientSocket.close();
                    } else {
                        out.writeUTF("Неизвестный запрос");
                    }
                }

                }
                else out.writeUTF("Запрос слишком короткий, введите коректный");
                out.flush();
            }
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}


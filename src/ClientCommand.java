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
            //Создаем потоки для ввода/вывода информации через сокет
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            ObjectInputStream objInput = null;
            ObjectOutputStream objOutput = null;
            String username = "";

            //Вошел ли пользователь или нет
            boolean isLogin = false;

            //До тех пор, пока сервер не закрыт работаем
            while (!Server.server.isClosed()) {
                String entry = "";
                try {
                    //Читаем введенный клиентом запрос
                    entry = in.readUTF();
                } catch (SocketException e) {
                    System.out.println("Соединение сброшено");
                    clientSocket.close();
                    break;
                } catch (EOFException e) {
                    System.out.println("Клиент отключился");
                    clientSocket.close();
                    break;
                }
                //Разделяем запрос на слова
                String[] words = entry.split(" ");
                //Если ввели (exit) прекращаем цикл
                if (words[0].equals("exit"))
                    break;
                if (words.length > 1) {

                    // Вход пользователя в аккаунт
                    if (words[0].equals("login") && words[1].startsWith("-u=")) {
                        username = words[1].substring(3);
                        out.writeUTF("Вы успешно вошли, " + username);
                        isLogin = true;
                    } else {
                        if (!isLogin)
                            out.writeUTF("Вы не вошли в систему");

                            //Проверяем остальные введенные команды пользователя
                        else if (words[0].equals("create") && words[1].equals("topic") && words[2].startsWith("-n=")) {

                            //Создаем папку (если не существует) с именем <topic>
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
                        }
                        //view -t=<topic>
                        else if (words[0].equals("view") && words.length < 3 && words[1].startsWith("-t=")) {
                            //Получаем папку (chapters) со всеми разделами
                            String topic = words[1].substring(3);
                            String dir = System.getProperty("user.dir");
                            dir += "\\chapters";
                            File dir4 = new File(dir);
                            String ans = "";
                            int count = 0;

                            //Берем все существующие на сервере разделы
                            for (File file : dir4.listFiles()) {
                                //Проверяем на название
                                if (file.getName().equals(topic)) {
                                    count++;
                                    ans += "Раздел - " + file.getName() + ". ";
                                    ans += " Кол-во голосований - " + file.listFiles().length;
                                }
                            }
                            //Если разделов на нашлось
                            if (count == 0)
                                ans = "Разделы с именем " + topic + " не найдены";
                            //Отправляем ответ
                            out.writeUTF(ans);
                        }
                        //view -t=<topic> -v=<vote>
                        else if (words[0].equals("view") && words.length >= 3 && words[1].startsWith("-t=")
                                && words[2].startsWith("-v=")) {

                            String topic = words[1].substring(3);
                            String vote = words[2].substring(3);
                            String path_dir = "chapters\\" + topic + "\\" + vote + ".txt";
                            File file = new File(path_dir);
                            //Ищем файл голосования в разделе <topic> с именем <vote>
                            if (!file.exists()) {
                                out.writeUTF("Файл не найден");
                                continue;
                            }
                            out.writeUTF("view");
                            //Читаем файл голосвания
                            FileReader fr = new FileReader(file);
                            BufferedReader reader = new BufferedReader(fr);
                            String line = reader.readLine();
                            List<String> lines = new ArrayList<>();
                            while (line != null) {
                                lines.add(line);
                                line = reader.readLine();
                            }

                            //Отправляем список ответа в формате
                            /*
                             * тему голосования
                             * варианты ответа и количество пользователей выбравших данный вариант
                             */
                            objOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                            objOutput.writeObject(lines);
                            fr.close();
                            reader.close();
                        }
                        //create vote -t=<vote>
                        else if (words[0].equals("create") && words[1].equals("vote") && words[2].startsWith("-t=")) {
                            String topic = words[2].substring(3);
                            String dir_path = System.getProperty("user.dir") + "\\chapters\\" + topic;
                            File file_topic = new File(dir_path);
                            if (!file_topic.exists()) {
                                out.writeUTF("Такой раздел не существует");
                                continue;
                            }
                            out.writeUTF("create");
                            //Запрашиваем название, тему голосования, кол-во вариантов ответа, варианты ответа
                            String name_vote = in.readUTF();
                            String topic_vote = in.readUTF();
                            int count_vote = Integer.parseInt(in.readUTF());

                            objInput = new ObjectInputStream(clientSocket.getInputStream());
                            List<String> list;
                            list = (List<String>) objInput.readObject();
                            //Список из строк, которые будут записаны в файл голосования
                            List<String> lines = new ArrayList<>();
                            lines.add(topic_vote);
                            lines.addAll(list);
                            lines.add(username);
                            Path file = Paths.get("chapters\\" + topic + "\\" + name_vote + ".txt");
                            //Записываем голосование в файл
                            Files.write(file, lines, StandardCharsets.UTF_8);
                            out.writeUTF("Голосование успешно создано");
                        }
                        //vote -t=<topic> -v=<vote>
                        else if (words[0].equals("vote") && words[1].startsWith("-t=") && words[2].startsWith("-v")) {
                            //Находим файл с голосованием
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
                            //Выводим все возможные варианты
                            objOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                            objOutput.writeObject(lines);

                            //Получаем ответ с выбором варианта
                            String choice = in.readUTF();
                            String[] wordss = lines.get(Integer.parseInt(choice)).split(" ");
                            int kol = Integer.parseInt(wordss[wordss.length - 1]) + 1;
                            String str = "";
                            for (int j = 0; j < wordss.length - 1; ++j) {
                                str += wordss[j] + " ";
                            }
                            str += kol;
                            lines.set(Integer.parseInt(choice), str);
                            Path file1 = Paths.get("chapters\\" + topic + "\\" + vote + ".txt");
                            //Перезаписываем голосование
                            Files.write(file1, lines, StandardCharsets.UTF_8);
                            fr.close();
                            reader.close();
                        }
                        //delete -t=<topic> -v=<vote>
                        else if (words[0].equals("delete") && words[1].startsWith("-t=") && words[2].startsWith("-v=")) {
                            String topic = words[1].substring(3);
                            String vote = words[2].substring(3);
                            //Ищем такой файл с голосованием
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
                            //Проверяем автора голосования с пользователем
                            if (username.equals(lines.get(lines.size() - 1))) {
                                //Пытаемся удалить
                                if (file.delete()) {
                                    out.writeUTF("Успешно удалено");
                                } else {
                                    out.writeUTF("Файл не найден");
                                }
                            } else {
                                out.writeUTF("Нет прав доступа");
                            }

                        }
                        //учше
                        else if (words[0].equals("exit")) {
                            out.writeUTF("До свидания");
                            clientSocket.close();
                        }
                        //Если пришел неизвестный запрос или неправильный
                        else {
                            out.writeUTF("Command not found");
                        }
                    }

                } else out.writeUTF("Command not found");
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


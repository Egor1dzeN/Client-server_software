import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServerCommand
        implements Runnable
{
    public void run()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String s = "";

                //Проверяем ввели ли что-то
                if (br.ready()) {

                    //Считываем, что ввел пользователь
                    s = br.readLine();
                    //Разбиваем команду на слова
                    String words[] = s.split(" ");
                    String type_commamd = words[0];
                    /*
                      После того как разбили введенную команду на слова,
                      проверяем первое слово на соответствие команде (load,save,exit)
                     */

                    //Загружаем голосование в папку download
                    if (type_commamd.equals("load")) {
                        String filename = words[1];
                        try {

                            File file = new File(filename);
                            //Проверяем существует ли файл и правильный ли формат файла
                            if(!file.exists()){
                                System.err.println("Файл не найден");
                                continue;
                            }
                            if(!file.isFile() && !file.getName().endsWith(".txt")) {
                                System.err.println("Пожалуйста введите файл формата .txt");
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
                            //Сохраняем файл в папку download
                            Path file_path = Paths.get("chapters\\download\\" + file.getName());
                            Files.write(file_path, lines, StandardCharsets.UTF_8);
                            System.out.println("Файл был добавлен в папку download");
                            fr.close();
                            reader.close();
                        }catch (FileNotFoundException e){
                            System.err.println("Файл не найден");
                        }
                    }
                    //сохраняем все папки из chapters в указанную папку
                    else if (type_commamd.equals("save")) {
                        String filename = words[1];
                        File file = new File(filename);
                        String path_chapters = System.getProperty("user.dir")+"\\chapters";
                        File chapters = new File(path_chapters);
                        if(!file.isDirectory())
                            System.err.println("Введите путь к папке!");
                        else{
                            for(File file1:chapters.listFiles()){
                                filename = words[1]+"\\"+file1.getName();
                                File check_directory = new File(filename);
                                if(!check_directory.exists()){
                                    Files.createDirectory(Path.of(filename));
                                }
                                for(File file2:file1.listFiles()){
                                    String path = filename+"\\"+file2.getName();
                                    Files.copy(file2.toPath(), Path.of(path));
                                }
                            }
                            System.out.println("Все данные успешно сохранены в папке!");
                        }
                    }
                    //Выключаем сервер
                    else if (words[0].equals("exit")) {
                        Server.server.close();
                        Server.work = false;
                        break;
                    } else {
                        System.err.println("Вы ввели неизвестную серверу команду");
                    }

                }
            } catch (IOException e) {
                System.out.println("Ошибка");
            }

        }
    }
}

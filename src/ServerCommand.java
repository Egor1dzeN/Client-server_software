import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ServerCommand        //Нечто, реализующее интерфейс Runnable
        implements Runnable        //(содержащее метод run())
{
    public void run()//Этот метод будет выполняться в побочном потоке
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String s = "";
                if (br.ready()) {
                    s = br.readLine();
                    String words[] = s.split(" ");
                    String type_commamd = words[0];
                    //System.out.println(type_commamd);
                    if (type_commamd.equals("load")) {
                        String filename = words[1];
                        try {
                            File file = new File(filename);

                            FileReader fr = new FileReader(file);
                            //создаем BufferedReader с существующего FileReader для построчного считывания
                            BufferedReader reader = new BufferedReader(fr);
                            // считаем сначала первую строку
                            String line = reader.readLine();
                            List<String> lines = new ArrayList<>();
                            while (line != null) {
                                lines.add(line);
                                // считываем остальные строки в цикле
                                line = reader.readLine();
                            }
                            Path file_path = Paths.get("chapters\\download\\" + file.getName());
                            Files.write(file_path, lines, StandardCharsets.UTF_8);
                            System.out.println("Файл был добавлен в папку download");
                        }catch (FileNotFoundException e){
                            System.out.println("Файл не найден");
                        }
                    } else if (type_commamd.equals("save")) {
                        String filename = words[0];
                        Server.save();
                        System.out.println("Все данные сохранены на сервере");
                    } else if (words[0].equals("exit")) {
                        //System.out.println("До свидания");
                        Server.server.close();
                        Server.work = false;
                    } else {
                        System.out.println("unknown command");
                    }

                }
            } catch (IOException e) {
                System.out.println("Ошибка");
            }

        }
    }
}

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", 8081);
        DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
        DataInputStream ois = new DataInputStream(socket.getInputStream());
        ObjectOutputStream objOut = null;ObjectInputStream objIn = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Scanner in = new Scanner(System.in);
        System.out.println("Вы подключились к серверу!");
        try{
            while(!socket.isOutputShutdown()){
                if(br.ready()) {
                    String clientCommand = br.readLine();
                    if(clientCommand.equals("exit")){
                        socket.close();
                        break;
                    }
                    oos.writeUTF(clientCommand);
                    oos.flush();
                    String server_ans = ois.readUTF();
                    System.out.println(server_ans);
                    if (server_ans.equals("create")) {
                        System.out.print("Введите название голосования: ");
                        String name_vote = br.readLine();
                        System.out.println();
                        System.out.print("Введите тему голосования: ");
                        String topic_vote = br.readLine();
                        System.out.println();
                        System.out.print("Введите количество вариантов ответа: ");
                        int count_vote = Integer.valueOf(br.readLine());
                        System.out.println();
                        List<String> list = new ArrayList<>();
                        System.out.println("Введите варианты ответов: ");
                        for (int i = 1; i <= count_vote; ++i) {
                            System.out.print(i + ". ");
                            list.add(br.readLine() + " 0");
                        }
                        oos.writeUTF(name_vote);
                        oos.writeUTF(topic_vote);
                        oos.writeUTF(String.valueOf(count_vote));
                        objOut = new ObjectOutputStream(socket.getOutputStream());
                        objOut.writeObject(list);
                        String ans = ois.readUTF();
                        System.out.println(ans);
                    } else if (server_ans.equals("view")) {
                        objIn = new ObjectInputStream(socket.getInputStream());
                        List<String> list = (List<String>) objIn.readObject();
                        for (int i = 0; i < list.size(); ++i) {
                            if (i == 0) {
                                System.out.println("Тема: " + list.get(i));
                            } else if (list.get(i).equals("")) {
                            } else if (i == list.size() - 1)
                                System.out.println("Разработчик  - " + list.get(i));
                            else {
                                //String words[] = list.get(i).split(" ");
                                System.out.println(i + ". " +list.get(i));
                            }


                        }
                    } else if (server_ans.equals("vote")) {

                        objIn = new ObjectInputStream(socket.getInputStream());
                        List<String> list = (List<String>) objIn.readObject();
                        System.out.println(list.size());
                        int count = 0;
                        for (int i = 0; i < list.size(); ++i) {
                            if (i == 0) {
                                System.out.println("Тема: " + list.get(i));
                            } else if (list.get(i).equals("")) {
                            } else if (i == list.size() - 1)
                                System.out.println("Разработчик  - " + list.get(i));
                            else {
                                System.out.println(i + ". " + list.get(i));
                                count++;
                            }

                        }
                        System.out.println("Выберите от 1 до " + count);
                        int choice = in.nextInt();
                        oos.writeUTF(String.valueOf(choice));
                        System.out.println("Вы успешно проголосовали!");
                    }
                }

            }
            System.out.println("До свидания!");
        } catch (SocketException e) {
            System.err.println("Сервер выключен или сломался");
        }
    }
}

import java.io.*;
import java.net.Socket;

public class Client2 {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 8081);
        DataOutputStream oos = new DataOutputStream(socket.getOutputStream());
        DataInputStream ois = new DataInputStream(socket.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(!socket.isOutputShutdown()){
            if(br.ready()){
                String clientCommand = br.readLine();
                oos.writeUTF(clientCommand);
                oos.flush();
                String server_ans  = ois.readUTF();
                System.out.println(server_ans);
            }
        }
    }
}
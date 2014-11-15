import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by user on 11/15/14.
 */
public class Server {
    public static void main(String[] args) {
        Server server = new Server();
        server.runServer();
    }

    public void runServer() {
        int serverPort = 50505;
        HashMap peers = new HashMap<String, String>();

        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);

            while (true) {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(2000);
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();

                String currentPeer = socket.getRemoteSocketAddress().toString().substring(1);
                String request = readDataFromInput(sin, 1).trim();
                System.out.println(String.format("Connected: %s Request: %s", currentPeer, request));

                String answer = "";
                if (request.equals("get")) {
                    answer = getPeers(peers);
                    System.out.println(String.format("Answer: %s", answer));
                    sout.write(answer.getBytes(Charset.forName("UTF-8")));
                }

                if (request.split(":")[0].equals("register")) {
                    answer = "registered";
                    peers.put(currentPeer, "peer");
                    System.out.println(String.format("Answer: %s", answer));
                    sout.write(answer.getBytes(Charset.forName("UTF-8")));
                }

                socket.close();

            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public String readDataFromInput(InputStream sin, int linesToRead) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(sin));
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < linesToRead; i++) {
            String currentData = br.readLine();
            if(currentData == null || currentData.trim().length() == 0) {
                break;
            } else {
                s.append(currentData + "\n");
            }
        }
        return s.toString();
    }

    public String getPeers(HashMap<String, String> peers) {
        StringBuilder result = new StringBuilder();

        for (String host : peers.keySet()) {
            result.append(host + "\n");
        }
        result.append("stop\n");
        return result.toString();
    }
}

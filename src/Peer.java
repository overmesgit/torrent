import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 11/15/14.
 */
public class Peer {
    HashMap<Integer, HashMap<Integer, String>> dataToShare = new HashMap<>();
    int peerPort = 50506;
    private int serverPort = 50505;
    private String serverIp = "127.0.0.1";

    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";

        String testData = "что же ты делаешь? почему не спишь? почему заснуть не можешь?";
        String testData2 = "что же";
        String testData3 = "почему не спишь? почему засну";


        Peer client = new Peer();

        client.addDataToShare(testData);
        client.addDataToShare(testData2);
        client.addDataToShare(testData3);

        client.runPeer();
    }

    public void runPeer() throws IOException {
        registerPeer();
        ServerSocket serverSocket = new ServerSocket(peerPort);

        while (true) {
            Socket socket = serverSocket.accept();
            socket.setSoTimeout(2000);
            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();
            System.out.println(String.format("External connection from %s", socket.getRemoteSocketAddress()));

            String request = readDataFromInput(sin, 1);
            System.out.println(String.format("Request %s", request));
            String[] splitRequest = request.split(":");
            int requestType = splitRequest.length;
            if (requestType == 1) {
                String answer = String.format("%s:%s\n", splitRequest[0].trim(), checkShareData(splitRequest[0]));
                System.out.println(String.format("Answer %s", answer));
                sout.write(answer.getBytes(Charset.forName("UTF-8")));
            }

            if (requestType == 2) {
                String answer = null;
                if (checkShareData(splitRequest[0])) {
                    answer = String.format("%s:%s:%s\n", splitRequest[0].trim(), splitRequest[1].trim(), getDataPeace(splitRequest[0], splitRequest[1]));
                } else {
                    answer = String.format("%s:%s\n", splitRequest[0].trim(), false);
                }

                System.out.println(String.format("Answer %s", answer));
                sout.write(answer.getBytes(Charset.forName("UTF-8")));
            }

            socket.close();

        }
    }

    public boolean checkShareData(String sHash) {
        Integer hash = Integer.parseInt(sHash.trim());
        return dataToShare.containsKey(hash);
    }

    public String getDataPeace(String sHash, String sSubHash) {
        Integer hash = Integer.parseInt(sHash.trim());
        Integer hash2 = Integer.parseInt(sSubHash.trim());
        return dataToShare.get(hash).get(hash2);
    }

    public ArrayList<String> splitDataToPeaces(String s) {
        int peacesCount = 10;
        ArrayList<String> result = new ArrayList<String>();
        int step = (int)Math.round(Math.ceil((float)s.length()/peacesCount));
        for (int currentPeaceIndex = 0; currentPeaceIndex < peacesCount; currentPeaceIndex += 1) {
            int from = step * currentPeaceIndex;
            if (from > s.length()) {
                break;
            }
            int to = step * (currentPeaceIndex + 1) > s.length() ? s.length() : step * (currentPeaceIndex + 1);
            String currentPeace = s.substring(from, to);
            result.add(currentPeace);
        }
        return result;
    }

    public HashMap<Integer, String> getHashMap(String s) {
        int peacesCount = 10;
        HashMap<Integer, String> result = new HashMap<Integer, String>();
        ArrayList<String> splitData = splitDataToPeaces(s);
        for (String peace: splitData) {
            result.put(peace.hashCode(), peace);
        }
        return result;
    }

    public String getTorrentFile(String s) {
        StringBuilder result = new StringBuilder();
        ArrayList<String> splitData = splitDataToPeaces(s);

        result.append(String.format("%s:", s.hashCode()));
        for (String peace: splitData) {
            result.append(String.format("%s:", peace.hashCode()));
        }
        return result.toString().substring(0, result.length() - 1);
    }

    public void addDataToShare(String s) {
        System.out.println(String.format("Torrent data: %s", getTorrentFile(s)));
        dataToShare.put(s.hashCode(), getHashMap(s));
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

    public void registerPeer() throws IOException {
        Socket socket = new Socket(serverIp, serverPort, InetAddress.getByName("0.0.0.0"), peerPort);
        InputStream sin = socket.getInputStream();
        OutputStream sout = socket.getOutputStream();

        sout.write(String.format("register\n", peerPort).getBytes(Charset.forName("UTF-8")));
        socket.close();
        System.out.println("Register");
    }
}

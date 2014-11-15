import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 11/15/14.
 */
public class Peer {
    HashMap<Integer, HashMap<Integer, String>> dataToShare = new HashMap<>();
    int peerPort;
    private int serverPort;
    private String serverIp;

    public static void main(String[] args) throws IOException, InterruptedException {
        Peer client = new Peer(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));

        for (int i = 3; i < args.length; i++) {
            client.addDataToShare(readFile(args[i]));
        }

        while (true) {
            client.runPeer();
        }
    }

    public Peer(String serverIp, int serverPort, int peerPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.peerPort = peerPort;
    }

    public void runPeer() throws IOException, InterruptedException {


        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            registerPeer();
            serverSocket = new ServerSocket(peerPort);

            while (true) {
                socket = serverSocket.accept();
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
                        answer = String.format("%s:%s:%s\nstop\n", splitRequest[0].trim(), splitRequest[1].trim(), getDataPeace(splitRequest[0], splitRequest[1]));
                    } else {
                        answer = String.format("%s:%s\n", splitRequest[0].trim(), false);
                    }
                    System.out.println(String.format("Answer %s", answer));
                    sout.write(answer.getBytes(Charset.forName("UTF-8")));
                }

                socket.close();

            }
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            if (serverSocket != null) serverSocket.close();
            if (socket != null) socket.close();
            Thread.sleep(2000);
        }
    }

    static String readFile(String path) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.forName("UTF-8"));
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

    public void registerPeer() throws IOException, InterruptedException {
        Socket socket = null;
        try {
            socket = new Socket(serverIp, serverPort, InetAddress.getByName("0.0.0.0"), peerPort);
            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            sout.write(String.format("register\n", peerPort).getBytes(Charset.forName("UTF-8")));
            socket.close();
            System.out.println("Register");
        } finally {
            if (socket != null) {
                socket.close();
                Thread.sleep(2000);
            }
        }
    }
}

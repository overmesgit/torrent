import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by user on 11/15/14.
 */
public class Downloader {
    HashMap<Integer, HashMap<Integer, String>> dataToShare = new HashMap<>();
    private String serverIp;
    private int serverPort;

    public static void main(String[] args) throws IOException {

        Downloader downloader = new Downloader();
        downloader.runDownloader(args[0], args[1], args[2]);
    }

    public void runDownloader(String serverIp, String serverPort, String torrentFile) throws IOException {
        this.serverIp = serverIp;
        this.serverPort = Integer.parseInt(serverPort);
        List<String> peers = getPeers();

        String[] splitTorrenFile = torrentFile.split(":");
        String hash = splitTorrenFile[0];

        ArrayList<String> hostsWithFile = new ArrayList<>();
        for (String host: peers) {
            if (haveData(host, hash)) {
                hostsWithFile.add(host);
            }
        }
        Random randomGenerator = new Random();

        StringBuilder result = new StringBuilder();
        for (int peace = 1; peace < splitTorrenFile.length; peace++) {
            int index = randomGenerator.nextInt(hostsWithFile.size());
            result.append(getDataFromPeer(hostsWithFile.get(index), hash, splitTorrenFile[peace]));
        }

        System.out.println(result);
    }

    public boolean haveData(String hostData, String hash) throws IOException {
        String[] splitData = hostData.split(":");
        System.out.println(String.format("Connect to %s ", hostData));
        Socket socket = new Socket(splitData[0], Integer.parseInt(splitData[1]));
        InputStream sin = socket.getInputStream();
        OutputStream sout = socket.getOutputStream();

        System.out.println(String.format("Send Request to %s for %s", hostData, hash));
        sout.write((hash + "\n").getBytes(Charset.forName("UTF-8")));
        String data = readDataFromInput(sin, 1, true);
        System.out.println(String.format("Received Answer from %s %s", hostData, data));
        if (data.split(":")[1].trim().equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    public String getDataFromPeer(String hostData, String hash, String subHash) throws IOException {
        String[] splitData = hostData.split(":");
        System.out.println(String.format("Connect to %s ", hostData));
        Socket socket = new Socket(splitData[0], Integer.parseInt(splitData[1]));
        InputStream sin = socket.getInputStream();
        OutputStream sout = socket.getOutputStream();

        System.out.println(String.format("Send Request to %s for %s:%s", hostData, hash, subHash));
        sout.write(String.format("%s:%s\n", hash, subHash).getBytes(Charset.forName("UTF-8")));
        String data = readDataFromInput(sin, 1000, true);
        System.out.println(String.format("Received Answer from %s:  %s", hostData, data));
        return data.split(":")[2];
    }

    public String readDataFromInput(InputStream sin, int linesToRead, boolean addTerminator) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(sin));
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < linesToRead; i++) {
            String currentData = br.readLine();
            if(currentData == null || currentData.equals("stop")) {
                break;
            } else {
                currentData += addTerminator ? "\n":"";
                s.append(currentData);
            }
        }
        return s.toString();
    }



    public List<String> getPeers() throws IOException {
        Socket socket = new Socket(serverIp, serverPort);
        InputStream sin = socket.getInputStream();
        OutputStream sout = socket.getOutputStream();

        System.out.println("Get Peers");
        sout.write("get\n".getBytes(Charset.forName("UTF-8")));

        String sPeers = readDataFromInput(sin, 1000, true);
        socket.close();
        ArrayList<String> result = new ArrayList<String>();
        for (String peer: sPeers.split("\n")) {
            result.add(peer);
        }
        return result;
    }
}

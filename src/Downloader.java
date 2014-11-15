import java.io.*;
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

    public static void main(String[] args) throws IOException {
        Downloader downloader = new Downloader();
        downloader.runDownloader("2081709300:-995075484:-1822253299:394351321:-1338556733:484850113:427097428:184964084:-1048190922:1028727964");
    }

    public void runDownloader(String torrentFile) throws IOException {
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
        for (int peace = 1; peace < splitTorrenFile.length; peace++) {
            int index = randomGenerator.nextInt(hostsWithFile.size());
            System.out.println(getDataFromPeer(hostsWithFile.get(index), hash, splitTorrenFile[peace]));
        }
    }

    public boolean haveData(String hostData, String hash) throws IOException {
        String[] splitData = hostData.split(":");
        System.out.println(String.format("Connect to %s ", hostData));
        Socket socket = new Socket(splitData[0], Integer.parseInt(splitData[1]));
        InputStream sin = socket.getInputStream();
        OutputStream sout = socket.getOutputStream();

        System.out.println(String.format("Send Request to %s for %s", hostData, hash));
        sout.write((hash + "\n").getBytes(Charset.forName("UTF-8")));
        String data = readDataFromInput(sin, 1);
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
        String data = readDataFromInput(sin, 1000);
        System.out.println(String.format("Received Answer from %s:  %s", hostData, data));
        return data;
    }

    public String readDataFromInput(InputStream sin, int linesToRead) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(sin));
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < linesToRead; i++) {
            String currentData = br.readLine();
            if(currentData == null || currentData.trim().length() == 0 || currentData.equals("stop")) {
                break;
            } else {
                s.append(currentData + "\n");
            }
        }
        return s.toString();
    }

    public List<String> getPeers() throws IOException {
        int serverPort = 50505;
        String host = "127.0.0.1";

        Socket socket = new Socket(host, serverPort);
        InputStream sin = socket.getInputStream();
        OutputStream sout = socket.getOutputStream();

        System.out.println("Get Peers");
        sout.write("get\n".getBytes(Charset.forName("UTF-8")));

        String sPeers = readDataFromInput(sin, 1000);
        socket.close();
        ArrayList<String> result = new ArrayList<String>();
        for (String peer: sPeers.split("\n")) {
            result.add(peer);
        }
        return result;
    }
}

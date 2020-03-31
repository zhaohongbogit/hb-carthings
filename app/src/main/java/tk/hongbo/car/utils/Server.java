package tk.hongbo.car.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ServerClient> serverClients;

    public void createSocket(ServerClient.OnMessageListener listener) {
        serverClients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(12345);
            Socket socket = serverSocket.accept();
            ServerClient client = new ServerClient(socket, listener);
            client.start();
            serverClients.add(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        if (serverClients != null && !serverClients.isEmpty()) {
            for (ServerClient client : serverClients) {
                client.sendMessage(msg);
            }
        }
    }

    public void sendImage(String imageStr) {
        if (serverClients != null && !serverClients.isEmpty()) {
            for (ServerClient client : serverClients) {
                client.sendImage(imageStr);
            }
        }
    }
}

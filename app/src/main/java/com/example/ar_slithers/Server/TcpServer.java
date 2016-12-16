package com.example.ar_slithers.Server;

import java.io.*;
import java.net.*;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.*;
/**
 * Created by Bear on 2016/12/12.
 */
import com.google.gson.Gson;

public class TcpServer {
    public static final int LISTEN_PORT = 12345;
    public player[] player = new player[4];
    private Stack<Integer> empty = new Stack<Integer>();

    public void listenRequest() {
        for(int i = 3 ; i >= 0 ; i--){
            empty.push(i);
        }
        ServerSocket serverSocket = null;
        ExecutorService threadExecutor = Executors.newFixedThreadPool(20);
        try {
            serverSocket = new ServerSocket(LISTEN_PORT);
            System.out.println("Server listening requests...");
            while(true) { //accept()
                Socket socket = serverSocket.accept();
                threadExecutor.execute(new RequestThread(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (threadExecutor != null)
                threadExecutor.shutdown();
            if (serverSocket != null)
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    class RequestThread implements Runnable {
        private Socket clientSocket;
        private String message = "";
        private int thisPlayer = 0;

        public RequestThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }


        public void run() {
            System.out.println("有" + clientSocket.getRemoteSocketAddress() + "連線進來!");
            DataInputStream input = null;
            DataOutputStream output = null;
            JSONObject ServerData = new JSONObject();
            Gson gson = new Gson();

            if ( empty.size() > 0 ) {
                int thisId = empty.pop();  // ���t�@��Player���o�Ӫ��a  �ðO��ip
                player[thisId] = new player( (thisId+1) , clientSocket.getRemoteSocketAddress() + "");
                thisPlayer = thisId;

                // �U���n�I�s�N player[thisPlayer]
                try {
                    input = new DataInputStream(this.clientSocket.getInputStream());
                    output = new DataOutputStream(this.clientSocket.getOutputStream());
                    // ��̷s����ƶǵ� client
                    ServerData.put("Data", gson.toJson(player));
                    output.writeUTF(ServerData.toString());
                    output.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                while (!this.clientSocket.isClosed()) {
                    try {
                        message = input.readUTF();
                        System.out.println("P" + (thisPlayer + 1) + "說:" + message);
                        try {
                            JSONObject messageJSON = new JSONObject(message);
                            player[thisPlayer].Lat = messageJSON.get("lat").toString();
                            player[thisPlayer].Lng = messageJSON.get("lng").toString();
                            ServerData.put("Data", gson.toJson(player));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // �e�X�� client ������
                        output.writeUTF(ServerData.toString());
                        output.flush();
                    } catch (IOException e) {
                        // e.printStackTrace();
                        System.out.println(String.format("連線中斷,%s", clientSocket.getRemoteSocketAddress()));
                        int delete = player[thisPlayer].id-1;
                        player[thisPlayer] = null;
                        empty.push(delete);

                        try {
                            if (input != null)
                                input.close();
                            if (output != null)
                                output.close();
                            if (this.clientSocket != null && !this.clientSocket.isClosed())
                                this.clientSocket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            }else{// 超過四個PLAYER
                try {
                    this.clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("已經滿人了!!!"+clientSocket.getRemoteSocketAddress()+"離開連線");
            }
        }
    }

    public class player {
        public int id;
        public String Lat = "test";
        public String Lng = "test";
        public String ip = "test";

        public player(int id, String ip) {
            this.id = id;
            this.ip = ip;
        }
    }
}

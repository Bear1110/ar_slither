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
            while(true) { //accept() 會等待連線
                Socket socket = serverSocket.accept();
                System.out.println("request");
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

    /**
     * @param args
     */
    public static void main(String[] args) {
        TcpServer server = new TcpServer();
        server.listenRequest();
    }

    /**
     * 處理Client端的Request執行續。
     *
     * @version
     */
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
                int thisId = empty.pop();  // 分配一個Player給這個玩家  並記錄ip
                player[thisId] = new player( (thisId+1) , clientSocket.getRemoteSocketAddress() + "");
                thisPlayer = thisId;

                // 下面要呼叫就 player[thisPlayer]
                try {
                    input = new DataInputStream(this.clientSocket.getInputStream());
                    output = new DataOutputStream(this.clientSocket.getOutputStream());
                    // 把最新的資料傳給 client
                    ServerData.put("Data", gson.toJson(player));
                    output.writeUTF(ServerData.toString());
                    System.out.println("output");
                    output.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                while (!this.clientSocket.isClosed()) {
                    try {
                        // 讀入送到 server 的消息，這裡也會等待 read
                        message = input.readUTF();
                        System.out.println("P" + (thisPlayer + 1) + "對我說:" + message);
                        try {
                            JSONObject messageJSON = new JSONObject(message); // 轉成JSON
                            player[thisPlayer].Lat = messageJSON.get("lat").toString();
                            player[thisPlayer].Lng = messageJSON.get("lng").toString();
                            ServerData.put("Data", gson.toJson(player));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // 送出到 client 的消息
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

            }else{// 這邊是超過四個人的時候做的事情
                try {
                    this.clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("已經超過四位使用者了!!!"+clientSocket.getRemoteSocketAddress()+"<<抱歉你進不來");
            }
        }
    }

    public class player {
        public player(int id, String ip) {
            this.id = id;
            this.ip = ip;
        }

        public int id;
        public String Lat = "test";
        public String Lng = "test";
        public String ip = "test";
    }
}
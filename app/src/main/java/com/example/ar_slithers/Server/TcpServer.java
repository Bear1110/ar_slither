package com.example.ar_slithers.Server;

import java.io.*;
import java.net.*;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;
import org.json.*;
/**
 * Created by Bear on 2016/12/12.
 */
import com.google.gson.Gson;

public class TcpServer {
    public static final int LISTEN_PORT = 12345;
    public player[] player = new player[4];
    private Stack<Integer> empty = new Stack<Integer>();
    public double[] mapCenter = {0, 0};

    public void listenRequest() {
        for (int i = 3; i >= 0; i--) {
            empty.push(i);
        }
        // player[0] = new player(1, "");
        // player[0].Lat = "121.187504"; // meeting
        // player[0].Lng = "24.966835";
        // player[1] = new player(2, "");
        // player[1].Lat = "121.187637"; // 水滴
        // player[1].Lng = "24.967377";
        // player[1].map[0] = (121.187637 - 121.187504) * 240000; // 水滴
        // player[1].map[1] = (24.967377 - 24.966835) * 240000;
        // player[2] = new player(3, "");
        // player[2].Lat = "121.187596"; // 水滴
        // player[2].Lng = "24.966342";
        // player[2].map[0] = (121.187596 - 121.187504) * 240000; // 停車場
        // player[2].map[1] = (24.966342 - 24.966835) * 240000;
        // player[3] = new player(4, "");
        // player[3].Lat = "121.187121"; // 貨梯
        // player[3].Lng = "24.967114";
        // player[3].map[0] = (121.187121 - 121.187504) * 140000; // 停車場
        // player[3].map[1] = (24.967114 - 24.966835) * 140000;

        ServerSocket serverSocket = null;
        ExecutorService threadExecutor = Executors.newFixedThreadPool(20);
        try {
            serverSocket = new ServerSocket(LISTEN_PORT);
            System.out.println("Server listening requests...");
            while (true) { // accept()
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

    public static void main(String[] args) {
        TcpServer server = new TcpServer();
        server.listenRequest();
    }

    class RequestThread implements Runnable {
        private Socket clientSocket;
        private String message = "";
        private player thisPlayer = null;

        public RequestThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public void run() {
            System.out.println(
                    "有" + clientSocket.getRemoteSocketAddress() + "連線進來!");
            DataInputStream input = null;
            DataOutputStream output = null;
            JSONObject ServerData = new JSONObject();
            Gson gson = new Gson();

            if (empty.size() > 0) {
                int thisId = empty.pop(); // 這是把 stack pop出來給他 算是發id
                player[thisId] = new player((thisId + 1),
                        clientSocket.getRemoteSocketAddress() + "");
                thisPlayer = player[thisId]; // 這是有意義的 因為要存取到 全部
                // 下面要呼叫用 thisPlayer
                try {///////////////////////// 這是第一次傳送數據
                    input = new DataInputStream(
                            this.clientSocket.getInputStream());
                    output = new DataOutputStream(
                            this.clientSocket.getOutputStream());
                    ServerData.put("id", thisPlayer.id);// 第一次 portocal 有 要傳 id 過去
                    ServerData.put("Data", gson.toJson(player));
                    output.writeUTF(ServerData.toString());
                    output.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } ///////////////////////////// 這是第一次傳送數據 以上

                while (!this.clientSocket.isClosed()) {
                    try {
                        message = input.readUTF();
                        System.out.println(
                                "P" + (thisPlayer.id) + "說:" + message);
                        try {
                            JSONObject messageJSON = new JSONObject(message);
                            if (!messageJSON.get("lat").toString().equals("")) {
                                double Lat = Double.parseDouble(
                                        messageJSON.get("lat").toString());
                                double Lng = Double.parseDouble(
                                        messageJSON.get("lng").toString());
                                if (mapCenter[0] == 0) {// 初始化地圖座標中心
                                    setCenterMap(Lat, Lng);
                                }
                                setThisPlayerMap(Lat, Lng);
                            }
                            ServerData.put("Data", gson.toJson(player));
                            ServerData.put("mapCenter", gson.toJson(mapCenter));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        output.writeUTF(ServerData.toString());
                        output.flush();
                    } catch (IOException e) {
                        // e.printStackTrace();
                        disconnect();// 中斷連線處理程式
                        try {
                            if (input != null)
                                input.close();
                            if (output != null)
                                output.close();
                            if (this.clientSocket != null
                                    && !this.clientSocket.isClosed())
                                this.clientSocket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            } else {// 超過四個PLAYER
                try {
                    this.clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("已經滿人了!!!"
                        + clientSocket.getRemoteSocketAddress() + "離開連線");
            }
        }

        private void disconnect() {
            // TODO Auto-generated method stub
            System.out.println(String.format("連線中斷,%s",
                    clientSocket.getRemoteSocketAddress()));
            final int delete = thisPlayer.id - 1;
            player[delete] = null;
            empty.push(delete);
        }

        private void setThisPlayerMap(double lat, double lng) {
            int c = 240000;
            thisPlayer.map[0] = (lat - mapCenter[0]) * c;
            thisPlayer.map[1] = (lng - mapCenter[1]) * c;
            if (Math.abs(thisPlayer.map[0]) > 150
                    || Math.abs(thisPlayer.map[1]) > 150) {// 此人離中心已經太遠
                if (thisPlayer.remoteCenter[0] == 0.0) {// 這是第一次給他中心座標
                    setRemoteCenter(lat, lng);
                }
                // 算此人位置
                thisPlayer.map[0] = (lat - thisPlayer.remoteCenter[0]) * c
                        + thisPlayer.fakeCenter[0];
                thisPlayer.map[1] = (lng - thisPlayer.remoteCenter[1]) * c
                        + thisPlayer.fakeCenter[1];
            }
            setBody();
        }

        private void setBody() {
            thisPlayer.body[0][0] = thisPlayer.map[0];
            thisPlayer.body[0][1] = thisPlayer.map[1];
            for (int i = thisPlayer.body.length-1 ; i > 0 ; i--) {
                thisPlayer.body[i][0] = thisPlayer.body[i-1][0];
                thisPlayer.body[i][1] = thisPlayer.body[i-1][1];
            }
        }

        private void setCenterMap(double lat, double lng) {
            mapCenter[0] = lat;
            mapCenter[1] = lng;
        }

        private void setRemoteCenter(double lat, double lng) {
            Random ran = new Random();
            thisPlayer.remoteCenter[0] = lat;
            thisPlayer.remoteCenter[1] = lng;
            int x = 1, y = 1; // 預設第一象限
            if (thisPlayer.map[1] < 0) {
                if (thisPlayer.map[0] < 0)
                    x = y = -1;// 第三象限
                else
                    y = -1; // 第四象限
            } else if (thisPlayer.map[0] < 0) { // 第二象限
                x = -1;
            }
            thisPlayer.fakeCenter =
                    new double[] {x * ran.nextInt(50), y * ran.nextInt(50)};
        }

    }
    public class player {
        public int id;
        public String ip = "test";

        public double[] remoteCenter = {0.0, 0.0}; // 自身在遠處的進來座標
        public double[] fakeCenter = {0.0, 0.0}; // 遠處的人假的 地圖中心 (隨機位置)
        public double[][] body = new double[7][2];
        public double[] map = {0, 0};

        public player(int id, String ip) {
            this.id = id;
            this.ip = ip;
        }
    }
}


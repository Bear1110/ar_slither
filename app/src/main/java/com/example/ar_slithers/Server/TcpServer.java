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
    public double[] mapCenter = {0,0};

    public void listenRequest() {
        for(int i = 3 ; i >= 0 ; i--){
            empty.push(i);
        }
//        player[1] = new player( 2 ,  "");
//        player[1].Lat = "121.187637"; //水滴
//        player[1].Lng = "24.967377";
//        player[2] = new player( 3 ,  "");
//        player[2].Lat = "121.187596"; //停車場
//        player[2].Lng = "24.966342";
//        player[3] = new player( 4 ,  "");
//        player[3].Lat = "121.186685"; //貨梯
//        player[3].Lng = "24.967114";
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
            System.out.println("有" + clientSocket.getRemoteSocketAddress() + "連線進來!");
            DataInputStream input = null;
            DataOutputStream output = null;
            JSONObject ServerData = new JSONObject();
            Gson gson = new Gson();

            if ( empty.size() > 0 ) {
                int thisId = empty.pop();  // 這是把 stack pop出來給他 算是發id
                thisPlayer = new player( (thisId+1) , clientSocket.getRemoteSocketAddress() + "");
                // 下面要呼叫用 thisPlayer
                try {
                    input = new DataInputStream(this.clientSocket.getInputStream());
                    output = new DataOutputStream(this.clientSocket.getOutputStream());
                    ServerData.put("id",thisPlayer.id);//第一次 portocal 有 要傳 id 過去
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
                        System.out.println("P" + (thisPlayer.id) + "說:" + message);
                        try {
                            JSONObject messageJSON = new JSONObject(message);
                            thisPlayer.Lat = messageJSON.get("lat").toString();
                            thisPlayer.Lng = messageJSON.get("lng").toString();
                            if(mapCenter[0]==0 && thisPlayer.Lat != ""){//初始化地圖座標中心
                                mapCenter[0] = Double.parseDouble(thisPlayer.Lat);
                                mapCenter[1] = Double.parseDouble(thisPlayer.Lng);
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
                        System.out.println(String.format("連線中斷,%s", clientSocket.getRemoteSocketAddress()));
                        int delete = thisPlayer.id-1;
                        player[delete] = null;
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
        public player(int id, String ip) {
            this.id = id;
            this.ip = ip;
        }
        public int id;
        public String Lat = "0.0";
        public String Lng = "0.0";
        public String ip = "test";
    }
}
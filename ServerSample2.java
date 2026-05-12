import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSample2 {
    private int port;
    private boolean[] online;
    private PrintWriter[] out;
    private Receiver[] receiver;

    public ServerSample2(int port) {
        this.port = port;
        out = new PrintWriter[2];
        receiver = new Receiver[2];
        online = new boolean[2];
    }

    class Receiver extends Thread {
        private InputStreamReader sisr;
        private BufferedReader br;
        private int playerNo;

        Receiver(Socket socket, int playerNo) {
            try {
                this.playerNo = playerNo;
                // 出力ストリームを保存（メッセージ転送用）
                out[playerNo] = new PrintWriter(socket.getOutputStream(), true);
                sisr = new InputStreamReader(socket.getInputStream());
                br = new BufferedReader(sisr);
            } catch (IOException e) {
                System.err.println("データ受信時にエラーが発生しました: " + e);
            }
        }

        public void run() {
            try {
                while (true) {
                    String inputLine = br.readLine();
                    if (inputLine != null) {
                        // メッセージを転送
                        forwardMessage(inputLine, playerNo);
                    }
                }
            } catch (IOException e) {
                System.err.println("プレイヤ " + playerNo + " との接続が切れました。");
                online[playerNo] = false;
            }
        }
    }

   public void acceptClient() {
    try {
        ServerSocket ss = new ServerSocket(port);
        System.out.println("サーバが起動しました。ポート:" + port);

        // 2人接続するまでループ
        for (int i = 0; i < 2; i++) {
            System.out.println("プレイヤ " + i + " の接続を待っています...");
            
            // ここで接続を待機（接続されるまでプログラムが一時停止します）
            Socket socket = ss.accept(); 
            
            online[i] = true;
            System.out.println("プレイヤ " + i + " が接続しました。");

            // 重要：ここで各プレイヤー専用のReceiverを作成し、開始する
            receiver[i] = new Receiver(socket, i);
            receiver[i].start(); 
        }

        // 2人揃ったら対戦開始の合図を送る（オプション）
        sendColor();
        System.out.println("2人のプレイヤが揃いました。対戦を開始します。");

    } catch (Exception e) {
        System.err.println("接続受付中にエラーが発生しました: " + e);
    }
}

    // 自分の打った手を「自分も含めた全員」に送ることで同期させます
    public void forwardMessage(String msg, int playerNo) {
        System.out.println("プレイヤ " + playerNo + " からのメッセージ: " + msg);
        for (int i = 0; i < 2; i++) {
            if (online[i]) {
                out[i].println(msg);
                out[i].flush();
            }
        }
    }

    // クライアント側に「あなたは黒(先手)」「あなたは白(後手)」と通知するメソッドの例
    public void sendColor() {
        if (online[0] && online[1]) {
            out[0].println("black"); // 0番目の人に黒を通知
            out[1].println("white"); // 1番目の人に白を通知
        }
    }

    public static void main(String[] args) {
        ServerSample2 server = new ServerSample2(10000);
        server.acceptClient();
    }
}
//junki
//パッケージのインポート
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class CPU3 extends JFrame{
	private JButton buttonArray[];//オセロ盤用のボタン配列
	private JButton stop, pass; //停止、スキップ用ボタン
	private JLabel colorLabel; // 色表示用ラベル
	private JLabel turnLabel; // 手番表示用ラベル
	private JLabel infoLabel, resultLabel;
	private Container c; // コンテナ
	private ImageIcon blackIcon, whiteIcon, boardIcon, hintIcon; //アイコン
	private PrintWriter out;//データ送信用オブジェクト
	private Receiver receiver; //データ受信用オブジェクト
	private OthelloSample1 game; //Othelloオブジェクト
	private PlayerSample1 player; //Playerオブジェクト
	private String myColor = "";

    private int []pointTable={
        100,12,96,84,83,95,11,99,
         10, 4,68,67,66,65, 3, 9,
         94,64,88,76,75,87,63,93,
         82,62,74, 0, 0,73,61,81,
         80,60,72, 0, 0,71,59,79,
         92,58,86,70,69,85,57,91,
          8, 2,56,55,56,53, 1, 7,
         98, 6,90,78,77,89, 5,97
    };

	// コンストラクタ
	public CPU3(OthelloSample1 game, PlayerSample1 player) { //OthelloオブジェクトとPlayerオブジェクトを引数とする
		this.game = game; //引数のOthelloオブジェクトを渡す
		this.player = player; //引数のPlayerオブジェクトを渡す
		String [] grids = game.getGrids(); //getGridメソッドにより局面情報を取得
		int row = game.getRow(); //getRowメソッドによりオセロ盤の縦横マスの数を取得
		//ウィンドウ設定
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
		setTitle("ネットワーク対戦型オセロゲーム");//ウィンドウのタイトル
		setSize(row * 45 + 10, row * 45 + 200);//ウィンドウのサイズを設定
		c = getContentPane();//フレームのペインを取得
		//アイコン設定(画像ファイルをアイコンとして使う)
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpg");
		hintIcon = new ImageIcon("blue.jpg"); // 水色の小丸画像
		c.setLayout(null);//
		//オセロ盤の生成
		buttonArray = new JButton[row * row];//ボタンの配列を作成
		for(int i = 0 ; i < row * row ; i++){
			if(grids[i].equals("black")){ buttonArray[i] = new JButton(blackIcon);}//盤面状態に応じたアイコンを設定
			if(grids[i].equals("white")){ buttonArray[i] = new JButton(whiteIcon);}//盤面状態に応じたアイコンを設定
			if(grids[i].equals("board")){ buttonArray[i] = new JButton(boardIcon);}//盤面状態に応じたアイコンを設定
			c.add(buttonArray[i]);//ボタンの配列をペインに貼り付け
			// ボタンを配置する
			int x = (i % row) * 45;
			int y = (int) (i / row) * 45;
			buttonArray[i].setBounds(x, y, 45, 45);//ボタンの大きさと位置を設定する．
			//buttonArray[i].addMouseListener(this);//マウス操作を認識できるようにする
			buttonArray[i].setActionCommand(Integer.toString(i));//ボタンを識別するための名前(番号)を付加する
		}
		
		infoLabel = new JLabel("接続待機中...", JLabel.CENTER);
        infoLabel.setBounds(10, row * 45 + 20, row * 45, 30);
        infoLabel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.BLACK));
        c.add(infoLabel);
        
        
		
		JButton quitBtn = new JButton("投了");
        quitBtn.setBounds(10, row * 45 + 60, row * 45, 40);
        quitBtn.addActionListener(e -> System.exit(0));
        add(quitBtn);
        
        resultLabel = new JLabel("", JLabel.CENTER);
        resultLabel.setBounds(10, row * 45 + 110, row * 45, 30);
        resultLabel.setFont(new Font("MS UI Gothic", Font.BOLD, 16));
        c.add(resultLabel);
        
        updateDisp();
        
	}

	// メソッド
	public void connectServer(String ipAddress, int port){	// サーバに接続
		Socket socket = null;
		try {
			socket = new Socket(ipAddress, port); //サーバ(ipAddress, port)に接続
			out = new PrintWriter(socket.getOutputStream(), true); //データ送信用オブジェクトの用意
			receiver = new Receiver(socket); //受信用オブジェクトの準備
			receiver.start();//受信用オブジェクト(スレッド)起動
		} catch (UnknownHostException e) {
			System.err.println("ホストのIPアドレスが判定できません: " + e);
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("サーバ接続時にエラーが発生しました: " + e);
			System.exit(-1);
		}
	}

	public void sendMessage(String msg){	// サーバに操作情報を送信
		out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データを送る
		System.out.println("サーバにメッセージ " + msg + " を送信しました"); //テスト標準出力
	}

	// データ受信用スレッド(内部クラス)
	class Receiver extends Thread {
		private InputStreamReader sisr; //受信データ用文字ストリーム
		private BufferedReader br; //文字ストリーム用のバッファ

		// 内部クラスReceiverのコンストラクタ
		Receiver (Socket socket){
			try{
				sisr = new InputStreamReader(socket.getInputStream()); //受信したバイトデータを文字ストリームに
				br = new BufferedReader(sisr);//文字ストリームをバッファリングする
			} catch (IOException e) {
				System.err.println("データ受信時にエラーが発生しました: " + e);
			}
		}
		// 内部クラス Receiverのメソッド
		public void run(){
			try{
				while(true) {//データを受信し続ける
					String inputLine = br.readLine();//受信データを一行分読み込む
					if (inputLine != null){//データを受信したら
						receiveMessage(inputLine);//データ受信用メソッドを呼び出す
					}
				}
			} catch (IOException e){
				System.err.println("データ受信時にエラーが発生しました: " + e);
			}
		}
	}

	public void receiveMessage(String msg) {
	    System.out.println("受信: " + msg);

	    // 色割り当ての判定
	    if (msg.equals("black") || msg.equals("white")) {
	        this.myColor = msg;
	        updateDisp();
	        return;
	    }

	    // 石を置く動作の判定
	    try {
	        int pos = Integer.parseInt(msg);
	        String currentColor = game.getTurn();
	        
	        if (game.putStone(pos, currentColor, true)) {
	            game.changeTurn(); // 手番交代
	            updateDisp();
	            
	            // --- 勝利判定（終局チェック） ---
	            if (game.isGameover()) {
	                String result = game.getWinnerMessage();
	                resultLabel.setText("対局終了");
	                JOptionPane.showMessageDialog(this, result, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
	            } else {
	                // 自分がパスになる場合の自動処理
	                checkMyPass();
	            }
	        }
            //つけたし
			if(game.getTurn().equals(myColor)){
				choicePos();
			}
	    } catch (NumberFormatException e) {
	        if (msg.equals("pass")) {
	            game.changeTurn();
	            updateDisp();
	            // パスされた後、自分がさらに置けない可能性もあるので再度チェック
	            if (game.isGameover()) {
	                JOptionPane.showMessageDialog(this, game.getWinnerMessage());
	            }
	        }
	    }
	}	
	
    private void choicePos(){
        java.util.List<Integer> canmove = game.getValidMoves(myColor);
        if(canmove.isEmpty()){
			sendMessage("pass");
			return;
		}
		int pos_best=-1;//座標
        for(int i=0;i<64;i++){
            if(canmove.contains(i)){
                if(pos_best==-1){
                    pos_best=i;
                }
                else{
                    if(pointTable[i]>pointTable[pos_best]){
                        pos_best=i;
                    }
                }
            }
        }
        String msg=Integer.toString(pos_best);
        sendMessage(msg);
    }

	private void checkMyPass() {
	    // 自分の番なのに置ける場所がない場合
	    if (game.getTurn().equals(myColor)) {
	        if (game.getValidMoves(myColor).isEmpty()) {
				//CPUだから確認しない
	            //JOptionPane.showMessageDialog(this, "置ける場所がないのでパスします。");
	            //つけたし
				game.changeTurn();
				sendMessage("pass"); // サーバ経由で相手に通知
				updateDisp();
	        }
	    }
	}
	
	public void updateDisp() {
        String[] grids = game.getGrids();
        // 自分の手番の時だけヒントを取得
        java.util.List<Integer> hints = new java.util.ArrayList<>();
        if (myColor.equals(game.getTurn())) {
            hints = game.getValidMoves(myColor);
        }

        for (int i = 0; i < buttonArray.length; i++) {
            buttonArray[i].setIcon(null); // 一旦クリア
            if (grids[i].equals("black")) {
                buttonArray[i].setIcon(blackIcon);
            } else if (grids[i].equals("white")) {
                buttonArray[i].setIcon(whiteIcon);
            } else if (hints.contains(i)) {
                buttonArray[i].setIcon(hintIcon); // ヒント（水色の丸）を表示
            }else {
                // 石もヒントもない場所は GreenFrame.jpg を表示
                buttonArray[i].setIcon(boardIcon);
            }
        }

        // 石の数と状態の更新
        if (!myColor.equals("")) {
            int myCnt = game.getCount(myColor);
            String opColor = myColor.equals("black") ? "white" : "black";
            int opCnt = game.getCount(opColor);
            infoLabel.setText("あなた(" + myColor + "): " + myCnt + "  /  相手: " + opCnt);
            
            if (game.getTurn().equals(myColor)) {
                resultLabel.setText("あなたの番です");
                resultLabel.setForeground(Color.RED);
            } else {
                resultLabel.setText("相手の番です");
                resultLabel.setForeground(Color.BLACK);
            }
        }
    }	
	
	private void checkGameOver() {
	    // 次の番の人が置ける場所があるか
	    java.util.List<Integer> nextMoves = game.getValidMoves(game.getTurn());
	    
	    if (nextMoves.isEmpty()) {
	        // 次の人が置けない場合、さらにその次の人（今の番の人）が置けるか確認
	        String opponent = game.getTurn().equals("black") ? "white" : "black";
	        java.util.List<Integer> opMoves = game.getValidMoves(opponent);
	        
	        if (opMoves.isEmpty()) {
	            // 両者置けないのでゲーム終了
	            String result = game.getWinnerMessage();
	            //CPUのためいらない
				//resultLabel.setText("対局終了！");
	            //JOptionPane.showMessageDialog(this, result, "ゲーム終了", JOptionPane.INFORMATION_MESSAGE);
	        }else {
	            // 自分（game.getTurn()）が置けないことを確認
	            if (game.getTurn().equals(myColor)) {
	                JOptionPane.showMessageDialog(this, "置ける場所がないのでパスします。");
	                sendMessage("pass"); // サーバに送信して相手に手番を回す
	            }
	        }
	    }
	}
	
	

  	//マウスクリック時の処理
    //CPUのためなし
    /* 
	public void mouseClicked(MouseEvent e) {
        if (myColor.equals("") || !game.getTurn().equals(myColor)) return;

        JButton theButton = (JButton)e.getComponent();
        String command = theButton.getActionCommand();
        
        try {
            int pos = Integer.parseInt(command);
            // 自分の色で置けるかチェック
            if (game.putStone(pos, myColor, false)) { // false=確認だけ
                sendMessage(command); // サーバへ送信（受信した時に実際に置かれる）
            }
        } catch (NumberFormatException ex) {}
    }*/
	public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
	public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
	public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
	public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理

	//テスト用のmain
	public static void main(String args[]){
		//ログイン処理
        //CPU
		/* 
        String myName = JOptionPane.showInputDialog(null,"名前を入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);
		if(myName.equals("")){
			myName = "No name";//名前がないときは，"No name"とする
		}*/
        String myName="CPU3";
		PlayerSample1 player = new PlayerSample1(); //プレイヤオブジェクトの用意(ログイン)
		player.setName(myName); //名前を受付
		OthelloSample1 game = new OthelloSample1(); //オセロオブジェクトを用意
		CPU3 oclient = new CPU3(game, player); //引数としてオセロオブジェクトを渡す
		oclient.setVisible(true);
		oclient.connectServer("localhost", 10000);
	}
}
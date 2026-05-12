import java.util.ArrayList;
import java.util.List;

public class OthelloSample1 {
	private int row = 8;	//オセロ盤の縦横マス数(2の倍数のみ)
	private String [] grids = new String [row * row]; //局面情報
	private String turn; //手番

	// コンストラクタ
	public OthelloSample1(){
		turn = "black"; //黒が先手
		for(int i = 0 ; i < row * row ; i++){
			grids[i] = "board"; //初めは石が置かれていない
			int center = row * row / 2;
			grids[center - row / 2 - 1] = "black";
			grids[center + row / 2    ] = "black";
			grids[center - row / 2    ] = "white";
			grids[center + row / 2 - 1] = "white";
		}
	}

	// メソッド
	public String checkWinner(){	// 勝敗を判断
		return "black";
	}
	public String getTurn(){ // 手番情報を取得
		return turn;
	}
	public String [] getGrids(){ // 局面情報を取得
		return grids;
	}
	// OthelloSample1.java 内に追加
	public void changeTurn() {
	    if (turn.equals("black")) {
	        turn = "white";
	    } else {
	        turn = "black";
	    }
	}
	public boolean isGameover(){	// 対局終了を判断
		
		// 黒が置けるかチェック
	    boolean blackCanMove = !getValidMoves("black").isEmpty();
	    // 白が置けるかチェック
	    boolean whiteCanMove = !getValidMoves("white").isEmpty();
	    
	    // 両方置けなければゲーム終了
	    return !blackCanMove && !whiteCanMove;
	}
	public boolean putStone(int i, String color, boolean effect_on){ // (操作を)局面に反映
		
		if (!grids[i].equals("board")) return false; // すでに石がある

	    int r = i / row;
	    int c = i % row;
	    boolean canPut = false;

	    // 8方向のオフセット（dx, dy）
	    int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
	    int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

	    String opponent = color.equals("black") ? "white" : "black";

	    for (int d = 0; d < 8; d++) {
	        int x = r + dx[d];
	        int y = c + dy[d];
	        boolean hasOpponentBetween = false;

	        // 指定方向に相手の石が続く限り進む
	        while (x >= 0 && x < row && y >= 0 && y < row && grids[x * row + y].equals(opponent)) {
	            x += dx[d];
	            y += dy[d];
	            hasOpponentBetween = true;
	        }

	        // 相手の石を挟んで自分の石があった場合
	        if (hasOpponentBetween && x >= 0 && x < row && y >= 0 && y < row && grids[x * row + y].equals(color)) {
	            canPut = true;
	            if (effect_on) { // 実際に石をひっくり返す処理
	                int tx = r + dx[d];
	                int ty = c + dy[d];
	                while (tx != x || ty != y) {
	                    grids[tx * row + ty] = color;
	                    tx += dx[d];
	                    ty += dy[d];
	                }
	            }
	        }
	    }

	    if (canPut && effect_on) {
	        grids[i] = color; // 最後にクリックした場所に石を置く
	    }
	    return canPut;
		
	}
	
	public List<Integer> getValidMoves(String color) {
        List<Integer> moves = new ArrayList<>();
        for (int i = 0; i < row * row; i++) if (putStone(i, color, false)) moves.add(i);
        return moves;
    }
	
	public int getCount(String color) {
        int cnt = 0;
        for (String s : grids) if (s.equals(color)) cnt++;
        return cnt;
    }
	
	public int getRow(){ //縦横のマス数を取得
		return row;
	}
	
	public String getWinnerMessage() {
	    int black = getCount("black");
	    int white = getCount("white");
	    
	    if (black > white) {
	        return "黒の勝ち！ (黒:" + black + " 対 白:" + white + ")";
	    } else if (white > black) {
	        return "白の勝ち！ (黒:" + black + " 対 白:" + white + ")";
	    } else {
	        return "引き分け！ (" + black + " 対 " + white + ")";
	    }
	}
	
	// OthelloSample1.java 内に追加
	public void setGrid(int pos, String color) {
	    if (pos >= 0 && pos < grids.length) {
	        grids[pos] = color;
	    }
	}
}
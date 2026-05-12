public class OthelloSample1 {
	private int row = 8;	//オセロ盤の縦横マス数(2の倍数のみ)
	private String [] grids = new String [row * row]; //局面情報
	private String [] pred_grids=new String[row*row]; //置くことのできる場所の情報
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
		
		for(int i=0;i<row*row;i++) {
			pred_grids[i]="cannot_put";
		}
	}

	// メソッド
	public String checkWinner(){	// 勝敗を判断
		String winner;	  //勝者
		int blacknum=0;	  //黒のコマの数
		int whitenum=0;	  //白のコマの数
		for(int i=0;i<row*row;i++) {
			if("black".equals(grids[i])) {
				blacknum++;
			}else if("white".equals(grids[i])) {
				whitenum++;
			}
		}
		if(blacknum>whitenum) {
			winner="black";
		}else if(whitenum>blacknum) {
			winner="white";
		}else {
			winner="draw";
		}
		return winner;
	}
	public String getTurn(){ // 手番情報を取得
		return turn;
	}
	public String [] getGrids(){ // 局面情報を取得
		return grids;
	}
	public String [] getPred_grids() {
		return pred_grids;
	}
	public void changeTurn(){ //　手番を変更
		if("black".equals(turn)) {
			turn="white";
		}else if("white".equals(turn)) {
			turn="black";
		}
	}
	
	public boolean isPass() {	//パスかどうかの判断
		boolean ispass=true;
		for(int i=0;i<row*row;i++) {
			if("can_put".equals(pred_grids[i])) {
				ispass=false;
			}
		}
		
		return ispass;
	}
	public boolean isGameover(){	// 対局終了を判断
		boolean gameover=false;
		set_pred_grids();
		if(isPass()) {
			gameover=true;
		}
		changeTurn();
		set_pred_grids();
		if(isPass()) {
			gameover=true;
		}		
		return gameover;
	}
	public void set_pred_grids() {	//盤面上の置くことのできる場所の設定
		for(int i=0;i<row*row;i++) {
			pred_grids[i]="cannot_put";
		}
		
		for(int i=0;i<8;i++) {
			for(int j=0;j<8;j++) {
				int flipnum=0;
				flipnum=pred_putStone(j,i,0,-1);
				if(flipnum>0) {
					pred_grids[i*8+j]="can_put";
				}
			}
		}
	}
	public int pred_putStone(int x,int y,int direction,int flip_num) {	//置くことができるかの確認
		int[] dx= {-1,0,1,-1,1,-1,0,1};
		int[] dy= {-1,-1,-1,0,0,1,1,1};
		int total_num=0;
		if(x<0||x>7||y<0||y>7) {
			return 0;
		}
		if(flip_num==-1) {
			if(!"board".equals(grids[y*8+x])) {
				return 0;
			}
			for(int i=0;i<8;i++) {
				flip_num=0;
				flip_num=pred_putStone(x+dx[i],y+dy[i],i,flip_num);
				total_num+=flip_num;
			}
			flip_num=total_num;
		}else if(turn.equals(grids[y*8+x])) {
			return flip_num;
		}else if("board".equals(grids[y*8+x])) {
			return 0;
		}else {
			flip_num++;
			flip_num=pred_putStone(x+dx[direction],y+dy[direction],direction,flip_num);
		}
		
		return flip_num;
		
	}
	public boolean putStone(int x,int y,int direction,boolean first,boolean success_flip){ // (操作を)局面に反映
		int[] dx= {-1,0,1,-1,1,-1,0,1};
		int[] dy= {-1,-1,-1,0,0,1,1,1};
		boolean success_total=false;
		if(x<0||x>7||y<0||y>7) {
			return false;
		}
		
		if(first) {
			for(int i=0;i<8;i++) {
				grids[y*8+x]=turn;
				success_flip=false;
				success_flip=putStone(x+dx[i],y+dy[i],i,false,success_flip);
				if(success_flip) {
					success_total=true;
				}
			}
		}else if(turn.equals(grids[y*8+x])) {
			return true;
		}else if("board".equals(grids[y*8+x])) {
			return false;
		}else {
			success_flip=putStone(x+dx[direction],y+dy[direction],direction,false,success_flip);
		}
		
		if(success_flip||success_total) {
			grids[y*8+x]=turn;
			return true;
		}else {
			return false;
		}
		
		
	}
	public int getRow(){ //縦横のマス数を取得
		return row;
	}
}
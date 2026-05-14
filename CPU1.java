import java.util.List;
import java.util.Random;

public class CPU1 {

    private String cpuColor;
    private Random rand;

    // コンストラクタ
    public CPU1(String color) {

        cpuColor = color;

        rand = new Random();
    }

    // CPUの手を決定
    public int decideMove(OthelloSample1 game) {

        // 置ける場所一覧を取得
        List<Integer> moves =
            game.getValidMoves(cpuColor);

        // 置ける場所がない場合
        if (moves.isEmpty()) {

            return -1;
        }

        // ランダムに選択
        int index =
            rand.nextInt(moves.size());

        return moves.get(index);
    }

    // CPUの色を取得
    public String getColor() {

        return cpuColor;
    }
}
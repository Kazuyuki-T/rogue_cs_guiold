import java.awt.Point;

public class Action
{
	static final int ATTACK = 0;
	static final int MOVE = 1;
	static final int USE_ITEM = 2;
	static final int STAY = 3;

	// dir
	// 7 8 9
	// 4 5 6
	// 1 2 3
	static final int BOTTOM_LEFT = 1;
	static final int BOTTOM = 2;
	static final int BOTTOM_RIGHT = 3;
	static final int LEFT = 4;
	static final int CENTER = 5;
	static final int RIGHT = 6;
	static final int TOP_LEFT = 7;
	static final int TOP = 8;
	static final int TOP_RIGHT = 9;

	int action;
	int dir;
	Point difPos; // 移動による座標の差分
	int itemIndex; // 使用したアイテムインデックス
	double evaVal; // アクションの評価値

	public Action(int pdir)
	{
		action = 0;
		//dir = pdir - 1;
		dir = 2;
		difPos = new Point(0, 0);
		itemIndex = 0;
		evaVal = 0;
	}
        
        public void sysoutput()
        {
            System.out.println("act:" + action);
            System.out.println("dir:" + dir);
            System.out.println("difpos:(" + difPos.x + ", " + difPos.y + ")");
            System.out.println("itemindex:" + itemIndex);
            System.out.println("evaVal:" + evaVal);
        }
}
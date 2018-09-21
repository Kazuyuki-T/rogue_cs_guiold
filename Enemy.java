import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;

public class Enemy extends Unit implements Cloneable
{
	private Background bg;

	Player player;
	Random random;

	static int[][] actionTable = {{7, 7, 8, 9, 9},
								  {7, 0, 0, 0, 9},
								  {4, 0,-1, 0, 6},
								  {1, 0, 0, 0, 3},
								  {1, 1, 2, 3, 3}};

	// 撃破時に得られる経験値
	public int spoint;

	// 立っているとき，カウント
	boolean repopFlag = false;
	// カウントが一定ターン->生成
	int repopCount = 0;

	// 敵識別インデックス
	int index;

	public Enemy clone(Player simuplayer)
	{
		Enemy enemy = new Enemy();
		try{
			//enemy = (Enemy)super.clone();
			
                        // プレイヤの情報を参照する
			// コピーではない
			enemy.player = simuplayer;

			// deepcopyができていない？
			enemy.img = this.img;
			enemy.objName = this.objName;
			enemy.objNum = this.objNum;
			enemy.active = this.active;
			enemy.x = this.x;
			enemy.y = this.y;
			enemy.gridScrX = this.gridScrX;
			enemy.gridScrY = this.gridScrY;
			enemy.gridMapX = this.gridMapX;
			enemy.gridMapY = this.gridMapY;
			enemy.sizeX = this.sizeX;
			enemy.sizeY = this.sizeY;
			enemy.sizeMagX = this.sizeMagX;
			enemy.sizeMagY = this.sizeMagY;
			enemy.action_flag = this.action_flag;
			enemy.dir = this.dir;
			enemy.speed = this.speed;
			enemy.maxHp = this.maxHp;
			enemy.hp = this.hp;
			enemy.attack = this.attack;
			enemy.view = this.view;
                        
                        enemy.index = this.index;
                        enemy.repopFlag = this.repopFlag;
                        enemy.repopCount = this.repopCount;
                        enemy.bg = this.bg;

                        

		}catch(Exception e){
			e.printStackTrace();
		}
		//System.out.println("test");
		return enemy;
	}

	Enemy(){}

	// コンストラクタ
	Enemy(Player iplayer, Background background)
	{
		bg = background;

		// プレイヤー情報
		player = iplayer;

		// 初期化時activeでない
		active = false;

		// ランダム
		random = new Random();
	}

	// インスタンス
	public void activate(int ix, int iy, int ind)
	{
		objName = new String("enemy");
		objNum = 3;
		active = true;

		index = ind;

		// リポップ関係を初期化
		repopFlag = false;
		repopCount = 0;

		gridMapX = ix;
		gridMapY = iy;
		gridScrX = player.gridScrX - player.gridMapX + gridMapX;
		gridScrY = player.gridScrY - player.gridMapY + gridMapY;
		x = gridWinX2x(gridScrX);
		y = gridWinY2y(gridScrY);
		dir = 0;
		action_flag = false;

		// プレイヤーの視界ぎりぎりで逃げられた際に追いかけられるように，プレイヤの視界+1
		// actionTableとの関係上，2以上とする
		// 検討の必要あり
		view = MyCanvas.SCREENGRIDSIZE_X / 2 + 1;

		// 現在の階層によって性能を変更
		if(MyCanvas.floorNumber == 0){
			maxHp = 60;
			hp = 60;
			attack = 20;
			spoint = 15;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
		else if(MyCanvas.floorNumber == 1)
		{
			maxHp = 80;
			hp = 80;
			attack = 25;
			spoint = 20;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
		else if(MyCanvas.floorNumber == 2)
		{
			maxHp = 90;
			hp = 90;
			attack = 30;
			spoint = 25;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
		else if(MyCanvas.floorNumber == 3)
		{
			maxHp = 95;
			hp = 95;
			attack = 35;
			spoint = 30;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
		else
		{
			maxHp = 60;
			hp = 60;
			attack = 35;
			spoint = 15;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
	}
        
        // 指定したlvの敵を生成
        public void activate(int ix, int iy, int ind, int level)
	{
		objName = new String("enemy");
		objNum = 3;
		active = true;

		index = ind;

		// リポップ関係を初期化
		repopFlag = false;
		repopCount = 0;

		gridMapX = ix;
		gridMapY = iy;
		gridScrX = player.gridScrX - player.gridMapX + gridMapX;
		gridScrY = player.gridScrY - player.gridMapY + gridMapY;
		x = gridWinX2x(gridScrX);
		y = gridWinY2y(gridScrY);
		dir = 0;
		action_flag = false;

		// プレイヤーの視界ぎりぎりで逃げられた際に追いかけられるように，プレイヤの視界+1
		// actionTableとの関係上，2以上とする
		// 検討の必要あり
		view = MyCanvas.SCREENGRIDSIZE_X / 2 + 1;

		// 現在の階層によって性能を変更
		if(level == 0){
			maxHp = 60;
			hp = 60;
			attack = 20;
			spoint = 15;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
		else if(level == 1)
		{
			maxHp = 80;
			hp = 80;
			attack = 25;
			spoint = 20;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
		else if(level == 2)
		{
			maxHp = 90;
			hp = 90;
			attack = 30;
			spoint = 25;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
		else if(level == 3)
		{
			maxHp = 95;
			hp = 95;
			attack = 35;
			spoint = 30;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
		else
		{
			maxHp = 60;
			hp = 60;
			attack = 35;
			spoint = 15;
			img = loadImage("mat/enemy.png"); // 敵機読み込み
			sizeX = img.getWidth(this);
			sizeY = img.getHeight(this);
			sizeMagX = MyCanvas.MAPCHIP_MAGX;
			sizeMagY = MyCanvas.MAPCHIP_MAGY;
		}
	}

	// unitを継承したための仮実装
	public void moveobj(){}

	// 引数として与えられた方向に移動
	public void moveDir(int dir)
	{
		// mapObjの更新
		// 元の敵座標をクリア
		//Background.mapUnit[gridMapY][gridMapX] = -1;
		bg.setMapUnit(gridMapX, gridMapY, -1);

		switch(dir)
		{
		// 移動
		case ObjectSet.BOTTOM_LEFT:
			// 左下
			if(isNextMoveCheck(gridMapX - 1, gridMapY + 1) == true && isDiagonalMoveCheck(-1, 1, bg) == false){
				gridMapX -= 1;
				gridMapY += 1;
			}
			// 左
			else if(isNextMoveCheck(gridMapX - 1, gridMapY) == true){
				gridMapX -= 1;
				gridMapY += 0;
			}
			// 下
			else if(isNextMoveCheck(gridMapX, gridMapY + 1) == true){
				gridMapX += 0;
				gridMapY += 1;
			}
			break;

		case ObjectSet.BOTTOM:
			// 下
			if(isNextMoveCheck(gridMapX, gridMapY + 1) == true){
				gridMapX += 0;
				gridMapY += 1;
			}
			// 左下
			else if(isNextMoveCheck(gridMapX - 1, gridMapY + 1) == true && isDiagonalMoveCheck(-1, 1, bg) == false){
				gridMapX -= 1;
				gridMapY += 1;
			}
			// 右下
			else if(isNextMoveCheck(gridMapX + 1, gridMapY + 1) == true && isDiagonalMoveCheck(1, 1, bg) == false){
				gridMapX += 1;
				gridMapY += 1;
			}
			break;

		case ObjectSet.BOTTOM_RIGHT:
			// 右下
			if(isNextMoveCheck(gridMapX + 1, gridMapY + 1) == true && isDiagonalMoveCheck(1, 1, bg) == false){
				gridMapX += 1;
				gridMapY += 1;
			}
			// 右
			else if(isNextMoveCheck(gridMapX + 1, gridMapY) == true){
				gridMapX += 1;
				gridMapY += 0;
			}
			// 下
			else if(isNextMoveCheck(gridMapX, gridMapY + 1) == true){
				gridMapX += 0;
				gridMapY += 1;
			}
			break;

		case ObjectSet.LEFT:
			// 左
			if(isNextMoveCheck(gridMapX - 1, gridMapY) == true){
				gridMapX -= 1;
				gridMapY += 0;
			}
			// 左上
			else if(isNextMoveCheck(gridMapX - 1, gridMapY - 1) == true && isDiagonalMoveCheck(-1, -1, bg) == false){
				gridMapX -= 1;
				gridMapY -= 1;
			}
			// 左下
			else if(isNextMoveCheck(gridMapX - 1, gridMapY + 1) == true && isDiagonalMoveCheck(-1, 1, bg) == false){
				gridMapX -= 1;
				gridMapY += 1;
			}
			break;

		case ObjectSet.CENTER:
			// その場で待機
			break;

		case ObjectSet.RIGHT:
			// 右
			if(isNextMoveCheck(gridMapX + 1, gridMapY) == true){
				gridMapX += 1;
				gridMapY += 0;
			}
			// 右上
			else if(isNextMoveCheck(gridMapX + 1, gridMapY - 1) == true && isDiagonalMoveCheck(1, -1, bg) == false){
				gridMapX += 1;
				gridMapY -= 1;
			}
			// 右下
			else if(isNextMoveCheck(gridMapX + 1, gridMapY + 1) == true && isDiagonalMoveCheck(1, 1, bg) == false){
				gridMapX += 1;
				gridMapY += 1;
			}
			break;

		case ObjectSet.TOP_LEFT:
			// 左上
			if(isNextMoveCheck(gridMapX - 1, gridMapY - 1) == true && isDiagonalMoveCheck(-1, -1, bg) == false){
				gridMapX -= 1;
				gridMapY -= 1;
			}
			// 左
			else if(isNextMoveCheck(gridMapX - 1, gridMapY) == true){
				gridMapX -= 1;
				gridMapY += 0;
			}
			// 上
			else if(isNextMoveCheck(gridMapX, gridMapY - 1) == true){
				gridMapX += 0;
				gridMapY -= 1;
			}
			break;

		case ObjectSet.TOP:
			// 上
			if(isNextMoveCheck(gridMapX, gridMapY - 1) == true){
				gridMapX += 0;
				gridMapY -= 1;
			}
			// 左上
			else if(isNextMoveCheck(gridMapX - 1, gridMapY - 1) == true && isDiagonalMoveCheck(-1, -1, bg) == false){
				gridMapX -= 1;
				gridMapY -= 1;
			}
			// 右上
			else if(isNextMoveCheck(gridMapX + 1, gridMapY - 1) == true && isDiagonalMoveCheck(1, -1, bg) == false){
				gridMapX += 1;
				gridMapY -= 1;
			}
			break;

		case ObjectSet.TOP_RIGHT:
			// 右上
			if(isNextMoveCheck(gridMapX + 1, gridMapY - 1) == true && isDiagonalMoveCheck(1, -1, bg) == false){
				gridMapX += 1;
				gridMapY -= 1;
			}
			// 右
			else if(isNextMoveCheck(gridMapX + 1, gridMapY) == true){
				gridMapX += 1;
				gridMapY += 0;
			}
			// 上
			else if(isNextMoveCheck(gridMapX, gridMapY - 1) == true){
				gridMapX += 0;
				gridMapY -= 1;
			}
			break;
		}

		// 移動後の座標を更新
		//Background.mapUnit[gridMapY][gridMapX] = 3;
		bg.setMapUnit(gridMapX, gridMapY, 3);
	}

	/*-- オーバーライド --*/
	/*--------------------*/
	// 引数のユニットから攻撃されたときのダメージ計算
	public void damageCalc(Unit u)
	{
		// hpから攻撃力分のダメージを引く
		hp -= u.attack;

		if(hp <= 0)
		{
			active = false;
			hp = 0;
			//Background.mapUnit[gridMapY][gridMapX] = -1;
			bg.setMapUnit(gridMapX, gridMapY, -1);
			//Game.appendRog("beat " + getName());

			repopFlag = true;
		}
	}

	// 攻撃されたときのダメージ計算
	public void damageCalc(int udam, Info info)
	{
		// hpから攻撃力分のダメージを引く
		hp -= udam;

		if(hp <= 0)
		{
			active = false;
			hp = 0;
			//bgSimu.mapUnit[gridMapY][gridMapX] = -1;
			info.mapUnit[gridMapY][gridMapX] = -1;
			//Game.appendRog("beat " + getName());

			repopFlag = true;
		}
	}

	// 攻撃されたときのダメージ計算
	public void damageCalc(int dam)
	{
		// hpから攻撃力分のダメージを引く
		hp -= dam;

		if(hp <= 0)
		{
			active = false;
			hp = 0;
			//Background.mapUnit[gridMapY][gridMapX] = -1;
			bg.setMapUnit(gridMapX, gridMapY, -1);
			//Game.appendRog("beat " + getName());

			repopFlag = true;
		}
	}
	/*--------------------*/
	/*--------------------*/

	// 移動処理
	public void moveobj(int enemyIndex)
	{
		// 視界内のプレイヤの有無により変化
		// プレイヤーが視界内にいるとき
		// 最短距離を詰め，隣接した場合攻撃
		if(isPlayerinView() == true)
		{
			// プレイヤーとの距離の差を計算
			int difx = player.gridMapX - gridMapX;
			int dify = player.gridMapY - gridMapY;

			// 必要な範囲に縮小
			if(difx < -2)	difx = -2;
			if(difx > 2) 	difx = 2;
			if(dify < -2)	dify = -2;
			if(dify > 2) 	dify = 2;

			// 0以上の整数にするために補正をかける
			// 0~4の値に変更
			difx += 2;
			dify += 2;

			int index = actionTable[dify][difx];
			if(index == 0)
			{
				// 斜め攻撃の時
				// 隣接１マス以内のため，1<=(difx,dify)<=3
				// 補正している分(+2)を取り除く
				// -1<=(ndifx,ndify)<=1
				int ndifx = difx - 2;
				int ndify = dify - 2;
				if(ndifx != 0 && ndify != 0)
				{
					// 斜め攻撃ができないとき
					if(isDiagonalAtkCheck(ndifx, ndify, bg) == true)
					{
						// 上に通行不可な部分があるとき
						if(isNextMoveCheck(gridMapX, gridMapY + ndify) == false)
						{
							// 右に進む
							if(ndifx == 1)
							{
								moveDir(ObjectSet.RIGHT);
							}
							// 左に進む
							else if(ndifx == -1)
							{
								moveDir(ObjectSet.LEFT);
							}
						}
						// 下に通行不可な部分があるとき
						else if(isNextMoveCheck(gridMapX, gridMapY + ndify) == false)
						{
							// 右に進む
							if(ndifx == 1)
							{
								moveDir(ObjectSet.RIGHT);
							}
							// 左に進む
							else if(ndifx == -1)
							{
								moveDir(ObjectSet.LEFT);
							}
						}
						// 左に通行不可な部分があるとき
						else if(isNextMoveCheck(gridMapX + ndifx, gridMapY) == false)
						{
							// 上に進む
							if(ndify == -1)
							{
								moveDir(ObjectSet.TOP);
							}
							// 下に進む
							else if(ndify == 1)
							{
								moveDir(ObjectSet.BOTTOM);
							}
						}
						// 右に通行不可な部分があるとき
						else if(isNextMoveCheck(gridMapX + ndifx, gridMapY) == false)
						{
							// 上に進む
							if(ndify == -1)
							{
								moveDir(ObjectSet.TOP);
							}
							// 下に進む
							else if(ndify == 1)
							{
								moveDir(ObjectSet.BOTTOM);
							}
						}
					}
					// 斜め攻撃ができるとき
					else
					{
						int oldhp = player.hp;
						player.damageCalc(ObjectSet.enemy[enemyIndex], bg);
						int newhp = player.hp;
						Game.appendLog("player:" + oldhp + "->" + newhp);
					}
				}
				// 斜め攻撃ではないとき
				else
				{
					// 攻撃
					int oldhp = player.hp;
					player.damageCalc(ObjectSet.enemy[enemyIndex], bg);
					int newhp = player.hp;
					Game.appendLog("player:" + oldhp + "->" + newhp);
				}
			}
			else
			{
				// 移動
				moveDir(index);
			}
		}
		// ランダムムーブ
		else
		{
			int index = random.nextInt(9) + 1;
			moveDir(index);
		}
	}



	// 隣接してプレイヤーが存在するか
	public int isPlayerNextGrid()
	{
		// 隣接してプレイヤーがいる場合，1~9のいずれかを返す
		// 存在しない場合，-1を返す
		if(gridMapX - 1 == player.gridMapX && gridMapY - 1 == player.gridMapY)
		{
			return 7;
		}
		else if(gridMapX == player.gridMapX && gridMapY - 1 == player.gridMapY)
		{
			return 8;
		}
		else if(gridMapX + 1 == player.gridMapX && gridMapY - 1 == player.gridMapY)
		{
			return 9;
		}

		else if(gridMapX - 1 == player.gridMapX && gridMapY == player.gridMapY)
		{
			return 4;
		}
		else if(gridMapX - 1 == player.gridMapX && gridMapY == player.gridMapY)
		{
			return 6;
		}

		else if(gridMapX - 1 == player.gridMapX && gridMapY - 1 == player.gridMapY)
		{
			return 1;
		}
		else if(gridMapX == player.gridMapX && gridMapY - 1 == player.gridMapY)
		{
			return 2;
		}
		else if(gridMapX + 1 == player.gridMapX && gridMapY - 1 == player.gridMapY)
		{
			return 3;
		}
		else
		{
			return -1;
		}
	}

	// 視界内にプレイヤーがいるか否か
	public boolean isPlayerinView()
	{
		// 部屋の視界を含める場合

		// 座標から，部屋or通路を判別

		// 部屋番号，あるいは通路であることを取得
		int eIndex = bg.getMapRoomNum(gridMapX, gridMapY);
		// 通路の場合，視界内にいるとき
		if(eIndex == -1)
		{
			// プレイヤーとの距離の差を計算
			int difx = player.gridMapX - gridMapX;
			int dify = player.gridMapY - gridMapY;

			// 視界内にいる場合
			if(-view <= difx && difx <= view && -view <= dify && dify <= view)
			{
				return true;
			}
		}
		// 部屋の場合
		else
		{
			// プレイヤーと同じ部屋にいる
			int pIndex = bg.getMapRoomNum(player.gridMapX, player.gridMapY);
			if(pIndex == eIndex)
			{
				return true;
			}

			// または，視界内にいる
			int difx = player.gridMapX - gridMapX;
			int dify = player.gridMapY - gridMapY;
			if(-view <= difx && difx <= view && -view <= dify && dify <= view)
			{
				return true;
			}
		}

		// プレイヤーとの距離の差を計算
		int difx = player.gridMapX - gridMapX;
		int dify = player.gridMapY - gridMapY;

		// 視界内にいる場合
		if(-view <= difx && difx <= view && -view <= dify && dify <= view)
		{
			return true;
		}

		return false;
	}

	// 移動可否チェック
	public boolean isNextMoveCheck(int nx, int ny)
	{
		// 以下の項目に該当した場合，座標の更新を行わない(偽を返す)

		// 1
		// マップの外に移動しようとしたとき
		// マップ外に行くとき，値を更新を行わない
		if((0 > nx) || (nx >= MyCanvas.MAPGRIDSIZE_X) || (0 > ny) || (ny >= MyCanvas.MAPGRIDSIZE_Y))
		{
			return false;
		}
		// 2
		// マップの通行付加部分と重複しようとしたとき
		//else if(Background.map[ny][nx] == 1)
		else if(bg.getMap(nx, ny) == 1)
		{
			return false;
		}
		// プレイヤーorほかの敵と重複しようとしたとき
		//else if(Background.mapUnit[ny][nx] != -1)
		else if(bg.getMapUnit(nx, ny) != -1)
		{
			return false;
		}

		return true;
	}

	public void draw(Graphics g)
	{
		// ウィンドウ上の座標の再計算
		gridScrX = player.gridScrX - player.gridMapX + gridMapX;
		gridScrY = player.gridScrY - player.gridMapY + gridMapY;
		// ウィンドウ上での実際の座標の再計算
		x = gridWinX2x(gridScrX);
		y = gridWinY2y(gridScrY);

		// ウィンドウ上に存在する場合
		if(0 < gridScrX && gridScrX <= MyCanvas.SCREENGRIDSIZE_X && 0 < gridScrY && gridScrY <= MyCanvas.SCREENGRIDSIZE_Y)
		{
			// アクティブな場合
			if(active == true)
			{
				// 読み込んだ画像の出力
				g.drawImage(img, x - sizeMagX/2, y - sizeMagY/2, x + sizeMagX/2, y + sizeMagY/2, 0, 0, sizeX, sizeY, this);
				// 番号
				Font eFont = new Font("Alial", Font.BOLD, 30);
				g.setColor(Color.red);
				g.setFont(eFont);
				g.drawString(String.valueOf(index), x, y);
			}
		}
	}
}
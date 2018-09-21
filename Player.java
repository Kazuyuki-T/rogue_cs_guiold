import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

// プレイヤークラス
public class Player extends Unit implements Cloneable
{
	private Background bg;

	int maxSatiety; // 満腹度
	int satiety; // 満腹度
	Inventory inventory; // インベントリ

	static int gasi = 0;

	int level; // レベル
	int exp; // 獲得経験値
	int sumExp; // 累計獲得経験値

	int curFloor;

	// レベルアップに必要な経験値を求めるための係数
	static final double LVUPEXP_COEFFICIENT = 1.1;

	// Lv1->Lv2に必要な経験値
	static final int LVUPEXP_f1t2 = 10;

	// LvUpに必要な経験値
	int lvupExp;
        
        // 満腹度が1減少するターン数
	public static final int SP_DEC_TURNNUM = 10;
	public int spDecCount;
        
        // 1ターンに回復する，全体HPの割合
	// １ターン：全体HP/値の回復
	public static final int SPONTREC_PER = 200;

	// 1ターンの回復量
	public double spontRecVal;
	// 累計の回復量，回復した分は引く
	public double sumSpontRecVal;

        public int[] getItemfloorPotion = {0, 0, 0, 0};
        public int[] getItemfloorFood =  {0, 0, 0, 0};
        public int[] getItemfloorLStaff = {0, 0, 0, 0};
        public int[] getItemfloorWstaff = {0, 0, 0, 0};
        
	public Player clone()
	{
		Player player = new Player();
		try{
			//player = (Player)super.clone();

			// deepcopyができていない？

			player.img = this.img;
			player.objName = this.objName;
			player.objNum = this.objNum;
			player.active = this.active;
			player.x = this.x;
			player.y = this.y;
			player.gridScrX = this.gridScrX;
			player.gridScrY = this.gridScrY;
			player.gridMapX = this.gridMapX;
			player.gridMapY = this.gridMapY;
			player.sizeX = this.sizeX;
			player.sizeY = this.sizeY;
			player.sizeMagX = this.sizeMagX;
			player.sizeMagY = this.sizeMagY;
			player.action_flag = this.action_flag;
			player.dir = this.dir;
			player.speed = this.speed;
			player.maxHp = this.maxHp;
			player.hp = this.hp;
			player.attack = this.attack;
			player.view = this.view;
                        
                        player.maxSatiety = this.maxSatiety;
                        player.satiety = this.satiety;
                        
                        player.curFloor = this.curFloor;
                        player.exp = this.exp;
                        player.sumExp = this.sumExp;
                        player.level = this.level;
                        player.lvupExp = this.lvupExp;
                        
                        player.spDecCount = this.spDecCount;
                        player.spontRecVal = this.spontRecVal;
                        player.sumSpontRecVal = this.sumSpontRecVal;

			player.inventory = (Inventory)this.inventory.clone();
		}catch(Exception e){
			e.printStackTrace();
		}
		return player;
	}

	public Player()
        {
		inventory = new Inventory();
	}

	// コンストラクタ
	public Player(int ix, int iy, Background background)
	{
		bg = background;

		objName = new String("player");
		objNum = 6;

		// マップ上のプレイヤのグリッド座標
		gridMapX = ix;
		gridMapY = iy;

		/*-- 変更がいらない --*/
		// ウィンドウ上のプレイヤのグリッド座標
		gridScrX = MyCanvas.SCREENGRIDSIZE_X / 2 + 1;
		gridScrY = MyCanvas.SCREENGRIDSIZE_Y / 2 + 1;
		// ウィンドウ上での実際の座標の計算
		// 主人公の座標は常にグリッドの中心
		x = gridWinX2x(gridScrX);
		y = gridWinY2y(gridScrY);
		/*--------------------*/

		dir = 2; // 初期方向

		speed = 1;
		action_flag = false;
		maxHp = 100;
		hp = 100;
		attack = 35;
		maxSatiety = 100;
		satiety = 100;
		active = false;

		// レベル
		level = 1;
		// 獲得経験値
		exp = 0;
		// 累計獲得経験値
		sumExp = 0;

		lvupExp = LVUPEXP_f1t2;

		curFloor = 0;

		// 初期maxHPに対する1Tあたりの回復量
		spontRecVal = (double)maxHp / SPONTREC_PER;
		sumSpontRecVal = 0;

		// 満腹度の減少までのカウント
		spDecCount = 0;

		// 自機読み込み
		img = loadImage("mat/player.png");

		// 本来のimgサイズ
		sizeX = img.getWidth(this);
		sizeY = img.getHeight(this);
		// 倍率をかけたimgサイズ
		sizeMagX = MyCanvas.MAPCHIP_MAGX;
		sizeMagY = MyCanvas.MAPCHIP_MAGY;

		inventory = new Inventory();

                for(int i = 0; i < 4; i++)
                {
                    getItemfloorPotion[i] = 0;
                    getItemfloorFood[i] = 0;
                    getItemfloorLStaff[i] = 0;
                    getItemfloorWstaff[i] = 0;
                    
                }
                
		// pmapを初期化
		//initpmap();
		// pmapCurを初期化
		//initpCurmap();
	}

	public void updateAtk()
	{
		// atkの更新
		attack += 1;
	}

	public void updateMaxHp()
	{
		// maxHpの更新
		maxHp += 1;
		// spontRecValの更新
		spontRecVal = (double)maxHp / SPONTREC_PER;
	}

	public void levelUp(int addp)
	{
		//System.out.println("level-up:" + level + "->" + (level+1));
		level += addp;
		// maxHpの更新・spontRecValの更新
		updateMaxHp();
		// atkの更新
		updateAtk();
	}

	// 経験値の獲得
	public void addExp(int p)
	{
		exp += p;
		sumExp += exp;
		//System.out.println("add-exp");

		// もし，LvUpに必要な経験値があったら
		while(lvupExp <= exp)
		{
			// レベルを上げる
			levelUp(1);
			// expを初期化
			exp -= lvupExp;
			// 必要経験値の更新
			lvupExp *= LVUPEXP_COEFFICIENT;
		}
	}

	// プレイヤーの指定座標への配置
	public void setPlayer(int ix, int iy)
	{
		gridMapX = ix;
		gridMapY = iy;
	}

	// move(int, int)だとエラーを吐かれる
	// 親クラスの抽象メソッドを実装
	// しかし，使用しない
	public void moveobj(){}

	// 移動処理
	public boolean moveobj(int mx, int my)
	{
		// 斜め移動な場合
		if(mx != 0 && my !=0)
		{
			// 通行不可なとき
			// isDiagonalMoveCheck(mx, my)
			// 真：通行不可
			// 偽：通行可
			if(isDiagonalMoveCheck(mx, my, bg) == true)
			{
				return false;
			}
		}

		// isNextMoveCheck()
		// 移動可能なとき真
		// それ以外偽
		if(isNextMoveCheck(gridMapX + mx, gridMapY + my) == true)
                {
			// 移動前の座標の初期化
			//Background.mapUnit[gridMapY][gridMapX] = -1;
			bg.setMapUnit(gridMapX, gridMapY, -1);
			gridMapX += mx;
			gridMapY += my;
			// 移動後の座標の更新
			//Background.mapUnit[gridMapY][gridMapX] = 6;
			bg.setMapUnit(gridMapX, gridMapY, 6);

			// 移動した先にアイテムが存在する場合
			int index = inOverlapItemCheck(gridMapX, gridMapY);
			if(index != -1)
			{
				// インベントリにアイテムを追加
				//System.out.println("addItem");
				//System.out.println(index);
				if(inventory.addItem(index, this) == true)
				{
					// 追加できた場合，プレイヤーの持つマップからアイテムを消去
					//Background.mapObject[gridMapY][gridMapX] = -1;
					bg.setMapObject(gridMapX, gridMapY, -1);
				}
			}

			// 移動した先に階段が存在する場合
			if(isOverlapStairCheck(gridMapX, gridMapY) == true)
			{
				// 階数を増やす
				MyCanvas.floorNumber++;
				curFloor = MyCanvas.floorNumber;
				// マップ，オブジェクト，配置の初期化を行うためのフラグを立てる
				MyCanvas.startFlag = false;

				// 敵からの攻撃が行われないように，
				for (int i = 0; i < ObjectSet.ENEMY_MAX; i++)
				{
					ObjectSet.enemy[i].active = false;
				}

                                // プレイヤーの持つマップの情報を初期化する
				//initpmap();
				// プレイヤーの（現在）持つ情報を初期化する
				//initpCurmap();
			}

			//System.out.println("x:" + gridMapX + ", y:" + gridMapY);
			return true;
		}

		return false;
	}

	public int inOverlapItemCheck(int nx, int ny)
	{
		// いずれかのアイテムと重複したとき，
		// 重複したアイテムのインデックスを返す
		for(int i=0; i<ObjectSet.item.length; i++)
		{
			// プレイヤーと座標が重複
			if(ObjectSet.item[i].gridMapX == nx && ObjectSet.item[i].gridMapY == ny)
			{
				// アイテムがアクティブなとき
				if(ObjectSet.item[i].active == true){
					return i;
				}
			}
		}

		// いずれのアイテムとも重複しない場合
		return -1;
	}

	public boolean isOverlapStairCheck(int nx, int ny)
	{
		if(ObjectSet.stair.gridMapX == nx && ObjectSet.stair.gridMapY == ny)
		{
			if(ObjectSet.stair.active == true){
				return true;
			}
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
		// 敵と重複しようとしたとき
		// isNMOverlapMonsCheck
		// 敵と重複するとき真
		// それ以外偽
		else if(isNMOverlapMonsCheck(nx, ny) == true)
		{
			return false;
		}
		// 3
		// マップの通行付加部分と重複しようとしたとき
		//else if(Background.map[ny][nx] == 1)
		else if(bg.getMap(nx, ny) == 1)
		{
			return false;
		}

		return true;
	}

	public boolean isNMOverlapMonsCheck(int nx, int ny)
	{
		// いずれかのエネミーと重複したとき真を返す
		for(int i=0; i<ObjectSet.enemy.length; i++)
		{
			if(ObjectSet.enemy[i].gridMapX == nx && ObjectSet.enemy[i].gridMapY == ny)
			{
				if(ObjectSet.enemy[i].active == true)
				{
					return true;
				}
			}
		}

		// いずれのエネミーとも重複しない場合
		return false;
	}

	// 描画処理
	public void draw(Graphics g)
	{
		if (active == true)
		{
			// 読み込んだ画像の出力
			g.drawImage(img, x - sizeMagX/2, y - sizeMagY/2, x + sizeMagX/2, y + sizeMagY/2, 0, 0, sizeX, sizeY, this);

			// 向き
			Point p[] = new Point[3];
			switch(dir)
			{
			// 斜め向き
			case 1:
				p[0] = new Point(x - sizeMagX/2, y + sizeMagY/2);
				p[1] = new Point(p[0].x, p[0].y - sizeMagY / 3);
				p[2] = new Point(p[0].x + sizeMagX / 3, p[0].y);
				break;
			case 3:
				p[0] = new Point(x + sizeMagX/2, y + sizeMagY/2);
				p[1] = new Point(p[0].x - sizeMagX / 3, p[0].y);
				p[2] = new Point(p[0].x, p[0].y - sizeMagY / 3);
				break;
			case 7:
				p[0] = new Point(x - sizeMagX/2, y - sizeMagY/2);
				p[1] = new Point(p[0].x + sizeMagX / 3, p[0].y);
				p[2] = new Point(p[0].x, p[0].y + sizeMagY / 3);
				break;
			case 9:
				p[0] = new Point(x + sizeMagX/2, y - sizeMagY/2);
				p[1] = new Point(p[0].x, p[0].y + sizeMagY / 3);
				p[2] = new Point(p[0].x - sizeMagX / 3, p[0].y);
				break;

			// 上下左右
			case 2:
				p[0] = new Point(x, y + sizeMagY/2);
				p[1] = new Point(p[0].x - sizeMagX / 3, p[0].y - sizeMagY / 3);
				p[2] = new Point(p[0].x + sizeMagX / 3, p[0].y - sizeMagY / 3);
				break;
			case 4:
				p[0] = new Point(x - sizeMagX/2, y);
				p[1] = new Point(p[0].x + sizeMagX / 3, p[0].y - sizeMagY / 3);
				p[2] = new Point(p[0].x + sizeMagX / 3, p[0].y + sizeMagY / 3);
				break;
			case 6:
				p[0] = new Point(x + sizeMagX/2, y);
				p[1] = new Point(p[0].x - sizeMagX / 3, p[0].y + sizeMagY / 3);
				p[2] = new Point(p[0].x - sizeMagX / 3, p[0].y - sizeMagY / 3);
				break;
			case 8:
				p[0] = new Point(x, y - sizeMagY/2);
				p[1] = new Point(p[0].x + sizeMagX / 3, p[0].y + sizeMagY / 3);
				p[2] = new Point(p[0].x - sizeMagX / 3, p[0].y + sizeMagY / 3);
				break;
			default:
				p[0] = new Point(x, y + sizeMagY/2);
				p[1] = new Point(p[0].x - sizeMagX / 3, p[0].y - sizeMagY / 3);
				p[2] = new Point(p[0].x + sizeMagX / 3, p[0].y - sizeMagY / 3);
				break;
			}

			//System.out.println(dir);
			g.setColor(Color.red);//色をシアンに変更
		    // px,pyで指定される多角形を描く（塗りつぶしあり）を描く
		    g.fillPolygon(new int[]{p[0].x, p[1].x, p[2].x}, new int[]{p[0].y, p[1].y, p[2].y}, 3);
		}
	}
}

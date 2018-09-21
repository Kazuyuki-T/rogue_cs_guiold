import java.awt.Point;
import java.util.ArrayList;

public class Info implements Cloneable
{
	// マップの最大サイズ
        public int mapsizeX;
        public int mapsizeY;
        
        // 部屋にいるとき，左上と右下の座標
        public Point currentRTopLeft;
        public Point currentRButtomRight;
        
        // プレイヤから確認できる敵のリスト
        public ArrayList<Enemy> visibleEnemy;
        
        // プレイヤから確認できるアイテムのリスト
        public ArrayList<Item> visibleItem;
        
        // 各階層の経過ターン数
        public int[] floorturn;
        
        public int eatknum; // 敵がプレイヤに攻撃できる数
        public int patknum; // プレイヤが敵に攻撃できる数
        
        public Point stairpos; // 階段の座標
        
        // map
        // mapobj
        // mapunit
        // pmap
        // pcurmap
        // turn
        // itemlist
        // enemylist
        // player
    
        
        // ↓
        // 以前から用いているもの
        
        
        /*-- obj --*/
	public Player player; // 自機
	public Enemy[] enemy; // 敵機
	public Item[] item; // アイテム
	public Stair stair; // 階段

	// 探索範囲すべてを保持
	public boolean pmap[][];
	// 現在確認できる範囲（部屋の視界+周囲視界）を保持
	public boolean pCurmap[][];

	/*-- bg --*/
	// map草か石垣か…
	public int map[][];
	// オブジェクトの配置
	public int mapObject[][];
	// ユニットの配置
	public int mapUnit[][];
	// 部屋番号のみを記す
	public int mapRoomNum[][];
        
	//public int roomNum;
	//public int passNum;
	//public ArrayList<RoomPoint> rpList;
	//public ArrayList<PassPoint> ppList;
        
        /*-- tm --*/
	public int turn;
        
	// インナークラス
	// マップの部屋の座標
	class RoomPoint
	{
		// 左上の座標
		Point topLeft;
		// 右下の座標
		Point bottmRight;
	}

	// インナークラス
	// マップの通路の座標
	class PassPoint
	{
		// 左上の座標
		Point topLeft;
		// 右下の座標
		Point bottmRight;
	}

        // コンストラクタ
	public Info(int x, int y)
        {
            
		dif[0] = new Point(-1,  1);
		dif[1] = new Point( 0,  1);
		dif[2] = new Point( 1,  1);
		dif[3] = new Point(-1,  0);
		dif[4] = new Point( 0,  0);
		dif[5] = new Point( 1,  0);
		dif[6] = new Point(-1, -1);
		dif[7] = new Point( 0, -1);
		dif[8] = new Point( 1, -1);
            
                reset(x, y);
        }

        // 盤面情報のセット
        public void setInfo(ObjectSet objOrigin, Background bgOrigin, int tnum, int[] fturn)
        {
                // deepcopyを行う

		// クローンを作成
		player = objOrigin.player.clone();

		for(int index = 0; index < ObjectSet.ENEMY_MAX; index++)
		{
			enemy[index] = ObjectSet.enemy[index].clone(player);
		}
		for(int index = 0; index < ObjectSet.ITEM_MAX; index++)
		{
			item[index] = ObjectSet.item[index].clone(player);
		}
		stair = ObjectSet.stair.clone(player);

		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				pmap[y][x] = objOrigin.getpmap(x, y);
				pCurmap[y][x] = objOrigin.getpCurmap(x, y);
			}
		}

//		roomNum = bgOrigin.roomNum;
//		passNum = bgOrigin.passNum;
//		
//		for(int index = 0; index < bgOrigin.rpList.size(); index++)
//		{
//			RoomPoint rp = new RoomPoint();
//			rp.topLeft = new Point(bgOrigin.rpList.get(index).topLeft.x, bgOrigin.rpList.get(index).topLeft.y);
//			rp.bottmRight = new Point(bgOrigin.rpList.get(index).bottmRight.x, bgOrigin.rpList.get(index).bottmRight.y);
//			rpList.add(rp);
//		}
//
//		for(int index = 0; index < bgOrigin.ppList.size(); index++)
//		{
//			PassPoint pp = new PassPoint();
//			pp.topLeft = new Point(bgOrigin.ppList.get(index).topLeft.x, bgOrigin.ppList.get(index).topLeft.y);
//			pp.bottmRight = new Point(bgOrigin.ppList.get(index).bottmRight.x, bgOrigin.ppList.get(index).bottmRight.y);
//			ppList.add(pp);
//		}

		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				//map[y][x] = bgOrigin.getMap(x, y);
				//mapUnit[y][x] = bgOrigin.getMapUnit(x, y);
				//mapObject[y][x] = bgOrigin.getMapObject(x, y);
				mapRoomNum[y][x] = bgOrigin.getMapRoomNum(x, y);
			}
		}

                // 現在の総経過ターン
		turn = tnum;
                
                // 各階層における経過ターン数
                floorturn = new int[fturn.length];
                for(int i = 0; i < fturn.length; i++)
                {
                    floorturn[i] = fturn[i];
                }
        }
        
        // 初期化
        public void reset(int mapx, int mapy)
        {
                mapsizeX = mapx;
                mapsizeY = mapy;
                
                currentRTopLeft = new Point();
                currentRButtomRight = new Point();
                
                visibleEnemy = new ArrayList<Enemy>();
            
                stairpos = new Point(-1, -1);
                
                eatknum = 0;
                patknum = 0;
                
                player = new Player();
		enemy = new Enemy[ObjectSet.ENEMY_MAX];
		for(int index = 0; index < ObjectSet.ENEMY_MAX; index++)
		{
			enemy[index] = new Enemy();
		}
		item = new Item[ObjectSet.ITEM_MAX];
		for(int index = 0; index < ObjectSet.ITEM_MAX; index++)
		{
			item[index] = new Item();
		}
		stair = new Stair();

		pmap = new boolean[mapsizeY][mapsizeX];
		pCurmap = new boolean[mapsizeY][mapsizeX];
		
//		roomNum = 0;
//		passNum = 0;
//                rpList = new ArrayList<RoomPoint>();
//                ppList = new ArrayList<PassPoint>();
		//rpList.clear();
		//ppList.clear();

		map = new int[mapsizeY][mapsizeX];
		mapUnit = new int[mapsizeY][mapsizeX];
		mapObject = new int[mapsizeY][mapsizeX];
		mapRoomNum = new int[mapsizeY][mapsizeX];
                
		turn = 0;
        }
        
	public Info clone()
	{
		Info info = new Info(this.mapsizeX, this.mapsizeY);
		try{
			//info = (Info)super.clone();
                        
                        info.mapsizeX = this.mapsizeX;
                        info.mapsizeY = this.mapsizeY;

                        info.eatknum = this.eatknum;
                        info.patknum = this.patknum;
                        
                        //System.out.println(this.currentRTopLeft.x + "," + this.currentRTopLeft.y);
                        if(this.currentRTopLeft != null)
                        {
                            info.currentRTopLeft = new Point(this.currentRTopLeft.x, this.currentRTopLeft.y);
                        }
                        if(this.currentRButtomRight != null)
                        {
                            info.currentRButtomRight = new Point(this.currentRButtomRight.x, this.currentRButtomRight.y);
                        }
                        
                        //info.visibleEnemy = new ArrayList<Enemy>(this.visibleEnemy);
                        info.visibleEnemy = new ArrayList<Enemy>();
                        for(Enemy e : this.visibleEnemy)
                        {
                            info.visibleEnemy.add((Enemy)e.clone(info.player));
                        }
                        
                        
                        // fturn
                        info.floorturn = new int[this.floorturn.length];
                        for(int i = 0; i < this.floorturn.length; i++)
                        {
                            info.floorturn[i] = this.floorturn[i];
                        }
                        
                        
                        
			info.player = (Player)this.player.clone();

			info.enemy = new Enemy[ObjectSet.ENEMY_MAX];
			for(int index = 0; index < ObjectSet.ENEMY_MAX; index++)
			{
				info.enemy[index] = (Enemy)this.enemy[index].clone(info.player);
			}
			info.item = new Item[ObjectSet.ITEM_MAX];
			for(int index = 0; index < ObjectSet.ITEM_MAX; index++)
			{
				info.item[index] = (Item)this.item[index].clone(info.player);
			}
			info.stair = (Stair)this.stair.clone(player);

			for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
			{
				for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
				{
					info.pmap[y][x] = this.pmap[y][x];
					info.pCurmap[y][x] = this.pCurmap[y][x];
				}
                        }

//			info.roomNum = this.roomNum;
//			info.passNum = this.passNum;
//			info.rpList = new ArrayList<RoomPoint>();
//			info.ppList = new ArrayList<PassPoint>();
//
//			for(int index = 0; index < this.rpList.size(); index++)
//			{
//				RoomPoint rp = new RoomPoint();
//				rp.topLeft = new Point(this.rpList.get(index).topLeft.x, this.rpList.get(index).topLeft.y);
//				rp.bottmRight = new Point(this.rpList.get(index).bottmRight.x, this.rpList.get(index).bottmRight.y);
//				info.rpList.add(rp);
//			}
//
//			for(int index = 0; index < this.ppList.size(); index++)
//			{
//				PassPoint pp = new PassPoint();
//				pp.topLeft = new Point(this.ppList.get(index).topLeft.x, this.ppList.get(index).topLeft.y);
//				pp.bottmRight = new Point(this.ppList.get(index).bottmRight.x, this.ppList.get(index).bottmRight.y);
//				info.ppList.add(pp);
//			}

			for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
			{
				for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
				{
					info.map[y][x] = this.map[y][x];
					info.mapUnit[y][x] = this.mapUnit[y][x];
					info.mapObject[y][x] = this.mapObject[y][x];
					info.mapRoomNum[y][x] = this.mapRoomNum[y][x];
				}
			}

			info.turn = this.turn;
			//System.out.println("clone succsessed");

		}catch(Exception e){
			e.printStackTrace();
		}

		return info;
	}
        
        
        
        
        
        
        // 敵の位置による攻撃範囲を示す
	// プレイヤは中心(5)
	private final int[][] dArea = {{0, 0, 1, 0, 0, 1, 1, 1, 1},
					 {0, 0, 0, 0, 0, 0, 1, 1, 1},
					 {1, 0, 0, 1, 0, 0, 1, 1, 1},
					 {0, 0, 1, 0, 0, 1, 0, 0, 1},
					 {0, 0, 0, 0, 0, 0, 0, 0, 0},
					 {1, 0, 0, 1, 0, 0, 1, 0, 0},
					 {1, 1, 1, 0, 0, 1, 0, 0, 1},
					 {1, 1, 1, 0, 0, 0, 0, 0, 0},
					 {1, 1, 1, 1, 0, 0, 1, 0, 0}};

	// 11×11
	// 直線上に敵が存在するとき
	// どちらの方向にいるか
	private final int[][] eStright = {{ 6, -1, -1, -1, -1,  7, -1, -1, -1, -1,  8},
						{-1,  6, -1, -1, -1,  7, -1, -1, -1,  8, -1},
						{-1, -1,  6, -1, -1,  7, -1, -1,  8, -1, -1},
						{-1, -1, -1,  6, -1,  7, -1,  8, -1, -1, -1},
						{-1, -1, -1, -1,  6,  7,  8, -1, -1, -1, -1},
						{ 3,  3,  3,  3,  3, -1,  5,  5,  5,  5,  5},
						{-1, -1, -1, -1,  0,  1,  2, -1, -1, -1, -1},
						{-1, -1, -1,  0, -1,  1, -1,  2, -1, -1, -1},
						{-1, -1,  0, -1, -1,  1, -1, -1,  2, -1, -1},
						{-1,  0, -1, -1, -1,  1, -1, -1, -1,  2, -1},
						{ 0, -1, -1, -1, -1,  1, -1, -1, -1, -1,  2}};

	// 壁の位置による移動可不可表
	// 0:移動不可，1:移動可
	private final int[][] mArea = {{0,1,1,1,0,1,1,1,1},
					 {0,0,0,1,0,1,1,1,1},
					 {1,1,0,1,0,1,1,1,1},
					 {0,1,1,0,0,1,0,1,1},
					 {0,0,0,0,0,0,0,0,0},
					 {1,1,0,1,0,0,1,1,0},
					 {1,1,1,1,0,1,0,1,1},
					 {1,1,1,1,0,1,0,0,0},
					 {1,1,1,1,0,1,1,1,0}};

	// 向き0~8を入れることで，差分をPoint型で得る
	private Point[] dif = new Point[9];
        
        private final int[] diffx ={ -1 , 0 , 1 , -1 , 0 , 1 , -1 ,  0 ,  1 };
        private final int[] diffy ={  1 , 1 , 1 ,  0 , 0 , 0 , -1 , -1 , -1 };
        private final int[] diffsx = {1 , 0 , -1 , 0};
        private final int[] diffsy = {0 , 1 ,  0 ,-1};
        
        public int[] getMovableGrid()
        {
                // 移動できる１できない０
		int[] movable = {1,1,1,1,0,1,1,1,1};
                for(int i = 0 ; i< 9 ; i++)
                {
                    if (map[player.gridMapY + diffy[i]][player.gridMapX + diffx[i]] == 1) 
                    {
                        for (int index = 0; index < 9; index++) 
                        {
                            // 通行可能な部分：１
                            movable[index] *= mArea[i][index];
                        }
                    }
                }
                
                return movable;
        }
        
        // 更新した場面からの呼び出し
	public ArrayList<Action> makeActionList(ArrayList<Action> actList)
	{
		Action actSimu = new Action(player.dir);

                // 攻撃できる１できない０
		int[] attakable = {0,0,0,0,0,0,0,0,0};
		// 移動できる１できない０
		int[] movable = {1,1,1,1,0,1,1,1,1};

		for(int i = 0 ; i < 9 ; i++)
                {
                        if(map[player.gridMapY + diffy[i]][player.gridMapX + diffx[i]] == 1)
                        {
				for(int index = 0; index < 9; index++)
				{
					// 通行可能な部分：１
					movable[index] *= mArea[i][index];
				}
			}
                        else
                        {
				// 敵がいるとき
				if(mapUnit[player.gridMapY + diffy[i]][player.gridMapX + diffx[i]] == 3)
				{
                                    // 通行不可に
                                    movable[i] = 0;
                                    
                                    attakable[i] = 1;

                                    if(i==0 || i==2 || i==6 || i==8)
                                    {
                                        for(int k = 0; k < 4 ;k++)
                                        {
                                            // 斜め攻撃チェック
                                            // プレイヤの差分1マス以内の時
                                            if(Math.abs(diffy[i] + diffsy[k]) < 2 && Math.abs(diffx[i] + diffsx[k]) < 2)
                                            {
                                                // かつ，壁に阻まれるとき
                                                if(map[player.gridMapY + diffy[i] + diffsy[k] ][player.gridMapX + diffx[i] + diffsx[k]] == 1)
                                                {
                                                    attakable[i] *= 0;
                                                }
                                                else
                                                {
                                                    attakable[i] *= 1;
                                                }
                                            }
                                        }
                                    }
//                                    else
//                                    {
//                                        // 敵がいるとき
//                                        attakable[i] = 1;
//                                    }
                                }
                        }
                }

		// 攻撃0，移動1
		for(int actType = 0; actType < 2; actType++)
		{
			// 上下左右，斜め -> 8通り
			for(int dirType = 0; dirType < 9; dirType++)
			{
				// 4は向きを設定していない
				if(dirType == 4)
				{
					continue;
				}

				actSimu = new Action(player.dir);

				// アクションの種類の設定
				actSimu.action = actType;
				actSimu.dir = dirType;
				actSimu.difPos = new Point(dif[dirType].x, dif[dirType].y);

				// アクションが攻撃
				// 攻撃先できない・敵がいないとき
				if(actSimu.action == Action.ATTACK && attakable[actSimu.dir] == 0)
				{
					continue;
				}
				// アクションが移動
				// かつ移動先が移動できないとき
				if(actSimu.action == Action.MOVE && movable[actSimu.dir] == 0)
				{
					continue;
				}

				// リストに追加
				actList.add(actSimu);
			}
		}

		/*
		System.out.println("actListSize:" + actList.size());
		System.out.println("player:" + player.gridMapX + "," + player.gridMapY);
		System.out.print("attackable :");
		for(int i=0; i<9; i++)
		{
			System.out.print(" " + attakable[i]);
		}
		System.out.print("\n");

		System.out.print("movable    :");
		for(int i=0; i<9; i++)
		{
			System.out.print(" " + movable[i]);
		}
		System.out.print("\n");
		*/

		/*
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				System.out.print(bgSimu.mapObject[y][x][0] + ",");
			}
			System.out.print("\n");
		}
		*/

                // 食料，ポーションなど向きの関係ないアクションの追加
		int invSize = player.inventory.getInvItemNum();
		for(int index = 0; index < invSize; index++)
		{
			// 食料など向きが関係ないアイテムが存在するとき
			// アクションリストに追加する
			if(player.inventory.getInvItemName(index).equals(new String("normal-bread")))
			{
				actSimu = new Action(player.dir);
				actSimu.action = Action.USE_ITEM;
				actSimu.itemIndex = index;
				// リストに追加
				actList.add(actSimu);

				//System.out.println("ListaddPotion");

				break;
			}
		}

		for(int index = 0; index < invSize; index++)
		{
			// ポーションなど向きが関係ないアイテムが存在するとき
			// アクションリストに追加する
			if(player.inventory.getInvItemName(index).equals(new String("normal-potion")))
			{
				actSimu = new Action(player.dir);
				actSimu.action = Action.USE_ITEM;
				actSimu.itemIndex = index;
				// リストに追加
				actList.add(actSimu);

				//System.out.println("ListaddFood");

				break;
			}
		}

		// プレイヤーの視野から敵の情報を探索
		// 上下左右斜め方向に敵がいるか
		int[] usable = {0,0,0,0,0,0,0,0,0};

		int[][] pView = new int[11][11];

		// アイテムの射程
		for(int y = -5; y <= 5; y++)
		{
			for(int x = -5; x <= 5; x++)
			{
				// 初期化
				pView[y + 5][x + 5] = 0;

				// 敵がいて，かつ視界内
				if(0 <= player.gridMapY + y && player.gridMapY + y < MyCanvas.MAPGRIDSIZE_Y &&
				   0 <= player.gridMapX + x && player.gridMapX + x < MyCanvas.MAPGRIDSIZE_X &&
				   mapUnit[player.gridMapY + y][player.gridMapX + x] == 3 &&
				   pCurmap[player.gridMapY + y][player.gridMapX + x] == true)
				{
					// 11×11のマップを更新
					pView[y + 5][x + 5] = 1;
					//System.out.println("kousin");
				}
			}
		}

		// 5:自分
		for(int y = 0; y < 11; y++)
		{
			for(int x = 0; x < 11; x++)
			{
				// 直線上か
				// かつ敵が存在するか
				if((x == y || x + y == 10 || x == 5 || y == 5) && pView[y][x] == 1)
				{
					if(x==5 && y==5){
						continue;
					}

					if(eStright[y][x] == -1)
					{
						System.out.println("error : eStright[" + y + "][" + x + "] = -1");
					}
					else
					{
						//System.out.println("usable-kousin");
						usable[eStright[y][x]] = 1;
					}
				}
			}
		}

		/*
		System.out.print("usable :");
		for(int i=0; i<9; i++)
		{
			System.out.print(" " + usable[i]);
		}
		System.out.print("\n");
		*/

		// アイテム使用のとき
		// 上下左右，斜め -> 8通り
		// lightning-staffの要素番号
		// 一つ一つのアイテムについて行う必要あり
		int invS = player.inventory.getInvItemNum();
		// lightning-staff
		int indexLstaff = -1;
		for(int index = 0; index < invS; index++)
		{
			if(player.inventory.getInvItemName(index).equals(new String("lightning-staff")))
			{
				indexLstaff = index;
				//System.out.println("Lstaff in inv");
				break;
			}
		}

		// インベントリに存在するとき
		if(indexLstaff != -1)
		{
			for(int dirType = 0; dirType < 9; dirType++)
			{
				actSimu = new Action(player.dir);
				actSimu.action = Action.USE_ITEM;

				if(usable[dirType] == 0)
				{
					continue;
				}
				else
				{
					// 遮蔽物のチェック
					// 壁が間に挟まっているときにはアイテムを使用しない
					// 射程分の確認
					for(int i = 1; i <= player.inventory.itemList.get(indexLstaff).range; i++)
					{
						// もし壁に衝突 -> break
						if(map[player.gridMapY + (dif[dirType].y * i)][player.gridMapX + (dif[dirType].x * i)] == 1)
						{
							break;
						}
						// もし敵に衝突 -> アクションリストに追加，break
						else if(mapUnit[player.gridMapY + (dif[dirType].y * i)][player.gridMapX + (dif[dirType].x * i)] == 3)
						{
							actSimu.dir = dirType;
							actSimu.difPos = new Point(dif[dirType].x, dif[dirType].y);
							actSimu.itemIndex = indexLstaff;
							// リストに追加
							actList.add(actSimu);
							break;
						}
					}
				}
			}
		}

		int indexWstaff = -1;
		for(int index = 0; index < invS; index++)
		{
			if(player.inventory.getInvItemName(index).equals(new String("warp-staff")))
			{
				indexWstaff = index;
				//System.out.println("Wstaff in inv");
				break;
			}
		}

		// インベントリに存在するとき
		if(indexWstaff != -1)
		{
			for(int dirType = 0; dirType < 9; dirType++)
			{
				actSimu = new Action(player.dir);
				actSimu.action = Action.USE_ITEM;

				if(usable[dirType] == 0)
				{
					continue;
				}
				else
				{
					// 遮蔽物のチェック
					// 壁が間に挟まっているときにはアイテムを使用しない
					// 射程分の確認
					for(int i = 1; i <= player.inventory.itemList.get(indexWstaff).range; i++)
					{
						// もし壁に衝突 -> break
						if(map[player.gridMapY + (dif[dirType].y * i)][player.gridMapX + (dif[dirType].x * i)] == 1)
						{
							break;
						}
						// もし敵に衝突 -> アクションリストに追加，break
						else if(mapUnit[player.gridMapY + (dif[dirType].y * i)][player.gridMapX + (dif[dirType].x * i)] == 3)
						{
							actSimu.dir = dirType;
							actSimu.difPos = new Point(dif[dirType].x, dif[dirType].y);
							actSimu.itemIndex = indexWstaff;
							// リストに追加
							actList.add(actSimu);
							break;
						}
					}
				}
			}
		}
                
                actSimu = new Action(player.dir);
		// アクションの種類の設定
		actSimu.action = Action.STAY;
		// リストに追加
		actList.add(actSimu);

		return actList;
	}
        
        public ArrayList<Action> makeActionLimitedList(ArrayList<Action> actList)
	{
		Action actSimu = new Action(player.dir);
                
                int playerMaxHP = player.maxHp;
                int playerHP = player.hp;
                
                int playerMaxSP = player.maxSatiety;
                int playerSP = player.satiety;

                //System.out.println("player:(" + player.gridMapX + "," + player.gridMapY + ")");
                
		// 攻撃できる１できない０
		int[] attakable = {0,0,0,0,0,0,0,0,0};
		// 移動できる１できない０
		int[] movable = {1,1,1,1,0,1,1,1,1};

		int[] diffx ={ -1 , 0 , 1 , -1 , 0 , 1 , -1 ,  0 ,  1 };
                int[] diffy ={  1 , 1 , 1 ,  0 , 0 , 0 , -1 , -1 , -1 };
                int[] diffsx = {1 , 0 , -1 , 0};
                int[] diffsy = {0 , 1 ,  0 ,-1};
                
                actSimu = new Action(player.dir);
		// アクションの種類の設定
		actSimu.action = Action.STAY;
		// リストに追加
		actList.add(actSimu);
                
                for(int i = 0; i < 9; i++)
                {
                        if(player.gridMapX + diffx[i] < 0 || MyCanvas.MAPGRIDSIZE_X <= player.gridMapX + diffx[i])
                        {
                                System.out.println("player_x:" + player.gridMapX);
                                
                                for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
                                {
                                    for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
                                    {
                                        if(player.gridMapX == x && player.gridMapY == y)
                                        {
                                            System.out.print("p");
                                        }
                                        else if(map[y][x] == 0)
                                        {
                                            System.out.print(" ");
                                        }
                                        else if(map[y][x] == 1)
                                        {
                                            System.out.print("_");
                                        }
                                        else if(map[y][x] == -100)
                                        {
                                            System.out.print("?");
                                        }
                                    }
                                    System.out.print("\n");
                                }
                        }
                        if(player.gridMapY + diffy[i] < 0 || MyCanvas.MAPGRIDSIZE_Y <= player.gridMapY + diffy[i])
                        {
                                System.out.println("player_y:" + player.gridMapY);
                                
                                for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
                                {
                                    for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
                                    {
                                        if(player.gridMapX == x && player.gridMapY == y)
                                        {
                                            System.out.print("P");
                                        }
                                        else if(map[y][x] == 0)
                                        {
                                            System.out.print("_");
                                        }
                                        else if(map[y][x] == 1)
                                        {
                                            System.out.print("|");
                                        }
                                        else if(map[y][x] == -100)
                                        {
                                            System.out.print("~");
                                        }
                                    }
                                    System.out.print("\n");
                                }
                        }

                        // 通行不可部分
                        // または，未探索部分かつ端マスのとき
                        if(map[player.gridMapY + diffy[i]][player.gridMapX + diffx[i]] == 1 ||
                           (map[player.gridMapY + diffy[i]][player.gridMapX + diffx[i]] == -100 && 
                            (player.gridMapY + diffy[i] == 0 || player.gridMapY + diffy[i] == MyCanvas.MAPGRIDSIZE_Y - 1 ||
                             player.gridMapX + diffx[i] == 0 || player.gridMapX + diffx[i] == MyCanvas.MAPGRIDSIZE_X - 1 )))
                        {
				for(int index = 0; index < 9; index++)
				{
					// 通行可能な部分：１
					movable[index] *= mArea[i][index];
				}
			}
                        else
                        {
				// 敵がいるとき
				if(mapUnit[player.gridMapY + diffy[i]][player.gridMapX + diffx[i]] == 3)
				{
                                    // 通行不可に
                                    movable[i] *= 0;
                                    
                                    attakable[i] = 1;

                                    if( i==0 || i==2 || i==6 || i==8 )
                                    {
                                        for(int k = 0; k < 4; k++)
                                        {
                                            // 斜め攻撃チェック
                                            if(Math.abs(diffy[i] + diffsy[k]) < 2 && Math.abs(diffx[i] + diffsx[k]) < 2)
                                            {
                                                // かつ，壁に阻まれるとき
                                                if(map[player.gridMapY + diffy[i] + diffsy[k] ][player.gridMapX + diffx[i] + diffsx[k]] == 1)
                                                {
                                                    attakable[i] *= 0;
                                                }
                                                else
                                                {
                                                    attakable[i] *= 1;
                                                }
                                            }
                                        }
                                    }
                                }
                        }
                }

		// 攻撃0，移動1
		for(int actType = 0; actType < 2; actType++)
		{
			// 上下左右，斜め -> 8通り
			for(int dirType = 0; dirType < 9; dirType++)
			{
				// 4は向きを設定していない
				if(dirType == 4)
				{
					continue;
				}

				actSimu = new Action(player.dir);

				// アクションの種類の設定
				actSimu.action = actType;
				actSimu.dir = dirType;
				actSimu.difPos = new Point(dif[dirType].x, dif[dirType].y);

				// アクションが攻撃
				// 攻撃先できない・敵がいないとき
				if(actSimu.action == Action.ATTACK && attakable[actSimu.dir] == 0)
				{
					continue;
				}
				// アクションが移動
				// かつ移動先が移動できないとき
				if(actSimu.action == Action.MOVE && movable[actSimu.dir] == 0)
				{
					continue;
				}

				// リストに追加
				actList.add(actSimu);
			}
		}

		/*
		System.out.println("actListSize:" + actList.size());
		System.out.println("player:" + player.gridMapX + "," + player.gridMapY);
		System.out.print("attackable :");
		for(int i=0; i<9; i++)
		{
			System.out.print(" " + attakable[i]);
		}
		System.out.print("\n");

		System.out.print("movable    :");
		for(int i=0; i<9; i++)
		{
			System.out.print(" " + movable[i]);
		}
		System.out.print("\n");
		*/

		/*
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				System.out.print(bgSimu.mapObject[y][x][0] + ",");
			}
			System.out.print("\n");
		}
		*/

                // 食料，ポーションなど向きの関係ないアクションの追加
		int invSize = player.inventory.getInvItemNum();
		for(int index = 0; index < invSize; index++)
		{
			// 食料など向きが関係ないアイテムが存在するとき
			// アクションリストに追加する
			if(player.inventory.getInvItemName(index).equals(new String("normal-bread")) && playerHP < playerMaxHP)
                        //if(player.inventory.getInvItemName(index).equals(new String("normal-bread")) && playerHP <= (int)(playerMaxHP * 0.7d))
			{
				actSimu = new Action(player.dir);
				actSimu.action = Action.USE_ITEM;
				actSimu.itemIndex = index;
				// リストに追加
				actList.add(actSimu);

				//System.out.println("ListaddPotion");

				break;
			}
		}

		for(int index = 0; index < invSize; index++)
		{
			// ポーションなど向きが関係ないアイテムが存在するとき
			// アクションリストに追加する
			if(player.inventory.getInvItemName(index).equals(new String("normal-potion")) && playerSP <= (int)(playerMaxSP * 0.5d))
                        //if(player.inventory.getInvItemName(index).equals(new String("normal-potion")) && playerSP <= (int)(playerMaxSP * 0.3d))
			{
				actSimu = new Action(player.dir);
				actSimu.action = Action.USE_ITEM;
				actSimu.itemIndex = index;
				// リストに追加
				actList.add(actSimu);

				//System.out.println("ListaddFood");

				break;
			}
		}

		// プレイヤーの視野から敵の情報を探索
		// 上下左右斜め方向に敵がいるか
		int[] usable = {0,0,0,0,0,0,0,0,0};

		int[][] pView = new int[11][11];

		// アイテムの射程
		for(int y = -5; y <= 5; y++)
		{
			for(int x = -5; x <= 5; x++)
			{
				// 初期化
				pView[y + 5][x + 5] = 0;

				// 敵がいて，かつ視界内
				if(0 <= player.gridMapY + y && player.gridMapY + y < MyCanvas.MAPGRIDSIZE_Y &&
				   0 <= player.gridMapX + x && player.gridMapX + x < MyCanvas.MAPGRIDSIZE_X &&
				   mapUnit[player.gridMapY + y][player.gridMapX + x] == 3 &&
				   pCurmap[player.gridMapY + y][player.gridMapX + x] == true)
				{
					// 11×11のマップを更新
					pView[y + 5][x + 5] = 1;
					//System.out.println("kousin");
				}
			}
		}

		// 5:自分
		for(int y = 0; y < 11; y++)
		{
			for(int x = 0; x < 11; x++)
			{
				// 直線上か
				// かつ敵が存在するか
				if((x == y || x + y == 10 || x == 5 || y == 5) && pView[y][x] == 1)
				{
					if(x==5 && y==5){
						continue;
					}

					if(eStright[y][x] == -1)
					{
						System.out.println("error : eStright[" + y + "][" + x + "] = -1");
					}
					else
					{
						//System.out.println("usable-kousin");
						usable[eStright[y][x]] = 1;
					}
				}
			}
		}

		/*
		System.out.print("usable :");
		for(int i=0; i<9; i++)
		{
			System.out.print(" " + usable[i]);
		}
		System.out.print("\n");
		*/

		// アイテム使用のとき
		// 上下左右，斜め -> 8通り
		// lightning-staffの要素番号
		// 一つ一つのアイテムについて行う必要あり
		int invS = player.inventory.getInvItemNum();
		// lightning-staff
		int indexLstaff = -1;
		for(int index = 0; index < invS; index++)
		{
			if(player.inventory.getInvItemName(index).equals(new String("lightning-staff")))
			{
				indexLstaff = index;
				//System.out.println("Lstaff in inv");
				break;
			}
		}

		// インベントリに存在するとき
		if(indexLstaff != -1)
		{
			for(int dirType = 0; dirType < 9; dirType++)
			{
				actSimu = new Action(player.dir);
				actSimu.action = Action.USE_ITEM;

				if(usable[dirType] == 0)
				{
					continue;
				}
				else
				{
					// 遮蔽物のチェック
					// 壁が間に挟まっているときにはアイテムを使用しない
					// 射程分の確認
					for(int i = 1; i <= player.inventory.itemList.get(indexLstaff).range; i++)
					{
						// もし壁に衝突 -> break
						if(map[player.gridMapY + (dif[dirType].y * i)][player.gridMapX + (dif[dirType].x * i)] == 1)
						{
							break;
						}
						// もし敵に衝突 -> アクションリストに追加，break
						else if(mapUnit[player.gridMapY + (dif[dirType].y * i)][player.gridMapX + (dif[dirType].x * i)] == 3)
						{
							actSimu.dir = dirType;
							actSimu.difPos = new Point(dif[dirType].x, dif[dirType].y);
							actSimu.itemIndex = indexLstaff;
							// リストに追加
							actList.add(actSimu);
							break;
						}
					}
				}
			}
		}

		int indexWstaff = -1;
		for(int index = 0; index < invS; index++)
		{
			if(player.inventory.getInvItemName(index).equals(new String("warp-staff")))
			{
				indexWstaff = index;
				//System.out.println("Wstaff in inv");
				break;
			}
		}

		// インベントリに存在するとき
		if(indexWstaff != -1)
		{
			for(int dirType = 0; dirType < 9; dirType++)
			{
				actSimu = new Action(player.dir);
				actSimu.action = Action.USE_ITEM;

				if(usable[dirType] == 0)
				{
					continue;
				}
				else
				{
					// 遮蔽物のチェック
					// 壁が間に挟まっているときにはアイテムを使用しない
					// 射程分の確認
					for(int i = 1; i <= player.inventory.itemList.get(indexWstaff).range; i++)
					{
						// もし壁に衝突 -> break
						if(map[player.gridMapY + (dif[dirType].y * i)][player.gridMapX + (dif[dirType].x * i)] == 1)
						{
							break;
						}
						// もし敵に衝突 -> アクションリストに追加，break
						else if(mapUnit[player.gridMapY + (dif[dirType].y * i)][player.gridMapX + (dif[dirType].x * i)] == 3)
						{
							actSimu.dir = dirType;
							actSimu.difPos = new Point(dif[dirType].x, dif[dirType].y);
							actSimu.itemIndex = indexWstaff;
							// リストに追加
							actList.add(actSimu);
							break;
						}
					}
				}
			}
		}
//                for(int n = 0; n < actList.size(); n++)
//                {
//                    System.out.println("act num " + n);
//                    actList.get(n).sysoutput();
//                }

		return actList;
	}
}
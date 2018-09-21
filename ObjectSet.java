import java.awt.Graphics;
import java.awt.Point;
import java.util.Random;

// オブジェクト監視クラス
public class ObjectSet
{
	// 出現最大数
	public static final int ENEMY_MAX = 4;
	public static final int ITEM_MAX = 5;

	private Background bg;

	public Player player; // 自機
	static Enemy[] enemy; // 敵機
	static Item[] item; // アイテム
	static Stair stair;

	// 探索範囲すべてを保持
	private boolean[][] pmap;
	// 現在確認できる範囲（部屋の視界+周囲視界）を保持
	private boolean[][] pCurmap;

	// 敵のリポップ間隔
	public static final int REPOP_INTERVAL = 64;

	private Random random;

	// 7 8 9
	// 4 5 6
	// 1 2 3
	public static final int BOTTOM_LEFT = 1;
	public static final int BOTTOM = 2;
	public static final int BOTTOM_RIGHT = 3;
	public static final int LEFT = 4;
	public static final int CENTER = 5;
	public static final int RIGHT = 6;
	public static final int TOP_LEFT = 7;
	public static final int TOP = 8;
	public static final int TOP_RIGHT = 9;

	// コンストラクタ
	public ObjectSet(Background background)
	{
		// 探索範囲すべてを保持
                pmap = new boolean[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
                // 現在確認できる範囲（部屋の視界+周囲視界）を保持
                pCurmap = new boolean[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
            
                random = new Random();
            
                bg = background;

		// プレイヤーの作成
		// (初期x座標,初期y座標,スピード)
		player = new Player(4, 4, bg);
		player.active = true;

		// 敵の配列を確保
		enemy = new Enemy[ENEMY_MAX];
		for(int i = 0; i < enemy.length; i++)
		{
			enemy[i] = new Enemy(player, bg);
		}

		// アイテムの配列を確保
		item = new Item[ITEM_MAX];
		for(int i=0; i < item.length; i++)
		{
			item[i] = new Item(player);
		}

		// 階段のインスタンスを確保
		stair = new Stair(player);

		// csvファイルからアイテム情報を読み取る
		Item.setItemInfo();
	}

	// オブジェクトの初期化
	// 階をまたいだ時など
	public void initObjectsetExceptPlayer()
	{
		// 敵の配列を確保
		enemy = new Enemy[ENEMY_MAX];
		for(int i = 0; i < enemy.length; i++)
		{
			enemy[i] = new Enemy(player, bg);
		}

		// アイテムの配列を確保
		item = new Item[ITEM_MAX];
		for(int i=0; i < item.length; i++)
		{
			item[i] = new Item(player);
		}

		// 階段のインスタンスを確保
		stair = new Stair(player);
	}

        public boolean[][] getpmap()
        {
            return pmap;
        }
	public boolean getpmap(int x, int y)
	{
		return pmap[y][x];
	}
        
	public void setpmap(int x, int y, boolean tf)
	{
		pmap[y][x] = tf;
	}
        public boolean[][] getpCurmap()
        {
            return pCurmap;
        }
	public boolean getpCurmap(int x, int y)
	{
		return pCurmap[y][x];
	}
	public void setpCurmap(int x, int y, boolean tf)
	{
		pCurmap[y][x] = tf;
	}

	// プレイヤーの持つマップ情報の初期化
	public void initpmap()
	{
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				//pmap[y][x] = false;
				setpmap(x, y, false);
			}
		}
	}

	// プレイヤーの（現在）持つ情報の初期化
	public void initpCurmap()
	{
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				//pCurmap[y][x] = false;
				setpCurmap(x, y, false);
			}
		}
	}

	// 更新
	public void pmapUpdate()
	{
		int rn = bg.getMapRoomNum(player.gridMapX, player.gridMapY);

		// 現在地が部屋の時，かつ初めて訪れる部屋の場合
		// 部屋の情報を得る
		if(rn != -1)
		{
			// 部屋の情報の更新
			for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
			{
				for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
				{
					// 部屋番号が同じとき，pmapを更新
					//if(Background.mapRoomNum[y][x] == rn)
					if(bg.getMapRoomNum(x, y) == rn)
					{
						pmap[y][x] = true;

						// 部屋の周囲1マス分拡張したい
						// 何かしらの判定のほうが望ましい
						pmap[y-1][x-1] = true;
						pmap[y-1][x] = true;
						pmap[y-1][x+1] = true;
						pmap[y][x-1] = true;
						pmap[y][x+1] = true;
						pmap[y+1][x-1] = true;
						pmap[y+1][x] = true;
						pmap[y+1][x+1] = true;
					}
				}
			}
		}

		// 視界内の情報の更新
		for(int y = player.gridMapY - MyCanvas.SCREENGRIDSIZE_Y / 2; y <= player.gridMapY + MyCanvas.SCREENGRIDSIZE_Y / 2; y++)
		{
			for(int x = player.gridMapX - MyCanvas.SCREENGRIDSIZE_X / 2; x <= player.gridMapX + MyCanvas.SCREENGRIDSIZE_X / 2; x++)
			{
				// x,yが範囲内の時
				if(0 <= y && y < MyCanvas.MAPGRIDSIZE_Y && 0 <= x && x < MyCanvas.MAPGRIDSIZE_X)
				{
					pmap[y][x] = true;
				}
			}
		}
	}

	// 更新
	public void pCurmapUpdate()
	{
		int rn = bg.getMapRoomNum(player.gridMapX, player.gridMapY);

		// 現在地が部屋の時，かつ初めて訪れる部屋の場合
		// 部屋の情報を得る
		if(rn != -1)
		{
			// 部屋の情報の更新
			for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
			{
				for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
				{
					// 部屋番号が同じとき，pmapを更新
					if(bg.getMapRoomNum(x, y) == rn)
					{
						pCurmap[y][x] = true;

						// 部屋の周囲1マス分拡張したい
						// 何かしらの判定のほうが望ましい
						pCurmap[y-1][x-1] = true;
						pCurmap[y-1][x] = true;
						pCurmap[y-1][x+1] = true;
						pCurmap[y][x-1] = true;
						pCurmap[y][x+1] = true;
						pCurmap[y+1][x-1] = true;
						pCurmap[y+1][x] = true;
						pCurmap[y+1][x+1] = true;
					}
				}
			}
		}

		// 視界内の情報の更新
		for(int y = player.gridMapY - MyCanvas.SCREENGRIDSIZE_Y / 2; y <= player.gridMapY + MyCanvas.SCREENGRIDSIZE_Y / 2; y++)
		{
				for(int x = player.gridMapX - MyCanvas.SCREENGRIDSIZE_X / 2; x <= player.gridMapX + MyCanvas.SCREENGRIDSIZE_X / 2; x++)
				{
				// x,yが範囲内の時
				if(0 <= y && y < MyCanvas.MAPGRIDSIZE_Y && 0 <= x && x < MyCanvas.MAPGRIDSIZE_X)
				{
					pCurmap[y][x] = true;
				}
			}
		}
	}

	// 描画処理。すべてのゲームオブジェクトを描画する。
	// 描画順序が需要
	// ユニットは上に来るよう，後に描画
	public void drawAll(Graphics g)
	{
		doGameObjects(g, stair); // 階段
		doGameObjects(g, item); // アイテム

		doGameObjects(g, enemy); // 敵
		player.draw(g); // プレイヤー
	}

	public void doGameObjects(Graphics g, Object objary)
	{
		if (objary.active == true)
		{
			//objary.moveobj();
			objary.draw(g);				//委譲
		}
	}

	public void doGameObjects(Graphics g, Object[] objary)
	{
		for (int i = 0; i < objary.length; i++)
		{
			if (objary[i].active == true)
			{
				//objary[i].moveobj();
				objary[i].draw(g);				//委譲
			}
		}
	}

	public void doGameObjects(Graphics g, Unit[] objary)
	{
		for (int i = 0; i < objary.length; i++)
		{
			if (objary[i].active == true)
			{
				//objary[i].moveobj();
				objary[i].draw(g);				//委譲
			}
		}
	}

	// 敵の生成・初期化・再生成
	public static int newEnemy(int ix, int iy)
	{
		// 再生成
		for (int i = 0; i < ENEMY_MAX; i++)
		{
			if ((enemy[i].active) == false && enemy[i].repopCount == REPOP_INTERVAL)
			{
				enemy[i].activate(ix, iy, i);
				return i;
			}
		}
		// 生成・初期化
		for (int i = 0; i < ENEMY_MAX; i++)
		{
			if ((enemy[i].active) == false)
			{
				enemy[i].activate(ix, iy, i);
				return i;
			}
		}
		return -1;		//見つからなかった
	}

        // 敵の生成・初期化・再生成
        // レベル指定
        public static int newEnemy(int ix, int iy, int level)
	{
		// 再生成
		for (int i = 0; i < ENEMY_MAX; i++)
		{
			if ((enemy[i].active) == false && enemy[i].repopCount == REPOP_INTERVAL)
			{
				enemy[i].activate(ix, iy, i, level);
				return i;
			}
		}
		// 生成・初期化
		for (int i = 0; i < ENEMY_MAX; i++)
		{
			if ((enemy[i].active) == false)
			{
				enemy[i].activate(ix, iy, i, level);
				return i;
			}
		}
		return -1;		//見つからなかった
	}
        
	// アイテムの生成・初期化
	public static int newItem(int ix, int iy, int idn)
	{
		for (int i = 0; i < ITEM_MAX; i++)
		{
			if ((item[i].active) == false)
			{
				item[i].activate(ix, iy, idn);
				return i;
			}
		}
		return -1;		//見つからなかった
	}

	// 階段の生成
	public static boolean newStair(int ix, int iy)
	{
		if ((stair.active) == false)
		{
			stair.activate(ix, iy);
			return true;
		}

		return false;		//見つからなかった
	}

	// 真：４方向のいずれかに通路がある
	// 偽：４方向のいずれにも通路なし
	public boolean isCheckPassJustBefore(Background bg, Point p)
	{
		for(int i=0; i<bg.ppList.size(); i++)
		{
			// 上
			if(p.x == bg.ppList.get(i).bottmRight.x && p.y - 1 == bg.ppList.get(i).bottmRight.y)
			{
				return true;
			}
			// 下
			if(p.x == bg.ppList.get(i).topLeft.x && p.y + 1 == bg.ppList.get(i).topLeft.y)
			{
				return true;
			}
			// 左
			if(p.x - 1 == bg.ppList.get(i).bottmRight.x && p.y == bg.ppList.get(i).bottmRight.y)
			{
				return true;
			}
			// 右
			if(p.x + 1 == bg.ppList.get(i).topLeft.x && p.y == bg.ppList.get(i).topLeft.y)
			{
				return true;
			}
		}

		return false;
	}

	public Point geneRandPos()
	{
		// 生成する部屋をランダムに決定
		int randRoomNum;
		// ほかのオブジェクトとの重複チェック
		Point gP = new Point();
		do{
			randRoomNum = random.nextInt(bg.roomNum);
			// 選択した部屋の左上の座標と右上の座標
			// 0:top-left 1:bottom-right
			Point[] roomPoint = new Point[2];
			roomPoint = bg.getRoomPoint(randRoomNum);

			// 生成座標をランダムに決定
			// 0~(br-tl)+tl
			gP.x = random.nextInt(roomPoint[1].x - roomPoint[0].x) + roomPoint[0].x;
			gP.y = random.nextInt(roomPoint[1].y - roomPoint[0].y) + roomPoint[0].y;
		} while(//Background.mapUnit[gP.y][gP.x] != -1 ||
				bg.getMapUnit(gP.x, gP.y) != -1 ||
				//Background.mapObject[gP.y][gP.x] != -1 ||
				bg.getMapObject(gP.x, gP.y) != -1 ||
				//Background.map[gP.y][gP.x] == 1 ||
				bg.getMap(gP.x, gP.y) == 1 ||
				isCheckPassJustBefore(bg, gP) == true);
		// 生成した座標にobjが存在するとき，または通行不可な部分のとき
		// 再生成

		return gP;
	}

	// 引数として与えられた部屋番号に生成されないような
	public Point geneRandPos(int rn)
	{
		// 生成する部屋をランダムに決定
		int randRoomNum;
		// ほかのオブジェクトとの重複チェック
		Point gP = new Point();
		do{
			do{
				randRoomNum = random.nextInt(bg.roomNum);
			} while(randRoomNum == rn);
			// 部屋番号が重複している間，生成しなおす

			// 選択した部屋の左上の座標と右上の座標
			// 0:top-left 1:bottom-right
			Point[] roomPoint = new Point[2];
			roomPoint = bg.getRoomPoint(randRoomNum);

			// 生成座標をランダムに決定
			// 0~(br-tl)+tl
			gP.x = random.nextInt(roomPoint[1].x - roomPoint[0].x) + roomPoint[0].x;
			gP.y = random.nextInt(roomPoint[1].y - roomPoint[0].y) + roomPoint[0].y;
		} while(//Background.mapUnit[gP.y][gP.x] != -1 ||
				bg.getMapUnit(gP.x, gP.y) != -1 ||
				//Background.mapObject[gP.y][gP.x] != -1 ||
				bg.getMapObject(gP.x, gP.y) != -1 ||
				//Background.map[gP.y][gP.x] == 1 ||
				bg.getMap(gP.x, gP.y) == 1 ||
				isCheckPassJustBefore(bg, gP) == true);
		// 生成した座標にobjが存在するとき，または通行不可な部分のとき
		// 再生成

		return gP;
	}

        public void setObject(String objstr, int lv, int x, int y)
        {
                Point geneP = new Point(x, y);

		// 生成するオブジェクトにより生成時に呼び出す関数が異なる
		if(objstr.equals(new String("player")))
		{
			// プレイヤーのランダム配置
			player.setPlayer(geneP.x, geneP.y);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("player"));

			// プレイヤーの持つマップの初期化
			initpmap();
			// 更新
			pmapUpdate();

			// プレイヤーの持つ（現在の）マップの初期化
			initpCurmap();
			// 更新
			pCurmapUpdate();
		}
		else if(objstr.equals(new String("enemy")))
		{
			// インスタンス化
			ObjectSet.newEnemy(geneP.x, geneP.y, lv);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("enemy"));

			//System.out.println("enemy random-gene");
			//System.out.println("(" + geneP.x + ", " + geneP.y + ")");
		}
		else if(objstr.equals(new String("re-enemy")))
		{
			// プレイヤーと同じ部屋に生成されないように
			// geneRandPos(bg)を用いた場合
			/*
			while(true)
			{
				// プレイヤーと同じ部屋の時
				if(Background.getRoomNumber(geneP) == Background.getRoomNumber(new Point(player.gridMapX, player.gridMapY)))
				{
					geneP = geneRandPos(bg);
				}
				else
				{
					break;
				}
			}
			*/

			// プレイヤーと同じ部屋に生成されないように
			geneP = geneRandPos(bg.getMapRoomNum(player.gridMapX, player.gridMapY));

			// インスタンス化
			ObjectSet.newEnemy(geneP.x, geneP.y, lv);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("enemy"));

			//System.out.println("enemy random-gene");
			//System.out.println("(" + geneP.x + ", " + geneP.y + ")");
		}
		else if(objstr.equals(new String("item")))
		{
			// 生成するアイテムidを決定
			// 1~3
			int randItemId = random.nextInt(Item.ITEMSHEET_ROW - 1) + 1;

			// インスタンス化
			ObjectSet.newItem(geneP.x, geneP.y, randItemId);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("item"));

			//System.out.println("item random-gene");
			//System.out.println("id : " + randItemId);
			//System.out.println("(" + geneP.x + ", " + geneP.y + ")");
		}
		else if(objstr.equals(new String("stair")))
		{
			// インスタンス化
			ObjectSet.newStair(geneP.x, geneP.y);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("stair"));

			//System.out.println("stair random-gene");
			//System.out.println("(" + geneP.x + ", " + geneP.y + ")");
		}
        }
        
	// マップと比較し，ランダムにオブジェクトの生成位置を決める
	// 部屋にのみ生成，通路には生成しない
	// 部屋の１マス内側に生成
	public void setObjectRand(String objstr)
	{
		Point geneP = new Point();
		geneP = geneRandPos();

		// 生成するオブジェクトにより生成時に呼び出す関数が異なる
		if(objstr.equals(new String("player")))
		{
			// プレイヤーのランダム配置
			player.setPlayer(geneP.x, geneP.y);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("player"));

			// プレイヤーの持つマップの初期化
			initpmap();
			// 更新
			pmapUpdate();

			// プレイヤーの持つ（現在の）マップの初期化
			initpCurmap();
			// 更新
			pCurmapUpdate();
		}
		else if(objstr.equals(new String("enemy")))
		{
			// インスタンス化
			ObjectSet.newEnemy(geneP.x, geneP.y);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("enemy"));

			//System.out.println("enemy random-gene");
			//System.out.println("(" + geneP.x + ", " + geneP.y + ")");
		}
		else if(objstr.equals(new String("re-enemy")))
		{
			// プレイヤーと同じ部屋に生成されないように
			// geneRandPos(bg)を用いた場合
			/*
			while(true)
			{
				// プレイヤーと同じ部屋の時
				if(Background.getRoomNumber(geneP) == Background.getRoomNumber(new Point(player.gridMapX, player.gridMapY)))
				{
					geneP = geneRandPos(bg);
				}
				else
				{
					break;
				}
			}
			*/

			// プレイヤーと同じ部屋に生成されないように
			geneP = geneRandPos(bg.getMapRoomNum(player.gridMapX, player.gridMapY));

			// インスタンス化
			ObjectSet.newEnemy(geneP.x, geneP.y);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("enemy"));

			//System.out.println("enemy random-gene");
			//System.out.println("(" + geneP.x + ", " + geneP.y + ")");
		}
		else if(objstr.equals(new String("item")))
		{
			// 生成するアイテムidを決定
			// 1~3
			int randItemId = random.nextInt(Item.ITEMSHEET_ROW - 1) + 1;

			// インスタンス化
			ObjectSet.newItem(geneP.x, geneP.y, randItemId);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("item"));

			//System.out.println("item random-gene");
			//System.out.println("id : " + randItemId);
			//System.out.println("(" + geneP.x + ", " + geneP.y + ")");
		}
		else if(objstr.equals(new String("stair")))
		{
			// インスタンス化
			ObjectSet.newStair(geneP.x, geneP.y);
			// map+objの更新
			bg.mapObjectUpdate(geneP, new String("stair"));

			//System.out.println("stair random-gene");
			//System.out.println("(" + geneP.x + ", " + geneP.y + ")");
		}

	}

	// 選択したユニットがワープする処理
	public void warpUnit(Unit u)
	{
		// ユニットのワープ前座標の部分を初期化
		//Background.mapUnit[u.gridMapY][u.gridMapX] = -1;
		bg.setMapUnit(u.gridMapX, u.gridMapY, -1);

		Point geneP = new Point();
		int roomN = bg.getMapRoomNum(u.gridMapX, u.gridMapY);
		// 現在部屋の時，同じ部屋に飛ばないように
		if(roomN != -1)
		{
			// 同じ箇所にワープしないような工夫
			// ワープはいずれかの部屋に
			geneP = geneRandPos(roomN);
		}
		// 通路の時，いずれかの部屋
		else
		{
			geneP = geneRandPos();
		}

		// ランダムに座標を決定
		u.gridMapX = geneP.x;
		u.gridMapY = geneP.y;

		// ユニットのワープ後座標の更新
		//Background.mapUnit[u.gridMapY][u.gridMapX] = u.objNum;
		bg.setMapUnit(u.gridMapX, u.gridMapY, u.objNum);
	}

	// エネミーが移動する処理
	public void moveEnemy(Background bg)
	{
		for (int i = 0; i < enemy.length; i++)
		{
			if (enemy[i].active == true)
			{
				enemy[i].moveobj(i);
				// アクションフラグの更新必要か？
			}
			else
			{
				enemy[i].repopCount++;
			}

			if(enemy[i].repopCount == REPOP_INTERVAL)
			{
				// 再生成
				setObjectRand(new String("re-enemy"));
			}
		}
	}

	// 同時押しを考慮してカウンターを用意
	int counter = 0;

	// プレイヤーが移動する処理
	// 移動できればtrue
	// できなければfalse
	public boolean movePlayer(KeyInput keyinput)
	{
		// 上
		if(keyinput.keyUp == true)
		{
			// 待ち時間
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 下
			if(keyinput.keyLeft == false && keyinput.keyRight == false)
			{
				if(player.dir == TOP && player.moveobj(0, -1) == true)
				{
					player.action_flag = true;
					player.dir = TOP;
					return true;
				}
				else
				{
					player.dir = TOP;
					return false;
				}
			}
			// 左下
			else if(keyinput.keyLeft == true && keyinput.keyRight == false)
			{

			}
			// 右下
			else if(keyinput.keyLeft == false && keyinput.keyRight == true)
			{

			}
		}

		// 下
		else if(keyinput.keyDown == true)
		{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(keyinput.keyLeft == false && keyinput.keyRight == false)
			{

			}
			else if(keyinput.keyLeft == true && keyinput.keyRight == false)
			{

			}
			else if(keyinput.keyLeft == false && keyinput.keyRight == true)
			{

			}
		}

		// 左
		else if(keyinput.keyLeft == true)
		{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(keyinput.keyUp == false && keyinput.keyDown == false)
			{

			}
			else if(keyinput.keyUp == true && keyinput.keyDown == false)
			{

			}
			else if(keyinput.keyUp == false && keyinput.keyDown == true)
			{

			}
		}

		// 右
		else if(keyinput.keyRight == true)
		{
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(keyinput.keyUp == false && keyinput.keyDown == false)
			{

			}
			else if(keyinput.keyUp == true && keyinput.keyDown == false)
			{

			}
			else if(keyinput.keyUp == false && keyinput.keyDown == true)
			{

			}
		}

		/*
			// 左下
			if(keyinput.keyDown == true && keyinput.keyLeft == true)
			{
				//
				keyinput.keyDown = false;
				keyinput.keyLeft = false;

				// 向いている方向と同じ
				// かつ，移動可能
				if(player.dir == BOTTOM_LEFT && player.moveobj(-1, 1) == true)
				{
					// 移動
					player.action_flag = true;
					player.dir = BOTTOM_LEFT;
					return true;
				}
				// 向いている方向と異なる
				else
				{
					// 向き変更
					player.dir = BOTTOM_LEFT;
					return false;
				}
			}

			// 下
			else if(keyinput.keyDown == true)
			{
				keyinput.keyDown = false;
				if(player.dir == BOTTOM && player.moveobj(0, 1) == true)
				{
					player.action_flag = true;
					player.dir = BOTTOM;
					return true;
				}
				else
				{
					player.dir = BOTTOM;
					return false;
				}
			}

			// 右下
			else if(keyinput.keyDown == true && keyinput.keyRight == true)
			{
				keyinput.keyDown = false;
				keyinput.keyRight = false;
				if(player.dir == BOTTOM_RIGHT && player.moveobj(1, 1) == true)
				{
					player.action_flag = true;
					player.dir = BOTTOM_RIGHT;
					return true;
				}
				else
				{
					player.dir = BOTTOM_RIGHT;
					return false;
				}
			}

			// 左
			else if(keyinput.keyLeft == true)
			{
				keyinput.keyLeft = false;
				if(player.dir == LEFT && player.moveobj(-1, 0) == true)
				{
					player.action_flag = true;
					player.dir = LEFT;
					return true;
				}
				else
				{
					player.dir = LEFT;
					return false;
				}
			}

			// 右
			else if(keyinput.keyRight == true)
			{
				keyinput.keyRight = false;
				if(player.dir == RIGHT && player.moveobj(1, 0) == true)
				{
					player.action_flag = true;
					player.dir = RIGHT;
					return true;
				}
				else
				{
					player.dir = RIGHT;
					return false;
				}
			}

			// 左上
			else if(keyinput.keyUp == true && keyinput.keyLeft == true)
			{
				keyinput.keyUp = false;
				keyinput.keyLeft = false;
				if(player.dir == TOP_LEFT && player.moveobj(-1, -1) == true)
				{
					player.action_flag = true;
					player.dir = TOP_LEFT;
					return true;
				}
				else
				{
					player.dir = TOP_LEFT;
					return false;
				}
			}



			// 右上
			else if(keyinput.keyUp == true && keyinput.keyRight == true)
			{
				keyinput.keyUp = false;
				keyinput.keyRight = false;
				if(player.dir == TOP_RIGHT && player.moveobj(1, -1) == true)
				{
					player.action_flag = true;
					player.dir = TOP_RIGHT;
					return true;
				}
				else
				{
					player.dir = TOP_RIGHT;
					return false;
				}
			}

			// 待機
			else if(keyinput.keyS == true)
			{
				keyinput.keyS = false;
				player.action_flag = true;
				return true;
			}
			*/

		/*
		// 同時押しがあるか否か
		if(keyinput.keyAlt == true && keyinput.keyShift == false && keyinput.keyCtrl == false)
		{
			// Alt + 方向 -> 斜め移動

			// 同時押しフラグのリセット
			keyinput.keyAlt = false;

			// Alt + Left -> 左上
			if(keyinput.checkLeftShotKey() == 2)
			{
				if(player.moveobj(-1, -1) == true)
				{
					player.action_flag = true;
					player.dir = 7;
					return true;
				}
				else
				{
					player.dir = 7;
					return false;
				}
			}
			// Alt + Right -> 右下
			else if(keyinput.checkRightShotKey() == 2)
			{
				if(player.moveobj(1, 1) == true)
				{
					player.action_flag = true;
					player.dir = 3;
					return true;
				}
				else
				{
					player.dir = 3;
					return false;
				}
			}
			// Alt + Up -> 右上
			else if(keyinput.checkUpShotKey() == 2)
			{
				if(player.moveobj(1, -1))
				{
					player.action_flag = true;
					player.dir = 9;
					return true;
				}
				else
				{
					player.dir = 9;
					return false;
				}
			}
			// Alt + Down -> 左下
			else if(keyinput.checkDownShotKey() == 2)
			{
				if(player.moveobj(-1, 1))
				{
					player.action_flag = true;
					player.dir = 1;
					return true;
				}
				else
				{
					player.dir = 1;
					return false;
				}
			}
		}
		else if(keyinput.keyShift == true && keyinput.keyAlt == false && keyinput.keyCtrl == false){
			// Shift + 方向 -> 向き変更

			// 同時押しフラグのリセット
			keyinput.keyShift = false;

			// Shift + Left -> 左
			if(keyinput.checkLeftShotKey() == 2)
			{
				player.dir = 4;
				return true;
			}
			// Shift + Right -> 右
			else if(keyinput.checkRightShotKey() == 2)
			{
				player.dir = 6;
				return true;
			}
			// Shift + Up -> 上
			else if(keyinput.checkUpShotKey() == 2)
			{
				player.dir = 8;
				return true;
			}
			// Shift + Down -> 下
			else if(keyinput.checkDownShotKey() == 2)
			{
				player.dir = 2;
				return true;
			}
		}
		else if(keyinput.keyShift == true && keyinput.keyAlt == true && keyinput.keyCtrl == false){
			// Shift + Alt + 方向 -> 斜め方向を向く

			// 同時押しフラグのリセット
			keyinput.keyShift = false;
			keyinput.keyAlt = false;

			// 左が押されているとき
			// 左上
			if(keyinput.keyLEshot == 2)
			{
				keyinput.keyLEshot = 1;
				player.dir = 7;
				return true;
			}
			// 右が押されているとき
			// 右下
			else if(keyinput.keyRIshot == 2)
			{
				keyinput.keyRIshot = 1;
				player.dir = 3;
				return true;
			}
			// 上が押されているとき
			// 右上
			else if(keyinput.keyUPshot == 2)
			{
				keyinput.keyUPshot = 1;
				player.dir = 9;
				return true;
			}
			// 下が押されているとき
			// 左下
			else if(keyinput.keyDOshot == 2)
			{
				keyinput.keyDOshot = 1;
				player.dir = 1;
				return true;
			}
		}
		//else if(keyinput.keyCtrl == true && keyinput.keyAlt == false && keyinput.keyShift == false){
			// Ctrl + 方向 -> スキップ
		//}
		else if(keyinput.keyCtrl == false && keyinput.keyAlt == false && keyinput.keyShift == false)
		{
			// 矢印のみ

			// 左
			if(keyinput.checkLeftShotKey() == 2)
			{
				if(player.moveobj(-1, 0))
				{
					player.action_flag = true;
					player.dir = 4;
					return true;
				}
				else
				{
					player.dir = 4;
					return false;
				}
			}
			// 右
			else if(keyinput.checkRightShotKey() == 2)
			{
				if(player.moveobj(1, 0))
				{
					player.action_flag = true;
					player.dir = 6;
					return true;
				}
				else
				{
					player.dir = 6;
					return false;
				}
			}
			// 上
			else if(keyinput.checkUpShotKey() == 2)
			{
				if(player.moveobj(0, -1))
				{
					player.action_flag = true;
					player.dir = 8;
					return true;
				}
				else
				{
					player.dir = 8;
					return false;
				}
			}
			// 下
			else if(keyinput.checkDownShotKey() == 2)
			{
				if(player.moveobj(0, 1))
				{
					player.action_flag = true;
					player.dir = 2;
					return true;
				}
				else
				{
					player.dir = 2;
					return false;
				}
			}
			// 待機
			else if(keyinput.keyS == true)
			{
				player.action_flag = true;
				//System.out.println("s");
				return true;
			}
		}
		*/

		return false;
	}

	// プレイヤがアイテムを使用したとき
	public boolean useItemPlayer(KeyInput keyinput)
	{
		if(keyinput.checkUShotKey() == 2)
		{
			player.action_flag = true;
			return true;
		}

		return false;
	}

	// プレイヤがアイテムを投げた時
	public boolean throwItemPlayer(KeyInput keyinput)
	{
		if(keyinput.checkTShotKey() == 2)
		{
			player.action_flag = true;
			return true;
		}

		return false;
	}

	// 引数：座標(x,y)からキャラクターの向いている方向の1マス隣の座標を返す
	public Point nextGridAxis(int x, int y, int dir)
	{
		Point p = new Point(x, y);

		switch(dir)
		{
		case 1:
			p.x--;
			p.y++;
			break;
		case 2:
			p.y++;
			break;
		case 3:
			p.x++;
			p.y++;
			break;

		case 4:
			p.x--;
			break;
		case 6:
			p.x++;
			break;

		case 7:
			p.x--;
			p.y--;
			break;
		case 8:
			p.y--;
			break;
		case 9:
			p.x++;
			p.y--;
			break;

		default:
			System.out.println("nGA-default:" + dir);
			break;
		}

		return p;
	}

	// プレイヤが攻撃したとき
	public boolean attackPlayer(KeyInput keyinput)
	{
		if(keyinput.checkEnterShotKey() == 2)
		{
			// 隣の座標
			Point np = nextGridAxis(player.gridMapX, player.gridMapY, player.dir);

			// 1マス先に敵がいるとき
			int nextMonsIndex = inNextGridMonster(np);
			// なおかつ，斜め攻撃で地形に阻まれないとき
			if(nextMonsIndex != -1 && player.isDiagonalAtkCheck(np.x - player.gridMapX, np.y - player.gridMapY, bg) == false)
			{
				int oldhp = enemy[nextMonsIndex].hp;
				enemy[nextMonsIndex].damageCalc(player); // ダメージ計算・アクティブ処理
				int newhp = enemy[nextMonsIndex].hp < 0 ? 0 : enemy[nextMonsIndex].hp;
				Game.appendLog("enemy" + nextMonsIndex + ":" + oldhp + "->" + newhp);

				// 敵を倒したか否かのチェック
				if(enemy[nextMonsIndex].active == false)
				{
					// 倒した場合，経験値を得る
					player.addExp(enemy[nextMonsIndex].spoint);
				}
			}
			// いないときもターンはカウントする

			player.action_flag = true;
			return true;
		}

		return false;
	}

	public void attackPlayer()
	{
		// 隣の座標
		Point np = nextGridAxis(player.gridMapX, player.gridMapY, player.dir);

		// 1マス先に敵がいるとき
		int nextMonsIndex = inNextGridMonster(np);
		// なおかつ，斜め攻撃で地形に阻まれないとき
		if(nextMonsIndex != -1 &&
		   player.isDiagonalAtkCheck(np.x - player.gridMapX, np.y - player.gridMapY, bg) == false)
		{
			int oldhp = enemy[nextMonsIndex].hp;
			enemy[nextMonsIndex].damageCalc(player); // ダメージ計算・アクティブ処理
			int newhp = enemy[nextMonsIndex].hp < 0 ? 0 : enemy[nextMonsIndex].hp;
			Game.appendLog("enemy" + nextMonsIndex + ":" + oldhp + "->" + newhp);

			// 敵を倒したか否かのチェック
			if(enemy[nextMonsIndex].active == false)
			{
				// 倒した場合，経験値を得る
				player.addExp(enemy[nextMonsIndex].spoint);
				Game.appendLog("get-exp");
			}
		}
		else
		{
			Game.appendLog("attack-miss : " + nextMonsIndex + ", " + player.isDiagonalAtkCheck(np.x - player.gridMapX, np.y - player.gridMapY, bg));
			//System.out.println("attack-miss");
		}
		// いないときもターンはカウントする
	}

	// いずれかのエネミーが隣にいたとき，要素番号を返す
	public int inNextGridMonster(Point p)
	{
		for(int i = 0; i < ENEMY_MAX; i++)
		{
			// いずれかのエネミーが隣にいたとき，要素番号を返す
			if(enemy[i].gridMapX == p.x && enemy[i].gridMapY == p.y && enemy[i].active == true)
			{
				return i;
			}
		}

		return -1;
	}

	// ゲームオーバー判定
	public boolean isGameover()
	{
		return !this.player.active;
	}
}

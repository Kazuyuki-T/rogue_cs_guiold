import java.awt.Point;
import java.util.ArrayList;


public class TurnManager
{
	private int turn;
	private Background bg;

        private Info info;
        
        private int[] floorturn;
        
        // コンストラクタ
	public TurnManager(Background background, int x, int y)
	{
		bg = background;
                info = new Info(x, y);
                
                turn = 1;
                floorturn = new int[MyCanvas.TOPFLOOR];
                for(int i=0; i < floorturn.length; i++)
                {
                    floorturn[i] = (i == 0) ? 1 : 0;
                }
	}

	public void init()
	{
		turn = 1;
                for(int i=0; i < floorturn.length; i++)
                {
                    floorturn[i] = (i == 0) ? 1 : 0;
                }
	}

	public int getTurn()
	{
		return turn;
	}
        
        public int getTurn(int fnum)
        {
                return floorturn[fnum];
        }
        
        // 満腹度の減少
	public void spDec(Player player)
	{
		player.spDecCount++;
		if(player.spDecCount == player.SP_DEC_TURNNUM)
		{
			if(player.satiety > 0)
			{
				player.satiety--;
			}

			player.spDecCount = 0;
		}
	}

	// 満腹度0の際のHP減少
	public void spZeroHpDec(Player player)
	{
		player.damageCalc(1, bg);
	}

	// HPの自然回復
	public void spontRecovery(Player player)
	{
		player.sumSpontRecVal += player.spontRecVal;
		while(player.sumSpontRecVal >= 1.0){
			player.hp += 1;
			if(player.hp > player.maxHp)
			{
				player.hp = player.maxHp;
			}
			player.sumSpontRecVal -= 1.0;
		}
	}

	public void turnFlow(ObjectSet objectset, Background bg)
	{
		// 敵の行動
		objectset.moveEnemy(bg);
		// ...
		// ...

                
                
		// プレイヤがアクティブなとき
                // かつ，クリアしていないとき
		if(objectset.player.active == true && objectset.player.curFloor != MyCanvas.TOPFLOOR)
		{
			// アクションフラグリセット
			objectset.player.action_flag = false;

			// 満腹度の減少
			spDec(objectset.player);

			// 満腹度0の際のHP減少
			if(objectset.player.satiety == 0)
			{
				spZeroHpDec(objectset.player);
				// ０になったとき
				if(objectset.player.hp <= 0)
				{
					MyCanvas.gasi++;
                                        MyCanvas.gasif[MyCanvas.floorNumber]++;
				}
			}
			else
			{
				// HPの自然回復
				spontRecovery(objectset.player);
			}

			// プレイヤーの持つマップ情報の更新
			objectset.pmapUpdate();

			// プレイヤーの現在持つマップ情報
			objectset.initpCurmap();
			objectset.pCurmapUpdate();

			// ターン終了時に，プレイヤが持つアイテム一覧
			if(objectset.player.inventory.getInvItemNum() != 0)
			{
				Game.appendLog("Inventory");
			}
			for(int index = 0; index < objectset.player.inventory.getInvItemNum(); index++)
			{
				Game.appendLog(index + " : " + objectset.player.inventory.getInvItemName(index) + "(" + objectset.player.inventory.getInvItemUsageCount(index) + ")");
			}

			// ターンの更新
			turn++;
                        
                        // フロアの滞在ターン数の更新
                        floorturn[objectset.player.curFloor]++;
                        
			//System.out.println("turn:" + turn);
			Game.appendLog("\n" + "[turn : " + turn + "]");
		}
                else
                {
                    // プレイヤーがノンアクティブ -> ゲームオーバー
                }
                
		//System.out.println("/*----------------------------*/");
	}

	// アイテムの使用・投擲によるターン経過の管理
	public void turnCount(ObjectSet objectset, KeyInput keyinput, int sItemNum, Background bg)
	{
		// アイテム使用
		if(objectset.useItemPlayer(keyinput) == true)
		{
			// 使用したアイテムによる効果処理・削除
			objectset.player.inventory.useItem(objectset, sItemNum, bg);
		}
		// アイテム投擲
		else if(objectset.throwItemPlayer(keyinput) == true)
		{
			//
			//System.out.println("throw item : " + sItemNum);
		}

		// もしプレイヤーが動作をしたならば
		if(objectset.player.action_flag == true){
			turnFlow(objectset, bg);
		}
	}

	// 移動・攻撃によるターン経過の管理
	public void turnCount(ObjectSet objectset, KeyInput keyinput, Background bg)
	{
		// ・いずれかが行われた
		// ・すべて行われていない
		// ことを確認できれば良い
		//if move
		//else if attack

		if(objectset.movePlayer(keyinput) == true)
		{
			// 移動
		}
		else if(objectset.attackPlayer(keyinput) == true)
		{
			// 攻撃
		}

		// もしプレイヤーが動作をしているならば
		if(objectset.player.action_flag == true){
			turnFlow(objectset, bg);
		}
	}
        
        // 現在のobjset，bg
	public void turnCount(ObjectSet objectset, Background bg, Agent ag)
	{
		// objset，bgを与えられてアクションを返す
		Action act = new Action(objectset.player.dir);

                // agent に info を渡すための変換
                
		// infoの初期化
                info.reset(MyCanvas.MAPGRIDSIZE_X, MyCanvas.MAPGRIDSIZE_Y);
		
                // infoを更新する前に，制限した情報の作成
                // mapの制限
                info.map = visMapCreate(objectset.getpmap(), bg.getMap());
                info.mapObject = visMapUOCreate(objectset.getpmap(), bg.getMapObject());
                info.mapUnit = visMapUOCreate(objectset.getpCurmap(), bg.getMapUnit());
                
                // 確認できる敵
                info.visibleEnemy = setVisibleEnemy(objectset, bg.getMapUnit(), info);
                
                // 現在の部屋の左上・右下　とか
                Point[] TLBR = getCurRPtlbr(objectset.player, bg);
                
                if(TLBR == null)
                {
                    info.currentRTopLeft = null;
                    info.currentRButtomRight = null;
                }
                else
                {
                    info.currentRTopLeft = TLBR[0];
                    info.currentRButtomRight = TLBR[1];
                }
                
                // 情報の更新
                info.setInfo(objectset, bg, turn, floorturn);
                
                

                // 左上，右下のポイントの更新
                // 現在の視界から，mapの制限
                // 確認できる敵の更新
                // 確認できるアイテムの更新
                
                
                
		// 周囲の状況
//		String outputStr = new String();
//		for(int y = 1; y >= -1; y--)
//		{
//			for(int x = -1; x <= 1; x++)
//			{
//				int nobj = bg.getMapObject(x + objectset.player.gridMapX, y + objectset.player.gridMapY);
//				outputStr += nobj + " ";
//			}
//			outputStr += "\n";
//		}
//		//Game.appendRog(outputStr);
//		System.out.print(outputStr);
//		
//		Game.appendLog("player(" + objectset.player.gridMapX + ", " + objectset.player.gridMapY + "):" + objectset.player.hp + ", " + objectset.player.active);
//		for(int index = 0; index < objectset.ENEMY_MAX; index++)
//		{
//			Game.appendLog("enemy" + index + "(" + objectset.enemy[index].gridMapX + ", " + objectset.enemy[index].gridMapY + "):" + objectset.enemy[index].hp + ", " + objectset.enemy[index].active);
//		}
//		int pcounter = 0;
//		String outputStr = new String();
//		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
//		{
//			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
//			{
//				//int nobj = Background.mapUnit[y][x];
//				int nobj = bg.getMapUnit(x, y);
//				if(nobj==6)
//				{
//					outputStr += "p ";
//					pcounter++;
//				}
//				else if(nobj==3) 	outputStr += "e ";
//				else if(nobj==-1)	outputStr += "- ";
//				else				outputStr += "? ";
//			}
//			outputStr += "\n";
//		}
//		Game.appendLog(outputStr);
//		//System.out.print(outputStr);
//		if(pcounter == 0)
//		{
//			Game.appendLog("p not found");
//		}
//
//                System.out.println("player-act : " + act.action);
//		System.out.println("player-dir : " + act.dir);



//                System.out.println("----------------------floor:" + info.player.curFloor + "-------------------------");
//                for(int y = 0; y < info.mapsizeY; y++)
//                {
//                    for(int x = 0; x < info.mapsizeX; x++)
//                    {
//                        boolean flag = false;
//                        if(x == info.player.gridMapX && y == info.player.gridMapY)
//                        {
//                            System.out.print("p ");
//                            flag = true;
//                        }
//                        
//                        for(int index = 0; index < info.visibleEnemy.size(); index++)
//                        {
//                            if(x == info.visibleEnemy.get(index).gridMapX && y == info.visibleEnemy.get(index).gridMapY && info.visibleEnemy.get(index).active == true)
//                            {
//                                System.out.print("e" + info.visibleEnemy.get(index).index);
//                                flag = true;
//                            }
//                        }
//                        
//                        if(flag == false)
//                        {
//                            if(info.map[y][x] == 0)
//                            {
//                                System.out.print("_ ");
//                            }
//                            else
//                            {
//                                System.out.print("  ");
//                            }
//                        }
//                    }
//                    System.out.println();
//                }
//                for(int en = 0; en < info.visibleEnemy.size(); en++)
//                {
//                    System.out.println("en" + info.visibleEnemy.get(en).index + ":(" + 
//                            info.visibleEnemy.get(en).gridMapX + "," + info.visibleEnemy.get(en).gridMapY + ")");
//                }
//                System.out.println("player:(" + info.player.gridMapX + "," + info.player.gridMapY + ")");
//                System.out.println("Lv:" + info.player.level);
//                System.out.println("Hp:" + info.player.hp + "/" + info.player.maxHp);
//                System.out.println("sp:" + info.player.satiety);
//                // アイテムの出力
//                for(int i = 0; i < info.player.inventory.itemList.size(); i++)
//                {
//                    //
//                    System.out.println("item" + i + ":" + info.player.inventory.itemList.get(i).name + "(" + info.player.inventory.itemList.get(i).usageCount + ")");
//                }

                
                
                
                // infoを引数として，Actionが戻り値となる
		act = ag.makeAction(info);

		// act.dir 0~8->1~9
		act.dir++;
                
                
                
		// 攻撃のとき
		if(act.action == Action.ATTACK){
			objectset.player.dir = act.dir;
			objectset.attackPlayer();
			objectset.player.action_flag = true;
		}
		// 移動のとき
		else if(act.action == Action.MOVE)
		{
			objectset.player.dir = act.dir;
			if(objectset.player.moveobj(act.difPos.x, act.difPos.y) == true)
			{
				objectset.player.action_flag = true;
			}
		}
		// アイテム使用
		else if(act.action == Action.USE_ITEM)
		{
			objectset.player.dir = act.dir;
			objectset.player.inventory.useItem(objectset, act.itemIndex, bg);
			objectset.player.action_flag = true;
		}
		else if(act.action == Action.STAY)
		{
			objectset.player.dir = act.dir;
			objectset.player.action_flag = true;
		}

		// もしプレイヤーが行動しているならば
		if(objectset.player.action_flag == true)
		{
			turnFlow(objectset, bg);
		}
		// 行動していないとき
		else
		{
			System.out.println("do-nothing(act:" + act.action + ", dir(1~9):" + act.dir + ")");
		}
	}
        
        
        
        
        
        // 以下，infoへの変換用
        
        // pmapとの掛け合わせ
        public int[][] visMapCreate(boolean[][] pmap, int[][] map)
        {
            int limitedmap[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
            
            for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
            {
                for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
                {
                       if(pmap[y][x] == true)
                       {
                           limitedmap[y][x] = map[y][x];
                       }
                       else
                       {
                           if(x == 0 || x == MyCanvas.MAPGRIDSIZE_X - 1 || y == 0 || y == MyCanvas.MAPGRIDSIZE_Y - 1)
                           {
                               limitedmap[y][x] = 1;
                           }
                           else
                           {
                               limitedmap[y][x] = -100;
                           }
                       }
                }    
            }
            return limitedmap;
        }
        
        public int[][] visMapUOCreate(boolean[][] pmap, int[][] map)
        {
            int limitedmap[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
            
            for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
            {
                for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
                {
                       if(pmap[y][x] == true)
                       {
                           limitedmap[y][x] = map[y][x];
                       }
                       else
                       {
                           limitedmap[y][x] = -1;
                       }
                }    
            }
            return limitedmap;
        }
        
        // 修正必要?
        public Point[] getCurRPtlbr(Player pl, Background bg)
	{
                Point[] TLBR = new Point[2];
                int mrn = bg.getMapRoomNum(pl.gridMapX, pl.gridMapY);
                
                if(mrn == -1)
                {
                        return null;
                }
                else
                {
                        Point TopLeft = bg.rpList.get(mrn).topLeft;
                        Point ButtomRight = bg.rpList.get(mrn).bottmRight;
                        TLBR[0] = new Point(TopLeft.x,TopLeft.y);
                        TLBR[1] = new Point(ButtomRight.x,ButtomRight.y);
                        return TLBR;
                }
	}
        
        public ArrayList<Enemy> setVisibleEnemy(ObjectSet objectset, int[][] umap, Info info)
        {
                ArrayList<Enemy> visEnemy = new ArrayList<Enemy>();
            
                for(int y=0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
                {
                        for(int x=0; x < MyCanvas.MAPGRIDSIZE_X; x++)
                        {
                                if(objectset.getpCurmap(x, y) == true && umap[y][x] == 3)
                                {
                                        for(int eNum = 0; eNum < objectset.enemy.length; eNum++)
                                        {
                                                if(objectset.enemy[eNum].gridMapX == x && objectset.enemy[eNum].gridMapY == y && objectset.enemy[eNum].active == true)
                                                {
                                                        visEnemy.add(objectset.enemy[eNum].clone(objectset.player));
                                                        //visEnemy.add(info.enemy[eNum]);
                                                        break;
                                                }
                                        }
                                }
                        }
                }
                
                return visEnemy;
        }
}
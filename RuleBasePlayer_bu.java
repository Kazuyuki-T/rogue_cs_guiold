import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class RuleBasePlayer_bu implements Agent
{
	Random random;

	int[] difX = new int[] {-1, 0, 1,-1, 0, 1,-1, 0, 1};
	int[] difY = new int[] { 1, 1, 1, 0, 0, 0,-1,-1,-1};

	int playerFloor;

	// 通路のリスト
	ArrayList<PassPos> passList = new ArrayList<PassPos>();

	// 行ったことのあるなし
	int arriveMap[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];

	// 敵の位置による攻撃範囲を示す
	// プレイヤは中心(5)
	int[][] dArea = {{0, 0, 1, 0, 0, 1, 1, 1, 1},
					 {0, 0, 0, 0, 0, 0, 1, 1, 1},
					 {1, 0, 0, 1, 0, 0, 1, 1, 1},
					 {0, 0, 1, 0, 0, 1, 0, 0, 1},
					 {0, 0, 0, 0, 0, 0, 0, 0, 0},
					 {1, 0, 0, 1, 0, 0, 1, 0, 0},
					 {1, 1, 1, 0, 0, 1, 0, 0, 1},
					 {1, 1, 1, 0, 0, 0, 0, 0, 0},
					 {1, 1, 1, 1, 0, 0, 1, 0, 0}};

	public RuleBasePlayer_bu()
	{
		random = new Random();

		playerFloor = MyCanvas.floorNumber;

		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				arriveMap[y][x] = 0;
			}
		}
	}

	// 前回の履歴
	Point history = new Point();
        int stepsetup_ = 100;
        MonteCarloPlayer mcp = new MonteCarloPlayer();
        public void monteCarloStepSetup(int A){
        stepsetup_ = A;
        mcp.stepSetup(stepsetup_);
        }
        
                
	public Action ruleBased(Info info)
	{
		Action ruleBesedAct = new Action(info.player.dir);

		// 階層が変化したとき
		if(playerFloor !=  info.player.curFloor)
		{
			// 階層の更新
			playerFloor = info.player.curFloor;
			// 目的地リストのクリア
			passList.clear();
			// arriveMapの初期化
			for(int y = 0; y <  MyCanvas.MAPGRIDSIZE_Y; y++)
			{
				for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
				{
					arriveMap[y][x] = 0;
				}
			}
		}

		// 更新
		arriveMap[info.player.gridMapY][info.player.gridMapX]++;

		// 到達の更新
		if(passList.size() != 0)
		{
			isArriveCheck(info);
		}

		// 隣接している敵のリスト
		ArrayList<AroundEnemy> aroundEnemy = new ArrayList<AroundEnemy>();
		// 目的地のリスト
		ArrayList<Destination> destList = new ArrayList<Destination>();

		if(is1gridMosCheck(info, aroundEnemy) == true)
		{
			System.out.println("is1grid");

			ruleBesedAct = mcp.makeAction(info);
		}
		else if(is2gridOverMosCheck(info, aroundEnemy) == true)
		{
			System.out.println("is2grid");

			// 敵との距離を比較，一番近いものに対して行動を選択
			// 敵との距離，適当な大きい数字
			int targetEnemyIndex = 0;
			int distEnemy = aroundEnemy.get(targetEnemyIndex).dis;
			for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				if(distEnemy > aroundEnemy.get(eNum).dis)
				{
					// 更新
					distEnemy = aroundEnemy.get(eNum).dis;
					targetEnemyIndex = eNum;
				}
			}

			//System.out.println("size:"+aroundEnemy.size());
			//System.out.println("dist:"+distEnemy);

			// 5マス以下のとき
			if(distEnemy <= 5)
			{
				ruleBesedAct = mcp.makeAction(info);
			}
			// それ以上の時
			else
			{
				// 目的地を敵の座標に設定，１マス近づく
				ruleBesedAct.action = Action.MOVE;
				int eDir = aroundEnemy.get(targetEnemyIndex).dir;
				ruleBesedAct.difPos.x = difX[eDir];
				ruleBesedAct.difPos.y = difY[eDir];

				// 設定した座標が壁の場合，通路の探索に戻る
				if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
				{
					ruleBesedAct = passSearchMove(info);
				}
			}
		}
		// 最下層であり，部屋に階段がある->目的地を階段に設定，１マス近づく
		else if(info.player.curFloor == MyCanvas.TOPFLOOR - 1 && isStairCheck(info, destList) == true)
		{
			destChecker(info);

			for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 5)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					int sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}
					else
					{
						//
					}

					break;
				}
			}

			System.out.println("rulebased-3");
		}
		// 部屋の中にアイテムが落ちている->目的地をアイテムに設定，１マス近づく
		else if(isItemCheck(info, destList) == true && info.player.inventory.getInvItemNum() != Inventory.MAX_INV)
		{
			destChecker(info);

			for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 4)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					int sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}

					break;
				}
			}

			System.out.println("rulebased-4");
		}
		// おなかがすいた
		else if(info.player.satiety < 80)
		{
			// 食料がある
			int foodIndex = inInvFoodCheck(info.player.inventory);
			if(foodIndex != -1)
			{
				ruleBesedAct.action = Action.USE_ITEM;
				ruleBesedAct.dir = info.player.dir;
				ruleBesedAct.itemIndex = foodIndex;
			}
			// ない
			else
			{
				// 部屋に階段がある
				if(isStairCheck(info, destList) == true)
				{
					for(int index = 0; index < destList.size(); index++)
					{
						if(destList.get(index).objNum == 5)
						{
							ruleBesedAct.action = Action.MOVE;
							// リストの中から抽出
							int sDir = destList.get(index).dir;
							ruleBesedAct.difPos.x = difX[sDir];
							ruleBesedAct.difPos.y = difY[sDir];

							// 設定した座標が壁の場合，通路の探索に戻る
							if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
							{
								ruleBesedAct = passSearchMove(info);
							}

							break;
						}
					}
				}
				else
				{
					// 目的地の探索・移動
					ruleBesedAct = passSearchMove(info);
				}
			}

			System.out.println("rulebased-5");
		}
		// hpが7割以下であり，満腹度が半分以上
		else if(info.player.hp < info.player.maxHp * 0.7 && info.player.satiety > 50)
		{
			ruleBesedAct.action = Action.STAY;

			System.out.println("rulebased-6");
		}
		// 部屋の中に階段がある
		else if(isStairCheck(info, destList) == true)
		{
			for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 5)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					int sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}

					break;
				}
			}

			System.out.println("rulebased-7");
		}
		// いったことのない通路を目的地とする
		else
		{
			// 目的地の探索・移動
			ruleBesedAct = passSearchMove(info);

			System.out.println("rulebased-8");

			// 各アクションのdirの更新タイミング
			// アイテム使用は方向が重要
			// 移動は移動先決定 -> 方向をその向きに変更
		}

		// アクションが移動の時
		if(ruleBesedAct.action == Action.MOVE)
		{
			// 斜め移動の制御
			// 壁に突っ込まないように
			ruleBesedAct = convSlantingMove(info, ruleBesedAct);

			// 斜め補正をかけても移動できないとき
			if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
			{
				ruleBesedAct.action = Action.STAY;
			}

			// rbAct.dif -> rbAct.dir
			for(int index = 0; index < 9; index++)
			{
				if(ruleBesedAct.difPos.x == difX[index] && ruleBesedAct.difPos.y == difY[index])
				{
					ruleBesedAct.dir = index;
					break;
				}
			}

			// 履歴の更新
			history.x = info.player.gridMapX;
			history.y = info.player.gridMapY;
		}

		return ruleBesedAct;
	}
        public Action ruleBasedOnly(Info info)
	{
		Action ruleBesedAct = new Action(info.player.dir);

		// 階層が変化したとき
		if(playerFloor !=  info.player.curFloor)
		{
			// 階層の更新
			playerFloor = info.player.curFloor;
			// 目的地リストのクリア
			passList.clear();
			// arriveMapの初期化
			for(int y = 0; y <  MyCanvas.MAPGRIDSIZE_Y; y++)
			{
				for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
				{
					arriveMap[y][x] = 0;
				}
			}
		}

		// 更新
		arriveMap[info.player.gridMapY][info.player.gridMapX]++;
		// 到達の更新
		if(passList.size() != 0)
		{
			isArriveCheck(info);
		}

		// 隣接している敵のリスト
		ArrayList<AroundEnemy> aroundEnemy = new ArrayList<AroundEnemy>();
		// 目的地のリスト
		ArrayList<Destination> destList = new ArrayList<Destination>();

		
		
		// 敵が隣にいる
		if(is1gridMosCheck(info, aroundEnemy) == true)
		{
			int sumDamage = 0;
			for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				// ダメージの合計
				sumDamage += aroundEnemy.get(eNum).e.attack;
			}
			// くらう最大のダメージ
			int maxSumDamage = sumDamage;
			int targetEnemyIndex = 0;
			for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				if(aroundEnemy.get(eNum).e.hp <= info.player.attack &&
				   maxSumDamage > sumDamage - aroundEnemy.get(eNum).e.attack)
				{
					targetEnemyIndex = eNum;
					maxSumDamage = sumDamage - aroundEnemy.get(eNum).e.attack;
				}
			}
			//System.out.println(maxSumDamage);

			// hpが敵から受ける攻撃力超過->攻撃
			if(info.player.hp > maxSumDamage)
			{
				// ターゲットを攻撃
				ruleBesedAct.action = Action.ATTACK;
				// 方向を決定
				ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
			}
			// hpが敵から受ける攻撃力以下
			else
			{
				// アイテムにポーションがあり，ポーションの回復量で間に合う
				int poIndex = inInvPotionCheck(info.player.inventory);
				if(poIndex != -1)
				{
					// アイテムの使用
					//obj.player.inventory.useItem(obj, poIndex, bg);
					// 削除
					//obj.player.inventory.removeItem(poIndex);
					ruleBesedAct.action = Action.USE_ITEM;
					ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
					ruleBesedAct.itemIndex = poIndex;
				}
				// ない，間に合わない
				else
				{
					// 攻撃されないマスの探索
					int[] movable = {1, 1, 1, 1, 0, 1, 1, 1, 1};
					for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
					{
						int enemyDir = aroundEnemy.get(eNum).dir;

						//System.out.println(eNum + "," + enemyDir);

						// 安全な移動先の探索
						for(int dir = 0; dir<9; dir++)
						{
							movable[dir] *= dArea[enemyDir][dir];
						}
					}

					ArrayList<Integer> mList = new ArrayList<Integer>();
					for(int dir = 0; dir<9; dir++)
					{
						// もし攻撃されないマスがあるのならば
						// かつ移動可能なマスであるならば
						if(movable[dir] == 1 &&
						   info.map[info.player.gridMapY + difY[dir]][info.player.gridMapX + difX[dir]] == 0)
						{
							mList.add(dir);
						}
					}

					// 攻撃されない，移動可能なマスがない
					if(mList.size() == 0)
					{
						// ワープの杖があるか
						int warpIndex = inInvWarpCheck(info.player.inventory);
						if(warpIndex != -1)
						{
							ruleBesedAct.action = Action.USE_ITEM;
							ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
							ruleBesedAct.itemIndex = warpIndex;
						}
						// ないとき
						else
						{
							// どうしようもない
							// ターゲットを攻撃
							ruleBesedAct.action = Action.ATTACK;
							// 方向を決定
							ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
						}
					}
					// 移動可能なマスが存在
					else
					{
						// その方向に移動
						ruleBesedAct.action = Action.MOVE;
						//ruleBesedAct.difPos.x = dif[mList.get(0)].x;
						//ruleBesedAct.difPos.y = dif[mList.get(0)].y;
						ruleBesedAct.difPos.x = difX[mList.get(0)];
						ruleBesedAct.difPos.y = difY[mList.get(0)];
					}
				}
			}
		}
		// 敵が２マス以上離れたところにいる
		else if(is2gridOverMosCheck(info, aroundEnemy) == true)
		{
			// 敵との距離を比較，一番近いものに対して行動を選択
			// 敵との距離，適当な大きい数字
			int targetEnemyIndex = 0;
			int distEnemy = aroundEnemy.get(targetEnemyIndex).dis;
			for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				if(distEnemy > aroundEnemy.get(eNum).dis)
				{
					// 更新
					distEnemy = aroundEnemy.get(eNum).dis;
					targetEnemyIndex = eNum;
				}
			}

			//System.out.println("size:"+aroundEnemy.size());
			//System.out.println("dist:"+distEnemy);

			// ２マスのとき
			if(distEnemy == 2)
			{
				// アイテムに遠距離攻撃武器があり，当たる位置にいる
				int ldIndex = inInvLongDistAtkCheck(info.player.inventory);
				if(ldIndex != -1)
				{
					//obj.player.inventory.useItem(obj, ldIndex, bg);
					//obj.player.inventory.removeItem(ldIndex);
					ruleBesedAct.action = Action.USE_ITEM;
					ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
					ruleBesedAct.itemIndex = ldIndex;
				}
				// ない
				else
				{
					// 待機
					ruleBesedAct.action = Action.STAY;
					// 方向を決定
					ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
				}
			}
			// 射程内(５マス以内)のとき
			else if(distEnemy <= 5)
			{
				// アイテムに遠距離攻撃武器があり，当たる位置にいる
				int ldIndex = inInvLongDistAtkCheck(info.player.inventory);
				if(ldIndex != -1)
				{
					//obj.player.inventory.useItem(obj, ldIndex, bg);
					//obj.player.inventory.removeItem(ldIndex);
					ruleBesedAct.action = Action.USE_ITEM;
					ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
					ruleBesedAct.itemIndex = ldIndex;
				}
				// ない
				else
				{
					// 目的地を敵の座標に設定，１マス近づく
					ruleBesedAct.action = Action.MOVE;
					int eDir = aroundEnemy.get(targetEnemyIndex).dir;
					//ruleBesedAct.difPos.x = dif[eDir].x;
					//ruleBesedAct.difPos.y = dif[eDir].y;
					ruleBesedAct.difPos.x = difX[eDir];
					ruleBesedAct.difPos.y = difY[eDir];
					//System.out.println("dir:"+eDir);
					//System.out.println("dif:"+ruleBesedAct.difPos.x + "," + ruleBesedAct.difPos.y);

					// 設定した座標が壁の場合，通路の探索に戻る
					//if(Background.map[obj.player.gridMapY + ruleBesedAct.difPos.y][obj.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}
				}
			}
			// それ以上の時
			else
			{
				// 目的地を敵の座標に設定，１マス近づく
				ruleBesedAct.action = Action.MOVE;
				int eDir = aroundEnemy.get(targetEnemyIndex).dir;
				//ruleBesedAct.difPos.x = dif[eDir].x;
				//ruleBesedAct.difPos.y = dif[eDir].y;
				ruleBesedAct.difPos.x = difX[eDir];
				ruleBesedAct.difPos.y = difY[eDir];

				// 設定した座標が壁の場合，通路の探索に戻る
				//if(Background.map[obj.player.gridMapY + ruleBesedAct.difPos.y][obj.player.gridMapX + ruleBesedAct.difPos.x] == 1)
				if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
				{
					ruleBesedAct = passSearchMove(info);
				}
			}
		}
		// 最下層であり，部屋に階段がある->目的地を階段に設定，１マス近づく
		else if(info.player.curFloor == MyCanvas.TOPFLOOR - 1 && isStairCheck(info, destList) == true)
		{
			destChecker(info);

			for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 5)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					int sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}
					else
					{
						//
					}

					break;
				}
			}
		}
		// 部屋の中にアイテムが落ちている->目的地をアイテムに設定，１マス近づく
		else if(isItemCheck(info, destList) == true && info.player.inventory.getInvItemNum() != Inventory.MAX_INV)
		{
			destChecker(info);

			for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 4)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					int sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}

					break;
				}
			}
		}
		// おなかがすいた
		else if(info.player.satiety < 80)
		{
			// 食料がある
			int foodIndex = inInvFoodCheck(info.player.inventory);
			if(foodIndex != -1)
			{
				ruleBesedAct.action = Action.USE_ITEM;
				ruleBesedAct.dir = info.player.dir;
				ruleBesedAct.itemIndex = foodIndex;
			}
			// ない
			else
			{
				// 部屋に階段がある
				if(isStairCheck(info, destList) == true)
				{
					for(int index = 0; index < destList.size(); index++)
					{
						if(destList.get(index).objNum == 5)
						{
							ruleBesedAct.action = Action.MOVE;
							// リストの中から抽出
							int sDir = destList.get(index).dir;
							ruleBesedAct.difPos.x = difX[sDir];
							ruleBesedAct.difPos.y = difY[sDir];

							// 設定した座標が壁の場合，通路の探索に戻る
							if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
							{
								ruleBesedAct = passSearchMove(info);
							}

							break;
						}
					}
				}
				else
				{
					// 目的地の探索・移動
					ruleBesedAct = passSearchMove(info);
				}
			}
		}
		// hpが7割以下であり，満腹度が半分以上
		else if(info.player.hp < info.player.maxHp * 0.7 && info.player.satiety > 50)
		{
			ruleBesedAct.action = Action.STAY;
		}
		// 部屋の中に階段がある
		else if(isStairCheck(info, destList) == true)
		{
			for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 5)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					int sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}

					break;
				}
			}
		}
		// いったことのない通路を目的地とする
		else
		{
			// 目的地の探索・移動
			ruleBesedAct = passSearchMove(info);
			// 各アクションのdirの更新タイミング
			// アイテム使用は方向が重要
			// 移動は移動先決定 -> 方向をその向きに変更
		}

		// アクションが移動の時
		if(ruleBesedAct.action == Action.MOVE)
		{
			// 斜め移動の制御
			// 壁に突っ込まないように
			ruleBesedAct = convSlantingMove(info, ruleBesedAct);

			// 斜め補正をかけても移動できないとき
			if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
			{
				ruleBesedAct.action = Action.STAY;
			}

			// rbAct.dif -> rbAct.dir
			for(int index = 0; index < 9; index++)
			{
				if(ruleBesedAct.difPos.x == difX[index] && ruleBesedAct.difPos.y == difY[index])
				{
					ruleBesedAct.dir = index;
					break;
				}
			}

			// 履歴の更新
			history.x = info.player.gridMapX;
			history.y = info.player.gridMapY;
		}

		return ruleBesedAct;
	}
	public void destChecker(Info info)
	{
		// 到達の更新
		if(passList.size() != 0)
		{
			// 通路の目的地に到達したとき
			if(isArriveCheck(info) == true)
			{
				// 探索が成功
				if(searchNextPass(info) == true)
				{
					// リストに追加
				}
				// 探索が失敗したとき
				else
				{
					PassPos tmp = new PassPos();
					// リストを逆順処理
					// 要素の入れ替え
					tmp = passList.get(passList.size() - 1);
					passList.remove(passList.size() - 1);
					passList.add(0, tmp);
				}
			}
		}
	}

	public int inInvWarpCheck(Inventory pinv)
	{
		for(int index = 0; index < pinv.getInvItemNum(); index++)
		{
			if(pinv.itemList.get(index).type.equals(new String("staff")) && pinv.itemList.get(index).effect == 1)
			{
				return index;
			}
		}

		return -1;
	}

	public int inInvFoodCheck(Inventory pinv)
	{
		for(int index = 0; index < pinv.getInvItemNum(); index++)
		{
			if(pinv.itemList.get(index).type.equals(new String("food")))
			{
				return index;
			}
		}

		return -1;
	}

	public Action convSlantingMove(Info info, Action ruleBesedAct)
	{
		// 斜め移動で行動できないときの処理
		if(ruleBesedAct.action == Action.MOVE)
		{
			// 左下の時
			if(ruleBesedAct.difPos.x == difX[0] && ruleBesedAct.difPos.y == difY[0])
			{
				// 左下に壁があるとき
				if(info.map[info.player.gridMapY + difY[0]][info.player.gridMapX + difX[0]] == 1)
				{
					// 下に行って通路に挟まれるとき
					if(info.map[info.player.gridMapY + difY[0]][info.player.gridMapX - 1] == 1 &&
					   info.map[info.player.gridMapY + difY[0]][info.player.gridMapX    ] == 0 &&
					   info.map[info.player.gridMapY + difY[0]][info.player.gridMapX + 1] == 1)
					{
						// 左へ
						ruleBesedAct.dir = 3;
						ruleBesedAct.difPos.x = difX[3];
						ruleBesedAct.difPos.y = difY[3];
					}
					// 左に行って通路に挟まれるとき
					if(info.map[info.player.gridMapY - 1][info.player.gridMapX + difX[0]] == 1 &&
					   info.map[info.player.gridMapY    ][info.player.gridMapX + difX[0]] == 0 &&
					   info.map[info.player.gridMapY + 1][info.player.gridMapX + difX[0]] == 1)
					{
						// 下へ
						ruleBesedAct.dir = 1;
						ruleBesedAct.difPos.x = difX[1];
						ruleBesedAct.difPos.y = difY[1];
					}
				}
				// 左側に壁があるとき
				if(info.map[info.player.gridMapY][info.player.gridMapX + difX[0]] == 1)
				{
					// 下に移動
					ruleBesedAct.dir = 1;
					ruleBesedAct.difPos.x = difX[1];
					ruleBesedAct.difPos.y = difY[1];
				}
				// 下側に壁があるとき
				if(info.map[info.player.gridMapY + difY[0]][info.player.gridMapX] == 1)
				{
					// 左に移動
					ruleBesedAct.dir = 3;
					ruleBesedAct.difPos.x = difX[3];
					ruleBesedAct.difPos.y = difY[3];
				}
			}
			// 右下
			else if(ruleBesedAct.difPos.x == difX[2] && ruleBesedAct.difPos.y == difY[2])
			{
				// 右下に壁
				if(info.map[info.player.gridMapY + difY[2]][info.player.gridMapX + difX[2]] == 1)
				{
					// 右に行って通路に挟まれるとき
					if(info.map[info.player.gridMapY - 1][info.player.gridMapX + difX[2]] == 1 &&
					   info.map[info.player.gridMapY    ][info.player.gridMapX + difX[2]] == 0 &&
					   info.map[info.player.gridMapY + 1][info.player.gridMapX + difX[2]] == 1)
					{
						// 下に移動
						ruleBesedAct.dir = 1;
						ruleBesedAct.difPos.x = difX[1];
						ruleBesedAct.difPos.y = difY[1];
					}
					// 下に行って通路に挟まれるとき
					if(info.map[info.player.gridMapY + difY[2]][info.player.gridMapX - 1] == 1 &&
					   info.map[info.player.gridMapY + difY[2]][info.player.gridMapX    ] == 0 &&
					   info.map[info.player.gridMapY + difY[2]][info.player.gridMapX + 1] == 1)
					{
						// 右に移動
						ruleBesedAct.dir = 5;
						ruleBesedAct.difPos.x = difX[5];
						ruleBesedAct.difPos.y = difY[5];
					}
				}
				// 右側に壁があるとき
				if(info.map[info.player.gridMapY][info.player.gridMapX + difX[2]] == 1)
				{
					// 下に移動
					ruleBesedAct.dir = 1;
					ruleBesedAct.difPos.x = difX[1];
					ruleBesedAct.difPos.y = difY[1];
				}
				// 下側に壁があるとき
				if(info.map[info.player.gridMapY + difY[2]][info.player.gridMapX] == 1)
				{
					// 右に移動
					ruleBesedAct.dir = 5;
					ruleBesedAct.difPos.x = difX[5];
					ruleBesedAct.difPos.y = difY[5];
				}
			}
			// 左上
			else if(ruleBesedAct.difPos.x == difX[6] && ruleBesedAct.difPos.y == difY[6])
			{
				// 左上に壁
				if(info.map[info.player.gridMapY + difY[6]][info.player.gridMapX + difX[6]] == 1)
				{
					if(info.map[info.player.gridMapY - 1][info.player.gridMapX + difX[6]] == 1 &&
					   info.map[info.player.gridMapY    ][info.player.gridMapX + difX[6]] == 0 &&
					   info.map[info.player.gridMapY + 1][info.player.gridMapX + difX[6]] == 1)
					{
						// 上に移動
						ruleBesedAct.dir = 7;
						ruleBesedAct.difPos.x = difX[7];
						ruleBesedAct.difPos.y = difY[7];
					}
					if(info.map[info.player.gridMapY + difY[6]][info.player.gridMapX - 1] == 1 &&
					   info.map[info.player.gridMapY + difY[6]][info.player.gridMapX    ] == 0 &&
					   info.map[info.player.gridMapY + difY[6]][info.player.gridMapX + 1] == 1)
					{
						// 左に移動
						ruleBesedAct.dir = 3;
						ruleBesedAct.difPos.x = difX[3];
						ruleBesedAct.difPos.y = difY[3];
					}
				}
				// 左側に壁があるとき
				if(info.map[info.player.gridMapY][info.player.gridMapX + difX[6]] == 1)
				{
					// 上に移動
					ruleBesedAct.dir = 7;
					ruleBesedAct.difPos.x = difX[7];
					ruleBesedAct.difPos.y = difY[7];
				}
				// 上側に壁があるとき
				if(info.map[info.player.gridMapY + difY[6]][info.player.gridMapX] == 1)
				{
					// 左に移動
					ruleBesedAct.dir = 3;
					ruleBesedAct.difPos.x = difX[3];
					ruleBesedAct.difPos.y = difY[3];
				}
			}
			// 右上
			else if(ruleBesedAct.difPos.x == difX[8] && ruleBesedAct.difPos.y == difY[8])
			{
				// 右上に壁
				if(info.map[info.player.gridMapY + difY[8]][info.player.gridMapX + difX[8]] == 1)
				{
					if(info.map[info.player.gridMapY - 1][info.player.gridMapX + difX[8]] == 1 &&
					   info.map[info.player.gridMapY    ][info.player.gridMapX + difX[8]] == 0 &&
					   info.map[info.player.gridMapY + 1][info.player.gridMapX + difX[8]] == 1)
					{
						// 上に移動
						ruleBesedAct.dir = 7;
						ruleBesedAct.difPos.x = difX[7];
						ruleBesedAct.difPos.y = difY[7];
					}
					if(info.map[info.player.gridMapY + difY[8]][info.player.gridMapX - 1] == 1 &&
					   info.map[info.player.gridMapY + difY[8]][info.player.gridMapX    ] == 0 &&
					   info.map[info.player.gridMapY + difY[8]][info.player.gridMapX + 1] == 1)
					{
						// 右に移動
						ruleBesedAct.dir = 5;
						ruleBesedAct.difPos.x = difX[5];
						ruleBesedAct.difPos.y = difY[5];
					}
				}
				// 右側に壁があるとき
				if(info.map[info.player.gridMapY][info.player.gridMapX + difX[8]] == 1)
				{
					// 上に移動
					ruleBesedAct.dir = 7;
					ruleBesedAct.difPos.x = difX[7];
					ruleBesedAct.difPos.y = difY[7];
				}
				// 上側に壁があるとき
				if(info.map[info.player.gridMapY + difY[8]][info.player.gridMapX] == 1)
				{
					// 右に移動
					ruleBesedAct.dir = 5;
					ruleBesedAct.difPos.x = difX[5];
					ruleBesedAct.difPos.y = difY[5];
				}
			}
		}

		return ruleBesedAct;
	}

	public Action passSearchMove(Info info)
	{
		Action rbAct = new Action(info.player.dir);

		// リストに要素がない
		if(passList.size() == 0)
		{
			// 追加
			// 部屋につながる通路の探索
			// 目的地となる通路をpassListに追加
			searchNextPass(info);

			// その座標目指して移動
			rbAct.action = Action.MOVE;
			// リストの中から抽出
			int paDir = passList.get(passList.size() - 1).dir;
			rbAct.difPos.x = difX[paDir];
			rbAct.difPos.y = difY[paDir];
		}
		// リストに要素がある
		else
		{
			// passListの更新
			passListUpdate(info);

			// 通路か部屋か
			int passType = passChecker(info, new Point(info.player.gridMapX, info.player.gridMapY));

			if(isArriveCheck(info) == false)
			{
				// 現在の座標を最後の要素の１つ手前に挿入

				// 重複のチェックが必要
				if(isPassListORcheck(new Point(info.player.gridMapX, info.player.gridMapY)) == false)
				{
					PassPos ppos = new PassPos();
					ppos.p = new Point(info.player.gridMapX, info.player.gridMapY);
					Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
					int absDisx = Math.abs(pp.x - ppos.p.x);
					int absDisy = Math.abs(pp.y - ppos.p.y);
					ppos.dis = absDisx > absDisy ? absDisx : absDisy;
					ppos.dir = convPos2Dir(ppos.p, pp);
					ppos.vh = passType;
					passList.add(passList.size() - 1, ppos);
				}

				// その座標目指して移動
				rbAct.action = Action.MOVE;
				// リストの中から抽出
				int paDir = passList.get(passList.size() - 1).dir;
				rbAct.difPos.x = difX[paDir];
				rbAct.difPos.y = difY[paDir];

				//System.out.println("x,y:" + ruleBesedAct.difPos.x + ", " + ruleBesedAct.difPos.y);
				//System.out.println("dir:" + paDir);
				//System.out.println("dif:" + ruleBesedAct.difPos.x + "," + ruleBesedAct.difPos.y);
			}
			// 座標に到達しており
			else
			{
				// 現在地が通路で
				// 現在地に+1することで，部屋に到達するかどうかチェック
				// もし，１マス先が部屋だった場合，目的地に追加
				if( (passType == 0 || passType == 1) &&
					 nextOneRoomChecker(info, new Point(info.player.gridMapX, info.player.gridMapY)) == true)
				{
					// 追加された座標目指して移動
					rbAct.action = Action.MOVE;
					// リストの中から抽出
					int paDir = passList.get(passList.size() - 1).dir;
					rbAct.difPos.x = difX[paDir];
					rbAct.difPos.y = difY[paDir];
				}
				// 探索が成功したとき
				else if(searchNextPass(info) == true)
				{
					// 追加された座標目指して移動
					rbAct.action = Action.MOVE;
					// リストの中から抽出
					int paDir = passList.get(passList.size() - 1).dir;
					rbAct.difPos.x = difX[paDir];
					rbAct.difPos.y = difY[paDir];
				}
				// 探索が失敗したとき
				else{
					/*
					while(passList.get(passList.size() - 1).count > 0)
					{

					}
					*/
					PassPos tmp = new PassPos();
					// リストを逆順処理

					// 追加された座標目指して移動
					rbAct.action = Action.MOVE;
					// リストの中から抽出
					int paDir = passList.get(passList.size() - 1).dir;
					rbAct.difPos.x = difX[paDir];
					rbAct.difPos.y = difY[paDir];

					// 要素の入れ替え
					tmp = passList.get(passList.size() - 1);
					passList.remove(passList.size() - 1);
					passList.add(0, tmp);
				}
			}
		}

		return rbAct;
	}

	public boolean nextOneRoomChecker(Info info, Point playerPos)
	{
		if(info.mapRoomNum[playerPos.y - 1][playerPos.x] != -1 &&
		   info.map[playerPos.y - 1][playerPos.x] == 0 &&
		   arriveMap[playerPos.y - 1][playerPos.x] == 0)
		{
			// 目的地にその座標を追加
			PassPos ppos = new PassPos();
			ppos.p = new Point(playerPos.x, playerPos.y - 1);
			Point pp = new Point(playerPos.x, playerPos.y);
			int absDisx = Math.abs(pp.x - ppos.p.x);
			int absDisy = Math.abs(pp.y - ppos.p.y);
			ppos.dis = absDisx > absDisy ? absDisx : absDisy;
			ppos.dir = convPos2Dir(ppos.p, pp);
			ppos.vh = -1;
			passList.add(ppos);

			return true;
		}
		else if(info.mapRoomNum[playerPos.y + 1][playerPos.x] != -1 &&
				info.map[playerPos.y + 1][playerPos.x] == 0  &&
				arriveMap[playerPos.y + 1][playerPos.x] == 0)
		{
			// 目的地にその座標を追加
			PassPos ppos = new PassPos();
			ppos.p = new Point(playerPos.x, playerPos.y + 1);
			Point pp = new Point(playerPos.x, playerPos.y);
			int absDisx = Math.abs(pp.x - ppos.p.x);
			int absDisy = Math.abs(pp.y - ppos.p.y);
			ppos.dis = absDisx > absDisy ? absDisx : absDisy;
			ppos.dir = convPos2Dir(ppos.p, pp);
			ppos.vh = -1;
			passList.add(ppos);

			return true;
		}
		else if(info.mapRoomNum[playerPos.y][playerPos.x - 1] != -1 &&
				info.mapRoomNum[playerPos.y][playerPos.x - 1] == 0  &&
				arriveMap[playerPos.y][playerPos.x - 1] == 0)
		{
			// 目的地にその座標を追加
			PassPos ppos = new PassPos();
			ppos.p = new Point(playerPos.x - 1, playerPos.y);
			Point pp = new Point(playerPos.x, playerPos.y);
			int absDisx = Math.abs(pp.x - ppos.p.x);
			int absDisy = Math.abs(pp.y - ppos.p.y);
			ppos.dis = absDisx > absDisy ? absDisx : absDisy;
			ppos.dir = convPos2Dir(ppos.p, pp);
			ppos.vh = -1;
			passList.add(ppos);

			return true;
		}
		else if(info.mapRoomNum[playerPos.y][playerPos.x + 1] != -1 &&
				info.map[playerPos.y][playerPos.x + 1] == 0 &&
				arriveMap[playerPos.y][playerPos.x + 1] == 0)
		{
			// 目的地にその座標を追加
			PassPos ppos = new PassPos();
			ppos.p = new Point(playerPos.x + 1, playerPos.y);
			Point pp = new Point(playerPos.x, playerPos.y);
			int absDisx = Math.abs(pp.x - ppos.p.x);
			int absDisy = Math.abs(pp.y - ppos.p.y);
			ppos.dis = absDisx > absDisy ? absDisx : absDisy;
			ppos.dir = convPos2Dir(ppos.p, pp);
			ppos.vh = -1;
			passList.add(ppos);

			return true;
		}

		return false;
	}

	public int passChecker(Info info, Point p)
	{
		if(info.map[p.y][p.x - 1] == 1 &&
		   info.map[p.y][p.x    ] == 0 &&
		   info.map[p.y][p.x + 1] == 1)
		{
			return 0; // 縦
		}
		else if(info.map[p.y - 1][p.x] == 1 &&
				info.map[p.y    ][p.x] == 0 &&
				info.map[p.y + 1][p.x] == 1)
		{
			return 1; // 横
		}
		else
		{
			return -1; // どちらにも属さない->部屋
		}
	}

	public void passListUpdate(Info info)
	{
		// プレイヤーの座標から見た方向の更新
		for(int index = 0; index < passList.size(); index++)
		{
			PassPos ppos = passList.get(index);

			Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
			int absDisx = Math.abs(pp.x - ppos.p.x);
			int absDisy = Math.abs(pp.y - ppos.p.y);
			ppos.dis = absDisx > absDisy ? absDisx : absDisy;
			ppos.dir = convPos2Dir(ppos.p, pp);
		}
	}

	public boolean isArriveCheck(Info info)
	{
		Point p = new Point();
		p.x = passList.get(passList.size() - 1).p.x;
		p.y = passList.get(passList.size() - 1).p.y;

		if(info.player.gridMapX == p.x && info.player.gridMapY == p.y)
		{
			passList.get(passList.size() - 1).count++;
			return true;
		}

		return false;
	}

	public boolean searchNextPass(Info info)
	{
		PassPos ppos = new PassPos();
		PassPos closepos = new PassPos();
		closepos.p = new Point();
		closepos.dis = 1000;

		for(int y = 1; y < MyCanvas.MAPGRIDSIZE_Y - 1; y++)
		{
			for(int x = 1; x < MyCanvas.MAPGRIDSIZE_X - 1; x++)
			{
				// 縦通路
				if(info.pCurmap[y][x] == true && passChecker(info, new Point(x, y)) == 0)
				{
					// 通路確定
					// もし，まだリストに追加されていないならば
					if(isPassListORcheck(new Point(x, y)) == false)
					{
						// 追加
						ppos.p = new Point(x, y); // 目的地の座標
						Point pp = new Point(info.player.gridMapX, info.player.gridMapY); // プレイヤーの座標
						// 目的地とプレイヤーの距離
						int absDisx = Math.abs(pp.x - ppos.p.x);
						int absDisy = Math.abs(pp.y - ppos.p.y);
						ppos.dis = absDisx > absDisy ? absDisx : absDisy;
						// プレイヤーから見た目的地の方向を0-8で表す
						ppos.dir = convPos2Dir(ppos.p, pp);
						ppos.vh = 0;

						if(closepos.dis > ppos.dis)
						{
							closepos.p = new Point(ppos.p.x, ppos.p.y);
							closepos.dis = ppos.dis;
							closepos.dir = ppos.dir;
							closepos.vh = ppos.vh;
						}
					}
				}
				// 横通路
				if(info.pCurmap[y][x] == true && passChecker(info, new Point(x, y)) == 1)
				{
					// 通路確定
					// もし，まだリストに追加されていないならば
					if(isPassListORcheck(new Point(x, y)) == false)
					{
						// 追加
						ppos.p = new Point(x, y);
						Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
						int absDisx = Math.abs(pp.x - ppos.p.x);
						int absDisy = Math.abs(pp.y - ppos.p.y);
						ppos.dis = absDisx > absDisy ? absDisx : absDisy;
						ppos.dir = convPos2Dir(ppos.p, pp);
						ppos.vh = 1;

						if(closepos.dis > ppos.dis)
						{
							closepos.p = new Point(ppos.p.x, ppos.p.y);
							closepos.dis = ppos.dis;
							closepos.dir = ppos.dir;
							closepos.vh = ppos.vh;
						}
					}
				}
			}
		}

		// 初期値ではないとき
		if(closepos.dis != 1000)
		{
			passList.add(closepos);
			return true;
		}

		return false;
	}

	public boolean isPassListORcheck(Point p)
	{
		for(int index = 0; index < passList.size(); index++)
		{
			if(passList.get(index).p.x == p.x && passList.get(index).p.y == p.y)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isItemCheck(Info info, ArrayList<Destination> dList)
	{
		Destination dest = new Destination();

		for(int y=0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x=0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				if(info.pCurmap[y][x] == true && info.mapObject[y][x] == 4)
				{
					dest.objNum = 4;
					dest.p = new Point(x, y);
					Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
					int absDisx = Math.abs(pp.x - dest.p.x);
					int absDisy = Math.abs(pp.y - dest.p.y);
					dest.dis = absDisx > absDisy ? absDisx : absDisy;
					dest.dir = convPos2Dir(dest.p, pp);
					dList.add(dest);
					break;
				}
			}
		}

		// 目的地リストの中にアイテムが存在しているとき
		for(int index = 0; index < dList.size(); index++)
		{
			if(dList.get(index).objNum == 4)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isStairCheck(Info info, ArrayList<Destination> dList)
	{
		Destination dest = new Destination();

		for(int y=0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x=0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				if(info.pCurmap[y][x] == true && info.mapObject[y][x] == 5)
				{
					dest.objNum = 5;
					dest.p = new Point(x, y);
					Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
					int absDisx = Math.abs(pp.x - dest.p.x);
					int absDisy = Math.abs(pp.y - dest.p.y);
					dest.dis = absDisx > absDisy ? absDisx : absDisy;
					dest.dir = convPos2Dir(dest.p, pp);
					dList.add(dest);
					break;
				}
			}
		}

		// 目的地リストの中に階段が存在しているとき
		for(int index = 0; index < dList.size(); index++)
		{
			if(dList.get(index).objNum == 5)
			{
				return true;
			}
		}

		return false;
	}

	public int inInvLongDistAtkCheck(Inventory pinv)
	{
		for(int index = 0; index<pinv.getInvItemNum(); index++)
		{
			if(pinv.itemList.get(index).type.equals(new String("staff")) && pinv.itemList.get(index).damage > 0)
			{
				return index;
			}
		}

		return -1;
	}

	public int inInvPotionCheck(Inventory pinv)
	{
		for(int index = 0; index<pinv.getInvItemNum(); index++)
		{
			if(pinv.itemList.get(index).type.equals(new String("potion")))
			{
				return index;
			}
		}

		return -1;
	}

	public int convPos2Dir(Point p1, Point p2)
	{
		int convDifx = p1.x - p2.x;
		int convDify = p1.y - p2.y;
		convDifx =  convDifx >  1 ?  1 : convDifx;
		convDifx =  convDifx < -1 ? -1 : convDifx;
		convDify =  convDify >  1 ?  1 : convDify;
		convDify =  convDify < -1 ? -1 : convDify;

		for(int i = 0; i < 9; i++)
		{
			if(difX[i] == convDifx && difY[i] == convDify)
			{
				return i; // 0~8
			}
		}

		return -1;
	}

	// プレイヤーの周りにモンスターが
	// true :いる
	// false:いない
	public boolean is1gridMosCheck(Info info, ArrayList<AroundEnemy> aroundEnemy)
	{
		// 0~8まで
		for(int dir = 0; dir < 9; dir++)
		{
			Point checkp = new Point(info.player.gridMapX + difX[dir], info.player.gridMapY + difY[dir]);
			// チェックした座標に敵がいるとき
			if(info.mapUnit[checkp.y][checkp.x] == 3)
			{
				for(int eNum = 0; eNum < info.enemy.length; eNum++)
				{
					//
					if(info.enemy[eNum].gridMapX == checkp.x && info.enemy[eNum].gridMapY == checkp.y)
					{
						AroundEnemy aenemy = new AroundEnemy();
						aenemy.e = info.enemy[eNum];
						aenemy.dis = 1;
						aenemy.dir = dir;
						aroundEnemy.add(aenemy);
						break;
					}
				}
			}
		}

		//System.out.println("aroundEnemy:" + aroundEnemy.size());
		return aroundEnemy.size() > 0 ? true : false;
	}

	public boolean is2gridOverMosCheck(Info info, ArrayList<AroundEnemy> aroundEnemy)
	{
		AroundEnemy aenemy = new AroundEnemy();

		// プレイヤーの視界から確認

		// 敵の中から，
		// ２マス離れている敵，２マス以上ではない
		/*
		for(int y = obj.player.gridScrY - 2; y <= obj.player.gridScrY + 2; y++)
		{
			if(y != obj.player.gridScrY - 2 || y != obj.player.gridScrY + 2)
			{
				continue;
			}

			for(int x = obj.player.gridScrX - 2; x <= obj.player.gridScrX + 2; x++)
			{
				if(x != obj.player.gridScrX - 2 || x != obj.player.gridScrX + 2)
				{
					continue;
				}

				Point checkp = new Point(x, y);
				for(int eNum=0; eNum<ObjectSet.enemy.length; eNum++)
				{
					if(ObjectSet.enemy[eNum].gridMapX == checkp.x && ObjectSet.enemy[eNum].gridMapY == checkp.y)
					{
						aenemy.e = ObjectSet.enemy[eNum];
						aenemy.dis = 2;
						Point pp = new Point(obj.player.gridMapX, obj.player.gridMapY);
						Point ep = new Point(aenemy.e.gridMapX, aenemy.e.gridMapY);
						aenemy.dir = convPos2Dir(ep, pp);
						aroundEnemy.add(aenemy);
						break;
					}
				}
			}
		}
		 */

		// マップを探索
		// プレイヤーの現在の視界の中に敵がいる
		// 要素番号を判定し，距離，方向を計算
		// リストに追加する
		for(int y=0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x=0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				if(info.pCurmap[y][x] == true && info.mapUnit[y][x] == 3)
				{
					for(int eNum = 0; eNum < info.enemy.length; eNum++)
					{
						if(info.enemy[eNum].gridMapX == x && info.enemy[eNum].gridMapY == y)
						{
							aenemy.e = info.enemy[eNum];
							Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
							Point ep = new Point(aenemy.e.gridMapX, aenemy.e.gridMapY);
							int absDisx = Math.abs(pp.x - ep.x);
							int absDisy = Math.abs(pp.y - ep.y);
							aenemy.dis = absDisx > absDisy ? absDisx : absDisy;
							aenemy.dir = convPos2Dir(ep, pp);
							aroundEnemy.add(aenemy);
							break;
						}
					}
				}
			}
		}

		return aroundEnemy.size() > 0 ? true : false;
	}

	public Action makeAction(Info info)
	{
		Action act = new Action(info.player.dir);
		//act.difPos = new Point();

		act = ruleBased(info);
                //act = ruleBasedOnly(info);
                

		return act;
	}

	class AroundEnemy
	{
		Enemy e; // 敵の情報
		int dis; // プレイヤーからの距離
		int dir; // プレイヤーからの方向
	}

	class Destination
	{
		int objNum; // 種類
		Point p; // 座標
		int dis; // プレイヤーからの距離
		int dir; // プレイヤーからの方向
	}

	class PassPos
	{
		Point p; // 座標
		int vh; // 縦v0横h1
		int count = 0;
		int dis; // プレイヤーからの距離
		int dir; // プレイヤーからの方向
	}
}
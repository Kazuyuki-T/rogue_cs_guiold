import java.awt.Point;
import java.util.Random;
import java.util.ArrayList;

public class ObjectSetSimulator
{
	private Random random;

	static int[][] actionTable = {{7, 7, 8, 9, 9},
								  {7, 0, 0, 0, 9},
								  {4, 0,-1, 0, 6},
								  {1, 0, 0, 0, 3},
								  {1, 1, 2, 3, 3}};

	// コンストラクタ
	public ObjectSetSimulator()
	{
		random = new Random();
	}

	public void attackPlayer(Info info)
	{
		// 隣の座標
		Point np = nextGridAxis(info.player.gridMapX, info.player.gridMapY, info.player.dir);

		// 1マス先に敵がいるとき
		int nextMonsIndex = inNextGridMonster(np, info);
                
                // なおかつ，斜め攻撃で地形に阻まれないとき
		if(nextMonsIndex != -1 &&
		   isPlayerDiagonalAtkCheck(np.x - info.player.gridMapX, np.y - info.player.gridMapY, info) == false)
		{
			// ダメージ計算・アクティブ処理
			calcEnemyDamage(info, nextMonsIndex, info.player.attack);
			// 敵を倒したか否かのチェック
			if(info.enemy[nextMonsIndex].active == false)
			{
				// 倒した場合，経験値を得る
				info.player.exp += info.enemy[nextMonsIndex].spoint;
			}
//                        if(info.visibleEnemy.get(nextMonsIndex).active == false)
//			{
//				// 倒した場合，経験値を得る
//				info.player.exp += info.visibleEnemy.get(nextMonsIndex).spoint;
//			}
		}
                else
                {
//                    System.out.println("**attack miss**");
//                    System.out.println("nmons : " + nextMonsIndex);
//                    System.out.println("p(" + info.player.gridMapX + ", " + info.player.gridMapY + "), dir(1-9):" + info.player.dir);
//                    for(int eindex = 0; eindex < info.enemy.length; eindex++)
//                    {
//                        System.out.println("e[]->e" + info.enemy[eindex].index +"(" + info.enemy[eindex].gridMapX + ", " + info.enemy[eindex].gridMapY + "), " + 
//                                            info.enemy[eindex].hp + "/" + info.enemy[eindex].maxHp + ", " + info.enemy[eindex].active);
//                    }
//                    for(int eindex = 0; eindex < info.visibleEnemy.size(); eindex++)
//                    {
//                        System.out.println("vis->e" + info.visibleEnemy.get(eindex).index +"(" + info.visibleEnemy.get(eindex).gridMapX + ", " + info.visibleEnemy.get(eindex).gridMapY + "), " + 
//                                            info.visibleEnemy.get(eindex).hp + "/" + info.visibleEnemy.get(eindex).maxHp + ", " + info.visibleEnemy.get(eindex).active);
//                    }
//                    // mapの敵確認
//                    for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
//                    {
//                        for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
//                        {
//                            if(info.mapUnit[y][x] == 3)
//                            {
//                                System.out.println("mapUnit->e(" + x + "," + y + ")");
//                            }
//                        }
//                    }
                }
		// いないときもターンはカウントする
	}
        
        public void calcEnemyDamage(Info info, int eIndex, int pAtk)
	{
		// hpから攻撃力分のダメージを引く
		info.enemy[eIndex].hp -= pAtk;

		if(info.enemy[eIndex].hp <= 0)
		{
			info.enemy[eIndex].active = false;
			info.enemy[eIndex].hp = 0;
			
                        info.mapUnit[info.enemy[eIndex].gridMapY][info.enemy[eIndex].gridMapX] = -1;
			
			info.enemy[eIndex].repopFlag = true;
		}
//                // hpから攻撃力分のダメージを引く
//		info.visibleEnemy.get(eIndex).hp -= pAtk;
//
//		if(info.visibleEnemy.get(eIndex).hp <= 0)
//		{
//			info.visibleEnemy.get(eIndex).active = false;
//			info.visibleEnemy.get(eIndex).hp = 0;
//			
//                        info.mapUnit[info.visibleEnemy.get(eIndex).gridMapY][info.visibleEnemy.get(eIndex).gridMapX] = -1;
//			
//			info.visibleEnemy.get(eIndex).repopFlag = true;
//		}
	}

	// 斜め攻撃の可否
	public boolean isPlayerDiagonalAtkCheck(int mx, int my, Info info)
	{
		if(info.map[info.player.gridMapY][info.player.gridMapX + mx] == 1 ||
		   info.map[info.player.gridMapY + my][info.player.gridMapX] == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isPlayerDiagonalMoveCheck(int mx, int my, Info info)
	{
		//
		if(info.map[info.player.gridMapY][info.player.gridMapX + mx] == 1 ||
		   info.map[info.player.gridMapY + my][info.player.gridMapX] == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean movePlayer(Info info, int mx, int my)
	{
		// 斜め移動な場合
		if(mx != 0 && my !=0)
		{
			// 通行不可なとき
			// isDiagonalMoveCheck(mx, my)
			// 真：通行不可
			// 偽：通行可
			if(isPlayerDiagonalMoveCheck(mx, my, info) == true)
			{
				return false;
			}
		}

		// isNextMoveCheck()
		// 移動可能なとき真
		// それ以外偽
		if(isPlayerNextMoveCheck(info.player.gridMapX + mx, info.player.gridMapY + my, info) == true)
		{
			// 移動前の座標の初期化
			info.mapUnit[info.player.gridMapY][info.player.gridMapX] = -1;
			info.player.gridMapX += mx;
			info.player.gridMapY += my;
			// 移動後の座標の更新
			info.mapUnit[info.player.gridMapY][info.player.gridMapX] = 6;

			// 移動した先にアイテムが存在する場合
			int index = inOverlapItemCheck(info, info.player.gridMapX, info.player.gridMapY);
			if(index != -1)
			{
				// インベントリにアイテムを追加
				if(info.player.inventory.addItem(index, info) == true)
				{
					// 追加できた場合，プレイヤーの持つマップからアイテムを消去
					info.mapObject[info.player.gridMapY][info.player.gridMapX] = -1;
				}
			}

			// 移動した先に階段が存在する場合
			if(isOverlapStairCheck(info, info.player.gridMapX, info.player.gridMapY) == true)
			{
				// 階数を増やす
				info.player.curFloor++;
			}

			return true;
		}

		return false;
	}

	public int inOverlapItemCheck(Info info, int nx, int ny)
	{
		// いずれかのアイテムと重複したとき，
		// 重複したアイテムのインデックスを返す
		for(int index = 0; index < info.item.length; index++)
		{
			// プレイヤーと座標が重複
			if(info.item[index].gridMapX == nx && info.item[index].gridMapY == ny)
			{
				// アイテムがアクティブなとき
				if(info.item[index].active == true){
					return index;
				}
			}
		}

		// いずれのアイテムとも重複しない場合
		return -1;
	}

	// 階段との重複をチェック
	public boolean isOverlapStairCheck(Info info, int nx, int ny)
	{
		if(info.stair.gridMapX == nx && info.stair.gridMapY == ny)
		{
			if(info.stair.active == true){
				return true;
			}
		}

		return false;
	}

	// 移動可否チェック
	public boolean isPlayerNextMoveCheck(int nx, int ny, Info info)
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
		else if(isNMOverlapMonsCheck(nx, ny, info) == true)
		{
			return false;
		}
		// 3
		// マップの通行付加部分と重複しようとしたとき
		else if(info.map[ny][nx] == 1)
		{
			return false;
		}

		return true;
	}

	public boolean isNMOverlapMonsCheck(int nx, int ny, Info info)
	{
		// いずれかのエネミーと重複したとき真を返す
		for(int index = 0; index < info.enemy.length; index++)
		{
			if(info.enemy[index].gridMapX == nx && info.enemy[index].gridMapY == ny)
			{
				if(info.enemy[index].active == true)
				{
					return true;
				}
			}
		}

		// いずれのエネミーとも重複しない場合
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
		}

		return p;
	}

	// いずれかのエネミーが隣にいたとき，要素番号を返す
	public int inNextGridMonster(Point p, Info info)
	{
		for(int index = 0; index < info.enemy.length; index++)
		{
			// いずれかのエネミーが隣にいたとき，要素番号を返す
			if(info.enemy[index].gridMapX == p.x && info.enemy[index].gridMapY == p.y && info.enemy[index].active == true)
			{
				return index;
			}
		}
                
//                for(int index = 0; index < info.visibleEnemy.size(); index++)
//                {
//                    if(info.visibleEnemy.get(index).gridMapX == p.x && 
//                       info.visibleEnemy.get(index).gridMapY == p.y &&
//                       info.visibleEnemy.get(index).active == true)
//                    {
//                        return index;
//                    }
//                }

		return -1;
	}

	// エネミーが移動する処理
	public void moveEnemy(Info info)
	{
		for (int index = 0; index < info.enemy.length; index++)
		{
			if (info.enemy[index].active == true)
			{
				moveEachEnemy(index, info);

				// アクションフラグの更新必要か？
			}
		}
	}

	public void moveEachEnemy(int enemyIndex, Info info)
	{
		// 視界内のプレイヤの有無により変化
		// プレイヤーが視界内にいるとき
		// 最短距離を詰め，隣接した場合攻撃
		if(isPlayerinView(enemyIndex, info) == true)
		{
			// プレイヤーとの距離の差を計算
			int difx = info.player.gridMapX - info.enemy[enemyIndex].gridMapX;
			int dify = info.player.gridMapY - info.enemy[enemyIndex].gridMapY;

			// 必要な範囲に縮小
			if(difx < -2)difx = -2;
			if(difx > 2) difx = 2;
			if(dify < -2)dify = -2;
			if(dify > 2) dify = 2;

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
					if(isEnemyDiagonalAtkCheck(enemyIndex, ndifx, ndify, info) == true)
					{
						// 上or下に通行不可な部分があるとき
						if(isEnemyNextMoveCheck(info.enemy[enemyIndex].gridMapX, info.enemy[enemyIndex].gridMapY + ndify, info) == false)
						{
							// 右に進む
							if(ndifx == 1)
							{
								moveEnemyDir(enemyIndex, ObjectSet.RIGHT, info);
							}
							// 左に進む
							else if(ndifx == -1)
							{
								moveEnemyDir(enemyIndex, ObjectSet.LEFT, info);
							}
						}
						// 左or右に通行不可な部分があるとき
						else if(isEnemyNextMoveCheck(info.enemy[enemyIndex].gridMapX + ndifx, info.enemy[enemyIndex].gridMapY, info) == false)
						{
							// 上に進む
							if(ndify == -1)
							{
								moveEnemyDir(enemyIndex, ObjectSet.TOP, info);
							}
							// 下に進む
							else if(ndify == 1)
							{
								moveEnemyDir(enemyIndex, ObjectSet.BOTTOM, info);
							}
						}
					}
					// 斜め攻撃ができるとき
					else
					{
						calcPlayerDamage(info.enemy[enemyIndex].attack, info);
					}
				}
				// 斜め攻撃ではないとき
				else
				{
					// 攻撃
					calcPlayerDamage(info.enemy[enemyIndex].attack, info);
				}
			}
			else
			{
				// 移動
				moveEnemyDir(enemyIndex, index, info);
			}
		}
		// ランダムムーブ
		else
		{
			int index = random.nextInt(9) + 1;
			moveEnemyDir(enemyIndex, index, info);
		}
	}
        
        // ダメージ計算
	public void calcPlayerDamage(int pAtk, Info info)
	{
		info.player.damageCalc(pAtk, info);
        }

	public void moveEnemyDir(int index, int dir, Info info)
	{
		int gridMapX = info.enemy[index].gridMapX;
		int gridMapY = info.enemy[index].gridMapY;

		// mapObjの更新
		// 元の敵座標をクリア
		info.mapUnit[gridMapY][gridMapX] = -1;

		switch(dir)
		{
		// 移動
		case ObjectSet.BOTTOM_LEFT:
			// 左下
			if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY + 1, info) == true && isEnemyDiagonalMoveCheck(index, -1, 1, info) == false){
				gridMapX -= 1;
				gridMapY += 1;
			}
			// 左
			else if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY, info) == true){
				gridMapX -= 1;
				gridMapY += 0;
			}
			// 下
			else if(isEnemyNextMoveCheck(gridMapX, gridMapY + 1, info) == true){
				gridMapX += 0;
				gridMapY += 1;
			}
			break;

		case ObjectSet.BOTTOM:
			// 下
			if(isEnemyNextMoveCheck(gridMapX, gridMapY + 1, info) == true){
				gridMapX += 0;
				gridMapY += 1;
			}
			// 左下
			else if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY + 1, info) == true && isEnemyDiagonalMoveCheck(index, -1, 1, info) == false){
				gridMapX -= 1;
				gridMapY += 1;
			}
			// 右下
			else if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY + 1, info) == true && isEnemyDiagonalMoveCheck(index, 1, 1, info) == false){
				gridMapX += 1;
				gridMapY += 1;
			}
			break;

		case ObjectSet.BOTTOM_RIGHT:
			// 右下
			if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY + 1, info) == true && isEnemyDiagonalMoveCheck(index, 1, 1, info) == false){
				gridMapX += 1;
				gridMapY += 1;
			}
			// 右
			else if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY, info) == true){
				gridMapX += 1;
				gridMapY += 0;
			}
			// 下
			else if(isEnemyNextMoveCheck(gridMapX, gridMapY + 1, info) == true){
				gridMapX += 0;
				gridMapY += 1;
			}
			break;

		case ObjectSet.LEFT:
			// 左
			if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY, info) == true){
				gridMapX -= 1;
				gridMapY += 0;
			}
			// 左上
			else if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY - 1, info) == true && isEnemyDiagonalMoveCheck(index, -1, -1, info) == false){
				gridMapX -= 1;
				gridMapY -= 1;
			}
			// 左下
			else if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY + 1, info) == true && isEnemyDiagonalMoveCheck(index, -1, 1, info) == false){
				gridMapX -= 1;
				gridMapY += 1;
			}
			break;

		case ObjectSet.CENTER:
			// その場で待機
			break;

		case ObjectSet.RIGHT:
			// 右
			if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY, info) == true){
				gridMapX += 1;
				gridMapY += 0;
			}
			// 右上
			else if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY - 1, info) == true && isEnemyDiagonalMoveCheck(index, 1, -1, info) == false){
				gridMapX += 1;
				gridMapY -= 1;
			}
			// 右下
			else if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY + 1, info) == true && isEnemyDiagonalMoveCheck(index, 1, 1, info) == false){
				gridMapX += 1;
				gridMapY += 1;
			}
			break;

		case ObjectSet.TOP_LEFT:
			// 左上
			if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY - 1, info) == true && isEnemyDiagonalMoveCheck(index, -1, -1, info) == false){
				gridMapX -= 1;
				gridMapY -= 1;
			}
			// 左
			else if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY, info) == true){
				gridMapX -= 1;
				gridMapY += 0;
			}
			// 上
			else if(isEnemyNextMoveCheck(gridMapX, gridMapY - 1, info) == true){
				gridMapX += 0;
				gridMapY -= 1;
			}
			break;

		case ObjectSet.TOP:
			// 上
			if(isEnemyNextMoveCheck(gridMapX, gridMapY - 1, info) == true){
				gridMapX += 0;
				gridMapY -= 1;
			}
			// 左上
			else if(isEnemyNextMoveCheck(gridMapX - 1, gridMapY - 1, info) == true && isEnemyDiagonalMoveCheck(index, -1, -1, info) == false){
				gridMapX -= 1;
				gridMapY -= 1;
			}
			// 右上
			else if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY - 1, info) == true && isEnemyDiagonalMoveCheck(index, 1, -1, info) == false){
				gridMapX += 1;
				gridMapY -= 1;
			}
			break;

		case ObjectSet.TOP_RIGHT:
			// 右上
			if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY - 1, info) == true && isEnemyDiagonalMoveCheck(index, 1, -1, info) == false){
				gridMapX += 1;
				gridMapY -= 1;
			}
			// 右
			else if(isEnemyNextMoveCheck(gridMapX + 1, gridMapY, info) == true){
				gridMapX += 1;
				gridMapY += 0;
			}
			// 上
			else if(isEnemyNextMoveCheck(gridMapX, gridMapY - 1, info) == true){
				gridMapX += 0;
				gridMapY -= 1;
			}
			break;
		}

		info.enemy[index].gridMapX = gridMapX;
		info.enemy[index].gridMapY = gridMapY;

		// 移動後の座標を更新
		info.mapUnit[gridMapY][gridMapX] = 3;
	}

	public boolean isEnemyNextMoveCheck(int nx, int ny, Info info)
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
		else if(info.map[ny][nx] == 1)
		{
			return false;
		}
		// プレイヤーorほかの敵と重複しようとしたとき
		else if(info.mapUnit[ny][nx] != -1)
		{
			return false;
		}

		return true;
	}

	// 斜め攻撃の可否
	public boolean isEnemyDiagonalAtkCheck(int index, int mx, int my, Info info)
	{
		if(info.map[info.enemy[index].gridMapY][info.enemy[index].gridMapX + mx] == 1 ||
		   info.map[info.enemy[index].gridMapY + my][info.enemy[index].gridMapX] == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isEnemyDiagonalMoveCheck(int index, int mx, int my, Info info)
	{
		//
		if(info.map[info.enemy[index].gridMapY][info.enemy[index].gridMapX + mx] == 1 ||
		   info.map[info.enemy[index].gridMapY + my][info.enemy[index].gridMapX] == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean isPlayerinView(int index, Info info)
	{
		int view = info.enemy[index].view;

		// 部屋の視界を含める場合

		// 座標から，部屋or通路を判別

		// 部屋番号，あるいは通路であることを取得
		int enemyRN = info.mapRoomNum[info.enemy[index].gridMapY][info.enemy[index].gridMapX];
		// 通路の場合，視界内にいるとき
		if(enemyRN == -1)
		{
			// プレイヤーとの距離の差を計算
			int difx = info.player.gridMapX - info.enemy[index].gridMapX;
			int dify = info.player.gridMapY - info.enemy[index].gridMapY;

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
			int playerRN = info.mapRoomNum[info.player.gridMapY][info.player.gridMapX];
			if(playerRN == enemyRN)
			{
				return true;
			}

			// または，視界内にいる
			int difx = info.player.gridMapX - info.enemy[index].gridMapX;
			int dify = info.player.gridMapY - info.enemy[index].gridMapY;
			if(-view <= difx && difx <= view && -view <= dify && dify <= view)
			{
				return true;
			}
		}

		// プレイヤーとの距離の差を計算
		int difx = info.player.gridMapX - info.enemy[index].gridMapX;
		int dify = info.player.gridMapY - info.enemy[index].gridMapY;

		// 視界内にいる場合
		if(-view <= difx && difx <= view && -view <= dify && dify <= view)
		{
			return true;
		}

		return false;
	}

	public void warpUnit(Info info, int index)
	{
		warpUnit(info, info.enemy[index]);
	}

	// 選択したユニットがワープする処理
	public void warpUnit(Info info, Unit u)
	{
		// ユニットのワープ前座標の部分を初期化
		info.mapUnit[u.gridMapY][u.gridMapX] = -1;

		Point geneP = new Point();
		int roomN = info.mapRoomNum[u.gridMapY][u.gridMapX];
		// 現在部屋の時，同じ部屋に飛ばないように
		if(roomN != -1)
		{
			// 同じ箇所にワープしないような工夫
			// ワープはいずれかの部屋に
			geneP = geneRandPos(info, roomN);
		}
		// 通路の時，いずれかの部屋
		else
		{
			geneP = geneRandPos(info);
		}

		// ランダムに座標を決定
		u.gridMapX = geneP.x;
		u.gridMapY = geneP.y;

		// ユニットのワープ後座標の更新
		info.mapUnit[u.gridMapY][u.gridMapX] = u.objNum;
	}

	// 引数として与えられた部屋番号に生成されないような
//	public Point geneRandPos(Info info, int rn)
//	{
//		// 生成する部屋をランダムに決定
//		int randRoomNum;
//		// ほかのオブジェクトとの重複チェック
//		Point gP = new Point();
//		do{
//			do{
//				randRoomNum = random.nextInt(info.roomNum);
//			} while(randRoomNum == rn);
//			// 部屋番号が重複している間，生成しなおす
//
//			// 選択した部屋の左上の座標と右上の座標
//			// 0:top-left 1:bottom-right
//			Point[] roomPoint = new Point[2];
//			roomPoint = getRoomPoint(info, randRoomNum);
//
//			// 生成座標をランダムに決定
//			// 0~(br-tl)+tl
//			gP.x = random.nextInt(roomPoint[1].x - roomPoint[0].x) + roomPoint[0].x;
//			gP.y = random.nextInt(roomPoint[1].y - roomPoint[0].y) + roomPoint[0].y;
//		} while((info.mapUnit[gP.y][gP.x] != -1 && info.mapUnit[gP.y][gP.x] != -100) ||
//				(info.mapObject[gP.y][gP.x] != -1 && info.mapObject[gP.y][gP.x] != -100) ||
//				 info.map[gP.y][gP.x] == 1 ||
//				isCheckPassJustBefore(info.map, gP) == true);
//		// 生成した座標にobjが存在するとき，または通行不可な部分のとき
//		// 再生成
//                
//                // -100の際の判定を考慮していない，
//                // ワープの際は未探索部分に飛ばす？
//                // 初期配置，未探索部分多いとき，ワープを使用すると無限ループ
//                // 未探索部分の処理を考える必要あり
//
//		return gP;
//	}
//
//	public Point geneRandPos(Info info)
//	{
//		// 生成する部屋をランダムに決定
//		int randRoomNum;
//		// ほかのオブジェクトとの重複チェック
//		Point gP = new Point();
//		do{
//			randRoomNum = random.nextInt(info.roomNum);
//			// 選択した部屋の左上の座標と右上の座標
//			// 0:top-left 1:bottom-right
//			Point[] roomPoint = new Point[2];
//			roomPoint = getRoomPoint(info, randRoomNum);
//
//			// 生成座標をランダムに決定
//			// 0~(br-tl)+tl
//			gP.x = random.nextInt(roomPoint[1].x - roomPoint[0].x) + roomPoint[0].x;
//			gP.y = random.nextInt(roomPoint[1].y - roomPoint[0].y) + roomPoint[0].y;
//		} while((info.mapUnit[gP.y][gP.x] != -1 && info.mapUnit[gP.y][gP.x] != -100) ||
//                        (info.mapObject[gP.y][gP.x] != -1 && info.mapObject[gP.y][gP.x] != -100) ||
//                         info.map[gP.y][gP.x] == 1 ||
//			 isCheckPassJustBefore(info.map, gP) == true);
//		// 生成した座標にobjが存在するとき，または通行不可な部分のとき
//		// 再生成
//
//		return gP;
//	}
        
        public Point geneRandPos(Info info, int rn)
        {
            	// 配置可能な部分
                ArrayList<Integer> candnum = countCandNum(info);
                
                int randnum = random.nextInt(candnum.size());
                
                int coornum = candnum.get(randnum);
                
                Point gP = new Point(coornum/30, coornum%30);
                
                return gP;
        }
        
        public Point geneRandPos(Info info)
        {
            	// 配置可能な部分
                ArrayList<Integer> candnum = countCandNum(info);
                
                int randnum = random.nextInt(candnum.size());
                
                int coornum = candnum.get(randnum);
                
                Point gP = new Point(coornum/30, coornum%30);
                
                return gP;
        }

        public ArrayList<Integer> countCandNum(Info info)
        {
            ArrayList<Integer> cnum = new ArrayList<>();
            
            // 現在の部屋の左上の座標
            // 部屋でないとき，(-1,-1)
            Point pp = getCurRoomTL(info.map, info.player.gridMapX, info.player.gridMapY);
            
            for(int y = 1; y < info.mapsizeY - 1; y++)
            {
                for(int x = 1; x < info.mapsizeX - 1; x++)
                {
                    Point wp = getCurRoomTL(info.map, x, y);

                    // 未探索部分
                    // または，移動可能部分かつ現在の部屋と異なる
                    if(info.map[y][x] == -100 ||
                       (info.map[y][x] == 0 && diagonalCheck(info.map, x, y) == -1 && (pp.x != wp.x && pp.y != wp.y)))
                    {
                        cnum.add(calcRoomID(new Point(x, y)));
                    }
                }
            }
            
            return cnum;
        }
        
	// 真：４方向のいずれかに通路がある
	// 偽：４方向のいずれにも通路なし
	public boolean isCheckPassJustBefore(int[][] map, Point p)
	{       
                int jud;
                // 上
                jud = diagonalCheck(map, p.x, p.y - 1);
                if (jud == 0 || jud == 1) {
                    return true;
                }
                // 下
                jud = diagonalCheck(map, p.x, p.y + 1);
                if (jud == 0 || jud == 1) {
                    return true;
                }
                // 左
                jud = diagonalCheck(map, p.x - 1, p.y);
                if (jud == 0 || jud == 1) {
                    return true;
                }
                // 右
                jud = diagonalCheck(map, p.x + 1, p.y);
                if (jud == 0 || jud == 1) {
                    return true;
                }

		return false;
	}
        
        // 視界・現在地を引数とする
        // -1:部屋
        //  0:縦
        //  1:横
        // 不明:-100
        public int diagonalCheck(int[][] map, int px, int py)
        {
            int[] diffx ={1,0,-1,0};
            int[] diffy ={0,1,0,-1};
            
            for(int i = 0; i < 2 ; i++)
            {
                if((map[py + diffy[i]][px + diffx[i]] == 1) && ( map[py + diffy[i+2]][px + diffx[i+2]] == 1))
                {
                    return i;     
                }
            }
            
            if(map[py][px] == -100)
            {
                return -100;
            }
            else
            {
                return -1;
            }
        }

//	public Point[] getRoomPoint(Info info, int num)
//	{
//		Point[] p = new Point[2];
//		p[0] = new Point(info.rpList.get(num).topLeft.x, info.rpList.get(num).topLeft.y);
//		p[1] = new Point(info.rpList.get(num).bottmRight.x, info.rpList.get(num).bottmRight.y);
//		return p;
//	}

	// プレイヤーの持つマップ情報の初期化
	public void initpmap(Info info)
	{
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				info.pmap[y][x] = false;
			}
		}
	}

	// プレイヤーの（現在）持つ情報の初期化
	public void initpCurmap(Info info)
	{
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				info.pCurmap[y][x] = false;
			}
		}
	}

	// 更新
	public void pmapUpdate(Info info)
	{
//		int rn = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
//
//		// 現在地が部屋の時，かつ初めて訪れる部屋の場合
//		// 部屋の情報を得る
//		if(rn == -1)
//		{
//			// 部屋の情報の更新
//			for(int y = info.currentRTopLeft.y - 1; y < info.currentRButtomRight.y + 1; y++)
//			{
//				for(int x = info.currentRTopLeft.x - 1; x < info.currentRTopLeft.x + 1; x++)
//				{
//					info.pmap[y][x] = true;
//				}
//			}
//		}
            
                int rn = info.mapRoomNum[info.player.gridMapY][info.player.gridMapX];

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
					if(info.mapRoomNum[y][x] == rn)
					{
						info.pmap[y][x] = true;

						// 部屋の周囲1マス分拡張したい
						// 何かしらの判定のほうが望ましい
						info.pmap[y-1][x-1] = true;
						info.pmap[y-1][x  ] = true;
						info.pmap[y-1][x+1] = true;
						info.pmap[y  ][x-1] = true;
						info.pmap[y  ][x+1] = true;
						info.pmap[y+1][x-1] = true;
						info.pmap[y+1][x  ] = true;
						info.pmap[y+1][x+1] = true;
					}
				}
			}
		}

		// 視界内の情報の更新
		for(int y = info.player.gridMapY - MyCanvas.SCREENGRIDSIZE_Y / 2; y <= info.player.gridMapY + MyCanvas.SCREENGRIDSIZE_Y / 2; y++)
		{
			for(int x = info.player.gridMapX - MyCanvas.SCREENGRIDSIZE_X / 2; x <= info.player.gridMapX + MyCanvas.SCREENGRIDSIZE_X / 2; x++)
			{
				// x,yが範囲内の時
				if(0 <= y && y < MyCanvas.MAPGRIDSIZE_Y && 0 <= x && x < MyCanvas.MAPGRIDSIZE_X)
				{
					//pmap[y][x] = true;
					info.pmap[y][x] = true;
				}
			}
		}
	}

	// 更新
	public void pCurmapUpdate(Info info)
	{
//		int rn = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
//                
//		// 現在地が部屋の時，かつ初めて訪れる部屋の場合
//		// 部屋の情報を得る
//		if(rn == -1)
//		{
//			// 部屋の情報の更新
//			for(int y = info.currentRTopLeft.y - 1; y < info.currentRButtomRight.y + 1; y++)
//			{
//				for(int x = info.currentRTopLeft.x - 1; x < info.currentRTopLeft.x + 1; x++)
//				{
//					info.pCurmap[y][x] = true;
//				}
//			}
//		}

                int rn = info.mapRoomNum[info.player.gridMapY][info.player.gridMapX];

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
					if(info.mapRoomNum[y][x] == rn)
					{
						info.pCurmap[y][x] = true;

						// 部屋の周囲1マス分拡張したい
						// 何かしらの判定のほうが望ましい
						info.pCurmap[y-1][x-1] = true;
						info.pCurmap[y-1][x  ] = true;
						info.pCurmap[y-1][x+1] = true;
						info.pCurmap[y  ][x-1] = true;
						info.pCurmap[y  ][x+1] = true;
						info.pCurmap[y+1][x-1] = true;
						info.pCurmap[y+1][x  ] = true;
						info.pCurmap[y+1][x+1] = true;
					}
				}
			}
		}
            
		// 視界内の情報の更新
		for(int y = info.player.gridMapY - MyCanvas.SCREENGRIDSIZE_Y / 2; y <= info.player.gridMapY + MyCanvas.SCREENGRIDSIZE_Y / 2; y++)
		{
			for(int x = info.player.gridMapX - MyCanvas.SCREENGRIDSIZE_X / 2; x <= info.player.gridMapX + MyCanvas.SCREENGRIDSIZE_X / 2; x++)
			{
				// x,yが範囲内の時
				if(0 <= y && y < MyCanvas.MAPGRIDSIZE_Y && 0 <= x && x < MyCanvas.MAPGRIDSIZE_X)
				{
					info.pCurmap[y][x] = true;
				}
			}
		}
	}
        
        public Point getCurRoomTL(int[][] map, int px, int py)
        {
            // 部屋の時
            if(diagonalCheck(map, px, py) == -1)
            {
                int x = px;
                int y = py;
                for(; x >= 1; x--)
                {
                    // １マス先が壁
                    if(map[y][x - 1] == 1)
                    {
                        break;
                    }
                }
                for(; y >= 1; y--)
                {
                    // １マス先が壁
                    if(map[y - 1][x] == 1)
                    {
                        break;
                    }
                }

                //System.out.println("tl:(" + x + "," + y + ")");
                return new Point(x, y);
            }
            else
            {
                return new Point(-1, -1);
            }
        }
        
        public Point getCurRoomBR(int[][] map, int px, int py, int maxx, int maxy)
        {
            // 部屋の時
            if(diagonalCheck(map, px, py) == -1)
            {
                int x = px;
                int y = py;
                for(; x < maxx - 1; x++)
                {
                    // １マス先が壁
                    if(map[y][x + 1] == 1)
                    {
                        break;
                    }
                }
                for(; y < maxy - 1; y++)
                {
                    // １マス先が壁
                    if(map[y + 1][x] == 1)
                    {
                        break;
                    }
                }

                return new Point(x, y);
            }
            else
            {
                return new Point(-1, -1);
            }
        }
        
        public int calcRoomID(Point p)
        {
            return p.x * 30 + p.y;
        }
}
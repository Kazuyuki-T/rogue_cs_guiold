import java.util.ArrayList;

public class TurnManagerSimulator
{
        // コンストラクタ
        public TurnManagerSimulator()
        {
            
        }
        
        // 
        
        // ターン経過による自然回復
	public void countPlayerHpSpontRec(Info info)
	{
		info.player.sumSpontRecVal += info.player.spontRecVal;
		while(info.player.sumSpontRecVal >= 1.0)
		{
			info.player.hp += 1;
			if(info.player.hp > info.player.maxHp)
			{
				info.player.hp = info.player.maxHp;
			}
			info.player.sumSpontRecVal -= 1.0;
		}
	}
        
        // ターン経過による満腹度の減少
	public void countPlayerSat(Info info)
	{
		info.player.spDecCount++;
		if(info.player.spDecCount == info.player.SP_DEC_TURNNUM)
		{
			if(info.player.satiety > 0)
			{
				info.player.satiety--;
			}

			info.player.spDecCount = 0;
		}
	}

	public void turnFlow(Info info)
	{
		//System.out.println("turn flow req");
                
                ObjectSetSimulator objSimu = new ObjectSetSimulator();

                //
                
		// 敵の行動
		objSimu.moveEnemy(info);
		// ...
		// ...

		if(info.player.active == true)
		{
			// アクションフラグリセット
			info.player.action_flag = false;

			// 満腹度の減少
			countPlayerSat(info);

			// 満腹度0の際のHP減少
			if(info.player.satiety == 0)
			{
				objSimu.calcPlayerDamage(1, info);
			}
			else
			{
				// HPの自然回復
				countPlayerHpSpontRec(info);
			}

			// プレイヤーの持つマップ情報の更新
			objSimu.pmapUpdate(info);

			// プレイヤーの現在持つマップ情報
			objSimu.initpCurmap(info);
			objSimu.pCurmapUpdate(info);
                        
			// ターンの更新
			info.turn++;
                        
                        // フロアの滞在ターン数の更新
                        if(info.player.curFloor < MyCanvas.TOPFLOOR)
                        {
                            info.floorturn[info.player.curFloor]++;
                        }
		}
                
                for(int n1 = 0; n1 < info.visibleEnemy.size(); n1++)
		{
			for(int n2 = 0; n2 < info.enemy.length; n2++)
                        {
                                if(info.visibleEnemy.get(n1).index == info.enemy[n2].index)
                                {
                                        info.visibleEnemy.get(n1).hp = info.enemy[n2].hp;
                                        info.visibleEnemy.get(n1).gridMapX = info.enemy[n2].gridMapX;
                                        info.visibleEnemy.get(n1).gridMapY = info.enemy[n2].gridMapY;
                                        info.visibleEnemy.get(n1).active = info.enemy[n2].active;
                                        break;
                                }
                        }
		}
	}

	public void turnCount(Action act, Info info)
	{
		//System.out.println("turncount req");
            
                ObjectSetSimulator objSimu = new ObjectSetSimulator();

		// act.dir 0~8->1~9
		act.dir++;

                
                // 情報の
                
              
                
                //System.out.println("jud action");

                
                
		// 攻撃のとき
		if(act.action == Action.ATTACK)
                {
			//System.out.println("action attack st");
                        info.player.dir = act.dir;
			objSimu.attackPlayer(info);
                        
			info.player.action_flag = true;
                        //System.out.println("action attack end");
		}
		// 移動のとき
		else if(act.action == Action.MOVE)
		{
			//System.out.println("action move st");
                        info.player.dir = act.dir;
			if(objSimu.movePlayer(info, act.difPos.x, act.difPos.y) == true)
			{
				info.player.action_flag = true;
			}
                        else{
                            //System.out.println("action flag - false");
                        }
                        //System.out.println("action move end");
		}
		// アイテム使用
		else if(act.action == Action.USE_ITEM)
		{
			//System.out.println("action useitem start");
                        //info.getPlayerInv().sysOutItemList();
			//System.out.println("useitem(dir:" + act.dir + ", index:" + act.itemIndex + "/" + info.getPlayerInv().getInvItemNum() + ")");
			info.player.dir = act.dir;
			info.player.inventory.useItem(info, act.itemIndex);
			info.player.action_flag = true;
                        //System.out.println("action useitem end");
		}
		else if(act.action == Action.STAY)
		{
			info.player.dir = act.dir;
			info.player.action_flag = true;
		}

		// もしプレイヤーが行動しているならば
		if(info.player.action_flag == true){
			turnFlow(info);
		}
                else
                {
                    //System.out.println("action flag false 2");
                }
	}
        
//        public ArrayList<Enemy> setVisibleEnemy(Info info)
//        {
//                ArrayList<Enemy> visEnemy = new ArrayList<Enemy>();
//            
//                for(int y=0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
//                {
//                        for(int x=0; x < MyCanvas.MAPGRIDSIZE_X; x++)
//                        {
//                                if(objectset.getpCurmap(x, y) == true && umap[y][x] == 3)
//                                {
//                                        for(int eNum = 0; eNum < objectset.enemy.length; eNum++)
//                                        {
//                                                if(objectset.enemy[eNum].gridMapX == x && objectset.enemy[eNum].gridMapY == y)
//                                                {
//                                                        visEnemy.add(objectset.enemy[eNum]);
//                                                        break;
//                                                }
//                                        }
//                                }
//                        }
//                }
//                
//                return visEnemy;
//        }
}
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class MonteCarloPlayer implements Agent
{
        private int trialPlay_imp = 10;
	// 現在の状態から選択可能な次のアクションを探索
	// アクションごとにモンテカルロシミュレーション
	// 評価値の平均が最も高いアクションを選択

        private int[] difX = new int[] {-1, 0, 1,-1, 0, 1,-1, 0, 1};
	private int[] difY = new int[] { 1, 1, 1, 0, 0, 0,-1,-1,-1};
        
        private int[] dif4X = new int[] { 0, 1,-1, 0};
	private int[] dif4Y = new int[] { 1, 0, 0,-1};
        
        // 展開深さ
	private static final int DEPTHLIMITED = 2;

	private Random random;
        
        int curFloor; // シミュレーション開始時のプレイヤ階層
        
        int sfd;
        int spt;
        int sar;
        int sst;

        int stturn;
        
        int debug = 0;
        
        // コンストラクタ
	public MonteCarloPlayer()
	{
		random = new Random();
        }
        
        public void stepSetup(int A)
	{
		trialPlay_imp = A;
	}

	// セットされたアクションの実行
	public void playAction(Action act, Info info)
	{
		if(0 > act.dir || act.dir >= 10)
		{
			System.out.println("dir-error:" + act.dir);
		}

		TurnManagerSimulator tmSimu = new TurnManagerSimulator();

		// アクションを実行
		// ターン経過後の盤面を計算
		tmSimu.turnCount(act, info);
                
                // infoの更新
                info.currentRTopLeft = getCurRoomTL(info.map, info.player.gridMapX, info.player.gridMapY);
                info.currentRButtomRight = getCurRoomBR(info.map, info.player.gridMapX, info.player.gridMapY, info.mapsizeX, info.mapsizeY);
	
                countAtk(info);
                
                if (act.action == Action.ATTACK) {
                    info.patknum++;
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
        
	// ある盤面における評価値の計算
	public double calcEvaVal(Info info)
	{
		// 評価値計算
		double pHpPersent = (double)info.player.hp / info.player.maxHp;

		// 敵のhpの合計
		int sum = 0;
		int maxSum = 0;


                // すべての敵
//		for(int index = 0; index < info.enemy.length; index++)
//		{
//			sum += (double)info.enemy[index].hp;
//			maxSum += (double)info.enemy[index].maxHp;
//		}

                // 視界内の敵
                int countbeat = 0;
                for(int index = 0; index < info.visibleEnemy.size(); index++)
		{
			for(int eindex = 0; eindex < info.enemy.length; eindex++)
                        {
                            if(info.visibleEnemy.get(index).index == info.enemy[eindex].index)
                            {
                                sum += (double)info.enemy[index].hp;
                                maxSum += (double)info.enemy[index].maxHp;
                                
                                if(info.enemy[index].active == false)
                                {
                                    countbeat++;
                                }
                                
                                break;
                            }
                        }
		}
                

		/*
		for(int index = 0; index < objSimu.enemy.length; index++)
		{
			sum += (double)objSimu.enemy[index].hp;
			maxSum += (double)objSimu.enemy[index].maxHp;
		}
		*/

		/*
		System.out.print("hp:" + objSimu.player.hp);
		System.out.println(" mh:" + objSimu.player.maxHp);

		//
		for(int i=0; i < objSimu.enemy.length; i++)
		{
			System.out.println("  + e" + i + ":" + objSimu.enemy[i].hp + "/" + objSimu.enemy[i].maxHp);
		}
		 */

		//System.out.print("sum:" + sum);
		//System.out.println(" maxSum:" +maxSum);


		int life = (info.player.active == true) ? 1 : 0 ;

		//System.out.println("eva:" + (pHpPersent + (sum/maxSum) * 10) * life);


		int w1 = 1;
		int w2 = 10;
		//return  (pHpPersent + (1.0 - sum/maxSum) * 10) * life;
		//return  (objSimu.beatEnemy + 1) * (1.0 - sum/maxSum) * 10 * life;
		//return  ((1.0 - sum/maxSum) * 10 + (double)objSimu.atkNum) * life;
		//return ( (pHpPersent * w1) + ((1.0 - sum/maxSum) * w2) ) * life;
		//return ((pHpPersent * w1) + (maxSum - sum)) * life;
		//return ((1.0 - sum/maxSum) * w2);
		//return (double)objSimu.atkNum;

                
                int newfloor = 0;
                int gameclear = 0;
                if(info.player.curFloor < MyCanvas.TOPFLOOR)
                {
                    //newfloor = (info.floorturn[info.player.curFloor] == 1) ? 1 : 0;
                    newfloor = (curFloor != info.player.curFloor) ? 1 : 0;
                }
                else if(info.player.curFloor == MyCanvas.TOPFLOOR)
                {
                    gameclear = (info.player.curFloor == MyCanvas.TOPFLOOR) ? 1 : 0;
                }

                //int ist = info.player.inventory.getInvItemNum(4); // 杖数
                //int wst = 70;
                
//                System.out.println("---");
//                System.out.println("p(" + info.player.gridMapX + ", " + info.player.gridMapY + "), dir:" + info.player.dir);
//                // 参照がvisenemy，enemy[]にしなければならない
////                for(int index = 0; index < info.visibleEnemy.size(); index++)
////                {
////                    System.out.println("e" + info.visibleEnemy.get(index).index + 
////                                "(" + info.visibleEnemy.get(index).gridMapX + "," + info.visibleEnemy.get(index).gridMapY + "):" + 
////                                info.visibleEnemy.get(index).hp + "/" + info.visibleEnemy.get(index).maxHp);
////                }
//                for(int index = 0; index < info.visibleEnemy.size(); index++)
//		{
//			for(int eindex = 0; eindex < info.enemy.length; eindex++)
//                        {
//                            if(info.visibleEnemy.get(index).index == info.enemy[eindex].index)
//                            {
//                                System.out.println("e" + info.enemy[eindex].index + 
//                                "(" + info.enemy[eindex].gridMapX + "," + info.enemy[eindex].gridMapY + "):" + 
//                                info.enemy[eindex].hp + "/" + info.enemy[eindex].maxHp);
//                                break;
//                            }
//                        }
//		}
//                System.out.println("(maxSum - sum)  : " + (maxSum - sum));
//                System.out.println("(countbeat * 5) : " + (countbeat * 5));
//                System.out.println("(pmaxHp - php)  : " + (-1 * (info.player.maxHp - info.player.hp)));
//                System.out.println("(50 * life)     : " + (50 * life));
//                System.out.println("(100 * newflr)  : " + (100 * newfloor));
//                System.out.println("(1000 * gclear) : " + (1000 * gameclear));
//                System.out.println("(wst * ist)     : " + (wst * ist));
                
		//return (double)(maxSum - sum) + (double)(countbeat * 5) - (double)(info.player.maxHp - info.player.hp) + (double)(50 * life) + (double)(100 * newfloor) + (double)(1000 * gameclear) + (double)(wst * ist);
                
                
                int ifd = info.player.inventory.getInvItemNum(1); // 食料数*回復量
                int ipt = info.player.inventory.getInvItemNum(2); // ポーション数
                int iar = info.player.inventory.getInvItemNum(3); // 矢数
                int ist = info.player.inventory.getInvItemNum(4); // 杖数
                
                double value = 
                        (double)(0.0
                            + ((life == 0) ? -1000 : 0)
                            + (50 * countbeat)
                            + (-25 * ((sfd - ifd) < 0 ? 0 : (sfd - ifd))) 
                            + (-25 * (spt - ipt))
                            + (-25 * ((sar - iar) < 0 ? 0 : (sar - iar))) 
                            + (-25 * ((sst - ist) < 0 ? 0 : (sst - ist)))
                            + (-35 * ((info.eatknum - info.patknum)>0?info.eatknum - info.patknum:0))
                            + (25 * (info.patknum))
                             );
                
//                System.out.println("life                                        : " + ((life == 0) ? -1000 : 0));
//                System.out.println("(50 * countbeat)                            : " + (50 * countbeat));
//                System.out.println("(-25 * ((sfd - ifd) < 0 ? 0 : (sfd - ifd))) : " + (-25 * ((sfd - ifd) < 0 ? 0 : (sfd - ifd))));
//                System.out.println("(-25 * (spt - ipt))                         : " + (-25 * (spt - ipt)));
//                System.out.println("(-25 * ((sar - iar) < 0 ? 0 : (sar - iar))) : " + (-25 * ((sar - iar) < 0 ? 0 : (sar - iar))));
//                System.out.println("(-25 * ((sst - ist) < 0 ? 0 : (sst - ist))) : " + (-25 * ((sst - ist) < 0 ? 0 : (sst - ist))));
//                System.out.println("(-35 * (info.eatknum - info.patknum))       : " + (-35 * (info.eatknum - info.patknum)));
//                System.out.println("(info.patknum)                              : " + ( 50 * info.patknum));
//                System.out.println("= value                                     : " + value);
                
                return value;
                
//              
//
//                
//                double simuturn = stturn - info.floorturn[curFloor];
//                
//                // e1 : 平均化パーセプトロン -> 重み，2F分のみ
//                // e2 : 平均化パーセプトロン -> 重み，クリア合否
//                // e3 : 平均化パーセプトロン -> 重み，次階層突破合否
//                // e4 : 平均化パーセプトロン -> 重み，先生
//                int e = 3;
//                
//                // クリアしたとき
//                if(info.player.curFloor == MyCanvas.TOPFLOOR)
//                {
//                    //return 100000;
//                    return (double)(10000 + value + (500 * simuturn));
//                }
//                // その他
//                else
//                {
//                    int fd = info.player.inventory.getInvItemNum(1); // 食料数*回復量
//                    int pt = info.player.inventory.getInvItemNum(2); // ポーション数
//                    int ar = info.player.inventory.getInvItemNum(3); // 矢数
//                    int st = info.player.inventory.getInvItemNum(4); // 杖数
//
//                    double innerp = 0.0;
//
//                    // 平均化パーセプトロン -> 重み，2F分のみ
//                    if(e == 1)
//                    {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                        
//                            innerp = ((double)-1737.87659305395) + 
//                                     ((double)info.player.hp * 38.7841204200804) + 
//                                     ((double)info.player.level * 116.648577122649) +
//                                     ((double)(info.player.satiety + fd) * -44.6516670185182) +
//                                     ((double)pt * 202.435531883301) +
//                                     ((double)ar * 134.068083769612) +
//                                     ((double)st * 83.1771765887105);
//                    }
//                    // 平均化パーセプトロン -> 重み，クリア合否
//                    else if(e == 2)
//                    {
//                        if(curFloor == 0)
//                        {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-0.567722550202078) + 
//                                     ((double)info.player.hp * -1.1768128822023318) + 
//                                     ((double)info.player.level * 268.96038849742905) +
//                                     ((double)(info.player.satiety + fd) * -38.178950041261984) +
//                                     ((double)pt * 369.0276561078313) +
//                                     ((double)ar * 156.00577667745827) +
//                                     ((double)st * 229.92617279248398);
//                        }
//                        else if(curFloor == 1)
//                        {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-200.9942195359403) + 
//                                     ((double)info.player.hp * 33.31792552328412) + 
//                                     ((double)info.player.level * 77.79378955054356) +
//                                     ((double)(info.player.satiety + fd) * -47.72162502028233) +
//                                     ((double)pt * 59.872444426415704) +
//                                     ((double)ar * 100.38759532695116) +
//                                     ((double)st * 8.091716696414084);
//                        }
//                        else if(curFloor == 2)
//                        {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-217.9416762522221) + 
//                                     ((double)info.player.hp * 22.203074349053644) + 
//                                     ((double)info.player.level * -91.19293108857053) +
//                                     ((double)(info.player.satiety + fd) * 11.309840008365576) +
//                                     ((double)pt * 42.562820244693086) +
//                                     ((double)ar * 61.15356059813866) +
//                                     ((double)st * 61.45788455505595);
//                        }
//                        else if(curFloor == 3)
//                        {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-217.9416762522221) + 
//                                     ((double)info.player.hp * 22.203074349053644) + 
//                                     ((double)info.player.level * -91.19293108857053) +
//                                     ((double)(info.player.satiety + fd) * 11.309840008365576) +
//                                     ((double)pt * 42.562820244693086) +
//                                     ((double)ar * 61.15356059813866) +
//                                     ((double)st * 61.45788455505595);
//                        }
//                        else if(info.player.curFloor == 4)
//                        {
//                            innerp = 100000;
//                        }
//                    }
//                    // 平均化パーセプトロン -> 重み，次階層突破合否
//                    else if(e == 3)
//                    {
//                        if(curFloor == 0)
//                        {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)2.0364443211759813) + 
//                                     ((double)info.player.hp * 12.406989321869366) + 
//                                     ((double)info.player.level * 450.29377340174733) +
//                                     ((double)(info.player.satiety + fd) * -49.624351684925806) +
//                                     ((double)pt * 630.4329773956456) +
//                                     ((double)ar * 522.8628206906116) +
//                                     ((double)st * 336.1643877409513);
//                        }
//                        else if(curFloor == 1)
//                        {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-151.3174032459426) + 
//                                     ((double)info.player.hp * 56.49365792759051) + 
//                                     ((double)info.player.level * 139.79340823970037) +
//                                     ((double)(info.player.satiety + fd) * -72.05730337078651) +
//                                     ((double)pt * 109.40097378277153) +
//                                     ((double)ar * 229.79725343320848) +
//                                     ((double)st * -64.05078651685393);
//                        }
//                        else if(curFloor == 2)
//                        {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-213.31087261095985) + 
//                                     ((double)info.player.hp * 31.38835285098734) + 
//                                     ((double)info.player.level * -64.58407570911206) +
//                                     ((double)(info.player.satiety + fd) * -2.977900552486188) +
//                                     ((double)pt * -1.110100716381612) +
//                                     ((double)ar * 139.18195035554734) +
//                                     ((double)st * -30.549128975124905);
//                        }
//                        else if(curFloor == 3)
//                        {
////                            if(life == 0)
////                            {
////                                return -10000;
////                            }
////                            
////                            innerp = ((double)-213.31087261095985) + 
////                                     ((double)info.player.hp * 31.38835285098734) + 
////                                     ((double)info.player.level * -64.58407570911206) +
////                                     ((double)(info.player.satiety + fd) * -2.977900552486188) +
////                                     ((double)pt * -1.110100716381612) +
////                                     ((double)ar * 139.18195035554734) +
////                                     ((double)st * -30.549128975124905);
//                            if(life == 0)
//                            {
//                                innerp = -10000;
//                            }
//                        }
//                        else if(curFloor == 4)
//                        {
//                            innerp = 10000;
//                        }
//                    }
//                    // 平均化パーセプトロン -> 重み，先生
//                    else if(e == 4)
//                    {
//                        if(curFloor == 0)
//                        {
//                            // 死んでいるとき
//                            if(life == 0)
//                            {
//                                return -150000;
//                            }
//                            
//                            innerp = ((double)-340.763011374432) + 
//                                     ((double)info.player.hp * -29.0758410268433) + 
//                                     ((double)info.player.level * 325.406842881286) +
//                                     ((double)(info.player.satiety + fd) * -41.3226907525658) +
//                                     ((double)pt * 776.437733958073) +
//                                     ((double)ar * 282.771492476119) +
//                                     ((double)st * 485.544369395145);
//                        }
//                        else if(curFloor == 1)
//                        {
//                            // 死んでいるとき
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-1737.87659305395) + 
//                                     ((double)info.player.hp * 38.7841204200804) + 
//                                     ((double)info.player.level * 116.648577122649) +
//                                     ((double)(info.player.satiety + fd) * -44.6516670185182) +
//                                     ((double)pt * 202.435531883301) +
//                                     ((double)ar * 134.068083769612) +
//                                     ((double)st * 83.1771765887105);
//                        }
//                        else if(curFloor == 2)
//                        {
//                            // 死んでいるとき
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-1692.83670351477) + 
//                                     ((double)info.player.hp * 48.1688720345866) + 
//                                     ((double)info.player.level * -20.9999222223086) +
//                                     ((double)(info.player.satiety + fd) * 22.6880959021157) +
//                                     ((double)pt * 72.5431005076661) +
//                                     ((double)ar * 186.477792802452) +
//                                     ((double)st * 52.2077886580126);
//                        }
//                        else if(curFloor == 3)
//                        {
//                            // 死んでいるとき
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-1692.83670351477) + 
//                                     ((double)info.player.hp * 48.1688720345866) + 
//                                     ((double)info.player.level * -20.9999222223086) +
//                                     ((double)(info.player.satiety + fd) * 22.6880959021157) +
//                                     ((double)pt * 72.5431005076661) +
//                                     ((double)ar * 186.477792802452) +
//                                     ((double)st * 52.2077886580126);
//                        }
//                        else if(curFloor == 4)
//                        {
//                            innerp = 100000;
//                        }
//                    }
//                    else if(e == 5)
//                    {
//                        // e3
//                        if(curFloor == 0)
//                        {
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)2.0364443211759813) + 
//                                     ((double)info.player.hp * 12.406989321869366) + 
//                                     ((double)info.player.level * 450.29377340174733) +
//                                     ((double)(info.player.satiety + fd) * -49.624351684925806) +
//                                     ((double)pt * 630.4329773956456) +
//                                     ((double)ar * 522.8628206906116) +
//                                     ((double)st * 336.1643877409513);
//                        }
//                        // e4
//                        else if(curFloor == 1)
//                        {
//                            // 死んでいるとき
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-1737.87659305395) + 
//                                     ((double)info.player.hp * 38.7841204200804) + 
//                                     ((double)info.player.level * 116.648577122649) +
//                                     ((double)(info.player.satiety + fd) * -44.6516670185182) +
//                                     ((double)pt * 202.435531883301) +
//                                     ((double)ar * 134.068083769612) +
//                                     ((double)st * 83.1771765887105);
//                        }
//                        else if(curFloor == 2)
//                        {
//                            // 死んでいるとき
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-1692.83670351477) + 
//                                     ((double)info.player.hp * 48.1688720345866) + 
//                                     ((double)info.player.level * -20.9999222223086) +
//                                     ((double)(info.player.satiety + fd) * 22.6880959021157) +
//                                     ((double)pt * 72.5431005076661) +
//                                     ((double)ar * 186.477792802452) +
//                                     ((double)st * 52.2077886580126);
//                        }
//                        else if(curFloor == 3)
//                        {
//                            // 死んでいるとき
//                            if(life == 0)
//                            {
//                                return -10000;
//                            }
//                            
//                            innerp = ((double)-1692.83670351477) + 
//                                     ((double)info.player.hp * 48.1688720345866) + 
//                                     ((double)info.player.level * -20.9999222223086) +
//                                     ((double)(info.player.satiety + fd) * 22.6880959021157) +
//                                     ((double)pt * 72.5431005076661) +
//                                     ((double)ar * 186.477792802452) +
//                                     ((double)st * 52.2077886580126);
//                        }
//                        else if(curFloor == 4)
//                        {
//                            innerp = 100000;
//                        }
//                    }
//                    
//                    return innerp + value;
//                }                
	}

        public void countAtk(Info info) {
            for (Enemy tgen : info.visibleEnemy) {
                Point p = new Point(info.player.gridMapX, info.player.gridMapY);
                int dx = Math.abs(p.x - tgen.gridMapX);
                int dy = Math.abs(p.y - tgen.gridMapY);
                if (dx < 2 && dy < 2 && tgen.active == true) {
                    // 斜め4方向に注目
                    if (dx + dy == 2) {
                        boolean eAtkAble = true;
                        for (int i = 0; i < 4; i++) {
                            if (info.map[tgen.gridMapY + dif4Y[i]][tgen.gridMapX + dif4X[i]] == 1) {
                                eAtkAble = false;
                                break;
                            }
                        }
                        if (eAtkAble == true) {
                            info.eatknum++;
                        }

                    } else {
                        info.eatknum++;
                    }
                }
            }
        }

	// 真：視野内にモンスターがいる
	// 偽：モンスターがいない
	public boolean isCheckMonsterinPView(Info info)
	{
		// 視界内のモンスターの探索
		for(int y = -5; y <= 5; y++)
		{
			for(int x = -5; x <= 5; x++)
			{
				// 敵がいて，かつ視界内
				if(0 < info.player.gridMapY + y && info.player.gridMapY + y < MyCanvas.MAPGRIDSIZE_Y &&
				   0 < info.player.gridMapX + x && info.player.gridMapX + x < MyCanvas.MAPGRIDSIZE_X &&
				   info.mapUnit[info.player.gridMapY + y][info.player.gridMapX + x] == 3 &&
				   info.pCurmap[info.player.gridMapY + y][info.player.gridMapX + x] == true)
				{
					return true;
				}
			}
		}

		return false;
	}
        
        // 確認できた敵をすべて倒したとき，true
        // 確認できた敵が生き残っているとき，false
        public boolean isCheckMonsterBeat(Info info)
        {
            boolean flag = true;
            
            for(int index = 0; index < info.visibleEnemy.size(); index++)
            {
                if(info.visibleEnemy.get(index).active == true)
                {
                    flag = false;
                }
            }
            
            return flag;
        }

        
        
        //////////////
        // rulebase //
        //////////////
        public double RulePlaySimulator(Info info, RuleBasePlayer rbp) 
        {
            //ArrayList<Action> actList = new ArrayList<Action>();
            int trialPlay = trialPlay_imp;

            // アクションリストの作成
            while(true)
            {    
                // ルールベースによるアクションの決定
                Action act = rbp.ruleBasedforSimu(info);

                // アクション実行前の階層
                int curfloor = info.player.curFloor;

                // 設定したアクションを実行
                playAction(act, info);

                // ゲームオーバー player.active == false
                // アクション実行前と階層が異なる
                // 視野内にモンスターがいない
                trialPlay -=1;
                if (info.player.active == false
                        || info.player.curFloor != curfloor
                        || isCheckMonsterinPView(info) == false
                        || trialPlay <= 0) {
                    // これらの時は，評価値を計算し，値を返す
                    break;
                }
            }
            
            return calcEvaVal(info);
        }
        
	public double calcEvaValAve(Action acts, Info info, RuleBasePlayer rbp)
	{
		// 評価値の合計
		double sumEvaVal = 0.0;
		// 1アクションに対する試行回数
		int trialOneAct = 1000;

                // rbpにinfoを通して状況を更新
                //rbp.ruleBasedOnly(info);
                
                // rbp.history update
                if(acts.action == Action.MOVE)
                {
                    rbp.updateHistory(info.player.gridMapX, info.player.gridMapY);
                }
                
		// 引数として与えられたアクションを実行
		playAction(acts, info);
               
                
                
		for(int i = 0; i < trialOneAct; i++)
		{
			// 不明な盤面情報の補完
                        // infoの更新
                    
                        
                    
                    

                        //System.out.println("simu:" + i);
			// ランダムにアクションをとる
			// 最終的な盤面における評価値が戻る
			// 場面のコピーを引数とする
                        sumEvaVal += RulePlaySimulator(info.clone(), rbp.clone());
		}
		//System.out.println(sumEvaVal);
		return sumEvaVal / (double)trialOneAct;
	}

        public double mctsdepth(Action act, Info info, RuleBasePlayer rbp, int deep)
        {
            if(deep == DEPTHLIMITED)
            {
                return calcEvaValAve(act, info.clone(), rbp);
            }
            else
            {
                int curfloor = info.player.curFloor;

                // rbp.history update
                if(act.action == Action.MOVE)
                {
                    rbp.updateHistory(info.player.gridMapX, info.player.gridMapY);
                }
                
                // info + act -> new info
                playAction(act, info);
                
                // rbpにinfoを通して状況を更新
                rbp.update(info);
                
                // 以下の時，評価値を返す
                // プレイヤーが死んだとき
                // 階層が変化したとき
                // 敵が視界内から消えたとき
                if (info.player.active == false
                    || curfloor != info.player.curFloor
                    || isCheckMonsterinPView(info) == false
                    ) 
                {
                        // 評価値を計算し，値を返す
                        return calcEvaVal(info);
                }
                
                
                
                // 上の条件に当てはまらないとき，さらに行動を展開
                ArrayList<Action> actList = new ArrayList<Action>();
		actList = info.makeActionLimitedList(actList);
                
		for(int index = 0; index < actList.size(); index++)
		{
			actList.get(index).evaVal = mctsdepth(actList.get(index), info.clone(), rbp.clone(), deep + 1);
		}
                
                // 評価値の最も高いアクションをListの中から探索
		double maxEvaVal = actList.get(0).evaVal;
		for(int index = 1; index < actList.size(); index++)
		{
			if(maxEvaVal < actList.get(index).evaVal)
			{
				maxEvaVal = actList.get(index).evaVal;
			}
		}
                
                return maxEvaVal;
            }
        }
        
	public Action DLMCTS(Info info, RuleBasePlayer rbp)
	{
		// アクションリストの作成
		ArrayList<Action> actList = new ArrayList<Action>();

		// 取りうるアクションのリストアップ
		actList = info.makeActionLimitedList(actList);

		//System.out.println("maked Action List, player(" + info.player.gridMapX + "," + info.player.gridMapY + ")");

		// 各行動の評価値の平均を得る
		for(int index = 0; index < actList.size(); index++)
		{
			//actList.get(index).evaVal = calcEvaValAve(actList.get(index), info.clone());
                        
                        // 深さ分展開
                        actList.get(index).evaVal = mctsdepth(actList.get(index), info.clone(), rbp.clone(), 1);
		}

//		System.out.println("simulation");
//
//		for(int index = 0; index < actList.size(); index++)
//		{
//                        System.out.print(index + " -> act : " + actList.get(index).action);
//			// 評価済み
//			System.out.print(", dir(1~9) : " + actList.get(index).dir);
//			System.out.println(", eval : " + actList.get(index).evaVal);
//		}

		// 評価値の最も高いアクションをListの中から探索
		double maxEvaVal = actList.get(0).evaVal;
		int maxIndex = 0;
		for(int index = 1; index < actList.size(); index++)
		{
			if(maxEvaVal < actList.get(index).evaVal)
			{
				maxEvaVal = actList.get(index).evaVal;
				maxIndex = index;
			}
		}

		return actList.get(maxIndex);
	}
        
	// rulebaseからinfoを受け取り，アクションを返す
	public Action makeAction(Info info, RuleBasePlayer rbp)
	{
		Action act = new Action(info.player.dir);

		act = DLMCTS(info, rbp);

		// シミュレーション時にdir:1~9
		// 1~9 -> 0~8
		act.dir--;

//		System.out.println("action:" + act.action);
//		System.out.println("dir(0~8):" + act.dir);
//		System.out.println("evaVal:" + act.evaVal);

		return act;
	}
        
        
        
        ////////////
        // random //
        ////////////
        
        // ある盤目から，ランダムにアクションをとる
        public double randomPlay(Info info) 
        {
            // ランダムなアクション回数
            int trialPlay = trialPlay_imp;

            ArrayList<Action> actList = new ArrayList<Action>();

            // シミュレータの初期化
            while(true)
            {
                // アクションリストの作成
                actList.clear();
                actList = info.makeActionList(actList);

                //Action randAct = new Action(info.player.dir);
                // ランダムなアクションを決定
                Action randAct = actList.get(random.nextInt(actList.size()));

                // アクション実行前の階層
                //int curfloor = info.player.curFloor;

                // 設定したアクションを実行
                playAction(randAct, info);

                // ゲームオーバー player.active == false
                // アクション実行前と階層が異なる
                // 視野内にモンスターがいない
                trialPlay -=1;
                if (info.player.active == false
                        || info.player.curFloor != curFloor
//                        || isCheckMonsterinPView(info) == false
                        || isCheckMonsterBeat(info) == true
                        || trialPlay <= 0) {
                    // これらの時は，評価値を計算し，値を返す
                    break;
                }
            }

            // 一定のターン経過したとき -> 評価値を計算，値を返す
            return calcEvaVal(info);
        }

        public double actionLimitedRandomPlay(Info info) 
        {
            // ランダムなアクション回数
            int trialPlay = trialPlay_imp;

            ArrayList<Action> actList = new ArrayList<Action>();

            // シミュレータの初期化
            while(true)
            {
                // アクションリストの作成
                actList.clear();
                actList = info.makeActionLimitedList(actList);

                //Action randAct = new Action(info.player.dir);
                // ランダムなアクションを決定
                Action randAct = actList.get(random.nextInt(actList.size()));

                // アクション実行前の階層
                //int curfloor = info.player.curFloor;

                // 設定したアクションを実行
                playAction(randAct, info);

                // ゲームオーバー player.active == false
                // アクション実行前と階層が異なる
                // 視野内にモンスターがいない
                trialPlay -=1;
                if (info.player.active == false
                        || info.player.curFloor != curFloor
//                        || isCheckMonsterinPView(info) == false
                        || isCheckMonsterBeat(info) == true
                        || trialPlay <= 0) {
                    // これらの時は，評価値を計算し，値を返す
                    break;
                }
            }

            // 一定のターン経過したとき -> 評価値を計算，値を返す
            return calcEvaVal(info);
        }
        
        String logstr;
        String logsimurand;
        
        public double actionLimitedSemiRandomPlay(Info info) 
        {
            logstr = new String();
            logsimurand = new String();

            // ランダムなアクション回数
            int trialPlay = trialPlay_imp;

            ArrayList<Action> actList = new ArrayList<Action>();

            // シミュレータの初期化
            while(true)
            {
                // アクションリストの作成
                actList.clear();
                actList = info.makeActionLimitedList(actList);

                //Action randAct = new Action(info.player.dir);
                // ランダムなアクションを決定
                //Action randAct = actList.get(random.nextInt(actList.size()));
                
                RuleBasePlayer rbp = new RuleBasePlayer();
                Action randAct = new Action(info.player.dir);
                //randAct = rbp.semiRBP(info, actList);
                
                // アクション実行前の階層
                //int curfloor = info.player.curFloor;

                boolean randflag = false;
                
                
                
                int[] enpos = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1};
                
               
                
                int sumdam = 0;
                for(Enemy e : info.visibleEnemy)
                {
                    Point p = new Point(info.player.gridMapX, info.player.gridMapY);
                    if(Math.abs(p.x - e.gridMapX) < 2 && Math.abs(p.y - e.gridMapY) < 2 && e.active == true)
                    {
                        sumdam += e.attack;
                    }
                }
                
                if(sumdam != 0)
                {
                    if(info.player.hp - sumdam > 0)
                    {
                        boolean inflag = false;
                        
                        for(int dir = 0; dir < 9; dir++)
                        {
                            for(int i = 0; i < actList.size(); i++)
                            {
                                if(actList.get(i).action == Action.ATTACK && actList.get(i).dir == dir)
                                {
                                    if(trialPlay == 10)
                                    {
                                        inflag = true;
                                    }
                                    randAct = actList.get(i);
                                }
                            }
                        }
                        
                        if(inflag == true)
                        {
                            logsimurand += ("\t" + "in");
                        }
                    }
                    else
                    {
                        if(info.player.inventory.getInvItemNum(2) >= 1)
                        {
                            for(int i = 0; i < actList.size(); i++)
                            {
                                if(actList.get(i).action == Action.USE_ITEM && info.player.inventory.itemList.get(actList.get(i).itemIndex).id == 2)
                                {
                                    randAct = actList.get(i);
                                }
                            }
                        }
                        else
                        {
                            randAct = actList.get(random.nextInt(actList.size()));
                        }
                    }
                }
                else
                {
                    randAct = actList.get(random.nextInt(actList.size()));
                    randflag = true;
                }
                
                if(trialPlay == 10)
                {
                    logstr += (sumdam + "\t");
//                    System.out.println("player:(" + info.player.gridMapX + "," + info.player.gridMapY + ")");
//                    for(int e = 0; e < info.visibleEnemy.size(); e++)
//                    {
//                        System.out.println("e" + info.visibleEnemy.get(e).index + "(" + info.visibleEnemy.get(e).gridMapX + "," + info.visibleEnemy.get(e).gridMapY + ")" + 
//                                            info.visibleEnemy.get(e).hp + "/" + info.visibleEnemy.get(e).maxHp);
//                    }
                }
                
                if(randflag == true)
                {
                    logstr += "R-";
                }
                
                if(randAct.action == Action.ATTACK)
                {
                    logstr += "a" + (randAct.dir + 1) + ", "; 
                    //System.out.print("a" + randAct.dir + ", ");
                }
                else if(randAct.action == Action.USE_ITEM)
                {
                    logstr += "u(" + randAct.itemIndex + ")" + (randAct.dir + 1) + ", ";
                    //System.out.print("u(" + randAct.itemIndex + ")" + randAct.dir + ", ");
                }
                else if(randAct.action == Action.MOVE)
                {
                    logstr += "m" + (randAct.dir + 1) + ", ";
                    //System.out.print("m" + randAct.dir + ", ");
                }
                else if(randAct.action == Action.STAY)
                {
                    logstr += "p, ";
                    //System.out.print("m5");
                }
                
                //System.out.println("trialPlay" + trialPlay + " : patk=" + info.patknum);
                
                // 設定したアクションを実行
                playAction(randAct, info);
                
                // ゲームオーバー player.active == false
                // アクション実行前と階層が異なる
                // 視野内にモンスターがいない
                trialPlay -=1;
                if (info.player.active == false
                        || info.player.curFloor != curFloor
                        || info.player.curFloor == MyCanvas.TOPFLOOR
                        || isCheckMonsterinPView(info) == false
                        //|| isCheckMonsterBeat(info) == true
                        || trialPlay <= 0) {
                    // これらの時は，評価値を計算し，値を返す
                    break;
                }
            }

            // 一定のターン経過したとき -> 評価値を計算，値を返す
            return calcEvaVal(info);
        }
        
        
        
        public double calcEvaValAve(Action acts, Info info)
	{
		// 評価値の合計
		double sumEvaVal = 0.0;
		// 1アクションに対する試行回数
		int trialOneAct = 100;

                // rbpにinfoを通して状況を更新
                //rbp.ruleBasedOnly(info);
                
		// 引数として与えられたアクションを実行
		playAction(acts, info);
                
                
                
                // 以下の時，評価値を返す
                // プレイヤーが死んだとき
                // 階層が変化したとき
                // 敵が視界内から消えたとき
                if (info.player.active == false
                    || curFloor != info.player.curFloor
                    || isCheckMonsterinPView(info) == false
                    //|| isCheckMonsterBeat(info) == true
                    ) 
                {
                        //System.out.println("simu gameover");
                        // 評価値を計算し，値を返す
                        double breakval = calcEvaVal(info);
                        //System.out.println("break:" + breakval);
                        return breakval;
                }
                
                
                
//                System.out.println("pl:(" + info.player.gridMapX + "," + info.player.gridMapY + ")" + info.player.hp + "/" + info.player.maxHp + ", " + info.player.active);
//                for(int e = 0; e < info.visibleEnemy.size(); e++)
//                {
//                    System.out.println("e" + info.visibleEnemy.get(e).index + "(" + info.visibleEnemy.get(e).gridMapX + "," + info.visibleEnemy.get(e).gridMapY + ")" + 
//                                        info.visibleEnemy.get(e).hp + "/" + info.visibleEnemy.get(e).maxHp);
//                }
               
                for(int i = 0; i < trialOneAct; i++)
		{
			// 不明な盤面情報の補完
                    
                    
                    
                    

                        //System.out.println("simu:" + i);
			// ランダムにアクションをとる
			// 最終的な盤面における評価値が戻る
			// 場面のコピーを引数とする
			double val;

                        //val = randomPlay(info.clone());
                        //val = actionLimitedRandomPlay(info.clone());
                        val = actionLimitedSemiRandomPlay(info.clone());
                        
                        //System.out.println("");
                        
                        sumEvaVal += val;
                        
                        if(debug > 10) {
                            System.out.print("trial[" + i + "]:" + val);
                            System.out.print("\t" + logsimurand + "\t" + logstr + "\n");
                        }
                        
		}
                
                if(debug > 10) {
                    System.out.println("---");
                    System.out.println("sum:" + sumEvaVal);
                    System.out.println("ave:" + (sumEvaVal / (double)trialOneAct) + "\n");
                }
                
		return sumEvaVal / (double)trialOneAct;
	}

        public double mctsdepth(Action act, Info info, int deep)
        {
            if(deep == DEPTHLIMITED)
            {
                return calcEvaValAve(act, info.clone());
            }
            else
            {
                //int curfloor = info.player.curFloor;

                
                
                // info + act -> new info
                playAction(act, info);
                
                
//                System.out.println("pl:(" + info.player.gridMapX + "," + info.player.gridMapY + ")" + info.player.hp + "/" + info.player.maxHp + ", " + info.player.active);
//                for(int e = 0; e < info.visibleEnemy.size(); e++)
//                {
//                    System.out.println("e" + info.visibleEnemy.get(e).index + "(" + info.visibleEnemy.get(e).gridMapX + "," + info.visibleEnemy.get(e).gridMapY + ")" + 
//                                        info.visibleEnemy.get(e).hp + "/" + info.visibleEnemy.get(e).maxHp);
//                }
                
                
                // 以下の時，評価値を返す
                // プレイヤーが死んだとき
                // 階層が変化したとき
                // 敵が視界内から消えたとき
                if (info.player.active == false
                    || curFloor != info.player.curFloor
                    || isCheckMonsterinPView(info) == false
                    //|| isCheckMonsterBeat(info) == true
                    ) 
                {
                        //System.out.println("simu gameover");
                        // 評価値を計算し，値を返す
                        return calcEvaVal(info);
                }
                
                // 上の条件に当てはまらないとき，さらに行動を展開
                ArrayList<Action> actList = new ArrayList<Action>();
		actList = info.makeActionLimitedList(actList);
                
                // プレイアウトの評価値の平均を得る
		for(int index = 0; index < actList.size(); index++)
		{
                    if(debug > 10) {
                        System.out.println("/*----- act[" + acthistindex + "->" + index + "] -----*/");
                        System.out.print("act" + acthistindex + " : " + acthist.action);
                        if(acthist.action == Action.USE_ITEM)
                        {
                            System.out.print(", itemindex(" + acthist.itemIndex + ")");
                        }
                        System.out.print(", dir(1~9) : " + acthist.dir);
                        System.out.println("\n" + "↓");
                        System.out.print("act" + acthistindex + "->" + index + " : " + actList.get(index).action);
                        if(actList.get(index).action == Action.USE_ITEM)
                        {
                            System.out.print(", itemindex(" + actList.get(index).itemIndex + ")");
                        }
                        System.out.println(", dir(1~9) : " + (actList.get(index).dir + 1));
                    }
                    
                    actList.get(index).evaVal = mctsdepth(actList.get(index), info.clone(), deep + 1);
		}
                
                // 評価値の最も高いアクションをListの中から探索
		double maxEvaVal = actList.get(0).evaVal;
		for(int index = 1; index < actList.size(); index++)
		{
			if(maxEvaVal < actList.get(index).evaVal)
			{
				maxEvaVal = actList.get(index).evaVal;
			}
		}
                return maxEvaVal;
            }
        }
        
        int acthistindex;
        Action acthist;
        
	public Action DLMCTS(Info info)
	{
		// アクションリストの作成
		ArrayList<Action> actList = new ArrayList<Action>();

		// 取りうるアクションのリストアップ
		actList = info.makeActionLimitedList(actList);

		//System.out.println("maked Action List, player(" + info.player.gridMapX + "," + info.player.gridMapY + ")");

		// 各行動の評価値の平均を得る
		for(int index = 0; index < actList.size(); index++)
		{
			//actList.get(index).evaVal = calcEvaValAve(actList.get(index), info.clone());
                        
                        acthistindex = index;
                        acthist = actList.get(acthistindex);
                        if(debug > 10) {
                            System.out.println("/*----- act[" + index + "] -----*/");
                        }
                        
                        // 深さ分展開
                        actList.get(index).evaVal = mctsdepth(actList.get(index), info.clone(), 1);
		}

                if(debug > 0)
                {
                    System.out.println("simulation");
                    for(int index = 0; index < actList.size(); index++)
                    {
                            System.out.print(index + " -> act : " + actList.get(index).action);
                            if(actList.get(index).action == Action.USE_ITEM)
                            {
                                int iid = info.player.inventory.itemList.get(actList.get(index).itemIndex).id;
                                System.out.print(", itemid(" + iid + ")");
                            }
                            // 評価済み
                            System.out.print(", dir(1~9) : " + actList.get(index).dir);
                            System.out.println(", eval : " + actList.get(index).evaVal);
                    }
                }

		// 評価値の最も高いアクションをListの中から探索
		double maxEvaVal = actList.get(0).evaVal;
		int maxIndex = 0;
		for(int index = 1; index < actList.size(); index++)
		{
			if(maxEvaVal < actList.get(index).evaVal)
			{
				maxEvaVal = actList.get(index).evaVal;
				maxIndex = index;
			}
		}

		return actList.get(maxIndex);
	}
        
        int actcount = 0;
        
        // 
        public Action makeAction(Info info)
        {
                Action act = new Action(info.player.dir);

                sfd = info.player.inventory.getInvItemNum(1);
                spt = info.player.inventory.getInvItemNum(2);
                sar = info.player.inventory.getInvItemNum(3);
                sst = info.player.inventory.getInvItemNum(4);
                
                info.patknum = 0;
                info.eatknum = 0;
                countAtk(info);
                
                curFloor = info.player.curFloor;
                
                stturn = info.floorturn[curFloor];
                
                if(debug > 0)
                {
                    System.out.println("----------------------floor:" + info.player.curFloor + "-------------------------");
                    for(int e = 0; e < info.visibleEnemy.size(); e++)
                    {
                        System.out.println("e" + info.visibleEnemy.get(e).index + "(" + info.visibleEnemy.get(e).gridMapX + "," + info.visibleEnemy.get(e).gridMapY + ")" + 
                                            info.visibleEnemy.get(e).hp + "/" + info.visibleEnemy.get(e).maxHp);
                    }
                    for(int y = 0; y < info.mapsizeY; y++)
                    {
                        for(int x = 0; x < info.mapsizeX; x++)
                        {
                            boolean flag = false;
                            if(x == info.player.gridMapX && y == info.player.gridMapY)
                            {
                                System.out.print("p");
                                flag = true;
                            }

                            for(int index = 0; index < info.visibleEnemy.size(); index++)
                            {
                                if(x == info.visibleEnemy.get(index).gridMapX && y == info.visibleEnemy.get(index).gridMapY && info.visibleEnemy.get(index).active == true)
                                {
                                    System.out.print("e");
                                    flag = true;
                                }
                            }

                            if(flag == false)
                            {
                                if(info.map[y][x] == 0)
                                {
                                    System.out.print("_");
                                }
                                else
                                {
                                    System.out.print(" ");
                                }
                            }

                            System.out.print(" ");
                        }
                        System.out.println();
                    }
                    System.out.println("player:(" + info.player.gridMapX + "," + info.player.gridMapY + ")");
                    System.out.println("Lv:" + info.player.level);
                    System.out.println("Hp:" + info.player.hp + "/" + info.player.maxHp);
                    System.out.println("sp:" + info.player.satiety);

                    // アイテムの出力
                    for(int i = 0; i < info.player.inventory.itemList.size(); i++)
                    {
                        //
                        System.out.println("item" + i + ":" + info.player.inventory.itemList.get(i).name + "(" + info.player.inventory.itemList.get(i).usageCount + ")");
                    }
                }
                
		act = DLMCTS(info);

		// シミュレーション時にdir:1~9
		// 1~9 -> 0~8
		act.dir--;

                
                if(debug > 0)
                {
                    System.out.println("action:" + act.action);
                    if(act.action == Action.USE_ITEM)
                    {
                        System.out.println("itemindex:" + act.itemIndex);
                        System.out.println("itemid:" + info.player.inventory.itemList.get(act.itemIndex).id);
                    }
                    System.out.println("dir(1~9):" + (act.dir + 1));
                    System.out.println("evaVal:" + act.evaVal);
                    System.out.println();
                }

		return act;
        }
}
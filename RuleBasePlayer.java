import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Stack;

public class RuleBasePlayer implements Agent, Cloneable
{
	private Random random;

	private int[] difX = new int[] {-1, 0, 1,-1, 0, 1,-1, 0, 1};
	private int[] difY = new int[] { 1, 1, 1, 0, 0, 0,-1,-1,-1};
        
        private int[] difsX = {1 , 0 , -1 , 0};
        private int[] difsY = {0 , 1 ,  0 ,-1};

        private int px;
        private int py;
        
        private int mx;
        private int my;

        // 初期値：-1
        // 戦闘のルールに入る：1
        // その他のルールに入る：0
        private int battleflag;
        private int battleflaghist; // 1行動前のbattleflag
        
	// 行ったことのあるなし
	private int arriveMap[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];

	// 敵の位置による攻撃範囲を示す
	// プレイヤは中心(5)
	private int[][] dArea = {{0, 0, 1, 0, 0, 1, 1, 1, 1},
					 {0, 0, 0, 0, 0, 0, 1, 1, 1},
					 {1, 0, 0, 1, 0, 0, 1, 1, 1},
					 {0, 0, 1, 0, 0, 1, 0, 0, 1},
					 {0, 0, 0, 0, 0, 0, 0, 0, 0},
					 {1, 0, 0, 1, 0, 0, 1, 0, 0},
					 {1, 1, 1, 0, 0, 1, 0, 0, 1},
					 {1, 1, 1, 0, 0, 0, 0, 0, 0},
					 {1, 1, 1, 1, 0, 0, 1, 0, 0}};
        
        // 階段が進行方向にある際の回避方向
        private int[][] savoid = {{1,3}, {0,2}, {1,5}, {0,6}, {4,4}, {2,8}, {3,7}, {6,8}, {5,7}}; 
        
        // 目的地
	private Point target;
	// 経路の候補
	// popして次の経路を決定
	private Stack<Point> stack;
	// 短期的な経路の履歴
	// 行き止まりの際（stackをpopできない）にはこちらをpopする
	private Stack<Point> stackHist;
	// 長期的な経路の履歴
	// popしたすべての座標をもつ
	private ArrayList<Point> allstackHist;
        // 訪問済み部屋リスト
        private ArrayList<Integer> hrList;
        
        // 前回の履歴
	private Point history;
        
        // 目的地に到達するため経由が必要な目的地
        private Point subtarget;
        
        // 通路移動しているときのターゲット
        private Point ptarget;
        
        private int playerFloor;
        
        private int floorItem;
        
        private int seq; // シーケンス
        private int seqHist; // １つ前ののシーケンス
        private int histDir; // どの方向に向かっていたか
        private int roomHist; // 現在の部屋の前に通過した部屋番号
        private int firstRoomID; // 初期生成の部屋番号
        private int lastRoomID; // 現在の部屋番号
        private int stairRoomID; // 階段の存在する部屋番号
        
        // ターゲットの隣接する部屋
        private int targetRoomID;
        
        // 隣接している敵のリスト
        private ArrayList<AroundEnemy> aroundEnemy;
	// 目的地のリスト
	private ArrayList<Destination> destList;
        
        // 通路の入り口（０），出口（１）
        private int[] pathHist = new int[2];
                
        //存在しているノードのリスト
        private ArrayList<node> allnode;
        //node Library ノード存在しているか確認と参照配列
        private node[] nodes;
        
        // インナークラス
        //public class node
        public static class node
        {
            int ID;
            public ArrayList<node> connected = new ArrayList<node>();
            public node[] reachRoot = new node[1500];
            public int[] path = new int[1500];
            
            public node(int ID_){
                ID = ID_;
            }
        }
        
        public int getStairRoomID()
        {
            return stairRoomID;
        }        
        
        // クローン
        public RuleBasePlayer clone()
        {
                RuleBasePlayer rulebaseplayer = new RuleBasePlayer();
                
                try
                {
			//rulebaseplayer = (RuleBasePlayer)super.clone();
                        
                        rulebaseplayer.px = this.px;
                        rulebaseplayer.py = this.py;
                        
                        rulebaseplayer.mx = this.mx;
                        rulebaseplayer.my = this.my;
                        
                        for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
                        {
                            for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
                            {
                                    rulebaseplayer.arriveMap[y][x] = this.arriveMap[y][x];
                            }
                        }
                        
                        rulebaseplayer.target = new Point(this.target.x, this.target.y);
                        // stack deep copy
                        rulebaseplayer.stack = stackDeepCopy(this.stack);
                        // stackHist deep copy
                        rulebaseplayer.stackHist = stackDeepCopy(this.stackHist);
                        // allstackHist deep copy
                        rulebaseplayer.allstackHist = alistPointDeepCopy(this.allstackHist);
                        // hrList deep copy
                        rulebaseplayer.hrList = alistIntDeepCopy(this.hrList);
                        
                        rulebaseplayer.history = new Point(this.history.x, this.history.y);
                        rulebaseplayer.subtarget = new Point(this.subtarget.x, this.subtarget.y);
                        rulebaseplayer.ptarget = new Point(this.ptarget.x, this.ptarget.y);
                        
                        rulebaseplayer.playerFloor = this.playerFloor;
                        rulebaseplayer.floorItem = this.floorItem;
                        rulebaseplayer.seq = this.seq;
                        rulebaseplayer.seqHist = this.seqHist;
                        rulebaseplayer.histDir = this.histDir;
                        rulebaseplayer.roomHist = this.roomHist;
                        rulebaseplayer.firstRoomID = this.firstRoomID;
                        rulebaseplayer.lastRoomID = this.lastRoomID;
                        rulebaseplayer.stairRoomID = this.stairRoomID;
                        rulebaseplayer.targetRoomID = this.targetRoomID;
                        
                        rulebaseplayer.pathHist[0] = this.pathHist[0];
                        rulebaseplayer.pathHist[1] = this.pathHist[1];
                        
                        // nodes deep copy
                        for(int index = 0; index < this.nodes.length; index++)
                        {
                            // id
                            if(this.nodes[index] != null)
                            {
                                //rulebaseplayer.nodes[index].ID = this.nodes[index].ID;
                                rulebaseplayer.nodes[index] = new node(index);
                            }
                        }
                        for(int index = 0; index < this.nodes.length; index++)
                        {
                            if(this.nodes[index] == null)
                            {
                                continue;
                            }

                            // connected
                            for(int rn = 0; rn < this.nodes[index].connected.size(); rn++)
                            {
                                rulebaseplayer.nodes[index].connected.add(rulebaseplayer.nodes[this.nodes[index].connected.get(rn).ID]);
                            }
                            // reachroot
                            for(int rn = 0; rn < this.nodes[index].reachRoot.length; rn++)
                            {
                                if(this.nodes[index].reachRoot[rn] != null)
                                {
                                    rulebaseplayer.nodes[index].reachRoot[rn] = rulebaseplayer.nodes[this.nodes[index].reachRoot[rn].ID];
                                }
                            }
                            // path
                            for(int rn = 0; rn < this.nodes[index].path.length; rn++)
                            {
                                rulebaseplayer.nodes[index].path[rn] = this.nodes[index].path[rn];
                            }
                        }
                        
                        // allnode deep copy
                        for(int index = 0; index < this.allnode.size(); index++)
                        {
                            int rn = this.allnode.get(index).ID;
                            rulebaseplayer.allnode.add(rulebaseplayer.nodes[rn]);
                        }
                        
                        for (node temp : rulebaseplayer.allnode)
                        {
                            AppendNodeRoot(temp);
                        }
		}
                catch(Exception e)
                {
			e.printStackTrace();
		}
                return rulebaseplayer;
        }
        
        public Stack<Point> stackDeepCopy(Stack<Point> stack)
        {
            Stack<Point> stcp = new Stack<Point>();
            
            for(int index = 0; index < stack.size(); index++)
            {
                Point sp = stack.get(index);
                stcp.add(new Point(sp.x, sp.y));
            }
            
            return stcp;
        }
        
        public ArrayList<Point> alistPointDeepCopy(ArrayList<Point> alist)
        {
            ArrayList<Point> alistcp = new ArrayList<Point>();
            
            for(int index = 0; index < alist.size(); index++)
            {
                Point tmp = alist.get(index);
                alistcp.add(new Point(tmp.x, tmp.y));
            }
            
            return alistcp;
        }
        
        public ArrayList<Integer> alistIntDeepCopy(ArrayList<Integer> alist)
        {
            ArrayList<Integer> alistcp = new ArrayList<Integer>();
            
            for(int index = 0; index < alist.size(); index++)
            {
                int tmp = alist.get(index);
                alistcp.add(tmp);
            }
            
            return alistcp;
        }
        
        public void sysoutput()
        {
            System.out.println("histry:(" + history.x + ", " + history.y + ")");
        }
        
	public RuleBasePlayer()
	{
		random = new Random();

                battleflag = -1;
                battleflaghist = -1;
                
                // 階層の更新
		playerFloor = -1;
                
                floorItem = 0;
                
                history = new Point(-1, -1);
                
		// arriveMapの初期化
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				arriveMap[y][x] = 0;
			}
		}
                
                // 目的地
                target = new Point(-1, -1);
                //setTarget(new Point(-1, -1));
                // 経路の候補
                // popして次の経路を決定
                stack = new Stack<Point>();
                // 短期的な経路の履歴
                // 行き止まりの際（stackをpopできない）にはこちらをpopする
                stackHist = new Stack<Point>();
                // 長期的な経路の履歴
                // popしたすべての座標をもつ
                allstackHist = new ArrayList<Point>();
                // 訪問済み部屋リスト
                hrList = new ArrayList<Integer>();
                
                subtarget = new Point(-1, -1);
                
                seq = 0;
                seqHist = -1;
                histDir = -1;
                roomHist = -1;
                firstRoomID = -1;
                lastRoomID = -1;
                stairRoomID = -1;
                
                targetRoomID = -1;
                
                ptarget = new Point(-1, -1);
                
                //存在しているノードのリスト
                allnode = new ArrayList<node>();
                //node Library ノード存在しているか確認と参照配列
                nodes = new node[1500];
                
                pathHist = new int[2];
                
                // 隣接している敵のリスト
		aroundEnemy = new ArrayList<AroundEnemy>();
		// 目的地のリスト
		destList = new ArrayList<Destination>();
	}
        
        public void initPassSearch(int pf, Point topleft)
        {
                // 階層の更新
		playerFloor = pf;
                
                floorItem = 0;
                
                history = new Point(-1, -1);
                
		// arriveMapの初期化
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				arriveMap[y][x] = 0;
			}
		}

                // 目的地
                target = new Point(-1, -1);
                //setTarget(new Point(-1, -1));
                // 経路の候補
                // popして次の経路を決定
                stack.clear();
                // 短期的な経路の履歴
                // 行き止まりの際（stackをpopできない）にはこちらをpopする
                stackHist.clear();
                // 長期的な経路の履歴
                // popしたすべての座標をもつ
                allstackHist.clear();
                // 訪問済み部屋リスト
                hrList.clear();
                
                subtarget = new Point(-1, -1);
                
                seq = 0;
                seqHist = -1;
                histDir = -1;
                roomHist = -1;
                firstRoomID = -1;
                lastRoomID = calcRoomID(topleft);
                stairRoomID = -1;
                
                targetRoomID = -1;
                
                ptarget = new Point(-1, -1);
                
                allnode.clear();
                nodes = new node[1500];
                
                pathHist = new int[2];
                
                //System.out.println("init");
        }
        
        // 更新
        public void update(Info info)
        {
                // infoから現状の更新
                
                // historyとの差分
                int ptype = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
                // 更新
		arriveMap[info.player.gridMapY][info.player.gridMapX]++;
                
                // 生成直後のとき，階段が部屋の中にあるか
                if (history.x == -1 && history.y == -1)
                {
                        // 現在の部屋番号
                        int roomid = calcRoomID(info.currentRTopLeft);
                    
                        if(stairRoomID == -1 && isStairCheck(info) == true)
                        {
                            stairRoomID = roomid;
                        }
                }
                
		// 階層が変化したとき
		if(playerFloor != info.player.curFloor && ptype == -1)
		{
			//System.out.println("pf:" + playerFloor);
                        //System.out.println("pcf:" + MyCanvas.floorNumber);
                        initPassSearch(info.player.curFloor, info.currentRTopLeft);
                        
                        // 現在の部屋番号
                        int roomid = calcRoomID(info.currentRTopLeft);
                    
                        if(stairRoomID == -1 && isStairCheck(info) == true)
                        {
                            stairRoomID = roomid;
                        }
                        
                        addhrList(info);
                        if(nodes[roomid] == null)
                        {
                            nodes[roomid] = new node(roomid);
                            //System.out.println("new node(pop) : " + roomid);
                            allnode.add(nodes[roomid]);
                        }
                        updateTarget(info);
                        
                        firstRoomID = calcRoomID(info.currentRTopLeft);  
		}
                // 階層に変化なし
                else
                {
                    // 初期生成直後ではないとき，historyが存在するとき
                    if (history.x != -1 && history.y != -1)
                    {
                        // 部屋に到着したとき
                        // 現在：部屋，1行動前：通路
                        if(ptype == -1 && diagonalCheck(info.map, history.x, history.y) != -1) {
                            // 現在の部屋番号
                            int roomid = calcRoomID(info.currentRTopLeft);
                            //System.out.println("seq:" + seq);
                            
                            if(stairRoomID == -1 && isStairCheck(info) == true)
                            {
                                stairRoomID = roomid;
                            }
                            
                            // 探索終了前
                            // かつ，直前までいた部屋と現在の部屋が同じ（通路の途中で引き返したとき）
                            if(seq <= 2 && lastRoomID == roomid){
                                // 特に更新なし？
                            }
                            // 探索終了前
                            // かつ，直前までいた部屋と現在の部屋が同じ（通路の途中で引き返したとき）でないとき
                            else if(seq <= 2 && lastRoomID != roomid)
                            {
                                // 過去の部屋履歴を更新
                                roomHist = lastRoomID;
                                // 現在の部屋のIDに更新
                                lastRoomID = roomid;

                                // 通路の履歴を更新
                                // 直前に通過した部分を追加
                                pathHist[1] = calcRoomID(history);

                                // ツリーの作成
                                //int roomid = calcRoomID(info.currentRTopLeft);
                                addhrList(info);
                                if(nodes[roomid] == null)
                                {
                                    nodes[roomid] = new node(roomid);
                                    //System.out.println("new node(search) : " + roomid);
                                    allnode.add(nodes[roomid]);
                                }
                                
                                // ほかの部屋が見つかり次第，ノードを接続
                                // ノードがつながっていないとき
                                // connectedのListの中にnodes[roomHist]が存在しないとき：-1
                                if(nodes[roomid].connected.indexOf(nodes[roomHist]) == -1)
                                {
                                    nodes[roomid].connected.add(nodes[roomHist]);
                                    // 通路をつなげる，通路のおわり（１マス手前）
                                    nodes[roomid].path[nodes[roomHist].ID] = pathHist[1];
                                }
                                if(nodes[roomHist].connected.indexOf(nodes[roomid]) == -1)
                                {
                                    nodes[roomHist].connected.add(nodes[roomid]);
                                    // 通路をつなげる，通路の始まり
                                    nodes[roomHist].path[nodes[roomid].ID] = pathHist[0];
                                }
                                // connectedがない -> appendrootnodeでノード間の接続がされない

                                // targetの更新
                                //System.out.println("roomid:" + roomid);
                                updateTarget(info);
                                
                                // nodesを更新後，通路の履歴を初期化
                                pathHist = new int[2];
                            }
                            
                            // 探索終了後
                            if(seq >= 3)
                            {
                                // 過去の部屋履歴を更新
                                roomHist = lastRoomID;
                                // 現在の部屋のIDに更新
                                lastRoomID = roomid;
                                    
                                // 通路の履歴を更新
                                // 直前に通過した部分を追加
                                pathHist[1] = calcRoomID(history);

                                // 仮に，未探索の部屋に到達してしまった場合
                                if(nodes[roomid] == null)
                                {
                                    nodes[roomid] = new node(roomid);
                                    //System.out.println("new node(seq>=3) : " + roomid);
                                    allnode.add(nodes[roomid]);
                                    
                                    if(nodes[roomid].connected.indexOf(nodes[roomHist]) == -1)
                                    {
                                        nodes[roomid].connected.add(nodes[roomHist]);
                                        // 通路をつなげる，通路のおわり（１マス手前）
                                        nodes[roomid].path[nodes[roomHist].ID] = pathHist[1];
                                    }
                                    if(nodes[roomHist].connected.indexOf(nodes[roomid]) == -1)
                                    {
                                        nodes[roomHist].connected.add(nodes[roomid]);
                                        // 通路をつなげる，通路の始まり
                                        nodes[roomHist].path[nodes[roomid].ID] = pathHist[0];
                                    }
                                    
                                    seq = 0;
                                    updateTarget(info);
                                    
                                    // nodesを更新後，通路の履歴を初期化
                                    pathHist = new int[2];
                                }
                                // スタックが空でない
                                else if(!stack.isEmpty())
                                {
                                    // 現在の部屋に，未探索の通路がある場合
                                    if (isinCurRoom(new Point[]{info.currentRTopLeft, info.currentRButtomRight, new Point(stack.peek().x, stack.peek().y)}, info.map) == true) 
                                    {
                                        //System.out.println("target -> stack.peek");
                                        //target = stack.peek();
                                        setTarget(stack.peek(), calcRoomID(info.currentRTopLeft));
                                        seq = 2;
                                        seqHist = 1;
                                    }
                                    else
                                    {
                                        node n = allnode.get(random.nextInt(allnode.size()));
                                        // 探索済みでのターゲットの更新
                                        // 通路の座標を目的地に設定
                                        while(true)
                                        {
                                            // 現在の部屋と同じ場合は，再生成
                                            if(roomid == n.ID)
                                            {
                                                n = allnode.get(random.nextInt(allnode.size()));
                                            }
                                            else
                                            {
                                                break;
                                            }
                                        }
                                        // id -> point
                                        int tg = nodes[roomid].path[nodes[roomid].reachRoot[n.ID].ID];
                                        //target = new Point(tg / 30, tg % 30);
                                        setTarget(new Point(tg / 30, tg % 30), roomid);
                                        //System.out.println("roomid:" + roomid + " >>tg_id:" + n.ID + " (Target Point" + target.x +","+ target.y + ")");
                                    }
                                }
                                else
                                {
                                    for (node temp : allnode)
                                    {
                                        AppendNodeRoot(temp);
                                    }

                                    // 階段発見済，現在の部屋と異なるとき -> その部屋に向かう
                                    // 階段未発見 -> ランダムムーブ
                                    node n = allnode.get(random.nextInt(allnode.size()));
                                    if(stairRoomID != -1 && stairRoomID != roomid)
                                    {
                                        for(int index = 0; index < allnode.size(); index++)
                                        {
                                            if(allnode.get(index).ID == stairRoomID)
                                            {
                                                n = allnode.get(index);
                                                break;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        // 探索済みでのターゲットの更新
                                        // 通路の座標を目的地に設定
                                        while(true)
                                        {
                                            // 現在の部屋と同じ場合は，再生成
                                            if(roomid == n.ID)
                                            {
                                                n = allnode.get(random.nextInt(allnode.size()));
                                            }
                                            else
                                            {
                                                break;
                                            }
                                        }
                                    }

                                    int tg; // ターゲット
                                    while(true)
                                    {
                                        try {
                                            tg = nodes[roomid].path[nodes[roomid].reachRoot[n.ID].ID];
                                            break;
                                        } catch (NullPointerException e) {
                                            System.out.println("NullPointerException!!");
                                            System.out.println("roomid:" + roomid);
                                            System.out.println("n.ID:" + n.ID);
                                            System.out.println("player:(" + info.player.gridMapX + "," + info.player.gridMapY + ")");
                                            // 現在のmap表示
                                            for(int y = 0; y < info.mapsizeY; y++)
                                            {
                                                for(int x = 0; x < info.mapsizeX; x++)
                                                {
                                                    System.out.print(info.map[y][x] + " ");
                                                }
                                                System.out.println();
                                            }
                                            
                                            // stack
                                            for(int index = 0; index < stack.size(); index++)
                                            {
                                                System.out.println("stack[" + index + "]:(" + stack.get(index).x + ","  + stack.get(index).y + ")");
                                            }
                                            // stackHist
                                            for(int index = 0; index < stackHist.size(); index++)
                                            {
                                                System.out.println("stackHist[" + index + "]:(" + stackHist.get(index).x + ","  + stackHist.get(index).y + ")");
                                            }
                                            // hrList
                                            for(int index = 0; index < hrList.size(); index++)
                                            {
                                                System.out.println("hrList[" + index + "]:" + hrList.get(index));
                                            }

                                            // node
                                            for(int index = 0; index < allnode.size(); index++)
                                            {
                                                // 
                                                System.out.println("node[" + index + "]:" + allnode.get(index).ID);
                                                System.out.print("connected:");
                                                for(int conn = 0; conn < allnode.get(index).connected.size(); conn++)
                                                {
                                                    System.out.print("(" + allnode.get(index).connected.get(conn).ID + ")");
                                                }
                                                System.out.println();
                                                System.out.print("path:");
                                                for(int pn = 0; pn < allnode.get(index).path.length; pn++)
                                                {
                                                    // pathが初期化されてから更新されていないとき
                                                    if(allnode.get(index).path[pn] != 0)
                                                    {
                                                        System.out.print("(" + pn/30 + "," + pn%30 + ")");
                                                    }
                                                }
                                                System.out.println();
                                            }
                                            System.out.println();
                                            
                                            System.exit(0);
                                        }
                                    }
                                    
                                    
                                    // 部屋同士の接続関係の出力
                                    //System.out.println("stair room : " + stairRoomID);
                                    //System.out.println("roomid : " + roomid + " >> tg_id : " + n.ID);

                                    // id -> point
                                    tg = nodes[roomid].path[nodes[roomid].reachRoot[n.ID].ID];
                                    //target = new Point(tg / 30, tg % 30);
                                    setTarget(new Point(tg / 30, tg % 30), roomid);
                                    //System.out.println("roomid:" + roomid + " >>tg_id:" + n.ID + " (Target Point" + target.x +","+ target.y + ")");
                                }
                            }
                        }
                        // 通路に到達したとき
                        else if (ptype != -1 && diagonalCheck(info.map, history.x, history.y) == -1) {
                            // 通路の履歴を更新，入口
                            pathHist[0] = calcRoomID(new Point(info.player.gridMapX, info.player.gridMapY));

                            // ptargetの更新
                            updatePTarget(info);
                        }
                        else{ 
                            // 止まる
                            // 
                        }
                    }
                }
        }
        
        private int stepsetup_ = 100;
        private MonteCarloPlayer mcp = new MonteCarloPlayer();
        
        public void monteCarloStepSetup(int A)
        {
            stepsetup_ = A;
            mcp.stepSetup(stepsetup_);
        }
        
        public Point getTarget()
        {
            return new Point(target.x, target.y);
        }
        
        public void setTarget(Point p, int tgr)
        {
            // ターゲットの更新
            target = p;
            // ターゲットの隣接する部屋
            targetRoomID = tgr;
        }
        
	public Action ruleBased(Info info)
	{
		// infoから内部変数の更新
                update(info);
                
                Action ruleBesedAct = new Action(info.player.dir);

		// 隣接している敵のリスト
		aroundEnemy.clear();
		// 目的地のリスト
		destList.clear();

		if(is1gridMosCheck(info, aroundEnemy) == true)
		{
			//System.out.println("is1grid");

                        //RuleBasePlayer rbp_dcopy = this.clone();
                        //ruleBesedAct = mcp.makeAction(info, rbp_dcopy);
                        ruleBesedAct = mcp.makeAction(info);
		}
		else if(is2gridOverMosCheck(info, aroundEnemy) == true)
		{
//                        //RuleBasePlayer rbp_dcopy = this.clone();
//                        //ruleBesedAct = mcp.makeAction(info, rbp_dcopy);
//                        ruleBesedAct = mcp.makeAction(info);
                        
			//System.out.println("is2grid");

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
                        isStairCheck(info, destList);
                        int stindex = 0;
                        int stdist = 1500;
                        for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 5)
				{
                                        stindex = index;
                                        stdist = destList.get(index).dis;
                                        break;
				}
			}
                        
                        // 最下層であり，かつ，敵がいるが階段のほうが近いとき
                        if(info.player.curFloor == MyCanvas.TOPFLOOR - 1 && distEnemy > stdist)
                        {
                                ruleBesedAct.action = Action.MOVE;
				// リストの中から抽出
				int sDir = destList.get(stindex).dir;
				ruleBesedAct.difPos.x = difX[sDir];
				ruleBesedAct.difPos.y = difY[sDir];
                        }
                        else if(distEnemy <= 5)
			{
				//RuleBasePlayer rbp_dcopy = this.clone();
                                //ruleBesedAct = mcp.makeAction(info, rbp_dcopy);
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

			//System.out.println("rulebased-3");
		}
		// 部屋の中にアイテムが落ちている->目的地をアイテムに設定，１マス近づく
		else if(isItemCheck(info, destList) == true && info.player.inventory.getInvItemNum() != Inventory.MAX_INV)
		{
			for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 4)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					int sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

                                        // 階段回避
                                        stairAvoid(info, ruleBesedAct);
                                        
					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}
                                        
                                        // 移動先がアイテムと重複するとき
                                        if(info.player.gridMapX + ruleBesedAct.difPos.x == destList.get(index).p.x && 
                                           info.player.gridMapY + ruleBesedAct.difPos.y == destList.get(index).p.y)
                                        {
                                            floorItem++;
                                        }

					break;
				}
			}

			//System.out.println("rulebased-4");
		}
		// hpが7割以下であり，満腹度が4割以上
		else if(info.player.hp < info.player.maxHp * 0.7 && info.player.satiety > 40)
		{
			ruleBesedAct.action = Action.STAY;

			//System.out.println("rulebased-6");
		}
                // おなかがすいた
		else if(info.player.satiety < 70)
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
					// 階段を発見している
                                            // その部屋へ
                                        // 階段未発見
                                            // 目的地の探索・移動
					
                                        ruleBesedAct = passSearchMove(info);
				}
			}

			//System.out.println("rulebased-5");
		}
		// 部屋の中に階段がある
		else if((floorItem >= 4 || isFloorSearch() == true) && isStairCheck(info, destList) == true)
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

			//System.out.println("rulebased-7");
		}
		// いったことのない通路を目的地とする
		else
		{
			// 目的地の探索・移動
			ruleBesedAct = passSearchMove(info);

			//System.out.println("rulebased-8");

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
                        
                        // 行動の履歴
                        history.x = info.player.gridMapX;
                        history.y = info.player.gridMapY; 
		}
                
                return ruleBesedAct;
	}
        
        public class EnemyInf
        {
            int[][] infmap;
            AroundEnemy aenemy;
            
            public EnemyInf(int[][] map, boolean[][] pcur, AroundEnemy ae)
            {
                infmap = new int[pcur.length][pcur[0].length];
                for(int y = 0; y < pcur.length; y++)
                {
                    for(int x = 0; x < pcur[y].length; x++)
                    {
                        //
                        infmap[y][x] = (map[y][x] != 1 && pcur[y][x] == true) ? calcDistance(new Point(x, y), new Point(ae.e.gridMapX, ae.e.gridMapY)) : -1;
                    }
                }
                
                aenemy = ae;
            }
        }
        
        public int calcDistance(Point p1, Point p2)
        {
            int dx = Math.abs(p1.x - p2.x);
            int dy = Math.abs(p1.y - p2.y);
            return (dx >= dy) ? dx : dy;
        }
        
        public boolean isCheckEnemyInfDist(ArrayList<EnemyInf> enemyInfList, Point p, int diffep)
        {
            int count = 0;
            
            for(int index = 0; index < enemyInfList.size(); index++)
            {
                if(enemyInfList.get(index).infmap[p.y][p.x] == diffep)
                {
                    count++;
                }
            }
            
            return count >= 2 ? true : false;
        }
        
        public Action semiRBP(Info info, ArrayList<Action> actList)
        {
                Action ruleBesedAct = new Action(info.player.dir);
                
		// 隣接している敵のリスト
		aroundEnemy.clear();
		
                // 敵が隣にいる
		if(is1gridMosCheckfsimu(info, aroundEnemy) == true)
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
                        int targethp = aroundEnemy.get(0).e.hp;
			for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				if(aroundEnemy.get(eNum).e.hp <= info.player.attack &&
				   maxSumDamage > sumDamage - aroundEnemy.get(eNum).e.attack)
				{
					targetEnemyIndex = eNum;
					maxSumDamage = sumDamage - aroundEnemy.get(eNum).e.attack;
				}
                                else
                                {
                                    if(targethp > aroundEnemy.get(eNum).e.hp)
                                    {
                                        targetEnemyIndex = eNum;
                                        targethp = aroundEnemy.get(eNum).e.hp;
                                    }
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
						   info.map[info.player.gridMapY + difY[dir]][info.player.gridMapX + difX[dir]] != 1)
						{
							mList.add(dir);
						}
					}

					// 攻撃されない，移動可能なマスがない
					if(mList.size() == 0)
					{
						ruleBesedAct = actList.get(random.nextInt(actList.size()));
					}
					// 移動可能なマスが存在
					else
					{
						// その方向に移動
						ruleBesedAct.action = Action.MOVE;
						ruleBesedAct.difPos.x = difX[mList.get(0)];
						ruleBesedAct.difPos.y = difY[mList.get(0)];
					}
				}
			}
		}
		else
                {
                    ruleBesedAct = actList.get(random.nextInt(actList.size()));
                }
                
//                for(int actn = 0; actn < actList.size(); actn++)
//                {
//                    if(actList.get(actn).action == ruleBesedAct.action && )
//                    {
//                        
//                    }
//                }
                
                return ruleBesedAct;
        }
        
        public Action ruleBasedOnly(Info info)
	{
//                System.out.println("player:(" + info.player.gridMapX + "," + info.player.gridMapX + ")");
//                System.out.println("mc-floor:" + MyCanvas.floorNumber);
//                System.out.println("in-floor:" + info.player.curFloor);
//                System.out.println("seq:" + seq);
                
            
                // 更新
                update(info);
                
                
                Action ruleBesedAct = new Action(info.player.dir);
                
		// 隣接している敵のリスト
		aroundEnemy.clear();
		// 目的地のリスト
		destList.clear();
		
                
                
                
                int rulenumber = -1;
                
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
                        int targethp = aroundEnemy.get(0).e.hp;
                        for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				// 倒せそうな敵がいるとき，ターゲットに設定
                                // いないとき，hpの低い敵をターゲットに設定
                                if(aroundEnemy.get(eNum).e.hp <= info.player.attack &&
				   maxSumDamage > (sumDamage - aroundEnemy.get(eNum).e.attack))
				{
					targetEnemyIndex = eNum;
					maxSumDamage = sumDamage - aroundEnemy.get(eNum).e.attack;
				}
                                else
                                {
                                    if(targethp > aroundEnemy.get(eNum).e.hp)
                                    {
                                        targetEnemyIndex = eNum;
                                        targethp = aroundEnemy.get(eNum).e.hp;
                                    }
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
                                
                                //System.out.println("attack1, dir" + ruleBesedAct.dir);
			}
			// hpが敵から受ける攻撃力以下
                        // info.player.hp <= maxSumDamage
			else
			{
				// アイテムにポーションがあり，ポーションの回復量で間に合う
				int poIndex = inInvPotionCheck(info.player.inventory);
				if(poIndex != -1 && info.player.inventory.itemList.get(poIndex).hpHealVal > sumDamage)
				{
					ruleBesedAct.action = Action.USE_ITEM;
					ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
					ruleBesedAct.itemIndex = poIndex;
                                        
                                        //System.out.println("useitem1, index" + ruleBesedAct.itemIndex);
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
						for(int dir = 0; dir < 9; dir++)
						{
							movable[dir] *= dArea[enemyDir][dir];
						}
					}

					ArrayList<Integer> mList = new ArrayList<Integer>();
					for(int dir = 0; dir < 9; dir++)
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
					if(mList.isEmpty() == true)
					{
						Point p = new Point(info.player.gridMapX, info.player.gridMapY);
                                                
                                                // 周囲の敵の中の，最もhpの高い敵を探索
                                                int mhpenIndex = 0;
                                                int mhpenHp = aroundEnemy.get(mhpenIndex).e.hp;
                                                for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
                                                {
                                                    if(mhpenHp < aroundEnemy.get(eNum).e.hp)
                                                    {
                                                        mhpenHp = aroundEnemy.get(eNum).e.hp;
                                                        mhpenIndex = eNum;
                                                    }
                                                }
                                                Point e = new Point(aroundEnemy.get(mhpenIndex).e.gridMapX, aroundEnemy.get(mhpenIndex).e.gridMapY);
                                                
                                                // ワープの杖があるか
						int warpIndex = inInvWarpCheck(info.player.inventory, p, e);
						if(warpIndex != -1)
						{
							ruleBesedAct.action = Action.USE_ITEM;
							ruleBesedAct.dir = aroundEnemy.get(mhpenIndex).dir;
							ruleBesedAct.itemIndex = warpIndex;
                                                        
                                                        //System.out.println("use wstaff, dir" + ruleBesedAct.dir + ", index" + ruleBesedAct.itemIndex);
						}
						// ないとき
						else
						{
							// どうしようもない
							// ターゲットを攻撃
							ruleBesedAct.action = Action.ATTACK;
							// 方向を決定
							ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
                                                        
                                                        //System.out.println("akirameru, dir" + ruleBesedAct.dir);
						}
					}
					// 移動可能なマスが存在
					else
					{
						// その方向に移動
						ruleBesedAct.action = Action.MOVE;
						ruleBesedAct.difPos.x = difX[mList.get(0)];
						ruleBesedAct.difPos.y = difY[mList.get(0)];
                                                
                                                //System.out.println("move1, difpos(" + ruleBesedAct.difPos.x + ", " + ruleBesedAct.difPos.y + ")");
					}
				}
			}
                        
                        rulenumber = 0;
		}
		// 敵が２マス以上離れたところにいる
		else if(is2gridOverMosCheck(info, aroundEnemy) == true)
		{
			// 敵との距離を比較，一番近いものに対して行動を選択
			// 敵との距離，適当な大きい数字
			int targetEnemyIndex = 0;
			int distEnemy = aroundEnemy.get(targetEnemyIndex).dis;
                        int maxdamEn = aroundEnemy.get(targetEnemyIndex).e.attack;
			for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				// より距離の近い敵がいるとき
                                if(distEnemy > aroundEnemy.get(eNum).dis)
				{
					// 更新
					distEnemy = aroundEnemy.get(eNum).dis;
					targetEnemyIndex = eNum;
				}
                                // より大きなダメージを与える敵がいるとき
                                if(maxdamEn < aroundEnemy.get(eNum).e.attack)
                                {
                                        maxdamEn = aroundEnemy.get(eNum).e.attack;
                                }
			}

			//System.out.println("size:"+aroundEnemy.size());
			//System.out.println("tgenIndex:" + aroundEnemy.get(targetEnemyIndex).e.index);
                        //System.out.println("dist:" + distEnemy);
                        
                        
                        isStairCheck(info, destList);
                        int stindex = 0;
                        int stdist = 1500;
                        for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 5)
				{
                                        stindex = index;
                                        stdist = destList.get(index).dis;
                                        break;
				}
			}
                        
                        // 最下層であり，かつ，敵がいるが階段のほうが近いとき
                        if(info.player.curFloor == MyCanvas.TOPFLOOR - 1 && distEnemy > stdist)
                        {
                                ruleBesedAct.action = Action.MOVE;
				// リストの中から抽出
				int sDir = destList.get(stindex).dir;
				ruleBesedAct.difPos.x = difX[sDir];
				ruleBesedAct.difPos.y = difY[sDir];
                        }
			// ２マスのとき
                        else if(distEnemy == 2)
			{
				Point p = new Point(info.player.gridMapX, info.player.gridMapY);
                                Point e = new Point(aroundEnemy.get(targetEnemyIndex).e.gridMapX, aroundEnemy.get(targetEnemyIndex).e.gridMapY);
                                int ldIndex = inInvLongDistAtkCheck(info.player.inventory, p, e);
                                int poIndex = inInvPotionCheck(info.player.inventory);
                                // hpが危なく，ポーションがある
                                if(((info.player.hp - maxdamEn) < info.player.maxHp * 0.25) && poIndex != -1)
                                {
                                        ruleBesedAct.action = Action.USE_ITEM;
					ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
					ruleBesedAct.itemIndex = poIndex;
                                }
                                // アイテムに遠距離攻撃武器があり，当たる位置にいる
				else if(ldIndex != -1)
				{
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
				Point p = new Point(info.player.gridMapX, info.player.gridMapY);
                                Point e = new Point(aroundEnemy.get(targetEnemyIndex).e.gridMapX, aroundEnemy.get(targetEnemyIndex).e.gridMapY);
                                // アイテムに遠距離攻撃武器があり，当たる位置にいる
				int ldIndex = inInvLongDistAtkCheck(info.player.inventory, p, e);
				if(ldIndex != -1)
				{
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
				ruleBesedAct.difPos.x = difX[eDir];
				ruleBesedAct.difPos.y = difY[eDir];

				// 設定した座標が壁の場合，通路の探索に戻る
				if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
				{
					ruleBesedAct = passSearchMove(info);
				}
			}
                        
                        rulenumber = 1;
                        
                        
                        
                        
                        
                        
                        
//                        // 確認できる敵の到達ターンマップの作製
//                        ArrayList<EnemyInf> enemyInfList = new ArrayList<EnemyInf>();
//                        // 最も近い敵（ce）を求める
//                        int nearestEnIndex = 0;
//                        int nearestEnDist = 1500;
//                        for(int index = 0; index < aroundEnemy.size(); index++)
//                        {
//                            EnemyInf einf = new EnemyInf(info.map, info.pCurmap, aroundEnemy.get(index));
//                            enemyInfList.add(einf);
//                            if(nearestEnDist > enemyInfList.get(index).aenemy.dis)
//                            {
//                                nearestEnIndex = index;
//                                nearestEnDist = enemyInfList.get(index).aenemy.dis;
//                            }
//                        }
//                        
//                        Point p = new Point(info.player.gridMapX, info.player.gridMapY);
//                        Point e = new Point(aroundEnemy.get(targetEnemyIndex).e.gridMapX, aroundEnemy.get(targetEnemyIndex).e.gridMapY);
//                        int ldIndex = inInvLongDistAtkCheck(info.player.inventory, p, e);
//                        int poIndex = inInvPotionCheck(info.player.inventory);
//                        
//                        // 体力がceの攻撃を2回耐えられないとき
//                            // インベントリにポーションあり，2回耐えられる
//                                // 使用
//                            // その他
//                                // 敵からの距離が最も遠くなるように移動
//                        
//                        if(info.player.hp <= enemyInfList.get(nearestEnIndex).aenemy.e.attack * 2)
//                        {
//                            if(poIndex != -1)
//                            {
//                                if(info.player.hp + info.player.inventory.itemList.get(poIndex).hpHealVal > enemyInfList.get(nearestEnIndex).aenemy.e.attack * 2)
//                                {
//                                    // use potion
//                                    ruleBesedAct.action = Action.USE_ITEM;
//                                    ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
//                                    ruleBesedAct.itemIndex = poIndex;
//                                }
//                                else
//                                {
//                                    // move
//                                }
//                            }
//                            else
//                            {
//                                // move
//                            }
//                        }
//
//                        // 弓矢がインベントリに存在する
//                            // ceに対して使用可能，かつほかの敵との距離がceと異なるとき
//                                // 使用
//                            // その他
//                                // 移動
//                                
//                        else if(ldIndex != -1)
//                        {
//                            if(isCheckEnemyInfDist(enemyInfList, p, nearestEnDist) == false)
//                            {
//                                // use ld
//                                ruleBesedAct.action = Action.USE_ITEM;
//				ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
//				ruleBesedAct.itemIndex = ldIndex;
//                            }
//                            else
//                            {
//                                // move
//                            }
//                        }
//                                
//                        // その他
//                            // 移動
//                            
//                        else
//                        {
//                            // move
//                            // 壁による移動の可・不可判定
//                            // 0:移動不可,1:移動可
//                            int[] movable = info.getMovableGrid();
//                            
//                            ArrayList<Point> nextGList = new ArrayList<Point>();
//                            int minum = 1500;
//                            for(int i = 0; i < 9; i++)
//                            {
//                                int nx = info.player.gridMapX + difX[i];
//                                int ny = info.player.gridMapY + difY[i];
//                                
//                                if(0 > nx || nx >= info.mapsizeX || 0 > ny || ny >= info.mapsizeY)
//                                {
//                                    continue;
//                                }
//                                
//                                // 通行可能であり，移動可能
//                                if(info.map[ny][nx] != 1 && movable[i] == 1)
//                                {
//                                    if(minum > enemyInfList.get(nearestEnIndex).infmap[ny][nx] && enemyInfList.get(nearestEnIndex).infmap[ny][nx] >= 2)
//                                    {
//                                       minum = enemyInfList.get(nearestEnIndex).infmap[ny][nx];
//                                       nextGList.clear();
//                                       nextGList.add(new Point(nx, ny));
//                                    }
//                                    else if(minum == enemyInfList.get(nearestEnIndex).infmap[ny][nx])
//                                    {
//                                        minum = enemyInfList.get(nearestEnIndex).infmap[ny][nx];
//                                        nextGList.add(new Point(nx, ny));
//                                    }
//                                    else
//                                    {
//                                        // 大きい
//                                    }
//                                }
//                            }
//                            
//                            // ほかの敵のinfmapの比較
//                            int[] count = new int[nextGList.size()];
//                            for(int n = 0; n < count.length; n++)
//                            {
//                                count[n] = 0;
//                            }
//                            int maxInfPos = 0; // nextGListにおける要素番号
//                            int maxInfVal = 0; // 最大合計inf値
//                            for(int n = 0; n < nextGList.size(); n++)
//                            {
//                                Point pickup = nextGList.get(n);
//                                for(int eindex = 0; eindex < enemyInfList.size(); eindex++)
//                                {  
//                                    if(eindex == nearestEnIndex)
//                                    {
//                                        continue;
//                                    }
//                                    
//                                    count[n] += enemyInfList.get(eindex).infmap[pickup.y][pickup.x];
//                                }
//                                
//                                if(maxInfVal < count[n])
//                                {
//                                    maxInfVal = count[n];
//                                    maxInfPos = n;
//                                }
//                            }
//                            
//                            // 移動
//                        }
//
//                        // 最も近い敵の到達ターンマップから，プレイヤ周囲9マスのうち最小のマスをリストアップ
//                        
//                        // その他の確認できる敵の到達ターンマップの合計が遠くなるようなマスに移動
                        
                        
                        
                        
                        
                        
                        
                        
		}
		// 最下層であり，部屋に階段がある->目的地を階段に設定，１マス近づく
		else if(info.player.curFloor == MyCanvas.TOPFLOOR - 1 && isStairCheck(info, destList) == true)
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
					else
					{
						//
					}

					break;
				}
			}
                        
                        rulenumber = 2;
		}
		// 部屋の中にアイテムが落ちている->目的地をアイテムに設定，１マス近づく
		else if(isItemCheck(info, destList) == true && info.player.inventory.getInvItemNum() <= Inventory.MAX_INV)
		{
			int sDir = -1;
                        for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 4)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

                                        // 階段回避
                                        stairAvoid(info, ruleBesedAct);
                                        
					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMove(info);
					}
                                        
                                        // 移動先がアイテムと重複するとき
                                        if(info.player.gridMapX + ruleBesedAct.difPos.x == destList.get(index).p.x && 
                                           info.player.gridMapY + ruleBesedAct.difPos.y == destList.get(index).p.y)
                                        {
                                            floorItem++;
                                        }

					break;
				}
			}
                        
                        rulenumber = 3;
		}
		// hpが7割以下であり，満腹度が半分以上
		else if(info.player.hp < info.player.maxHp * 0.7 && info.player.satiety > 40)
		{
			ruleBesedAct.action = Action.STAY;
                        
                        rulenumber = 4;
		}
                // おなかがすいた
		else if(info.player.satiety < 70)
                //else if(info.player.satiety < (info.player.maxSatiety / MyCanvas.TOPFLOOR + 1) * (MyCanvas.TOPFLOOR - (info.player.curFloor + 1)))
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
                        
                        rulenumber = 5;
		}
		// すべての部屋を探索済みで，部屋の中に階段がある
                // アイテムを3~4個拾ったらに変更するか？
		//else if(isFloorSearch() == true && isStairCheck(info, destList) == true)
		else if((floorItem >= 4 || isFloorSearch() == true) && isStairCheck(info, destList) == true)
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
                        
                        rulenumber = 6;
		}
		// いったことのない通路を目的地とする
		else
		{
			// 目的地の探索・移動
			ruleBesedAct = passSearchMove(info);
			// 各アクションのdirの更新タイミング
			// アイテム使用は方向が重要
			// 移動は移動先決定 -> 方向をその向きに変更
                        
                        rulenumber = 7;
		}

		// アクションが移動の時
                // 斜め移動の制御
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
                        
                           // 行動の履歴
                        history.x = info.player.gridMapX;
                        history.y = info.player.gridMapY;   
		}
                
                
                // 直前の行動をhistへ
                battleflaghist = battleflag;
                
                // 戦闘のルールに入る：1
                if(rulenumber == 0 || rulenumber == 1)
                {
                    battleflag = 1;
                }
                else
                {
                    battleflag = 0;
                }
                
                //System.out.println("stack: " + stack.size());
                //System.out.println("stackHist: " + stackHist.size());

		return ruleBesedAct;
	}
        
        public boolean getBattleEnd()
        {
                // 直前が戦闘，現在が戦闘以外 -> 戦闘終了
                if(battleflaghist == 1 && battleflag == 0)
                {
                    // 記録タイミング
                    return true;
                }
                else
                {
                    return false;
                }
        }
        
        public Action ruleBasedforSimu(Info info)
	{
//                System.out.println("player:(" + info.player.gridMapX + "," + info.player.gridMapX + ")");
//                System.out.println("mc-floor:" + MyCanvas.floorNumber);
//                System.out.println("in-floor:" + info.player.curFloor);
//                System.out.println("seq:" + seq);
//                 // historyとの差分
//                int ptype = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
//                
//                // 更新
//		arriveMap[info.player.gridMapY][info.player.gridMapX]++;
//                
//                // targetの設定，初期化
//		// 階層が変化したとき
//		if(playerFloor != info.player.curFloor && ptype == -1)
//		{
//			// 現在の部屋番号
//                        int roomid = calcRoomID(info.currentRTopLeft);
//                    
//                        //System.out.println("pf:" + playerFloor);
//                        //System.out.println("pcf:" + MyCanvas.floorNumber);
//                        initPassSearch(info.player.curFloor, info.currentRTopLeft);
//                        
//                        addhrList(info);
//                        if(nodes[roomid] == null)
//                        {
//                            nodes[roomid] = new node(roomid);
//                            allnode.add(nodes[roomid]);
//                        }
//                        updateTarget(info);
//                        
//                        firstRoomID = calcRoomID(info.currentRTopLeft);  
//		}
//                // 階層に変化なし
//                else
//                {
//                    // 初期生成直後ではないとき，historyが存在するとき
//                    if (history.x != -1 && history.y != -1){
//                        // 部屋に到着したとき
//                        if(ptype == -1 && diagonalCheck(info.map, history.x, history.y) != -1) {
//                            // 現在の部屋番号
//                            int roomid = calcRoomID(info.currentRTopLeft);
//                            //System.out.println("seq:" + seq);
//                            
//                            if (isStairCheck(info) == true) {
//                                stairRoomID = roomid;
//                            }
//                            
//                            // 探索終了前
//                            // かつ，直前までいた部屋と現在の部屋が同じ（通路の途中で引き返したとき）
//                            if(seq <= 2 && lastRoomID == roomid){
//                                // 特に更新なし？
//                            }
//                            // 探索終了前
//                            // かつ，直前までいた部屋と現在の部屋が異なる
//                            else if(seq <= 2 && lastRoomID != roomid){
//                                // 過去の部屋履歴を更新
//                                roomHist = lastRoomID;
//                                // 現在の部屋のIDに更新
//                                lastRoomID = roomid;
//
//                                // 通路の履歴を更新
//                                // 直前に通過した部分を追加
//                                pathHist[1] = calcRoomID(history);
//
//                                // ツリーの作成
//                                //int roomid = calcRoomID(info.currentRTopLeft);
//                                addhrList(info);
//                                if(nodes[roomid] == null)
//                                {
//                                    nodes[roomid] = new node(roomid);
//                                    allnode.add(nodes[roomid]);
//                                }
//								
//								// ほかの部屋が見つかり次第，ノードを接続
//                                // ノードがつながっていないとき
//                                if(nodes[roomid].connected.indexOf(nodes[roomHist]) == -1)
//                                {
//                                    nodes[roomid].connected.add(nodes[roomHist]);
//                                    // 通路をつなげる，通路のおわり（１マス手前）
//                                    nodes[roomid].path[nodes[roomHist].ID] = pathHist[1];
//                                }
//                                if(nodes[roomHist].connected.indexOf(nodes[roomid]) == -1)
//                                {
//                                    nodes[roomHist].connected.add(nodes[roomid]);
//                                    // 通路をつなげる，通路の始まり
//                                    nodes[roomHist].path[nodes[roomid].ID] = pathHist[0];
//                                }
//
//                                // targetの更新
//                                //System.out.println("roomid:" + roomid);
//                                updateTarget(info);
//                                
//                                // nodesを更新後，通路の履歴を初期化
//                                pathHist = new int[2];
//                            }
//                            // 探索終了後
//                            if(seq >= 3){
//                                // 階段発見済，現在の部屋と異なるとき -> その部屋に向かう
//                                // 階段未発見 -> ランダムムーブ
//                                node n = allnode.get(random.nextInt(allnode.size()));
//                                if(stairRoomID != -1 && stairRoomID != roomid)
//                                {
//                                    for(int index = 0; index < allnode.size(); index++)
//                                    {
//                                        if(allnode.get(index).ID == stairRoomID)
//                                        {
//                                            n = allnode.get(index);
//                                            break;
//                                        }
//                                    }
//                                }
//                                else
//                                {
//                                    // 探索済みでのターゲットの更新
//                                    // 通路の座標を目的地に設定
//                                    while(true)
//                                    {
//                                        // 現在の部屋と同じ場合は，再生成
//                                        if(roomid == n.ID)
//                                        {
//                                            n = allnode.get(random.nextInt(allnode.size()));
//                                        }
//                                        else
//                                        {
//                                            break;
//                                        }
//                                    }
//                                }
//                                
//                                // id -> point
//                                int tg = nodes[roomid].path[nodes[roomid].reachRoot[n.ID].ID];
//                                //target = new Point(tg/30, tg%30);
//                                setTarget(new Point(tg / 30, tg % 30), roomid);
//                                //System.out.println("roomid:" + roomid + " >>tg_id:" + n.ID + " (Target Point" + target.x +","+ target.y + ")");
//                            }
//                        }
//                        // 通路に到達したとき
//                        else if (ptype != -1 && diagonalCheck(info.map, history.x, history.y) == -1) {
//                            // 通路の履歴を更新，入口
//                            pathHist[0] = calcRoomID(new Point(info.player.gridMapX, info.player.gridMapY));
//
//                            // ptargetの更新
//                            updatePTarget(info);
//                        }
//                        else{ 
//                            // 止まる
//                            // 
//                        }
//                    }
//                }
		
                

                Action ruleBesedAct = new Action(info.player.dir);
                
		// 隣接している敵のリスト
		aroundEnemy.clear();
		// 目的地のリスト
		destList.clear();
		
                
                
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
                        int targethp = aroundEnemy.get(0).e.hp;
			for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				if(aroundEnemy.get(eNum).e.hp <= info.player.attack &&
				   maxSumDamage > sumDamage - aroundEnemy.get(eNum).e.attack)
				{
					targetEnemyIndex = eNum;
					maxSumDamage = sumDamage - aroundEnemy.get(eNum).e.attack;
				}
                                else
                                {
                                    if(targethp < aroundEnemy.get(eNum).e.hp)
                                    {
                                        targetEnemyIndex = eNum;
                                        targethp = aroundEnemy.get(eNum).e.hp;
                                    }
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
						Point p = new Point(info.player.gridMapX, info.player.gridMapY);
                                                Point e = new Point(aroundEnemy.get(targetEnemyIndex).e.gridMapX, aroundEnemy.get(targetEnemyIndex).e.gridMapY);
                                                // ワープの杖があるか
						int warpIndex = inInvWarpCheck(info.player.inventory, p, e);
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
                        int maxdamEn = aroundEnemy.get(targetEnemyIndex).e.attack;
			for(int eNum = 0; eNum < aroundEnemy.size(); eNum++)
			{
				// より距離の近い敵がいるとき
                                if(distEnemy > aroundEnemy.get(eNum).dis)
				{
					// 更新
					distEnemy = aroundEnemy.get(eNum).dis;
					targetEnemyIndex = eNum;
				}
                                // より大きなダメージを与える敵がいるとき
                                if(maxdamEn < aroundEnemy.get(eNum).e.attack)
                                {
                                        maxdamEn = aroundEnemy.get(eNum).e.attack;
                                }
			}

			//System.out.println("size:"+aroundEnemy.size());
			//System.out.println("dist:"+distEnemy);

                        
                        isStairCheck(info, destList);
                        int stindex = 0;
                        int stdist = 1500;
                        for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 5)
				{
                                        stindex = index;
                                        stdist = destList.get(index).dis;
                                        break;
				}
			}
                        
                        // 最下層であり，かつ，敵がいるが階段のほうが近いとき
                        if(info.player.curFloor == MyCanvas.TOPFLOOR - 1 && distEnemy > stdist)
                        {
                                ruleBesedAct.action = Action.MOVE;
				// リストの中から抽出
				int sDir = destList.get(stindex).dir;
				ruleBesedAct.difPos.x = difX[sDir];
				ruleBesedAct.difPos.y = difY[sDir];
                        }
			// ２マスのとき
                        else if(distEnemy == 2)
			{
				Point p = new Point(info.player.gridMapX, info.player.gridMapY);
                                Point e = new Point(aroundEnemy.get(targetEnemyIndex).e.gridMapX, aroundEnemy.get(targetEnemyIndex).e.gridMapY);
                                int ldIndex = inInvLongDistAtkCheck(info.player.inventory, p, e);
                                int poIndex = inInvPotionCheck(info.player.inventory);
                                // hpが危なく，ポーションがある
                                if(((info.player.hp - maxdamEn) < info.player.maxHp * 0.25) && poIndex != -1)
                                {
                                        ruleBesedAct.action = Action.USE_ITEM;
					ruleBesedAct.dir = aroundEnemy.get(targetEnemyIndex).dir;
					ruleBesedAct.itemIndex = poIndex;
                                }
                                // アイテムに遠距離攻撃武器があり，当たる位置にいる
				else if(ldIndex != -1)
				{
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
				Point p = new Point(info.player.gridMapX, info.player.gridMapY);
                                Point e = new Point(aroundEnemy.get(targetEnemyIndex).e.gridMapX, aroundEnemy.get(targetEnemyIndex).e.gridMapY);
                                // アイテムに遠距離攻撃武器があり，当たる位置にいる
				int ldIndex = inInvLongDistAtkCheck(info.player.inventory, p, e);
				if(ldIndex != -1)
				{
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
					ruleBesedAct.difPos.x = difX[eDir];
					ruleBesedAct.difPos.y = difY[eDir];
					//System.out.println("dir:"+eDir);
					//System.out.println("dif:"+ruleBesedAct.difPos.x + "," + ruleBesedAct.difPos.y);

					// 設定した座標が壁の場合，通路の探索に戻る
					//if(Background.map[obj.player.gridMapY + ruleBesedAct.difPos.y][obj.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMoveforSimu(info);
					}
				}
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
					ruleBesedAct = passSearchMoveforSimu(info);
				}
			}
		}
		// 最下層であり，部屋に階段がある->目的地を階段に設定，１マス近づく
		else if(info.player.curFloor == MyCanvas.TOPFLOOR - 1 && isStairCheck(info, destList) == true)
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
						ruleBesedAct = passSearchMoveforSimu(info);
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
		else if(isItemCheck(info, destList) == true && info.player.inventory.getInvItemNum() <= Inventory.MAX_INV)
		{
			int sDir = -1;
                        for(int index = 0; index < destList.size(); index++)
			{
				if(destList.get(index).objNum == 4)
				{
					ruleBesedAct.action = Action.MOVE;
					// リストの中から抽出
					sDir = destList.get(index).dir;
					ruleBesedAct.difPos.x = difX[sDir];
					ruleBesedAct.difPos.y = difY[sDir];

                                        // 階段回避
                                        stairAvoid(info, ruleBesedAct);
                                        
					// 設定した座標が壁の場合，通路の探索に戻る
					if(info.map[info.player.gridMapY + ruleBesedAct.difPos.y][info.player.gridMapX + ruleBesedAct.difPos.x] == 1)
					{
						ruleBesedAct = passSearchMoveforSimu(info);
					}
                                        
                                        // 移動先がアイテムと重複するとき
                                        if(info.player.gridMapX + ruleBesedAct.difPos.x == destList.get(index).p.x && 
                                           info.player.gridMapY + ruleBesedAct.difPos.y == destList.get(index).p.y)
                                        {
                                            floorItem++;
                                        }

					break;
				}
			}
		}
		// hpが7割以下であり，満腹度が半分以上
		else if(info.player.hp < info.player.maxHp * 0.7 && info.player.satiety > 40)
		{
			ruleBesedAct.action = Action.STAY;
		}
                // おなかがすいた
		else if(info.player.satiety < 70)
                //else if(info.player.satiety < (info.player.maxSatiety / MyCanvas.TOPFLOOR + 1) * (MyCanvas.TOPFLOOR - (info.player.curFloor + 1)))
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
								ruleBesedAct = passSearchMoveforSimu(info);
							}

							break;
						}
					}
				}
				else
				{
					// 目的地の探索・移動
					ruleBesedAct = passSearchMoveforSimu(info);
				}
			}
		}
		// すべての部屋を探索済みで，部屋の中に階段がある
                // アイテムを3~4個拾ったらに変更するか？
		//else if(isFloorSearch() == true && isStairCheck(info, destList) == true)
		else if((floorItem >= 4 || isFloorSearch() == true) && isStairCheck(info, destList) == true)
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
						ruleBesedAct = passSearchMoveforSimu(info);
					}

					break;
				}
			}
		}
		// いったことのない通路を目的地とする
		else
		{
			// 目的地の探索・移動
			ruleBesedAct = passSearchMoveforSimu(info);
			// 各アクションのdirの更新タイミング
			// アイテム使用は方向が重要
			// 移動は移動先決定 -> 方向をその向きに変更
		}

		// アクションが移動の時
                // 斜め移動の制御
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
                        
                           // 行動の履歴
                        history.x = info.player.gridMapX;
                        history.y = info.player.gridMapY;   
		}
                
                 
                
                //System.out.println("stack: " + stack.size());
                //System.out.println("stackHist: " + stackHist.size());

		return ruleBesedAct;
	}
        
        public void updateHistory(int px, int py)
        {
                history.x = px;
                history.y = py; 
        }
        
        public boolean isFloorSearch()
        {
            if(stack.isEmpty())
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        // 周囲1マスのみの場合しか使用しないため，あたり判定は省略
	public int inInvWarpCheck(Inventory pinv, Point p, Point e)
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
        
        public int calcRoomID(Point p)
        {
            return p.x * 30 + p.y;
        }
        
        public void addhrList(Info info)
        {
            int id = calcRoomID(info.currentRTopLeft);
            if(hrList.indexOf(id) == -1){
                hrList.add(id);
            }
        }
        
        public boolean isCheckhrList(Info info)
        {
            // 通路の時
            if(diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY) == -1)
            {
                return false;
            }
            // 部屋の時
            else
            {
                Point p = info.currentRTopLeft;
                int tmp = p.x * 30 + p.y;
                for(int index = 0; index < hrList.size(); index++)
                {
                    // 訪問済みの時
                    if(tmp == hrList.get(index))
                    {
                        return false;
                    }
                }

                // 通路でなく，初訪問の部屋の時
                return true;
            }
        }
        
        public boolean isCheckORallstackhist(Info info, Point p)
        {
            for(int index = 0; index < allstackHist.size(); index++)
            {
                if(allstackHist.get(index).x == p.x && allstackHist.get(index).y == p.y)
                {
                    return true;
                }
            }
            return false;
        }
        
        // 視界・現在地を引数とする
        // -1:部屋
        //  0:縦
        //  1:横
        public int diagonalCheck(int[][] map, int px, int py)
        {
            int[] diffx ={1,0,-1,0};
            int[] diffy ={0,1,0,-1};
            
            for(int i = 0; i < 2 ; i++)
            {
                if((py + diffy[i+2]) < 0 || (px + diffx[i+2]) < 0)
                {
                    System.out.println("px:" + px + ", py:" + py);
                    System.out.println("px + diffx[i+2]:" + (px + diffx[i+2]));
                    System.out.println("py + diffy[i+2]:" + (py + diffy[i+2]));
                }
                
                if((py + diffy[i]) >= 30 || (px + diffx[i]) >= 50)
                {
                    System.out.println("px:" + px + ", py:" + py);
                    System.out.println("px + diffx[i]:" + (px + diffx[i]));
                    System.out.println("py + diffy[i]:" + (py + diffy[i]));
                }
                
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
        
        public boolean isinCurRoom(Point[] p, int[][] map)
        {
            int ptype = diagonalCheck(map, p[2].x, p[2].y);
            // 縦
            if(ptype == 0)
            {
                if (p[2].y == p[0].y - 1 || p[2].y == p[1].y + 1)
                {
                    if(p[0].x <= p[2].x &&  p[2].x <= p[1].x)
                    {
                        return true;
                    }
                }
            }
            // 横
            else if(ptype == 1)
            {
                if (p[2].x == p[0].x - 1 || p[2].x == p[1].x + 1)
                {
                    if(p[0].y <= p[2].y &&  p[2].y <= p[1].y)
                    {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        public void updatePTarget(Info info)
        {
            //System.out.println("path");

            // ptargetの更新
            // 直前のマスが部屋の時，histdir更新
            if (diagonalCheck(info.map, history.x, history.y) == -1) 
            {
                for (int index = 0; index < 4; index++)
                {
                    // 現在地との差分から，方向を検出
                    if (difsX[index] == info.player.gridMapX - history.x && difsY[index] == info.player.gridMapY - history.y) 
                    {
                        //histDir = (index + 2 > 4) ? index - 2 : index + 2;
                        histDir = index;
                        break;
                    }
                }

                // 方向に従ってptargetを設定
                ptarget.x = difsX[histDir] * 50;
                ptarget.y = difsY[histDir] * 30;
            }
        }
        
        public boolean stackRemove(Point rev, Stack s)
        {
            int sindex = s.search(rev);

            if(sindex >= 0)
            {
                sindex = s.size() - sindex;
                s.remove(s.get(sindex));
                return true;
            }
            
            return false;
        }
        
        public void updateTarget(Info info)
        {
                // targetの更新
                if (seq == 2) 
                {
                        Point p = new Point(pathHist[0] / 30, pathHist[0] % 30);
                        if(target.x != p.x && target.y != p.y){
                            stackRemove(p, stack);
                        }
                        else{
                            allstackHist.add(new Point(stack.peek().x, stack.peek().y));
                            stack.pop();// targetとしてpeekしていたものをpop
                        }
                        
                        allstackHist.add(new Point(history.x, history.y));
                        stackHist.push(new Point(history.x, history.y));
                        // もしstackの中にhistと同じ座標が存在するならば，それを削除
                        if(stackRemove(history, stack) == true){
                            seq = 1;
                            seqHist = 2;
                        }
                        else{
                            seq = 0;
                            seqHist = 2;
                        }
                }
                
                if (seq == 0) 
                {
                    // 目的地に追加,直前の通路以外
                    //System.out.println("search-nextpass");
                    searchNextPass(info);
                    // 通路の探索と追加
                    seqHist = 0;
                    seq = 1;
                }
                
                if (seq == 1) 
                {
                    if (seqHist == 1 && !stackHist.isEmpty()) {
                        stackHist.pop();
                    }
                    
                    // stackがpop可能なとき
                    if (!stack.isEmpty()) {
                        // 現在の部屋に未探索通路があるとき
                        if (isinCurRoom(new Point[]{info.currentRTopLeft, info.currentRButtomRight, new Point(stack.peek().x, stack.peek().y)}, info.map) == true) {
                            //System.out.println("target -> stack.peek");
                            //target = stack.peek();
                            setTarget(stack.peek(), calcRoomID(info.currentRTopLeft));
                            seq = 2;
                            seqHist = 1;
                        }
                        // スタックサイズが0のとき
                        // かつ，スタックからpop可能なとき
                        // 現在の部屋に未探索の～の部分のifからelseifとつなげたい
                        else if (!stackHist.isEmpty())
                        {
                            if (isinCurRoom(new Point[]{info.currentRTopLeft, info.currentRButtomRight, new Point(stackHist.peek().x, stackHist.peek().y)}, info.map) == true) 
                            {
                                //target = stackHist.peek();
                                setTarget(stackHist.peek(), calcRoomID(info.currentRTopLeft));
                                seq = 1;
                                seqHist = 1;
                            }
                            else
                            {
                                setTarget(new Point(info.player.gridMapX, info.player.gridMapY), calcRoomID(info.currentRTopLeft));
                                seq = 3;
                            }
                        } 
                        else
                        {
                            // 両方ともなし
                            // 探索完了
                            //target = new Point(info.player.gridMapX, info.player.gridMapY);
                            setTarget(new Point(info.player.gridMapX, info.player.gridMapY), calcRoomID(info.currentRTopLeft));
                            //System.out.println("error?");
                            seq = 3;
                        }
                    }
                    // stackがpop不可なとき
                    else{
                        // スタックサイズが0のとき
                        // かつ，スタックヒストからpop可能なとき
                        if (!stackHist.isEmpty())
                        {
                            if (isinCurRoom(new Point[]{info.currentRTopLeft, info.currentRButtomRight, new Point(stackHist.peek().x, stackHist.peek().y)}, info.map) == true) 
                            {
                                //target = stackHist.peek();
                                setTarget(stackHist.peek(), calcRoomID(info.currentRTopLeft));
                                seq = 1;
                                seqHist = 1;
                            }
                            else
                            {
                                setTarget(new Point(info.player.gridMapX, info.player.gridMapY), calcRoomID(info.currentRTopLeft));
                                seq = 3;
                            }
                        } 
                        else
                        {
                            // 両方ともなし
                            // 探索完了
                            //target = new Point(info.player.gridMapX, info.player.gridMapY);
                            setTarget(new Point(info.player.gridMapX, info.player.gridMapY), calcRoomID(info.currentRTopLeft));
                            //System.out.println("error?");
                            seq = 3;
                        }
                    }
                }
                
                if(seq == 3)
                {
                    // スタックが空 -> 未探索通路の候補なし
                    // 探索完了
                    //target = new Point(info.player.gridMapX, info.player.gridMapY);
                    setTarget(new Point(info.player.gridMapX, info.player.gridMapY), calcRoomID(info.currentRTopLeft));
                    //System.out.println("finish");
                    seq = 3;

                    for (node temp : allnode) 
                    {
                        AppendNodeRoot(temp);
                    }

//                    for (int index = 0; index < hrList.size(); index++) {
//                        System.out.println("hrList" + index + ":" + hrList.get(index));
//                    }
//
//                    for (int index1 = 0; index1 < hrList.size(); index1++) {
//                        for (int index2 = 0; index2 < hrList.size(); index2++) {
//                            if (index1 != index2) {
//                                int num = nodes[hrList.get(index1)].path[nodes[hrList.get(index1)].reachRoot[hrList.get(index2)].ID];
//                                System.out.println(hrList.get(index1) + "->" + hrList.get(index2) + ":"
//                                        + nodes[hrList.get(index1)].reachRoot[hrList.get(index2)].ID + "("
//                                        + num / 30 + "," + num % 30 + ")");
//                            }
//                        }
//                    }
                }
                
                //System.out.println("target:(" + target.x + "," + target.y + ")");
        }
        
        public static void AppendNodeRoot(node focused) 
        {
            ArrayList<node> Search = new ArrayList<node>();
            ArrayList<node> indirect = new ArrayList<node>();
            Search.addAll(focused.connected);
            
            focused.reachRoot = new node[1500];
            
            while(true)
            {
                if(Search.isEmpty())
                {
                    break;
                }

                if(Search.get(0).connected.size() > 1)
                {
    //                System.out.println("now searching node " + Search.get(0).ID );
                    for(node conn: Search.get(0).connected)
                    {
                        if (focused.reachRoot[conn.ID] == null && conn.ID != focused.ID)
                        {
                            indirect.add(conn);
                        }
                    }
                    
                    while(true)
                    {
                        if(indirect.isEmpty())
                        {
                            break;
                        }
    //                    System.out.println("now searching indirect node " + indirect.get(0).ID );
                        for(node conn: indirect.get(0).connected)
                        {
                            if (focused.reachRoot[conn.ID] == null && conn.ID != focused.ID)
                            {
                                indirect.add(conn);
                                
//                                // 経路がないとき
//                                if(focused.reachRoot[conn.ID] == null)
//                                {
//                                    indirect.add(conn);
//                                }
//                                else
//                                {
//                                    // 通過する部屋数のカウント
//                                    // conn.ID == focused.ID まで
//                                    
//                                }
                                
                            }
                        }
                        
                        focused.reachRoot[indirect.get(0).ID] = Search.get(0);
                        indirect.remove(0);
                    }
                }
                
                focused.reachRoot[Search.get(0).ID] = Search.get(0);
                Search.remove(0);
            }
        }
        
        public void convTgtoAct(Info info, Action act, Point tg)
        {
            // target -> Action
            
            // 
            act.action = Action.MOVE;
            int tgdir = convPos2Dir(tg, new Point(info.player.gridMapX, info.player.gridMapY));
            act.difPos.x = difX[tgdir];
            act.difPos.y = difY[tgdir];
        }
        
        public void stairAvoid(Info info, Action act)
        {
            // 階段が移動先に存在するとき
            // 関数化，ルールによっては使う部分がある？
            int sdir = -1;
            for (int index = 0; index < 9; index++) {
                if (act.difPos.x == difX[index] && act.difPos.y == difY[index]) {
                    sdir = index;
                    break;
                }
            }
            if (isStairNextGridCheck(info.mapObject, new Point(info.player.gridMapX, info.player.gridMapY), sdir) == true) {
                System.out.println("stair avoid");
                for (int dirin = 0; dirin < 2; dirin++) {
                    int sav = savoid[sdir][dirin];
                    // 階段を回避したグリッドが通行可能なとき
                    if (info.map[info.player.gridMapY + difY[sav]][info.player.gridMapX + difX[sav]] == 0) {
                        act.difPos.x = difX[sav];
                        act.difPos.y = difY[sav];
                        break;
                    }
                }
            }
        }
        
        public Action passSearchMove(Info info) 
        {
            // 選択したアクション
            Action rbAct = new Action(info.player.dir);
            
            int passType = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
            // 部屋の時
            if(passType == -1)
            {
                // 現在の部屋番号
                int roomid = calcRoomID(info.currentRTopLeft);
                // 現在の部屋に目的地が存在しないとき
                if (targetRoomID != roomid) 
                {
                    // append -> 移動経路の決定
                    for (node temp : allnode)
                    {
                        AppendNodeRoot(temp);
                    }

                    if(nodes[roomid] == null)
                    {
                        System.out.println("nodes[roomid] == null, roomid = " + roomid);
                    }
                    if(nodes[roomid].reachRoot[targetRoomID] == null)
                    {
                        System.out.println("nodes[roomid].reachRoot[targetRoomID] == null, targetRoomID = " + targetRoomID);
                    }
                    int spath = nodes[roomid].path[nodes[roomid].reachRoot[targetRoomID].ID];

                    // 経由する目的地の更新
                    subtarget = new Point(spath / 30, spath % 30);
                    // target->Action
                    convTgtoAct(info, rbAct, subtarget);
                    System.out.println("subtarget");
                    System.out.println("allnode num:" + allnode.size());
                }
                else
                {
                    // target->Action
                    convTgtoAct(info, rbAct, target);
                }
                
                // 階段の回避
                stairAvoid(info, rbAct);
                
                //System.out.println("room");
            }
            // 通路の時
            else
            {
                convTgtoAct(info, rbAct, ptarget);
                //System.out.println("path");
            }
            
            return rbAct;
        }
        
        public Action passSearchMoveforSimu(Info info) 
        {
            // 選択したアクション
            Action rbAct = new Action(info.player.dir);
            
            int passType = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
            // 部屋の時
            if(passType == -1)
            {
                // target->Action
                convTgtoAct(info, rbAct, target);
                
                // 階段の回避
                stairAvoid(info, rbAct);
                
                //System.out.println("room");
            }
            // 通路の時
            else
            {
                convTgtoAct(info, rbAct, ptarget);
                //System.out.println("path");
            }
            
            return rbAct;
        }

        public boolean isStairNextGridCheck(int[][] mapobj, Point p, int sdir)
        {
            int playerx = p.x + difX[sdir];
            int playery = p.y + difY[sdir];
            
            if(mapobj[playery][playerx] == 5)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean stackpushpath(Info info, int x, int y)
        {
            boolean flag = false;
            
            if(info.map[y][x] == 0)
            {
                int passType = diagonalCheck(info.map, x, y);
                // もし，stackに存在しない
                // かつ，直前の座標と異なる
                // かつ，部屋が隣接ならば，追加
                if(isStackORcheck(new Point(x, y)) == false &&
                   (history.x != x || history.y != y) &&
                   isCheckNext(info, new Point(x, y), passType) == true)
                {
                    stack.push(new Point(x, y));
                    //System.out.println("stack-push:(" + x + "," + y + ")");
                    flag = true;
                }
            }
            
            return flag;
        }
        
	public boolean searchNextPass(Info info)
	{
		boolean flag = false;
                int xn, yn;

                if(info.currentRTopLeft.x <= 0 || info.currentRTopLeft.y <= 0)
                {
                    return flag;
                }
                
                if(info.currentRButtomRight.x >= info.mapsizeX - 1 || info.currentRButtomRight.y >= info.mapsizeY - 1)
                {
                    return flag;
                }
                
                // 部屋の左辺
                xn = info.currentRTopLeft.x - 1;
                for(yn = info.currentRTopLeft.y; yn <= info.currentRButtomRight.y; yn++)
                {
                    if(0 < yn && yn < info.mapsizeY - 1)
                    {
                        flag = stackpushpath(info, xn, yn);
                    }
                }
                // 部屋の右辺
                xn = info.currentRButtomRight.x + 1;
                for(yn = info.currentRTopLeft.y; yn <= info.currentRButtomRight.y; yn++)
                {
                    if(0 < yn && yn < info.mapsizeY - 1)
                    {
                        flag = stackpushpath(info, xn, yn);
                    }
                }
                // 部屋の上辺
                yn = info.currentRTopLeft.y - 1;
                for(xn = info.currentRTopLeft.x; xn <= info.currentRButtomRight.x; xn++)
                {
                    if(0 < xn && xn < info.mapsizeX - 1)
                    {
                        flag = stackpushpath(info, xn, yn);    
                    }
                }
                // 部屋の下辺
                yn = info.currentRButtomRight.y + 1;
                for(xn = info.currentRTopLeft.x; xn <= info.currentRButtomRight.x && xn < info.mapsizeX - 1; xn++)
                {        
                    if(0 < xn && xn < info.mapsizeX - 1)
                    {
                        flag = stackpushpath(info, xn, yn);    
                    }    
                }

                //System.out.println("push-flag: " + flag);
		return flag;
	}
        
        public boolean isCheckNext(Info info, Point p, int passtype)
        {
            // 縦通路の時
            if(passtype == 0){
                // 上に部屋があるとき
                if(info.pCurmap[p.y - 1][p.x] == true && info.mapRoomNum[p.y - 1][p.x] != -1)
                {
                    
                    return true;
                }
                // 下に部屋があるとき
                else if(info.pCurmap[p.y + 1][p.x] == true && info.mapRoomNum[p.y + 1][p.x] != -1)
                {
                    
                    return true;
                }
            }
            
            // 横通路の時
            else if(passtype == 1){
                // 左に部屋があるとき
                if(info.pCurmap[p.y][p.x - 1] == true && info.mapRoomNum[p.y][p.x - 1] != -1)
                {
                    
                    return true;
                }
                // 右に部屋があるとき
                else if(info.pCurmap[p.y][p.x + 1] == true && info.mapRoomNum[p.y][p.x + 1] != -1)
                {
                    
                    return true;
                }
            }
            
            // 部屋に面していないとき
            return false;
        }

	public boolean isStackORcheck(Point p)
	{
//		for(int index = 0; index < stack.size(); index++)
//		{
//			if(stack.peek().x == p.x && stack.peek().y == p.y)
//			{
//				return true;
//			}
//		}
                
                // 真：重複あり
                // 偽：重複なし
                if(stack.search(p) >= 0)
                {
                    //System.out.println("tyoufuku");
                    return true;
                }
                else
                {
                    //System.out.println("tyoufukunasi");
                    return false;
                }
	}

        // 部屋内の目的地リストに重複ありtrue，なしfalse
        public boolean isDestListORCheck(ArrayList<Destination> dList, int x, int y)
        {
            for(int index = 0; index < dList.size(); index++)
            {
                if(dList.get(index).p.x == x && dList.get(index).p.y == y)
                {
                    return true;
                }
            }
            return false;
        }
        
	public boolean isItemCheck(Info info, ArrayList<Destination> dList)
	{
		boolean flag = false;
                Destination dest = new Destination();
                dest.dis = 1500;
                
                // 部屋の時
                int pathType = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
                if(pathType == -1)
                {
                    // 部屋の左上と右下の座標から，探索範囲を現在の部屋に限定
                    for(int yn = info.currentRTopLeft.y; yn <= info.currentRButtomRight.y; yn++)
                    {
                        for(int xn = info.currentRTopLeft.x; xn <= info.currentRButtomRight.x; xn++)
                        {
                            if(info.mapObject[yn][xn] == 4 && isDestListORCheck(dList, xn, yn) == false)
                            {
				// アイテムがあり，目的地リストに重複なし
                                Point ip = new Point(xn, yn);
                                Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
                                int absDisx = Math.abs(pp.x - ip.x);
				int absDisy = Math.abs(pp.y - ip.y);
                                int distance = absDisx > absDisy ? absDisx : absDisy;
                                
                                // 従来の目的地よりも近い場合，目的地を変更
                                if(dest.dis > distance)
                                {
                                    dest.dis = distance;
                                    dest.objNum = 4;
                                    dest.p = new Point(ip);
                                    dest.dir = convPos2Dir(dest.p, pp);
                                }
                                
                                flag = true;
                            }
                        }
                    }
                }
                
                dList.add(dest);
                return flag;
	}

        public boolean isCheckStairNextGrid(int[][] map, int nx, int ny)
        {
                // (nx,ny)に階段があるとき
                if(map[ny][nx] == 5)
                {
                        return true;
                }
                else
                {
                        return false;    
                }
        }
        
        // 部屋に階段がある：真
        // 部屋に階段がない：偽
        public boolean isStairCheck(Info info)
	{
		// 部屋の時
                int pathType = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
                if(pathType == -1)
                {
                    // 部屋の左上と右下の座標から，探索範囲を現在の部屋に限定
                    for(int yn = info.currentRTopLeft.y; yn <= info.currentRButtomRight.y; yn++)
                    {
                        for(int xn = info.currentRTopLeft.x; xn <= info.currentRButtomRight.x; xn++)
                        {
                            if(info.mapObject[yn][xn] == 5)
                            {
                                return true;
                            }
                        }
                    }
                }

		return false;
	}
        
	public boolean isStairCheck(Info info, ArrayList<Destination> dList)
	{
		Destination dest = new Destination();

                // 部屋の時
                int pathType = diagonalCheck(info.map, info.player.gridMapX, info.player.gridMapY);
                if(pathType == -1)
                {
                    // 部屋の左上と右下の座標から，探索範囲を現在の部屋に限定
                    for(int yn = info.currentRTopLeft.y; yn <= info.currentRButtomRight.y; yn++)
                    {
                        for(int xn = info.currentRTopLeft.x; xn <= info.currentRButtomRight.x; xn++)
                        {
                            if(info.mapObject[yn][xn] == 5)
                            {
                                dest.objNum = 5;
                                dest.p = new Point(xn, yn);
                                Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
                                int absDisx = Math.abs(pp.x - dest.p.x);
                                int absDisy = Math.abs(pp.y - dest.p.y);
                                dest.dis = absDisx > absDisy ? absDisx : absDisy;
                                dest.dir = convPos2Dir(dest.p, pp);
                                dList.add(dest);
                                return true;
                            }
                        }
                    }
                }

		return false;
	}

	public int inInvLongDistAtkCheck(Inventory pinv, Point p, Point e)
	{
		int ldIndex = -1;
            
                for(int index = 0; index<pinv.getInvItemNum(); index++)
		{
			if(pinv.itemList.get(index).type.equals(new String("staff")) && pinv.itemList.get(index).damage > 0)
			{
				ldIndex = index;
			}
		}

                int dx = Math.abs(p.x - e.x);
                int dy = Math.abs(p.y - e.y);
                if(dx <= 5 && dy <= 5 && (dx == dy || dx == 0 || dy == 0))
                {
                    return ldIndex;
                }
                else
                {
                    return -1;
                }
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
//		for(int dir = 0; dir < 9; dir++)
//		{
//			Point checkp = new Point(info.player.gridMapX + difX[dir], info.player.gridMapY + difY[dir]);
//			// チェックした座標に敵がいるとき
//			if(info.mapUnit[checkp.y][checkp.x] == 3)
//			{
//				for(int eNum = 0; eNum < info.enemy.length; eNum++)
//				{
//					//
//					if(info.enemy[eNum].gridMapX == checkp.x && info.enemy[eNum].gridMapY == checkp.y)
//					{
//						AroundEnemy aenemy = new AroundEnemy();
//						aenemy.e = info.enemy[eNum];
//						aenemy.dis = 1;
//						aenemy.dir = dir;
//						aroundEnemy.add(aenemy);
//						break;
//					}
//				}
//			}
//		}
            
            
                AroundEnemy aenemy;
                for(int dir = 0; dir < 9; dir++)
                {
                    Point checkp = new Point(info.player.gridMapX + difX[dir], info.player.gridMapY + difY[dir]);
                    
                    for(int eNum = 0; eNum < info.visibleEnemy.size(); eNum++)
                    {
                            if(info.visibleEnemy.get(eNum).gridMapX == checkp.x && info.visibleEnemy.get(eNum).gridMapY == checkp.y)
                            {
                                    aenemy = new AroundEnemy();
                                    aenemy.e = info.visibleEnemy.get(eNum);
                                    aenemy.dis = 1;
                                    aenemy.dir = dir;
                                    aroundEnemy.add(aenemy);
                                    break;
                            }
                    }
                }
            
		//System.out.println("aroundEnemy:" + aroundEnemy.size());
		return aroundEnemy.size() > 0 ? true : false;
	}

        public boolean is1gridMosCheckfsimu(Info info, ArrayList<AroundEnemy> aroundEnemy)
	{            
                for(int dir = 0; dir < 9; dir++)
                {
                    Point checkp = new Point(info.player.gridMapX + difX[dir], info.player.gridMapY + difY[dir]);
                    
                    for(int eNum = 0; eNum < info.enemy.length; eNum++)
                    {
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
//		for(int y=0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
//		{
//			for(int x=0; x < MyCanvas.MAPGRIDSIZE_X; x++)
//			{
//				if(info.pCurmap[y][x] == true && info.mapUnit[y][x] == 3)
//				{
//					for(int eNum = 0; eNum < info.enemy.length; eNum++)
//					{
//						if(info.enemy[eNum].gridMapX == x && info.enemy[eNum].gridMapY == y)
//						{
//							aenemy.e = info.enemy[eNum];
//							Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
//							Point ep = new Point(aenemy.e.gridMapX, aenemy.e.gridMapY);
//							int absDisx = Math.abs(pp.x - ep.x);
//							int absDisy = Math.abs(pp.y - ep.y);
//							aenemy.dis = absDisx > absDisy ? absDisx : absDisy;
//							aenemy.dir = convPos2Dir(ep, pp);
//							aroundEnemy.add(aenemy);
//							break;
//						}
//					}
//				}
//			}
//		}

                for(int eNum = 0; eNum < info.visibleEnemy.size(); eNum++)
                {
                        aenemy = new AroundEnemy();
                        aenemy.e = info.visibleEnemy.get(eNum);
                        Point pp = new Point(info.player.gridMapX, info.player.gridMapY);
                        Point ep = new Point(aenemy.e.gridMapX, aenemy.e.gridMapY);
                        int absDisx = Math.abs(pp.x - ep.x);
                        int absDisy = Math.abs(pp.y - ep.y);
                        aenemy.dis = absDisx > absDisy ? absDisx : absDisy;
                        aenemy.dir = convPos2Dir(ep, pp);
                        aroundEnemy.add(aenemy);
                }

		return (aroundEnemy.size() > 0) ? true : false;
	}

	public Action makeAction(Info info)
	{
		Action act = new Action(info.player.dir);
		//act.difPos = new Point();

                px = info.player.gridMapX;
                py = info.player.gridMapY;
                
                act = ruleBased(info);
                //act = ruleBasedOnly(info);
                
                //RuleBasePlayer rbp_deepcopy = this.clone();
                //rbp_deepcopy.sysoutput();
                
                return act;
	}

	public static class AroundEnemy
	{
		Enemy e; // 敵の情報
		int dis; // プレイヤーからの距離
		int dir; // プレイヤーからの方向
	}

	public static class Destination
	{
		int objNum; // 種類
		Point p; // 座標
		int dis; // プレイヤーからの距離
		int dir; // プレイヤーからの方向
	}

	public static class PassPos
	{
		Point p; // 座標
		int vh; // 縦v0横h1
		int count = 0;
		int dis; // プレイヤーからの距離
		int dir; // プレイヤーからの方向
	}
}
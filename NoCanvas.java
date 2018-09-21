
import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kazuyuki.T
 */
public class NoCanvas 
{
        private ObjectSet objectset;
	private Random random;
	private Title title;
	private Background background;
        
        // ターン管理用
	private TurnManager turnmanager;

        // info -> log の管理
        private Logger logger;

        private Info info;
        
        private long start; // 処理開始前の時間を保持する
	private long end; // 実行時間を保持する

	// 実行回数
	public static final int TRYNUM = 10000;

	// 現在の階層
	public static int floorNumber = 0;
        
        // ルールベースプレイヤー
	private RuleBasePlayer rbp;
        private RuleBasePlayer_bu rbp_bu;

	private int deathCount = 0;
	private int clearCount = 0;
        private int gameCounter = 0;
        
        private boolean gameover;
        
        // ゲームの進行
        private int counter;
        
        // 初回判定
	public static boolean startFlag;

	// リザルト表示判定
	private boolean resultFlag;
        
        // シーン管理変数
	// 1:ゲームのメイン画面
	private int scene;
        
        private static final int SCENE_GAMEMAIN = 1;
        
        // ダンジョンの最下層
	// この数字の階に到達したらクリア
	public static final int TOPFLOOR = 4;
        
        	// ダンジョンのマップサイズ(グリッド)
	public static final int MAPGRIDSIZE_X = 50;
	public static final int MAPGRIDSIZE_Y = 30;
        
        // 0:死亡した階数，1:餓死の回数
	private int[] deathFloor = new int[TOPFLOOR];

	// 餓死回数
	public static int gasi = 0;
        public static int[] gasif = new int[4];
        
        private static int histCount = 0;
        
        public static int useItemPotion = 0;
	public static int useItemFood = 0;
	public static int useItemLStaff = 0;
	public static int useItemWStaff = 0;
        
        // コンストラクタ
        public NoCanvas(int lv)
        {
		random = new Random();		// 乱数
		title = new Title();
		background = new Background();

		turnmanager = new TurnManager(background, MAPGRIDSIZE_X, MAPGRIDSIZE_Y);
                
                logger = new Logger(0);

		floorNumber = 0;

		// 初期化
		for(int i = 0; i < TOPFLOOR; i++)
		{
			deathFloor[i] = 0;
                        gasif[i] = 0;
		}

                // ログの出力
		try{
                        File file = new File("log.txt");

			if (checkBeforeWritefile(file)){
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				Date date = new Date();
                                pw.print(date.toString() + "\n");
				pw.close();
			}else{
				System.out.println("ファイルに書き込めません");
			}
		}catch(IOException e){
			System.out.println(e);
		}
                
                info = new Info(MAPGRIDSIZE_X, MAPGRIDSIZE_Y);
                
		// 計測開始
		start = System.currentTimeMillis();
        }

        // 初期化
        public void init()
        {
		objectset = new ObjectSet(background);
		turnmanager = new TurnManager(background, MAPGRIDSIZE_X, MAPGRIDSIZE_Y);

		rbp = new RuleBasePlayer();
                rbp_bu = new RuleBasePlayer_bu();
                

		//シーンはタイトル画面
		//scene = SCENE_TITLE;
		scene = SCENE_GAMEMAIN;

		gameover = false;

		startFlag = false;

		resultFlag = false;

		floorNumber = 0;

		counter = 0;
        }

        // 実行
        public void run()
        {
                // counterにより経過管理
		for(counter = 0; ; counter++)
		{
                        //シーン遷移用の変数で分岐
			switch (scene)
			{
                            case SCENE_GAMEMAIN:
                                gameScene();
                                break;
                        }
                }
        }
        
        public void gameScene()
        {
                // ゲームオーバー判定
		if (objectset.isGameover() == true)
		{
			deathCount++;
			deathFloor[floorNumber]++;
                        gameCounter++;
			System.out.println("num:" + gameCounter);
			init();
		}
		// ゲームクリア判定
		else if(floorNumber == TOPFLOOR)
		{
			clearCount++;
			gameCounter++;
                        System.out.println("num:" + gameCounter);
			init();
		}
		else
                {
			// ゲーム開始直後の場合，オブジェクトを設置する
			if(startFlag == false)
			{
				Game.appendLog(floorNumber + "F");
				Game.appendLog("\n" + "[turn : " + turnmanager.getTurn() + "]");

				// マップの情報の更新
				background.mapUpdate();

				// プレイヤーの配置
				// プレイヤーの持つマップ情報の初期化・更新を含む
				objectset.setObjectRand(new String("player"));

				// オブジェクトの初期化
				objectset.initObjectsetExceptPlayer();

				// オブジェクトの配置
				// 敵
				for(int i=0; i<ObjectSet.ENEMY_MAX; i++)
				{
					objectset.setObjectRand(new String("enemy"));
				}

				// (x, y, アイテム種類)
				// 1/2の確率で4個
				int randItemNum = ObjectSet.ITEM_MAX - 1;
				if(random.nextInt(2) == 0)
				{
					if(random.nextInt(2) == 0)
					{
						randItemNum--;
					}
					else
					{
						randItemNum++;
					}
				}
				for(int i=0; i<randItemNum; i++)
				{
					objectset.setObjectRand(new String("item"));
				}

				// 階段
				// 通路の直前，部屋に入るとすぐのグリッドに生成されないように調整
				objectset.setObjectRand(new String("stair"));

				// マップ視野の描画を開始する
				CanvasMap.startDrawmap();

				startFlag = true;
                                
                                // ログに現在の状態を出力，0F以外
                                // ゲーム回数，階層数，プレイヤーの各アイテム数
                                
			}
                        
                        // ターンが経過していた場合，logへの追加
//                        info = new Info(MAPGRIDSIZE_X, MAPGRIDSIZE_Y);
//                        info.setInfo(objectset, background);
//                        logger.logappended(info);
                        
                        // 100Tごとにファイルを変更
                        int turn = turnmanager.getTurn();
                        if(turn % 100 == 0){
                            // ゲーム回数 + 分割ファイル番号
                            String fname = new String("log/log_" + gameCounter + "_" + turn / 100 + ".txt");
                            // ファイルへ出力
                            //logger.OutputFileLog(fname);
                            logger.initLog(logger.LogLevel);
                        }
                        
			// TurnManagerを用いてターン管理を行う
			// 移動・攻撃によるターン経過を管理する
			//turnmanager.turnCount(objectset, keyinput, background);

			// エージェント用
			//turnmanager.turnCount(objectset, background, rbp_bu);
			turnmanager.turnCount(objectset, background, rbp);
		}
	
		///*
                // 1ゲーム毎に終了時
                if(histCount != gameCounter)
                {
//                    // logの追加
//                    try{
//                        File file = new File("log.txt");
//        		if (checkBeforeWritefile(file)){
//                		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
//				pw.print(Game.getLog());
//				pw.close();
//			}else{
//				System.out.println("ファイルに書き込めません");
//			}
//                    }catch(IOException e){
//			System.out.println(e);
//                    }
                    
                    // logの初期化
                    Game.initLog();
                    
                    histCount = gameCounter;
                }
                // 既定のゲーム回数終了時
		if(gameCounter == TRYNUM)
		{
			// 結果をstrに
                        StringBuilder restr = new StringBuilder();
                        restr.append("試行回数" + TRYNUM + System.getProperty("line.separator"));
                        // 計測終了
			end = System.currentTimeMillis();
                        restr.append(((double) (end - start) / 1000) + "sec" + System.getProperty("line.separator"));
                        restr.append("clear:" + clearCount + System.getProperty("line.separator"));
                        restr.append("death:" + deathCount + System.getProperty("line.separator"));
                        for (int i = 0; i < TOPFLOOR; i++) {
                            restr.append("deathFloor" + i + ":" + deathFloor[i] + "(" + gasif[i] + ")" + System.getProperty("line.separator"));
                        }
                        restr.append("gasi:" + gasi + System.getProperty("line.separator"));
                        restr.append("useFood  :" + useItemFood + System.getProperty("line.separator"));
                        restr.append("usePotion:" + useItemPotion + System.getProperty("line.separator"));
                        restr.append("useLStaff:" + useItemLStaff + System.getProperty("line.separator"));
                        restr.append("useWStaff:" + useItemWStaff + System.getProperty("line.separator"));
                        
                        System.out.println(new String(restr));

                        //
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("'log'_yyyyMMdd_HHmmss'.txt'");
                        String fileName = simpleDateFormat.format(new Date(System.currentTimeMillis()));
                        logger.OutputFileLog(fileName, new String(restr));
                        
                        System.exit(0);
		}
        }
        
        private static boolean checkBeforeWritefile(File file)
	{
            if (file.exists()){
            	if (file.isFile() && file.canWrite())
		{
			return true;
		}
            }
            else{
                try{
                    file.createNewFile();
                    return true;
                }catch(IOException e){
                    System.out.println(e);
                }
            }
	    return false;
	}
}

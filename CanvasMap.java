import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

public class CanvasMap extends Canvas implements Runnable
{
	private Image imgBuf;
	private Graphics gBuf;

	// MyCanvasから参照
	private ObjectSet obj;
	private Background bg;
        
        private RuleBasePlayer rbp;
        //Point target;

	private int counter;

	// キャンバスのサイズ
	private int cSizeX;
	private int cSizeY;

        private int level;
        
	// マップ視野の描画開始のフラグ
	private static boolean startFlag;

	// コンストラクタ
	CanvasMap(int ix, int iy)
	{
		level = 0;
                
                cSizeX = ix;
		cSizeY = iy;

		startFlag = false;
	}

        public static void startDrawmap()
        {
            startFlag = true;
        }
        
        public static void endDrawmap()
        {
            startFlag = false;
        }
        
        public void setLevel(int lv)
        {
            level = lv;
        }
        
	// 初期化
	public void init(MyCanvas mc, int lv)
	{
                level = lv;
            
                rbp = mc.getrbp();
                obj = mc.getOBJinfo();
		bg = mc.getBGinfo();
		startFlag = false;
	}

	// 外部からのスレッド初期化
	public void initThread()
	{
		Thread thread = new Thread(this);
		thread.start();
	}

	// オーバーライド
	// クリア防止のため
	public void update(Graphics g)
	{
		paint(g);
	}

	// 描画
	public void paint(Graphics g)
	{
		// ちらつき防止 -> オフスクリーンバッファ使用
		// オフスクリーンバッファの内容を自分にコピー
		g.drawImage(imgBuf, 0, 0, this);
	}

	public void gBufClear(Graphics g)
	{
		g.setColor(Color.black);
		g.fillRect(0, 0, cSizeX, cSizeY);
	}

	// スレッドループ
	public void run()
	{
		//オフスクリーンバッファ作成
		imgBuf = createImage(cSizeX, cSizeY);
		gBuf = imgBuf.getGraphics();

		for(counter = 0; ; counter++)
		{
			// バッファをクリア
			gBufClear(gBuf);

			if(startFlag == true)
			{
				// プレイヤーの持つマップと比較し，描画する
				for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
				{
					for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
					{
						// プレイヤーの場合
						if(obj.getpmap(x, y) == true && bg.getMapUnit(x, y) == 6)
						{
							gBuf.setColor(Color.ORANGE);
							gBuf.fillRect(x*8, y*8, 8, 8);
							continue;
						}
						// 敵
						if(obj.getpmap(x, y) == true && bg.getMapUnit(x, y) == 3)
						{
							// 視界にない(部屋外，周囲視界になし)とき，表示しない

							// もし，同じ部屋の中ではなく，プレイヤの周囲視界内に敵が存在しないとき
							// 探索済みのマップ部分だとしても，表示を行わない
							if(obj.getpCurmap(x, y) == true)
							{
								gBuf.setColor(Color.red);
								gBuf.fillRect(x*8, y*8, 8, 8);
								continue;
							}

							// 常に見える状態にできる
							/*
							gBuf.setColor(Color.red);
							gBuf.fillRect(x*8, y*8, 8, 8);
							continue;
							*/
						}
						// アイテム
						if(obj.getpmap(x, y) == true && bg.getMapObject(x, y) == 4)
						{
							gBuf.setColor(Color.green);
							gBuf.fillRect(x*8, y*8, 8, 8);
							continue;
						}
						// 階段
						if(obj.getpmap(x, y) == true && bg.getMapObject(x, y) == 5)
						{
							gBuf.setColor(Color.gray);
							gBuf.fillRect(x*8, y*8, 8, 8);
							continue;
						}
						if(level >= 10)
						{
							Point tg = rbp.getTarget();
                                                        if(tg.x == x && tg.y == y){
                                                            gBuf.setColor(Color.yellow);
                                                            gBuf.fillRect(x*8+1, y*8+1, 6, 6);
                                                            continue;
                                                        }
						}
                                                // 通行可能な探索済みの部分
						if(obj.getpmap(x, y) == true && bg.getMap(x, y) == 0)
						{
							gBuf.setColor(Color.white);
							gBuf.fillRect(x*8, y*8, 8, 8);
							continue;
						}
					}
				}
			}

			repaint();

			try{
				// ループのウェイト
				Thread.sleep(50);
			}
			catch(InterruptedException e){}
		}
	}
}
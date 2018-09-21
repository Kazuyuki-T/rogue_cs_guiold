import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Background extends LoadImg
{
	static final int grass = 0; // 通行可
	static final int stone = 1; // 通行不可

	// map草か石垣か…
	private int map[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
	// オブジェクトの配置
	private int mapObject[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
	// ユニットの配置
	private int mapUnit[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
	// 部屋番号のみを記す
	private int mapRoomNum[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];

        // 限定されたmap
        private int limitedMap[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
        // 限定されたmapo
        private int limitedMapObj[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];        
        // 限定されたmapu
        private int limitedMapEnemy[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
        
        
        
	// 適切なimgサイズ
	int mcX = MyCanvas.MAPCHIP_MAGX;
	int mcY = MyCanvas.MAPCHIP_MAGY;

	// キャンバスのサイズ
	int fsizeX;
	int fsizeY;

	int roomNum;
	int passNum;
	ArrayList<RoomPoint> rpList = new ArrayList<RoomPoint>();
	ArrayList<PassPoint> ppList = new ArrayList<PassPoint>();

	Random random;

	public Background(int x, int y)
	{
		random = new Random();

		// マップチップの読み込み
		img = loadImage("mat/mapchip.png");
		// マップの読み込み
		//map = mapUpdate(new String("mat/d.txt"));
		mapUpdate();

		fsizeX = x;
		fsizeY = y;
	}
        
        public Background()
        {
                random = new Random();

		// マップチップの読み込み
		img = loadImage("mat/mapchip.png");
		// マップの読み込み
		//map = mapUpdate(new String("mat/d.txt"));
		mapUpdate();
        }
        
        

	// マップ
        public int[][] getMap()
        {
            return map;
        }
        
	public int getMap(int x, int y)
	{
		return map[y][x];
	}

	public void setMap(int x, int y, int num)
	{
		map[y][x] = num;
	}

        public int[][] getMapUnit()
        {
            return mapUnit;
        }
        
	public int getMapUnit(int x, int y)
	{
		return mapUnit[y][x];
	}

	public void setMapUnit(int x, int y, int num)
	{
		mapUnit[y][x] = num;
	}

        public int[][] getMapObject()
        {
            return mapObject;
        }
        
	public int getMapObject(int x, int y)
	{
		return mapObject[y][x];
	}

	public void setMapObject(int x, int y, int num)
	{
		mapObject[y][x] = num;
	}

	public int getMapRoomNum(int x, int y)
	{
		return mapRoomNum[y][x];
	}

	public void setMapRoomNum(int x, int y, int num)
	{
		mapRoomNum[y][x] = num;
	}

	public void mapObjectUpdate(Point p, String objstr)
	{
		// -1:ナシ
		// 0:
		// 1:
		// 2:
		// 3:敵
		// 4:アイテム
		// 5:階段
		// 6:プレイヤ
		// 7:
		// 8:
		// 9:

		// map+objの情報を更新
		if(objstr.equals(new String("enemy")))
		{
			mapUnit[p.y][p.x] = 3;
		}
		else if(objstr.equals(new String("item")))
		{
			mapObject[p.y][p.x] = 4;
		}
		else if(objstr.equals(new String("stair")))
		{
			mapObject[p.y][p.x] = 5;
		}
		else if(objstr.equals(new String("player")))
		{
			mapUnit[p.y][p.x] = 6;
		}
		else
		{
			mapUnit[p.y][p.x] = -1;
			mapObject[p.y][p.x] = -1;
		}
	}

        // マップの更新
	// 新しいマップの読み込み，引数のファイル
	public void mapUpdate(String fname, boolean tf)
	{
		int[][] tmp = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];
		
                // 既存のアレイリストを初期化
		rpList.clear();
		ppList.clear();
                
                tmp = loadMap(fname, tf);
		
                // 部屋番号の割り当て番号をマップ状に表記
		// 座標からマップ番号を直接読み取ることができるように
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				// すべてを-1で初期化
				mapRoomNum[y][x] = -1;

				for(int rn = 0; rn < roomNum; rn++)
				{
					// いずれかの部屋の中だった場合，部屋番号を格納
					Point[] p = new Point[2];
					p = getRoomPoint(rn);
					if(p[0].y <= y &&  y <= p[1].y && p[0].x <= x &&  x <= p[1].x)
					{
						mapRoomNum[y][x] = rn;
					}
				}
			}
		}

		// テスト
		/*
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				System.out.print(mapRoomNum[y][x] + ",");
			}
			System.out.print("\n");
		}
		*/

		// マップ+オブジェクト情報の初期化
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				mapUnit[y][x] = -1;
				mapObject[y][x] = -1;
			}
		}
                
                map = tmp;
	}

	// マップの更新
	// 新しいマップの読み込み，ランダム
	public void mapUpdate()
	{
		int[][] tmp = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];

		// 既存のアレイリストを初期化
		rpList.clear();
		ppList.clear();

		// ランダムに読み込むマップを変更
		String fname = new String("mat/d" + (random.nextInt(5) + 3) + ".txt");
                //System.out.println("map : " + fname);
		//String fname = new String("mat/d7.txt");
		tmp = loadMap(fname, true);

		// 部屋番号の割り当て番号をマップ状に表記
		// 座標からマップ番号を直接読み取ることができるように
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				// すべてを-1で初期化
				mapRoomNum[y][x] = -1;

				for(int rn = 0; rn < roomNum; rn++)
				{
					// いずれかの部屋の中だった場合，部屋番号を格納
					Point[] p = new Point[2];
					p = getRoomPoint(rn);
					if(p[0].y <= y &&  y <= p[1].y && p[0].x <= x &&  x <= p[1].x)
					{
						mapRoomNum[y][x] = rn;
					}
				}
			}
		}

		// テスト
		/*
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				System.out.print(mapRoomNum[y][x] + ",");
			}
			System.out.print("\n");
		}
		*/

		// マップ+オブジェクト情報の初期化
		for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
		{
			for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
			{
				mapUnit[y][x] = -1;
				mapObject[y][x] = -1;
			}
		}

		map =  tmp;
	}

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

	public Point[] getRoomPoint(int num)
	{
		Point[] p = new Point[2];

		p[0] = rpList.get(num).topLeft;
		p[1] = rpList.get(num).bottmRight;

		return p;
	}

	public Point[] getPassPoint(int num)
	{
		Point[] p = new Point[2];

		p[0] = ppList.get(num).topLeft;
		p[1] = ppList.get(num).bottmRight;

		return p;
	}

        // ファイルの読み込み
        // true:通路のランダム消去あり,false:通路のランダム消去なし
	public int[][] loadMap(String failname, boolean tf)
	{
		int tmp[][] = new int[MyCanvas.MAPGRIDSIZE_Y][MyCanvas.MAPGRIDSIZE_X];;

		try{
			File file = new File(failname);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);

			//一行ずつ読み込んで
			String str;
			int count;

			// 部屋数とそれらの座標の読み込み
			count = 0;
			while((str = br.readLine()) != null){
				// 部屋数の確認
				if(count == 0)
				{
					roomNum = Integer.parseInt(str);
				}
				// 座標の格納
				else
				{
					// x,y,x,yの並びを一時的に格納
					int[] tmpPoint = new int[4];
					tmpPoint = parseInts(str.split(","));

					// アレイリストに追加するために整理
					RoomPoint newrp = new RoomPoint();
					newrp.topLeft = new Point(tmpPoint[0], tmpPoint[1]);
					newrp.bottmRight = new Point(tmpPoint[2], tmpPoint[3]);
					rpList.add(newrp);
				}

				if(roomNum == count)
				{
					break;
				}
				else
				{
					count++;
				}
			}

			// 通路数とそれらの座標の読み込み
			count = 0;
			while((str = br.readLine()) != null){
				// 部屋数の確認
				if(count == 0)
				{
					passNum = Integer.parseInt(str);
				}
				// 座標の格納
				else
				{
					int[] tmpPoint = new int[4];
					tmpPoint = parseInts(str.split(","));

					PassPoint newpp = new PassPoint();
					newpp.topLeft = new Point(tmpPoint[0], tmpPoint[1]);
					newpp.bottmRight = new Point(tmpPoint[2], tmpPoint[3]);
					ppList.add(newpp);
				}

				if(passNum == count)
				{
					break;
				}
				else
				{
					count++;
				}
			}

			// マップの読み込み
			count = 0;
			while((str = br.readLine()) != null){
				//一行の内容を','で分割してそれぞれを[count=ノード番号]の２次元目の配列の要素として格納
				tmp[count] = parseInts(str.split(","));
				count++;
			}

			br.close();
		}catch(FileNotFoundException e){
		    System.out.println(e);
		}catch(IOException e){
		    System.out.println(e);
		}

		// 通路のランダム消去
		if(tf == true)
                {
                    tmp = randPassRemove(tmp);
                }

		return tmp;
	}

	public int[][] randPassRemove(int[][] tmpMap)
	{
		// 通路１本を消去，ランダムに発生
		// 通路が２本以上の時
		if(passNum >= 2)
		{
			// 1/2で発生
			if(random.nextInt(2) == 0)
			{
				int remPass = random.nextInt(passNum);

				Point pSt = ppList.get(remPass).topLeft;
				Point pEn = ppList.get(remPass).bottmRight;

				for(int y = pSt.y; y <= pEn.y; y++)
				{
					for(int x = pSt.x; x <= pEn.x; x++)
					{
						// 通行不可に変更
						tmpMap[y][x] = 1;
					}
				}

				// 通路のリストから削除
				ppList.remove(remPass);
			}
		}

		return tmpMap;
	}

	public int[] parseInts(String[] s)
	{
		// s[] = intに変換したいストリングを収めた配列
                int[] x = new int[s.length];
                for(int i = 0; i < s.length; i++)
                {
                    x[i] = Integer.parseInt(s[i]);
                }

                return x;
	}

	// マップ情報に基づきマップチップを用いて描画する
	public void drawGameBG(Graphics g, Player player)
	{
		// バッファをクリア
		g.setColor(Color.black);
		g.fillRect(0, 0, fsizeX, fsizeY);

		// プレイヤのグリッド座標
		int playerGX = player.gridMapX;
		int playerGY = player.gridMapY;

		// ↓が逆になっている
		// プレイヤーのgrid数が大きくなる
		// タイル描画のスタート位置が小さくなる

		// スクリーンに表示するグリッドサイズ
		// プレイヤ中心
		int max_X = MyCanvas.SCREENGRIDSIZE_X;
		int max_Y = MyCanvas.SCREENGRIDSIZE_Y;

		// プレイヤを中心とした範囲のみ描画
		for(int y = playerGY - max_Y/2, countY = -max_Y/2; y <= playerGY + max_Y/2; y++, countY++){
			for(int x = playerGX - max_X/2, countX = -max_X/2; x <= playerGX + max_X/2; x++, countX++){
				// マップの下限と上限
				// マップの範囲外に出た場合は黒背景
				if(y<0 || x<0 || y>=MyCanvas.MAPGRIDSIZE_Y || x>=MyCanvas.MAPGRIDSIZE_X)
				{
					continue;
				}

				// 描画は常に同じ座標から
				// プレイヤの初期位置グリッド(8,8)から逆算

				// map 0 草
				// map 1 石垣
				// map 2 ...

				// 表示方法に難あり
				// 支障はないので後回し
				// 普通に(0,0)から初めてはいかんのか？
				// プレイヤの位置にとらわれすぎでは？

				// スリーンサイズからの描画座標の算出
				int sDrowPointX = (max_X / 2 + 1 + countX) * mcX ;
				int sDrowPointY = (max_Y / 2 + 1 + countY) * mcY;
				if(map[y][x] == 0){
					g.drawImage(img, sDrowPointX - mcX/2, sDrowPointY - mcY/2,
							         sDrowPointX + mcX/2, sDrowPointY + mcY/2, 0, 0, MyCanvas.MAPCHIP_X, MyCanvas.MAPCHIP_Y, this);
				}
				else if (map[y][x] == 1){
					g.drawImage(img, sDrowPointX - mcX/2, sDrowPointY - mcY/2,
							         sDrowPointX + mcX/2, sDrowPointY + mcY/2, 64, 0, MyCanvas.MAPCHIP_X+64, MyCanvas.MAPCHIP_Y, this);
				}
			}
		}
	}

	public void drawGridBG(Graphics g)
	{
		// 必要ない部分にも網掛けあり
		// スクリーンサイズ 11*11

		// マップに対してグリッド線を表示する
		// 横の線
		for(int y=0; y<MyCanvas.SCREENGRIDSIZE_Y; y++){
			g.setColor(Color.black);
			// fillRect(x, y, width, height)
			// x~x+width-1,y~y+height-1
			g.fillRect(mcX/2 - 1, y*mcY + mcY/2 - 1, MyCanvas.SCREENGRIDSIZE_X * mcX, 2);
		}
		// 縦の線
		for(int x=0; x<MyCanvas.SCREENGRIDSIZE_X; x++){
			g.setColor(Color.black);
			g.fillRect(x*mcX + mcX/2 - 1, mcY/2 - 1, 2, MyCanvas.SCREENGRIDSIZE_Y * mcY);
		}
	}
}


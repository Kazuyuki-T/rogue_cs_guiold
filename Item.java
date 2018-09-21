import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class Item extends Object implements Cloneable
{
	Player player;
	Random random;

	int id; // アイテムの種類

	// アイテムの詳細
	static final int ITEM_ID = 0;
	static final int ITEM_TYPE = 1;
	static final int ITEM_NAME = 2;
	static final int ITEM_HP = 3;
	static final int ITEM_SATIETY = 4;
	static final int ITEM_RANGE = 5;
	static final int ITEM_DAMEGE = 6;
	static final int ITEM_EFFECT = 7;
	static final int ITEM_USAGECOUNT = 8;

	// csvファイルから読み込んだアイテム情報
	static final int ITEMSHEET_COLUMN = 9; // 要素
	static final int ITEMSHEET_ROW = 5; // アイテム種+1
	static String[][] allItemInfo = new String[ITEMSHEET_ROW][ITEMSHEET_COLUMN];

	public Item clone(Player iplayer)
	{
		Item item = new Item();
		try{
			//item = (Item)super.clone();
			item.player = iplayer;
                        
                                                item.active = this.active;
                        item.gridMapX = this.gridMapX;
                        item.gridMapY = this.gridMapY;
                        item.gridScrX = this.gridScrX;
                        item.gridScrY = this.gridScrY;
                        item.x = this.x;
                        item.y = this.y;
                        item.id = this.id;
                        item.img = this.img;
                        item.sizeX = this.sizeX;
                        item.sizeY = this.sizeY;
                        item.sizeMagX = this.sizeMagX;
                        item.sizeMagY = this.sizeMagY;
		}catch(Exception e){
			e.printStackTrace();
		}
		return item;
	}

	Item(){}

	// コンストラクタ
	Item(Player iplayer){
		// プレイヤー情報
		player = iplayer;

		// 初期化時activeでない
		active = false;

		// ランダム
		random = new Random();
	}

	static void setItemInfo()
	{
		try {
			File f = new File("mat/item.csv");
			BufferedReader br = new BufferedReader(new FileReader(f));

			String line = br.readLine();
			for (int row = 0; line != null; row++) {
				allItemInfo[row] = line.split(",", 0);
				line = br.readLine();
			}
			br.close();

			// CSVから読み込んだ配列の中身を表示
			//for(int row = 0; row < data.length; row++) {
			//	for(int col = 0; col < data[row].length; col++) {
			//		System.out.println(data[row][col] + ", ");
			//	}
			//	System.out.println("\n");
			//}
		} catch (IOException e) {
			System.out.println(e);
		}

		//System.out.println("setAllItemInfo");
	}

	// インスタンス化
	public void activate(int ix, int iy, int idn)
	{
		active = true;

		gridMapX = ix;
		gridMapY = iy;
		gridScrX = player.gridScrX - player.gridMapX + gridMapX;
		gridScrY = player.gridScrY - player.gridMapY + gridMapY;
		x = gridWinX2x(gridScrX);
		y = gridWinY2y(gridScrY);

		// アイテムの種類
		id = idn;

		String imgfName = new String("mat/item" + idn + ".png");
		img = loadImage(imgfName);

		sizeX = img.getWidth(this);
		sizeY = img.getHeight(this);
		sizeMagX = MyCanvas.MAPCHIP_MAGX;
		sizeMagY = MyCanvas.MAPCHIP_MAGY;
	}

	void draw(Graphics g)
	{
		// ウィンドウ上の座標の再計算
		gridScrX = player.gridScrX - player.gridMapX + gridMapX;
		gridScrY = player.gridScrY - player.gridMapY + gridMapY;
		// ウィンドウ上での実際の座標の再計算
		x = gridWinX2x(gridScrX);
		y = gridWinY2y(gridScrY);

		// ウィンドウ上に存在する場合
		if(0 < gridScrX && gridScrX <= MyCanvas.SCREENGRIDSIZE_X && 0 < gridScrY && gridScrY <= MyCanvas.SCREENGRIDSIZE_Y)
		{
			// アクティブな場合
			if(active == true)
			{
				// 読み込んだ画像の出力
				g.drawImage(img, x - sizeMagX/2, y - sizeMagY/2, x + sizeMagX/2, y + sizeMagY/2, 0, 0, sizeX, sizeY, this);
			}
		}
	}
}
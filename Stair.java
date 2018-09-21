import java.awt.Graphics;

public class Stair extends Object implements Cloneable
{
	Player player;

	public Stair clone(Player iplayer)
	{
		Stair stair = new Stair();
		try{
			//stair = (Stair)super.clone();
			stair.player = iplayer;
                        
                        stair.active = this.active;
                        stair.img = this.img;
                        stair.sizeX = this.sizeX;
                        stair.sizeY = this.sizeY;
                        stair.sizeMagX = this.sizeMagX;
                        stair.sizeMagY = this.sizeMagY;
                        stair.gridMapX = this.gridMapX;
                        stair.gridMapY = this.gridMapY;
                        stair.gridScrX = this.gridScrX;
                        stair.gridScrY = this.gridScrY;
                        stair.x = this.x;
                        stair.y = this.y;
		}catch(Exception e){
			e.printStackTrace();
		}
		return stair;
	}

	Stair(){}

	// コンストラクタ
	Stair(Player iplayer)
	{
		player = iplayer;

		active = false;

		img = loadImage("mat/stair.png"); // 階段画像読み込み
		sizeX = img.getWidth(this);
		sizeY = img.getHeight(this);
		sizeMagX = MyCanvas.MAPCHIP_MAGX;
		sizeMagY = MyCanvas.MAPCHIP_MAGY;
	}

	// インスタンス化
	public void activate(int ix, int iy)
	{
		active = true;

		gridMapX = ix;
		gridMapY = iy;
		gridScrX = player.gridScrX - player.gridMapX + gridMapX;
		gridScrY = player.gridScrY - player.gridMapY + gridMapY;
		x = gridWinX2x(gridScrX);
		y = gridWinY2y(gridScrY);
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
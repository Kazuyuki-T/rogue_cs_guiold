import java.awt.Graphics;

public abstract class Object extends LoadImg
{
	public String objName;
	public int objNum;

	// アクティブ
	// 画面内に存在するとき
	public boolean active;

	// 座標
	public int x;
	public int y;

	// ウィンドウ上のグリッド座標
	public int gridScrX;
	public int gridScrY;

	// マップ上のグリッド座標
	public int gridMapX;
	public int gridMapY;

	// 本来のimgサイズ
	public int sizeX;
	public int sizeY;

	// 倍率をかけたimgサイズ
	public int sizeMagX;
	public int sizeMagY;

	// 描画
	// 抽象メソッド,サブクラスにより定義
	abstract void draw(Graphics g);

	// グリッド座標を座標に変更
	public int gridWinX2x(int gwx)
	{
		return gwx * MyCanvas.MAPCHIP_MAGX;
	}

	public int gridWinY2y(int gwy)
	{
		return gwy * MyCanvas.MAPCHIP_MAGY;
	}

	public String getName()
	{
		return objName;
	}
}
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Title
{
	// アニメーション用カウンタ
	int count;
	Font titleFont;
	Font infoFont;

	// コンストラクタ
	Title()
	{
		count = 0;
		titleFont = new Font("Alial", Font.BOLD, 50);
		infoFont = new Font("sansserif", Font.BOLD, 11);
	}

	// ゲームオーバーの描画処理
	public void drawGameover(Graphics g)
	{
		g.setColor(Color.white);
		count++;
		g.setFont(titleFont);
		g.drawString("GAMEOVER", 100, 240);
	}

	// ゲームクリアの描画処理
	public void drawClear(Graphics g)
	{
		g.setColor(Color.white);
		count++;
		g.setFont(titleFont);
		g.drawString("GAMECLEAR!!", 100, 240);
	}
}


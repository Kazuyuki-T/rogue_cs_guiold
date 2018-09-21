import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;


public abstract class LoadImg extends Canvas
{
	// オブジェクトの持つ外見イメージ
	public BufferedImage img;

	// イメージのロード
	BufferedImage loadImage(String name)
	{
		try{
			FileInputStream in = new FileInputStream(name);// FileInputStream
			BufferedImage rv = ImageIO.read(in);//
			in.close();//
			return rv;//
		}catch(IOException e){
			System.out.println("Err e=" + e);//
			return null;// null
		}
	}
}
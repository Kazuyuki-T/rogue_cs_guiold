import java.awt.Point;
import java.util.ArrayList;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Kazuyuki.T
 */
public class SimuSupporter 
{
    public static Point p = new Point();
    public static ArrayList<Point> posList = new ArrayList<Point>();

    // コンストラクタ
    public SimuSupporter()
    {
        p = new Point();
        posList.clear();
    }
    
    public void setInfo(int[][] map, int[][] maprn, int playerrn)
    {
        for(int y = 0; y < MyCanvas.MAPGRIDSIZE_Y; y++)
        {
            for(int x = 0; x < MyCanvas.MAPGRIDSIZE_X; x++)
            {
                //
                if(map[y][x] == -100 && maprn[y][x] != -1 && maprn[y][x] != playerrn)
                {
                    posList.add(new Point(x, y));
                }
            }
        }
    }
}

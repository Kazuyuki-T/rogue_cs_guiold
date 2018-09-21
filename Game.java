import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;

public class Game extends JFrame
{
	// サブマップ
	public static CanvasMap canvm;

	// ログ
	private static JTextArea text;
        private JScrollPane scrollpane;
	private static JViewport view;
        
        private JFrame frame;

        private int fsizeX;
	private int fsizeY;

	// テキスト表示用の欄を設ける
	private GridBagLayout layout;
	private JPanel p;
		
	private GridBagConstraints gbc;

	private JMenuBar menubar;
        private JMenu menu1;
        private JMenu menu2;
        private JMenu menu3;
        private JMenu menu4;
        private JMenuItem menuitem1;
        private JMenuItem menuitem2;
        private JMenuItem menuitem3;
        
        // キャンバス
        // メインループ
        private MyCanvas mc;
        
	public static void main(String args[])
	{
		int level = 0;

                // フレーム（出力）の有無の決定
                Game game = new Game(level);
	}

        public Game(int lv)
        {
                // フレーム出力あり
                // 入力受付あり
                if(lv == 0)
                {
                    // 0:表示なし
                    // 10:表示あり
                    int oplv = 0;
                    // フレームの作成
                    // 出力の有無の選択など
                    createGameFrame(oplv);
                    // ゲームの開始（スレッドの稼働）
                    startGame(oplv);
                }
                // フレーム出力なし
                // 入力受付なし
                else
                {
                    // 初期化
                    NoCanvas nc = new NoCanvas(0);
                    // 初期化
                    nc.init();
                    // ゲームの開始
                    nc.run();
                }
        }
        
        public void startGame(int oplv)
        {
                // マルチスレッドで描画を行う
		// 1:プレイヤー中心に表示する周囲視野
		// 2:探索済みのマップを表示するマップ視野

		// 周囲視野
                // 引数：
		mc.init(); // ゲームデータの初期化
		mc.initThread(); // スレッドを作成

		// マップ視野
                if(oplv > 0)
                {
                    canvm.init(mc, 10);
                    canvm.initThread();
                }
        }
        
	// コンストラクタ
	public void createGameFrame(int oplv)
	{
		frame = new JFrame();

		fsizeX = 1024;
		fsizeY = 586;

		// テキスト表示用の欄を設ける
		layout = new GridBagLayout();
		p = new JPanel();
		p.setLayout(layout);

		gbc = new GridBagConstraints();

		menubar = new JMenuBar();
                menu1 = new JMenu("File");
                menu2 = new JMenu("Menu");
                menu3 = new JMenu("Setting");
                menu4 = new JMenu("Option");
                menubar.add(menu1);
                menubar.add(menu2);
                menubar.add(menu3);
                menubar.add(menu4);
                menuitem1 = new JMenuItem("New");
                menuitem2 = new JMenuItem("Open");
                menuitem3 = new JMenuItem("Close");
                menu1.add(menuitem1);
                menu1.add(menuitem2);
                menu1.add(menuitem3);
                // フレームにメニューバーを追加
                frame.setJMenuBar(menubar);

                
                
		// キャンパスの作成(周囲視野)
		mc = new MyCanvas(fsizeX - 500, fsizeY, oplv);
                
                
                
		// 以下，抜くと描画が不可？サイズの変化が原因か
		mc.setPreferredSize(new Dimension(fsizeX - 500, fsizeY)); // 適切なサイズの設定
		mc.setMinimumSize(new Dimension(fsizeX - 500, fsizeY)); // 最小サイズの設定
		mc.setMaximumSize(new Dimension(fsizeX - 500, fsizeY)); // 最大サイズの設定
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.weightx = 1.0d;
		gbc.weighty = 0.0d;
		layout.setConstraints(mc, gbc);

		// キャンバスの作成(マップ視野)
		canvm = new CanvasMap(400, 250);
		canvm.setPreferredSize(new Dimension(400, 250));
		canvm.setMinimumSize(new Dimension(400, 250));
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.weightx = 1.0d;
		gbc.weighty = 1.0d;
		layout.setConstraints(canvm, gbc);

		text = new JTextArea();
		text.append("*-- log --*\n");
		scrollpane = new JScrollPane();
		scrollpane.setPreferredSize(new Dimension(400, 200));
		scrollpane.setMinimumSize(new Dimension(400, 200));
		text.setEditable(false);
		text.setLineWrap(true); // 折り返しアリ

		view = scrollpane.getViewport();
		view.setView(text);

		// 初期値
		view.setViewPosition(new Point(0,0));

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0d;
		gbc.weighty = 1.0d;
		layout.setConstraints(scrollpane, gbc);

		// パネルに追加
		p.add(canvm);
		//p.add(scrollpane);
		p.add(mc);

		// 実際のサイズはもう少し小さい
		frame.setSize(fsizeX, fsizeY); // ウィンドウのサイズ
		frame.setResizable(false); // サイズ変更不可
		frame.setTitle("rogelike-kari-"); // タイトル

		// クローズ
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(p, BorderLayout.CENTER);
		frame.setVisible(true); // ウィンドウの表示
        }
        
	// ログの更新
	public static void appendLog(String str)
	{
		// 末尾に str を追加
		//text.append(str + "\n");
		// 16:1行当たりのおおよその高さ
		//int textPosY = text.getPreferredSize().height - 200;
		//view.setViewPosition(new Point(0, textPosY >= 0 ? textPosY : 0));
	}

	public static String getLog()
	{
		return text.getText();
	}
        
        public static void initLog()
	{
		text.setText("");
	}
}


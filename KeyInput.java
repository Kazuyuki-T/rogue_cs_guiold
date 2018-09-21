import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter
{
	//キーボード入力の状態を保持するフィールド
	boolean keyUp;
	boolean keyDown;
	boolean keyLeft;
	boolean keyRight;
	boolean keyEnter;

	boolean keyA;
	boolean keyC;
	boolean keyE;
	boolean keyS;
	boolean keyT;
	boolean keyU;
	boolean keyW;

	boolean keyShift;
	boolean keyCtrl;
	boolean keyAlt;

	// 押された瞬間を判別するため、0-2の値をとる
	// 0:押されていない 1:押されている 2:ついさっき押されたばかり
	int keySPshot;
	int keyUPshot;
	int keyDOshot;
	int keyLEshot;
	int keyRIshot;
	int keyENshot;

        
	int keyAshot;
	int keyCshot;
	int keyEshot;
	int keyTshot;
	int keyUshot;
        int keyWshot;

	KeyInput()
	{
		keyUp = false;
		keyDown = false;
		keyLeft = false;
		keyRight = false;
		keyEnter = false;

		keyA = false;
		keyC = false;
		keyE = false;
		keyS = false;
		keyT = false;
		keyU = false;
		keyW = false;

		keyShift = false;
		keyCtrl = false;
		keyAlt = false;

		keySPshot = 0;
		keyUPshot = 0;
		keyDOshot = 0;
		keyLEshot = 0;
		keyRIshot = 0;
		keyENshot = 0;

                keyAshot = 0;
		keyCshot = 0;
		keyEshot = 0;
		keyTshot = 0;
		keyUshot = 0;
                keyWshot = 0;
	}

	// キーが押されたときに呼ばれる処理。
	public void keyPressed(KeyEvent e)
	{
		int keycode = e.getKeyCode();

		// 拡張修飾子のマスクを返す
		// イベントが発生した直後のctrl,alt,マウスなど
		// すべてのモーダルキーの状態を表す
		int mod = e.getModifiersEx();

		// Shift同時押し
		if ((mod & InputEvent.SHIFT_DOWN_MASK) != 0){
			keyShift = true;
		}
		// Ctrl
		if ((mod & InputEvent.CTRL_DOWN_MASK) != 0){
			keyCtrl = true;
		}
		// Alt
		if ((mod & InputEvent.ALT_DOWN_MASK) != 0){
			keyAlt = true;
		}

		if (keycode == KeyEvent.VK_LEFT)
		{
			keyLeft = true;
			if(keyLEshot == 0){
				keyLEshot = 2;
			}
			else{
				keyLEshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_RIGHT)
		{
			keyRight = true;
			if(keyRIshot == 0){
				keyRIshot = 2;
			}
			else{
				keyRIshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_UP)
		{
			keyUp = true;
			if(keyUPshot == 0){
				keyUPshot = 2;
			}
			else{
				keyUPshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_DOWN)
		{
			keyDown = true;
			if(keyDOshot == 0){
				keyDOshot = 2;
			}
			else{
				keyDOshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_ENTER)
		{
			keyEnter = true;
			if(keyENshot == 0){
				keyENshot = 2;
			}
			else{
				keyENshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_A)
		{
			keyA = true;
                        if(keyAshot == 0){
				keyAshot = 2;
			}
			else{
				keyAshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_C)
		{
			keyC = true;
			if(keyCshot == 0){
				keyCshot = 2;
			}
			else{
				keyCshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_E)
		{
			keyE = true;
			if(keyEshot == 0){
				keyEshot = 2;
			}
			else{
				keyEshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_S)
		{
			keyS = true;
		}
		if (keycode == KeyEvent.VK_T)
		{
			keyT = true;
			if(keyTshot == 0){
				keyTshot = 2;
			}
			else{
				keyTshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_U)
		{
			keyU = true;
			if(keyUshot == 0){
				keyUshot = 2;
			}
			else{
				keyUshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_W)
		{
			keyW = true;
                        if(keyWshot == 0){
				keyWshot = 2;
			}
			else{
				keyWshot = 1;
			}
		}
		if (keycode == KeyEvent.VK_SPACE)
		{
			// 初めて押された
			if (keySPshot == 0)
			{
				// 押された瞬間を表すフラグ
				keySPshot = 2;
			}
			else
			{
				// 押されている状態
				keySPshot = 1;
			}
		}

		if (keycode == KeyEvent.VK_ESCAPE)
		{
			System.exit(0);
		}
	}

	// 押されていたキーを放したときに呼ばれる処理
	public void keyReleased(KeyEvent e)
	{
		int keycode = e.getKeyCode();
		if (keycode == KeyEvent.VK_LEFT)
		{
			keyLeft = false;
			keyLEshot = 0;
		}
		if (keycode == KeyEvent.VK_RIGHT)
		{
			keyRight = false;
			keyRIshot = 0;
		}
		if (keycode == KeyEvent.VK_UP)
		{
			keyUp = false;
			keyUPshot = 0;
		}
		if (keycode == KeyEvent.VK_DOWN)
		{
			keyDown = false;
			keyDOshot = 0;
		}
		if (keycode == KeyEvent.VK_ENTER)
		{
			keyEnter = false;
			keyENshot = 0;
		}
		if (keycode == KeyEvent.VK_A)
		{
			keyA = false;
                        keyAshot = 0;
		}
		if (keycode == KeyEvent.VK_C)
		{
			keyC = false;
			keyCshot = 0;
		}
		if (keycode == KeyEvent.VK_E)
		{
			keyE = false;
			keyEshot = 0;
		}
                if (keycode == KeyEvent.VK_S)
		{
			keyS = false;
		}
		if (keycode == KeyEvent.VK_T)
		{
			keyT = false;
			keyTshot = 0;
		}
                if (keycode == KeyEvent.VK_U)
		{
			keyU = false;
			keyUshot = 0;
		}
                if (keycode == KeyEvent.VK_W)
		{
			keyW = false;
			keyWshot = 0;
		}

		if (keycode == KeyEvent.VK_SPACE)
		{
			
			keySPshot = 0;
		}
	}

        public int checkAShotKey()
	{
		int ret = keyAshot;
		if(keyAshot == 2)
		{
			keyAshot = 1;
		}
		return ret;
	}
        
	public int checkCShotKey()
	{
		int ret = keyCshot;
		if(keyCshot == 2)
		{
			keyCshot = 1;
		}
		return ret;
	}

	public int checkEShotKey()
	{
		int ret = keyEshot;
		if(keyEshot == 2)
		{
			keyEshot = 1;
		}
		return ret;
	}

	public int checkTShotKey()
	{
		int ret = keyTshot;
		if(keyTshot == 2)
		{
			keyTshot = 1;
		}
		return ret;
	}

	public int checkUShotKey()
	{
		int ret = keyUshot;
		if(keyUshot == 2)
		{
			keyUshot = 1;
		}
		return ret;
	}
        
        public int checkWShotKey()
	{
		int ret = keyWshot;
		if(keyWshot == 2)
		{
			keyWshot = 1;
		}
		return ret;
	}

	public int checkLeftShotKey()
	{
		int ret = keyLEshot;
		if(keyLEshot == 2)
		{
			keyLEshot = 1;
		}
		return ret;
	}

	public int checkRightShotKey()
	{
		int ret = keyRIshot;
		if(keyRIshot == 2)
		{
			keyRIshot = 1;
		}
		return ret;
	}

	public int checkUpShotKey()
	{
		int ret = keyUPshot;
		if(keyUPshot == 2)
		{
			keyUPshot = 1;
		}
		return ret;
	}

	public int checkDownShotKey()
	{
		int ret = keyDOshot;
		if(keyDOshot == 2)
		{
			keyDOshot = 1;
		}
		return ret;
	}

	public int checkEnterShotKey()
	{
		int ret = keyENshot;
		if(keyENshot == 2)
		{
			keyENshot = 1;
		}
		return ret;
	}

	// ショットボタン（＝スペースキー）の状態を取得する
	// 0:押されていない 1:押されている 2:ついさっき押されたばかり
	public int checkSpaceShotKey()
	{
		int ret = keySPshot;
		if (keySPshot == 2)
		{
			keySPshot = 1;
		}
		return ret;
	}
}

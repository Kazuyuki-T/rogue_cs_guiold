// ゲームのフィールド上に存在するあたり判定のあるもののスーパークラスとなる
public abstract class Unit extends Object
{
	// ターン中のアクションの有無
	public boolean action_flag;

	// 向いている方向
	// テンキーに倣う
	// 7 8 9
	// 4 5 6
	// 1 2 3
	public int dir;

	// 速さ
	public int speed;

	// 体力
	// 0になると消滅
	public int maxHp;
	public int hp;

	// 攻撃力
	public int attack;

	// 視界
	int view;

	// 移動
	// 抽象メソッド,サブクラスにより定義
	abstract void moveobj();

	// 引数のユニットから攻撃されたときのダメージ計算
	public void damageCalc(Unit u, Background bg)
	{
		// hpから攻撃力分のダメージを引く
		hp -= u.attack;

		if(hp <= 0)
		{
			hp = 0;
			active = false;
			//Background.mapUnit[gridMapY][gridMapX] = -1;
			bg.setMapUnit(gridMapX, gridMapY, -1);
			//Game.appendRog("beat " + getName());
		}
	}

	// 引数のユニットから攻撃されたときのダメージ計算
	public void damageCalc(Unit u, Info info)
	{
		// hpから攻撃力分のダメージを引く
		hp -= u.attack;

		if(hp <= 0)
		{
			hp = 0;
			active = false;
			//bgSimu.mapUnit[gridMapY][gridMapX] = -1;
			info.mapUnit[gridMapY][gridMapX] = -1;
			//Game.appendRog("beat " + getName());
		}
	}

	// 攻撃されたときのダメージ計算
	public void damageCalc(int dam, Background bg)
	{
		// hpから攻撃力分のダメージを引く
		hp -= dam;

		if(hp <= 0)
		{
			hp = 0;
			active = false;

			//Background.mapUnit[gridMapY][gridMapX] = -1;
			bg.setMapUnit(gridMapX, gridMapY, -1);
			//Game.appendRog("beat " + getName());
		}
	}

	public void damageCalc(int dam, Info info)
	{
		// hpから攻撃力分のダメージを引く
		hp -= dam;

		if(hp <= 0)
		{
			hp = 0;
			active = false;

			//bgSimu.mapUnit[gridMapY][gridMapX] = -1;
			info.mapUnit[gridMapY][gridMapX] = -1;
			//Game.appendRog("beat " + getName());
		}
	}

	abstract public boolean isNextMoveCheck(int nx, int ny);

	// 斜め移動の可否
	// 真：通行不可
	// 偽：通行可
	public boolean isDiagonalMoveCheck(int mx, int my, Background bg)
	{
		//
		if(//Background.map[gridMapY][gridMapX + mx] == 1 ||
		   bg.getMap(gridMapX + mx, gridMapY) == 1 ||
		   //Background.map[gridMapY + my][gridMapX] == 1)
		   bg.getMap(gridMapX, gridMapY + my) == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// 斜め攻撃の可否
	public boolean isDiagonalAtkCheck(int mx, int my, Background bg)
	{
		if(//Background.map[gridMapY][gridMapX + mx] == 1 ||
		   bg.getMap(gridMapX + mx, gridMapY) == 1 ||
		   //Background.map[gridMapY + my][gridMapX] == 1)
		   bg.getMap(gridMapX, gridMapY + my) == 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}

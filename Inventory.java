import java.awt.Point;
import java.util.ArrayList;

public class Inventory implements Cloneable
{
	static final int MAX_INV = 20;

	// 所持アイテムのリスト
	ArrayList<ItemData> itemList = new ArrayList<ItemData>();

	public Inventory clone()
	{
		Inventory inventory = new Inventory();
		try{
			//inventory = (Inventory)super.clone();
			inventory.itemList = itemListDeepCopy();
		}catch(Exception e){
			e.printStackTrace();
		}
		return inventory;
	}

	// deep-copy
	public ArrayList<ItemData> itemListDeepCopy()
	{
		ArrayList<ItemData> iList = new ArrayList<ItemData>();

		for(int index = 0; index < itemList.size(); index++)
		{
			ItemData idata = new ItemData();

			idata.id = itemList.get(index).id;
			idata.type = itemList.get(index).type;
			idata.name = itemList.get(index).name;
			idata.hpHealVal = itemList.get(index).hpHealVal;
			idata.satietyHealVal = itemList.get(index).satietyHealVal;
			idata.range = itemList.get(index).range;
			idata.damage = itemList.get(index).damage;
			idata.effect = itemList.get(index).effect;
			idata.usageCount = itemList.get(index).usageCount;

			iList.add(idata);
		}

		return iList;
	}

	// コンストラクタ
	Inventory()
	{
		//
		itemList.clear();
	}

	// 所持アイテム数を返す
	public int getInvItemNum()
	{
		return itemList.size();
	}

	/*
	// 所持アイテム数を返す
	public int getInvItemNum(ObjectSetSimulator objSimu)
	{
		return objSimu.player.inventory.itemList.size();
	}
	*/

	// 仮実装
	public int getInvItemInfo(int num)
	{
		return 0;
	}

	// 与えられたインデックスのアイテムの名前を返す
	public String getInvItemName(int num)
	{
		return itemList.get(num).name;
	}

	/*
	// 与えられたインデックスのアイテムの名前を返す
	public String getInvItemName(int num, ObjectSetSimulator objSimu)
	{
		return objSimu.player.inventory.itemList.get(num).name;
	}
	*/

	// 与えられたインデックスのアイテムの使用回数を返す
	public int getInvItemUsageCount(int num)
	{
		return itemList.get(num).usageCount;
	}

	/*
	// 与えられたインデックスのアイテムの使用回数を返す
	public int getInvItemUsageCount(int num, ObjectSetSimulator objSimu)
	{
		return objSimu.player.inventory.itemList.get(num).usageCount;
	}
	*/

	// 与えられたインデックスのアイテムを消去する
	public void removeItem(int num)
	{
		//Game.appendRog(itemList.get(num).usageCount + " -> " + (itemList.get(num).usageCount - 1));
		itemList.get(num).usageCount--;
		if(itemList.get(num).usageCount == 0)
		{
			Game.appendLog("remove Item : " + itemList.get(num).name + "(slot" + num + ")");
			itemList.remove(num);
		}
	}

	//
	public void useItem(ObjectSet objectset, int num, Background bg)
	{
		// インベントリ何番目のなんというアイテムを使用したか
		String iName = getInvItemName(num);
		Game.appendLog("use item : " + iName + "(slot " + num + ")");

		// 使用したアイテムのタイプが食料の時
		if(itemList.get(num).type.equals(new String("food")))
		{
			int oldPlayerHp = objectset.player.hp;
			// 選択されたアイテムによるHPの回復
			objectset.player.hp += itemList.get(num).hpHealVal;
			// 上限を越さないように
			if(objectset.player.hp > objectset.player.maxHp)
			{
				objectset.player.hp = objectset.player.maxHp;
			}
			Game.appendLog("HP : " + oldPlayerHp + "->" + objectset.player.hp);

			int oldPlayerSatiety = objectset.player.satiety;
			// 選択されたアイテムによる満腹度の回復
			objectset.player.satiety += itemList.get(num).satietyHealVal;
			// 上限を越さないように
			if(objectset.player.satiety > objectset.player.maxSatiety)
			{
				objectset.player.satiety = objectset.player.maxSatiety;
			}
			// ログへの出力
			Game.appendLog("SP : " + oldPlayerSatiety + "->" + objectset.player.satiety);

			MyCanvas.useItemFood++;
		}

		// 使用したアイテムのタイプがポーションの時
		else if(itemList.get(num).type.equals(new String("potion")))
		{
			int oldPlayerHp = objectset.player.hp;
			// 選択されたアイテムによるHPの回復
			objectset.player.hp += itemList.get(num).hpHealVal;
			// 上限を越さないように
			if(objectset.player.hp > objectset.player.maxHp)
			{
				objectset.player.hp = objectset.player.maxHp;
			}
			Game.appendLog("HP : " + oldPlayerHp + "->" + objectset.player.hp);

			int oldPlayerSatiety = objectset.player.satiety;
			// 選択されたアイテムによる満腹度の回復
			objectset.player.satiety += itemList.get(num).satietyHealVal;
			// 上限を越さないように
			if(objectset.player.satiety > objectset.player.maxSatiety)
			{
				objectset.player.satiety = objectset.player.maxSatiety;
			}
			// ログへの出力
			Game.appendLog("SP : " + oldPlayerSatiety + "->" + objectset.player.satiety);

			MyCanvas.useItemPotion++;
		}

		// 使用したアイテムのタイプが杖の時
		else if(itemList.get(num).type.equals(new String("staff")))
		{
			// 杖の効果
			// レンジ内に敵がいる場合

                        boolean hitflag = false;
                    
			// npに初期のプレイヤ座標を設定
			// プレイヤの方向のレンジ内に敵がいればダメージ計算
			Point np = new Point(objectset.player.gridMapX, objectset.player.gridMapY);
			for(int i = 0; i < itemList.get(num).range; i++)
			{
				// 次のマス
				np = objectset.nextGridAxis(np.x, np.y, objectset.player.dir);

				// 1マス先に敵がいるとき
				int nextMonsIndex = objectset.inNextGridMonster(np);
				if(nextMonsIndex != -1)
				{
					hitflag = true;

                                        // 使用した杖の攻撃力が0より大きいとき
					if(itemList.get(num).damage > 0)
					{
						int oldhp = ObjectSet.enemy[nextMonsIndex].hp;
						ObjectSet.enemy[nextMonsIndex].damageCalc(itemList.get(num).damage);
						int newhp = ObjectSet.enemy[nextMonsIndex].hp < 0 ? 0 : ObjectSet.enemy[nextMonsIndex].hp;
						Game.appendLog("hit!! enemy" + nextMonsIndex + ":" + oldhp + "->" + newhp);

						MyCanvas.useItemLStaff++;
					}

					// 敵を倒したか否かのチェック
					if(ObjectSet.enemy[nextMonsIndex].active == false)
					{
						// 倒した場合，経験値を得る
						objectset.player.addExp(ObjectSet.enemy[nextMonsIndex].spoint);
					}
					else
					{
						// 倒していないとき，追加効果の確認
						// 0:なし
						// 1:ワープ
						if(itemList.get(num).effect == 1)
						{
							objectset.warpUnit(ObjectSet.enemy[nextMonsIndex]);
							Game.appendLog("hit-warp!!");

							MyCanvas.useItemWStaff++;
						}
					}

					// 直近の敵に当たればよいため，残った処理はスキップ
					break;
				}
			}
                        
//                        if(hitflag == false)
//                        {
//                            System.out.println("use item miss : " + itemList.get(num).name);
//                        }
		}

		removeItem(num);
	}

        // リストへ追加
        // 確認用
        public void addItem(int itemIndex)
        {
                if(itemList.size() < MAX_INV)
		{
                    itemList.add(setDetail(itemIndex));
                }
        }
        
	// アイテムをリストへ追加
	public boolean addItem(int itemIndex, Player p)
	{
		// インベントリに空きがある場合
		if(itemList.size() < MAX_INV)
		{
			// 取得したアイテムをidから判別
			itemList.add(setDetail(ObjectSet.item[itemIndex].id));
			ObjectSet.item[itemIndex].active = false;

                        if(itemList.get(itemList.size() - 1).name.equals(new String("normal-bread")))
                        {
                            MyCanvas.getItemFood[MyCanvas.floorNumber]++;
                            p.getItemfloorFood[MyCanvas.floorNumber]++;
                        }
                        else if(itemList.get(itemList.size() - 1).name.equals(new String("normal-potion")))
                        {
                            MyCanvas.getItemPotion[MyCanvas.floorNumber]++;
                            p.getItemfloorPotion[MyCanvas.floorNumber]++;
                        }
                        else if(itemList.get(itemList.size() - 1).name.equals(new String("lightning-staff")))
                        {
                            MyCanvas.getItemLStaff[MyCanvas.floorNumber]++;
                            p.getItemfloorLStaff[MyCanvas.floorNumber]++;
                        }
                        else if(itemList.get(itemList.size() - 1).name.equals(new String("warp-staff")))
                        {
                            MyCanvas.getItemWstaff[MyCanvas.floorNumber]++;
                            p.getItemfloorWstaff[MyCanvas.floorNumber]++;
                        }
                        
                        
			// 追加されたアイテムのログ表示
			Game.appendLog("get Item : " + itemList.get(itemList.size() - 1).name);

			/*
			for(int i=0; i<ObjectSet.item.length; i++)
			{
				System.out.println(i + ":" + ObjectSet.item[i].active);
			}
			*/

			//System.out.println("inv_num : " + itemList.size());

			return true;
		}

		return false;
	}

	public void removeItem(int num, Info info)
	{
		info.player.inventory.itemList.get(num).usageCount--;

		if(info.player.inventory.itemList.get(num).usageCount <= 0)
		{
			info.player.inventory.itemList.remove(num);
		}
	}

	public void useItem(Info info, int num)
	{
		ObjectSetSimulator objSimu = new ObjectSetSimulator();

		// 使用したアイテムのタイプが食料orポーションの時
		if(info.player.inventory.itemList.get(num).type.equals(new String("food")) ||
		   info.player.inventory.itemList.get(num).type.equals(new String("potion")))
		{
			// 選択されたアイテムによるHPの回復
			info.player.hp += info.player.inventory.itemList.get(num).hpHealVal;
			// 上限を越さないように
			if(info.player.hp > info.player.maxHp)
			{
				info.player.hp = info.player.maxHp;
			}

			// 選択されたアイテムによる満腹度の回復
			info.player.satiety += info.player.inventory.itemList.get(num).satietyHealVal;
			// 上限を越さないように
			if(info.player.satiety > info.player.maxSatiety)
			{
				info.player.satiety = info.player.maxSatiety;
			}
		}

                //System.out.println("useitem st");
                
                // 使用したアイテムのタイプが杖の時
		if(info.player.inventory.itemList.get(num).type.equals(new String("staff")))
		{
			// 杖の効果
			// レンジ内に敵がいる場合

                        boolean hitflag = false;
                        // npに初期のプレイヤ座標を設定
			// プレイヤの方向のレンジ内に敵がいればダメージ計算
			Point np = new Point(info.player.gridMapX, info.player.gridMapY);
			for(int i = 0; i < info.player.inventory.itemList.get(num).range; i++)
			{
				// 次のマス
				np = objSimu.nextGridAxis(np.x, np.y, info.player.dir);

				// 1マス先に敵がいるとき
				int nextMonsIndex = objSimu.inNextGridMonster(np, info);
				if(nextMonsIndex != -1)
				{
					hitflag = true;

                                        // 使用した杖の攻撃力が0より大きいとき
					if(info.player.inventory.itemList.get(num).damage > 0)
					{
						objSimu.calcEnemyDamage(info, nextMonsIndex, info.player.inventory.itemList.get(num).damage);
					}

					// 敵を倒したか否かのチェック
					if(info.enemy[nextMonsIndex].active == false)
					{
						// 倒した場合，経験値を得る
						info.player.exp += info.enemy[nextMonsIndex].spoint;
					}
					else
					{
						
                                                //System.out.println("use staff");
                                                // 倒していないとき，追加効果の確認
						// 0:なし
						// 1:ワープ
						if(info.player.inventory.itemList.get(num).effect == 1)
						{
							objSimu.warpUnit(info, nextMonsIndex);
						}
					}

					// 直近の敵に当たればよいため，残った処理はスキップ
					break;
				}
			}
                        
//                        if(hitflag == false)
//                        {
//                            System.out.println("use item miss");
//                        }
		}
                
                //System.out.println("useitem end");
                
		removeItem(num, info);
	}

	public boolean addItem(int itemIndex, Info info)
	{
		// インベントリに空きがある場合
		if(info.player.inventory.itemList.size() < MAX_INV)
		{
			// 取得したアイテムをidから判別
			info.player.inventory.itemList.add(info.player.inventory.setDetail(info.item[itemIndex].id));
			info.item[itemIndex].active = false;

			// 追加されたアイテムのログ表示
			//Game.appendRog("get Item : " + itemList.get(itemList.size() - 1).name);

			/*
			for(int i=0; i<ObjectSet.item.length; i++)
			{
				System.out.println(i + ":" + ObjectSet.item[i].active);
			}
			*/

			//System.out.println("inv_num : " + itemList.size());

			return true;
		}

		return false;
	}

	public void sysOutItemList()
	{
		for(int i=0; i < itemList.size(); i++)
		{
			System.out.println(itemList.get(i).name);
		}
	}

	// アイテムの詳細をItemData型に格納
	public ItemData setDetail(int itemId)
	{
		ItemData newGetItem = new ItemData();
		newGetItem.id = itemId;
		newGetItem.type = Item.allItemInfo[newGetItem.id][Item.ITEM_TYPE];
		newGetItem.name = Item.allItemInfo[newGetItem.id][Item.ITEM_NAME];
		newGetItem.hpHealVal = Integer.parseInt(Item.allItemInfo[newGetItem.id][Item.ITEM_HP]);
		newGetItem.satietyHealVal = Integer.parseInt(Item.allItemInfo[newGetItem.id][Item.ITEM_SATIETY]);
		newGetItem.range = Integer.parseInt(Item.allItemInfo[newGetItem.id][Item.ITEM_RANGE]);
		newGetItem.damage = Integer.parseInt(Item.allItemInfo[newGetItem.id][Item.ITEM_DAMEGE]);
		newGetItem.effect = Integer.parseInt(Item.allItemInfo[newGetItem.id][Item.ITEM_EFFECT]);
		newGetItem.usageCount = Integer.parseInt(Item.allItemInfo[newGetItem.id][Item.ITEM_USAGECOUNT]);

		//System.out.println(newGetItem.id + ", " + newGetItem.type + ", " + newGetItem.name + ", " +
		//                   newGetItem.hpHealVal + ", " + newGetItem.satietyHealVal);

		return newGetItem;
	}

	// インナークラス
	// 所持アイテムの詳細データ
	public class ItemData implements Cloneable
	{
		int id;
		String type;
		String name;
		int hpHealVal;
		int satietyHealVal;
		int range;
		int damage;
		int effect;
		int usageCount;

		ItemData(){}
	}
        
        // 指定したidのアイテムのインベントリ内の個数を返す
        // 矢：残りの使用回数
        // その他：アイテム数
        public int getInvItemNum(int itemid)
        {
                int counter = 0;
                for(int index = 0; index < itemList.size(); index++)
		{
                        if(itemList.get(index).id == itemid)
                        {
                                if(itemid == 3)
                                {
                                    counter += itemList.get(index).usageCount;
                                }
                                else
                                {
                                    counter++;
                                }
                        }
		}
                
                return counter;
        }
        
        public int getFoodHealVal()
        {
                for(int index = 0; index < itemList.size(); index++)
		{
                        if(itemList.get(index).id == 1)
                        {
                            return itemList.get(index).satietyHealVal;
                        }
                }
                
                return 0;
        }
}
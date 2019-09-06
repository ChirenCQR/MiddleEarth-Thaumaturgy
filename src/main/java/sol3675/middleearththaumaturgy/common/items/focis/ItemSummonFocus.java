package sol3675.middleearththaumaturgy.common.items.focis;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRLevelData;
import lotr.common.entity.LOTREntities;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.fac.LOTRAlignmentValues;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import sol3675.middleearththaumaturgy.references.LibMisc;
import sol3675.middleearththaumaturgy.references.SummonList;
import sol3675.middleearththaumaturgy.references.WandType;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ItemSummonFocus extends ItemMETTFocus
{
	
	public IIcon[] icon;
	private Random rand;
	private IIcon orn;
	
	public ItemSummonFocus()
	{
		super("summon");
		this.setHasSubtypes(true);
		this.setUnlocalizedName("focus_summon");
		this.rand = new Random();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register)
	{
		icon = new IIcon[WandType.types.length];
		for (int i = 0; i<WandType.types.length; ++i)
		{
			this.icon[i] = register.registerIcon(LibMisc.MODFULLID + ":focus_" + getName() + "_" + WandType.types[i]);
		}
		orn = register.registerIcon(LibMisc.MODFULLID + ":focus_" + getName() + "_orn");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int meta) {
		return this.icon[meta];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getOrnament(ItemStack focus)
	{
		return orn;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(final Item item, final CreativeTabs tab, final List list)
	{
		for (int i = 0; i < WandType.types.length; ++i)
		{
			list.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return super.getUnlocalizedName() + "." + WandType.types[stack.getItemDamage()];
	}
	
	@Override
	public AspectList getVisCost(final ItemStack focus)
	{
		return new AspectList().add(Aspect.FIRE, 3000).add(Aspect.EARTH, 3000).add(Aspect.ORDER, 3000);
	}
	
	@Override
	public WandFocusAnimation getAnimation(ItemStack stack)
	{
		return WandFocusAnimation.WAVE;
	}
	
	@Override
	public int getFocusColor(final ItemStack focus)
	{
		return WandType.color[focus.getItemDamage()];
	}
	
	@Override
	public ItemStack onFocusRightClick(final ItemStack itemStack, final World world, final EntityPlayer player, final MovingObjectPosition pos)
	{
		final ItemWandCasting wand = (ItemWandCasting)itemStack.getItem();
		int factionId = wand.getFocusItem(itemStack).getItemDamage();
		if(pos == null)
		{
			return itemStack;
		}
		double x = pos.blockX;
		double y = pos.blockY;
		double z = pos.blockZ;
		
		float alignmentValue = LOTRLevelData.getData(player).getAlignment(WandType.faction[factionId]);
		
		if(alignmentValue >= 100.0F)
		{
			switch(pos.sideHit)
			{
				case 0:
				{
					--y;
					break;
				}
				case 1:
				{
					++y;
					break;
				}
				case 2:
				{
					--z;
					break;
				}
				case 3:
				{
					z = z + 1.5;
					break;
				}
				case 4:
				{
					--x;
					break;
				}
				case 5:
				{
					x = x + 1.5;
					break;
				}
			}
			
			SummonList summonType = SummonList.getFromID(factionId);
			SummonList.SummonSpawnEntry entry;
			if(alignmentValue < 500.0F)
			{
				entry = (SummonList.SummonSpawnEntry)WeightedRandom.getRandomItem(this.rand, summonType.MobsTier1);
			}
			else
			{
				entry = (SummonList.SummonSpawnEntry)WeightedRandom.getRandomItem(this.rand, summonType.MobsTier2);
			}
			String entityName = LOTREntities.getStringFromClass(entry.getEntityClass());
			LOTREntityNPC npc = (LOTREntityNPC)EntityList.createEntityByName(entityName, world);
			
			npc.setLocationAndAngles(x, y, z, this.rand.nextFloat() * 360.0F, 0.0F);
			npc.liftSpawnRestrictions = true;
			
			Event.Result canSpawn = ForgeEventFactory.canEntitySpawn(npc, world, (float)npc.posX, (float)npc.posY, (float)npc.posZ);
			if(((canSpawn == Event.Result.ALLOW) || ((canSpawn == Event.Result.DEFAULT)/* && (npc.getCanSpawnHere())*/)) && wand.consumeAllVis(itemStack, player, this.getVisCost(itemStack), true, false))
			{
				npc.liftSpawnRestrictions = false;
				npc.onSpawnWithEgg(null);
				npc.isNPCPersistent = false;
				
				if(!world.isRemote)
				{
					world.spawnEntityInWorld(npc);
					world.playSoundEffect(x, y, z, "thaumcraft:wand", 1.0F, 1.0F);
				}
			}
		}
		else
		{
			LOTRAlignmentValues.notifyAlignmentNotHighEnough(player, 100.0F, WandType.faction[factionId]);
		}
		
		return itemStack;
	}
}

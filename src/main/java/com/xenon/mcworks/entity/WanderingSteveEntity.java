
package com.xenon.mcworks.entity;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.World;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.network.IPacket;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.VindicatorEntity;
import net.minecraft.entity.monster.SpiderEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.SilverfishEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.block.material.Material;

import com.xenon.mcworks.entity.renderer.WanderingSteveRenderer;
import com.xenon.mcworks.McworksModElements;

@McworksModElements.ModElement.Tag
public class WanderingSteveEntity extends McworksModElements.ModElement {
	public static EntityType entity = (EntityType.Builder.<CustomEntity>create(CustomEntity::new, EntityClassification.CREATURE)
			.setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(CustomEntity::new)
			.size(0.6f, 1.8f)).build("wandering_steve").setRegistryName("wandering_steve");
	public WanderingSteveEntity(McworksModElements instance) {
		super(instance, 2);
		FMLJavaModLoadingContext.get().getModEventBus().register(new WanderingSteveRenderer.ModelRegisterHandler());
		FMLJavaModLoadingContext.get().getModEventBus().register(new EntityAttributesRegisterHandler());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> entity);
		elements.items.add(() -> new SpawnEggItem(entity, -13421569, -36752, new Item.Properties().group(ItemGroup.MISC))
				.setRegistryName("wandering_steve_spawn_egg"));
	}

	@SubscribeEvent
	public void addFeatureToBiomes(BiomeLoadingEvent event) {
		event.getSpawns().getSpawner(EntityClassification.CREATURE).add(new MobSpawnInfo.Spawners(entity, 10, 1, 10));
	}

	@Override
	public void init(FMLCommonSetupEvent event) {
		EntitySpawnPlacementRegistry.register(entity, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos,
						random) -> (world.getBlockState(pos.down()).getMaterial() == Material.ORGANIC && world.getLightSubtracted(pos, 0) > 8));
	}
	private static class EntityAttributesRegisterHandler {
		@SubscribeEvent
		public void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
			AttributeModifierMap.MutableAttribute ammma = MobEntity.func_233666_p_();
			ammma = ammma.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0.35);
			ammma = ammma.createMutableAttribute(Attributes.MAX_HEALTH, 20);
			ammma = ammma.createMutableAttribute(Attributes.ARMOR, 0);
			ammma = ammma.createMutableAttribute(Attributes.ATTACK_DAMAGE, 14);
			ammma = ammma.createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 0.2);
			ammma = ammma.createMutableAttribute(Attributes.ATTACK_KNOCKBACK, 0.5);
			event.put(entity, ammma.create());
		}
	}

	public static class CustomEntity extends CreatureEntity {
		public CustomEntity(FMLPlayMessages.SpawnEntity packet, World world) {
			this(entity, world);
		}

		public CustomEntity(EntityType<CustomEntity> type, World world) {
			super(type, world);
			experienceValue = 0;
			setNoAI(false);
			this.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
			this.setItemStackToSlot(EquipmentSlotType.OFFHAND, new ItemStack(Items.DIAMOND_SWORD));
			this.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.IRON_HELMET));
			this.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
			this.setItemStackToSlot(EquipmentSlotType.LEGS, new ItemStack(Items.IRON_LEGGINGS));
			this.setItemStackToSlot(EquipmentSlotType.FEET, new ItemStack(Items.IRON_BOOTS));
		}

		@Override
		public IPacket<?> createSpawnPacket() {
			return NetworkHooks.getEntitySpawningPacket(this);
		}

		@Override
		protected void registerGoals() {
			super.registerGoals();
			this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, ZombieEntity.class, false, false));
			this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, SkeletonEntity.class, false, false));
			this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, SpiderEntity.class, false, false));
			this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, CreeperEntity.class, false, false));
			this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, PillagerEntity.class, false, false));
			this.targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, VindicatorEntity.class, false, false));
			this.targetSelector.addGoal(7, new NearestAttackableTargetGoal(this, ShulkerBulletEntity.class, false, false));
			this.targetSelector.addGoal(8, new NearestAttackableTargetGoal(this, ShulkerEntity.class, false, false));
			this.targetSelector.addGoal(9, new NearestAttackableTargetGoal(this, SilverfishEntity.class, false, false));
			this.goalSelector.addGoal(10, new MeleeAttackGoal(this, 1.2, false));
			this.goalSelector.addGoal(11, new RandomWalkingGoal(this, 1));
			this.targetSelector.addGoal(12, new HurtByTargetGoal(this));
			this.goalSelector.addGoal(13, new LookRandomlyGoal(this));
			this.goalSelector.addGoal(14, new SwimGoal(this));
		}

		@Override
		public CreatureAttribute getCreatureAttribute() {
			return CreatureAttribute.UNDEFINED;
		}

		protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
			super.dropSpecialItems(source, looting, recentlyHitIn);
			this.entityDropItem(new ItemStack(Items.APPLE));
		}

		@Override
		public net.minecraft.util.SoundEvent getHurtSound(DamageSource ds) {
			return (net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.hurt"));
		}

		@Override
		public net.minecraft.util.SoundEvent getDeathSound() {
			return (net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
		}
	}
}

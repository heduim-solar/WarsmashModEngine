package com.etheller.warsmash.viewer5.handlers.w3x.simulation;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.Rectangle;
import com.etheller.warsmash.parsers.w3x.w3i.Player;
import com.etheller.warsmash.units.DataTable;
import com.etheller.warsmash.units.manager.MutableObjectData;
import com.etheller.warsmash.util.War3ID;
import com.etheller.warsmash.util.WarsmashConstants;
import com.etheller.warsmash.viewer5.handlers.w3x.environment.PathingGrid;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.combat.attacks.CUnitAttackInstant;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.combat.attacks.CUnitAttackMissile;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.combat.projectile.CAttackProjectile;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.data.CAbilityData;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.data.CUnitData;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.pathing.CPathfindingProcessor;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.players.CMapControl;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.players.CPlayer;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.players.CRace;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.util.SimulationRenderController;

public class CSimulation {
	private final CUnitData unitData;
	private final CAbilityData abilityData;
	private final List<CUnit> units;
	private final List<CPlayer> players;
	private final List<CAttackProjectile> projectiles;
	private final List<CAttackProjectile> newProjectiles;
	private final HandleIdAllocator handleIdAllocator;
	private transient final SimulationRenderController simulationRenderController;
	private int gameTurnTick = 0;
	private final PathingGrid pathingGrid;
	private final CWorldCollision worldCollision;
	private final CPathfindingProcessor pathfindingProcessor;
	private final CGameplayConstants gameplayConstants;
	private final Random seededRandom;

	public CSimulation(final DataTable miscData, final MutableObjectData parsedUnitData,
			final MutableObjectData parsedAbilityData, final SimulationRenderController simulationRenderController,
			final PathingGrid pathingGrid, final Rectangle entireMapBounds, final Random seededRandom,
			final List<Player> playerInfos) {
		this.gameplayConstants = new CGameplayConstants(miscData);
		this.simulationRenderController = simulationRenderController;
		this.pathingGrid = pathingGrid;
		this.unitData = new CUnitData(parsedUnitData);
		this.abilityData = new CAbilityData(parsedAbilityData);
		this.units = new ArrayList<>();
		this.projectiles = new ArrayList<>();
		this.newProjectiles = new ArrayList<>();
		this.handleIdAllocator = new HandleIdAllocator();
		this.worldCollision = new CWorldCollision(entireMapBounds, this.gameplayConstants.getMaxCollisionRadius());
		this.pathfindingProcessor = new CPathfindingProcessor(pathingGrid, this.worldCollision);
		this.seededRandom = seededRandom;
		this.players = new ArrayList<>();
		for (int i = 0; i < WarsmashConstants.MAX_PLAYERS; i++) {
			if (i < playerInfos.size()) {
				final Player playerInfo = playerInfos.get(i);
				this.players.add(new CPlayer(playerInfo.getId().getValue(), CMapControl.values()[playerInfo.getType()],
						playerInfo.getName(), CRace.parseRace(playerInfo.getRace()), playerInfo.getStartLocation()));
			}
			else {
				this.players.add(new CPlayer(i, CMapControl.NONE, "Default string", CRace.OTHER, new float[] { 0, 0 }));
			}
		}
		this.players.add(new CPlayer(this.players.size(), CMapControl.NEUTRAL,
				miscData.getLocalizedString("WESTRING_PLAYER_NA"), CRace.OTHER, new float[] { 0, 0 }));
		this.players.add(new CPlayer(this.players.size(), CMapControl.NEUTRAL,
				miscData.getLocalizedString("WESTRING_PLAYER_NV"), CRace.OTHER, new float[] { 0, 0 }));
		this.players.add(new CPlayer(this.players.size(), CMapControl.NEUTRAL,
				miscData.getLocalizedString("WESTRING_PLAYER_NE"), CRace.OTHER, new float[] { 0, 0 }));
		this.players.add(new CPlayer(this.players.size(), CMapControl.NEUTRAL,
				miscData.getLocalizedString("WESTRING_PLAYER_NP"), CRace.OTHER, new float[] { 0, 0 }));

	}

	public CUnitData getUnitData() {
		return this.unitData;
	}

	public CAbilityData getAbilityData() {
		return this.abilityData;
	}

	public List<CUnit> getUnits() {
		return this.units;
	}

	public CUnit createUnit(final War3ID typeId, final int playerIndex, final float x, final float y,
			final float facing, final BufferedImage buildingPathingPixelMap) {
		final CUnit unit = this.unitData.create(this, playerIndex, this.handleIdAllocator.createId(), typeId, x, y,
				facing, buildingPathingPixelMap);
		this.units.add(unit);
		this.worldCollision.addUnit(unit);
		return unit;
	}

	public CAttackProjectile createProjectile(final CUnit source, final float launchX, final float launchY,
			final float launchFacing, final CUnitAttackMissile attack, final CWidget target, final float damage,
			final int bounceIndex) {
		final CAttackProjectile projectile = this.simulationRenderController.createAttackProjectile(this, launchX,
				launchY, launchFacing, source, attack, target, damage, bounceIndex);
		this.newProjectiles.add(projectile);
		return projectile;
	}

	public void createInstantAttackEffect(final CUnit source, final CUnitAttackInstant attack, final CWidget target) {
		this.simulationRenderController.createInstantAttackEffect(this, source, attack, target);
	}

	public PathingGrid getPathingGrid() {
		return this.pathingGrid;
	}

	public List<Point2D.Float> findNaiveSlowPath(final CUnit ignoreIntersectionsWithThisUnit,
			final CUnit ignoreIntersectionsWithThisSecondUnit, final float startX, final float startY,
			final Point2D.Float goal, final PathingGrid.MovementType movementType, final float collisionSize,
			final boolean allowSmoothing) {
		return this.pathfindingProcessor.findNaiveSlowPath(ignoreIntersectionsWithThisUnit,
				ignoreIntersectionsWithThisSecondUnit, startX, startY, goal, movementType, collisionSize,
				allowSmoothing);
	}

	public void update() {
		final Iterator<CUnit> unitIterator = this.units.iterator();
		while (unitIterator.hasNext()) {
			final CUnit unit = unitIterator.next();
			if (unit.update(this)) {
				unitIterator.remove();
				this.simulationRenderController.removeUnit(unit);
			}
		}
		final Iterator<CAttackProjectile> projectileIterator = this.projectiles.iterator();
		while (projectileIterator.hasNext()) {
			final CAttackProjectile projectile = projectileIterator.next();
			if (projectile.update(this)) {
				projectileIterator.remove();
			}
		}
		this.projectiles.addAll(this.newProjectiles);
		this.newProjectiles.clear();
		this.gameTurnTick++;
	}

	public int getGameTurnTick() {
		return this.gameTurnTick;
	}

	public CWorldCollision getWorldCollision() {
		return this.worldCollision;
	}

	public CGameplayConstants getGameplayConstants() {
		return this.gameplayConstants;
	}

	public Random getSeededRandom() {
		return this.seededRandom;
	}

	public void unitDamageEvent(final CUnit damagedUnit, final String weaponSound, final String armorType) {
		this.simulationRenderController.spawnUnitDamageSound(damagedUnit, weaponSound, armorType);
	}

	public CPlayer getPlayer(final int index) {
		return this.players.get(index);
	}
}

package wtf.moneymod.client.impl.utility.impl.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import wtf.moneymod.client.impl.utility.Globals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BlockUtil implements Globals {
    INSTANCE;

    private boolean unshift = false;

    public boolean placeBlock(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        EnumFacing direction = calcSide(pos);
        if (direction == null) {
            return false;
        }
        boolean activated = block.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, EnumHand.MAIN_HAND, direction, 0.0f, 0.0f, 0.0f);
        if (activated) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos.offset(direction), direction.getOpposite(), EnumHand.MAIN_HAND, 0.5f, 0.5f, 0.5f));
        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
        if (activated || unshift) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            unshift = false;
        }
        mc.playerController.updateController();
        return true;
    }

    public EnumFacing calcSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            IBlockState offsetState = mc.world.getBlockState(pos.offset(side));
            boolean activated = offsetState.getBlock().onBlockActivated(mc.world, pos, offsetState, mc.player, EnumHand.MAIN_HAND, side, 0.0f, 0.0f, 0.0f);
            if (activated) {
                mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                unshift = true;
            }
            if (!offsetState.getBlock().canCollideCheck(offsetState, false) || offsetState.getMaterial().isReplaceable())
                continue;
            return side;
        }
        return null;
    }

    public boolean canBlockBeBroken(final BlockPos pos) {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, mc.world, pos) != -1;
    }

    public List<BlockPos> getUnsafePositions ( Vec3d vector, int level ) {
        return getLevels( level ).stream( ).map( vec -> new BlockPos( vector ).add( vec.x, vec.y, vec.z ) ).filter( bp -> mc.world.getBlockState( bp ).getMaterial( ).isReplaceable( ) ).collect( Collectors.toList( ) );
    }

    public List<Vec3d> getLevels (int y ) {
        return Arrays.asList( new Vec3d( -1, y, 0 ), new Vec3d( 1, y, 0 ), new Vec3d( 0, y, 1 ), new Vec3d( 0, y, -1 ) );
    }

    //1 - not placeable, 0 - placeable, -1 - not placeable cuz fuckin entity
    public int isPlaceable ( BlockPos bp ) {
        if ( mc.world == null || bp == null ) return 1;
        if ( !mc.world.getBlockState( bp ).getMaterial( ).isReplaceable( ) ) return 1;
        for ( Entity e : mc.world.getEntitiesWithinAABB( Entity.class, new AxisAlignedBB( bp ) ) ) {
            if ( e instanceof EntityXPOrb || e instanceof EntityItem) continue;
            if ( e instanceof EntityPlayer) return 1;
            return -1;
        }
        return 0;
    }

}
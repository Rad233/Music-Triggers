package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.eventsCommon;
import net.minecraft.block.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import java.util.UUID;

@SuppressWarnings("NullableProblems")
public class MusicRecorder extends Block {

    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

    public MusicRecorder(AbstractBlock.Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.FALSE));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult ray) {
        if (state.getValue(HAS_RECORD)) {
            this.dropRecord(worldIn, pos);
            state = state.setValue(HAS_RECORD, false);
            worldIn.setBlock(pos, state, 2);
            return ActionResultType.sidedSuccess(worldIn.isClientSide);
        } else {
            return ActionResultType.PASS;
        }
    }

    public void insertRecord(World worldIn, BlockPos pos, BlockState state, ItemStack recordStack, UUID uuid) {
        eventsCommon.recordWorld.put(pos,worldIn);
        eventsCommon.recordHolder.put(pos, recordStack.copy());
        eventsCommon.recordUUID.put(pos, uuid);
        eventsCommon.tickCounter.put(pos, 0);
        worldIn.setBlock(pos, state.setValue(HAS_RECORD, Boolean.TRUE), 2);
    }

    private void dropRecord(World worldIn, BlockPos pos) {
        if (!worldIn.isClientSide) {
            eventsCommon.recordHolder.putIfAbsent(pos,ItemStack.EMPTY);
            ItemStack itemstack = eventsCommon.recordHolder.get(pos);
            if (!itemstack.isEmpty()) {
                eventsCommon.recordHolder.put(pos,ItemStack.EMPTY);
                double d0 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.15000000596046448D;
                double d1 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
                double d2 = (double) (worldIn.random.nextFloat() * 0.7F) + 0.15000000596046448D;
                ItemStack itemstack1 = itemstack.copy();
                ItemEntity itemEntity = new ItemEntity(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, itemstack1);
                itemEntity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itemEntity);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState state2, boolean b) {
        if(!state.is(state2.getBlock())) {
            this.dropRecord(worldIn, pos);
            super.onRemove(state,worldIn,pos,state2,b);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        ItemStack itemstack = eventsCommon.recordHolder.get(pos);
        if (!itemstack.isEmpty()) {
            return 15;
        }
        return 0;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }


    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(HAS_RECORD);
    }
}

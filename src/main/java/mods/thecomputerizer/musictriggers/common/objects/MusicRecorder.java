package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.common.EventsCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class MusicRecorder extends Block {

    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;
    public static final BooleanProperty HAS_DISC = BooleanProperty.create("has_disc");

    public MusicRecorder(BlockBehaviour.Properties s) {
        super(s);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, false).setValue(HAS_DISC, Boolean.FALSE));
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult res) {
        if (state.getValue(HAS_RECORD)) {
            this.dropRecord(worldIn, pos);
            state = state.setValue(HAS_RECORD, false);
            worldIn.setBlock(pos, state, Block.UPDATE_CLIENTS);
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        } else if(state.getValue(HAS_DISC)) {
            this.dropRecord(worldIn, pos);
            state = state.setValue(HAS_DISC, false);
            worldIn.setBlock(pos, state, Block.UPDATE_CLIENTS);
            return InteractionResult.sidedSuccess(worldIn.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public void insertRecord(Level worldIn, BlockPos pos, BlockState state, ItemStack recordStack, UUID uuid) {
        EventsCommon.recordWorld.put(pos,worldIn);
        EventsCommon.recordHolder.put(pos, recordStack.copy());
        EventsCommon.recordUUID.put(pos, uuid);
        EventsCommon.tickCounter.put(pos, 0);
        if(recordStack.getItem() instanceof BlankRecord) {
            worldIn.setBlock(pos, state.setValue(HAS_RECORD, Boolean.TRUE), 2);
            EventsCommon.recordIsCustom.put(pos, false);
        }
        else {
            worldIn.setBlock(pos, state.setValue(HAS_DISC, Boolean.TRUE), 2);
            if(recordStack.getItem() instanceof CustomRecord)
                EventsCommon.recordIsCustom.put(pos, true);
            else EventsCommon.recordIsCustom.put(pos, false);
        }
    }

    private void dropRecord(Level worldIn, BlockPos pos) {
        if (!worldIn.isClientSide) {
            EventsCommon.recordHolder.putIfAbsent(pos,ItemStack.EMPTY);
            ItemStack itemstack = EventsCommon.recordHolder.get(pos);
            if (!itemstack.isEmpty()) {
                EventsCommon.recordHolder.put(pos,ItemStack.EMPTY);
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
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState state2, boolean b) {
        if (state.is(state2.getBlock())) {
            return;
        }
        this.dropRecord(worldIn, pos);
        super.onRemove(state,worldIn,pos,state2,b);
    }

    @Override
    public void playerWillDestroy (Level world, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (state.getValue(HAS_RECORD)) {
            return 15;
        }
        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_RECORD).add(HAS_DISC);
    }
}

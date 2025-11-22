package io.github.daxigua2333.mocai_clues.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InvisibleThinPlaneBlock extends FaceAttachedHorizontalDirectionalBlock {

    // TODO: config maybe
    // shape: 1*1e-3*1
    // collision: None
    private static final double THICKNESS = 5e-2;
    private static final VoxelShape FLOOR = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, THICKNESS, 1.0D);
    private static final VoxelShape NORTH = Shapes.box(0.0D, 0.0D, 1-THICKNESS, 1.0D, 1.0D, 1.0D);
    private static final VoxelShape SOUTH = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, THICKNESS);
    private static final VoxelShape WEST = Shapes.box(1-THICKNESS, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final VoxelShape EAST = Shapes.box(0.0D, 0.0D, 0.0D, THICKNESS, 1.0D, 1.0D);
    private static final VoxelShape CEILING = Shapes.box(0.0D, 1-THICKNESS, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final VoxelShape COLLISION_SHAPE = Shapes.empty();

    // TODO: constructor, maybe other params
    protected InvisibleThinPlaneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH)  //
        );
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACE, FACING);
    }

    // TODO: Implement codec()
    public static final MapCodec<InvisibleThinPlaneBlock> CODEC = BlockBehaviour.simpleCodec(InvisibleThinPlaneBlock::new);
    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return CODEC;
    }


    // Behaviours:
    // Placement: determine attach face + horizontal facing from context
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clicked = ctx.getClickedFace(); // the face the player clicked on
        Direction horizontal;
        AttachFace attachFace;
        if (clicked == Direction.UP) {
            attachFace = AttachFace.FLOOR;
            horizontal = ctx.getHorizontalDirection().getOpposite();
        } else if (clicked == Direction.DOWN) {
            attachFace = AttachFace.CEILING;
            horizontal = ctx.getHorizontalDirection().getOpposite();
        }
        else {
            attachFace = AttachFace.WALL;
            horizontal = clicked;
        }

        BlockState state = this.defaultBlockState()
            .setValue(FACE, attachFace)
            .setValue(FACING, horizontal);
        // optionally check canSurvive and return null if invalid (vanilla blocks often rely on parent)
        return state;
    }

    // Return a shape depending on face + facing
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        AttachFace face = state.getValue(FACE);
        Direction dir = state.getValue(FACING);

        // VERY simple switch; expand all directions/face combos to proper shapes
        if (face == AttachFace.FLOOR) {
            return FLOOR;
        } else if (face == AttachFace.WALL) {
            if (dir == Direction.NORTH) return NORTH;
            else if (dir == Direction.SOUTH) return SOUTH;
            else if (dir == Direction.WEST) return WEST;
            else if (dir == Direction.EAST) return EAST;
        } else { // CEILING
            return CEILING;
        }
        return Shapes.block(); // fallback
    }
    // Ensure block is removed if support is gone (optional—parent may already handle this)
    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction supportDir;
        switch (state.getValue(FACE)) {
            case FLOOR: supportDir = Direction.DOWN; break;
            case CEILING: supportDir = Direction.UP; break;
            default: // WALL
                supportDir = state.getValue(FACING).getOpposite(); // attached to wall in the opposite of facing
        }
        BlockPos supportPos = pos.relative(supportDir);
        return world.getBlockState(supportPos).isSolidRender(world, supportPos); // or more specific test
    }
    // collision box
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }
    // render as invisible
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

}

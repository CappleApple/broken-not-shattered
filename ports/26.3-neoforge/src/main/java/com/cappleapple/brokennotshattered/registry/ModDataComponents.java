package com.cappleapple.brokennotshattered.registry;

import com.cappleapple.brokennotshattered.BrokenNotShattered;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTER =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, BrokenNotShattered.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> BREAK_SEED =
        REGISTER.registerComponentType("break_seed", builder -> builder
            .persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    private ModDataComponents() {}
}

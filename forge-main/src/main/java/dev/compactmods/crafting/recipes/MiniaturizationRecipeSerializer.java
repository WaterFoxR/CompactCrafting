package dev.compactmods.crafting.recipes;

import javax.annotation.Nullable;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.compactmods.crafting.CompactCrafting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class MiniaturizationRecipeSerializer implements RecipeSerializer<MiniaturizationRecipe> {

    @Override
    public MiniaturizationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        CompactCrafting.LOGGER.debug("Beginning deserialization of recipe: {}", recipeId.toString());
        DataResult<MiniaturizationRecipe> parseResult = MiniaturizationRecipe.CODEC.parse(JsonOps.INSTANCE, json);

        if (parseResult.error().isPresent()) {
            DataResult.PartialResult<MiniaturizationRecipe> pr = parseResult.error().get();
            CompactCrafting.RECIPE_LOGGER.error("Error loading recipe: {}", pr.message());
            return null;
        }

        return parseResult.result()
                .map(r -> {
                    r.setId(recipeId);
                    return r;
                })
                .orElse(null);
    }

    @Nullable
    @Override
    public MiniaturizationRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        CompactCrafting.LOGGER.debug("Starting recipe read: {}", recipeId);

        if(!buffer.isReadable() || buffer.readableBytes() == 0) {
            CompactCrafting.LOGGER.error("Recipe not readable from buffer: {}", recipeId);

            return null;
        }

//        try {
//            final MiniaturizationRecipe recipe = buffer.readWithCodec(MiniaturizationRecipe.CODEC);
//            recipe.setId(recipeId);
//
//            CompactCrafting.LOGGER.debug("Finished recipe read: {}", recipeId);
//
//            return recipe;
//        }
//
//        catch(EncoderException ex) {
//            CompactCrafting.RECIPE_LOGGER.error("Error reading recipe information from network: " + ex.getMessage());
//            return null;
//        }
        try {
            // 读取NBT数据并使用CODEC解析
            CompoundTag nbt = buffer.readNbt();
            if (nbt == null) {
                CompactCrafting.LOGGER.error("Recipe NBT data is null: {}", recipeId);
                return null;
            }

            DataResult<MiniaturizationRecipe> parseResult = MiniaturizationRecipe.CODEC.parse(NbtOps.INSTANCE, nbt);
            if (parseResult.error().isPresent()) {
                DataResult.PartialResult<MiniaturizationRecipe> pr = parseResult.error().get();
                CompactCrafting.RECIPE_LOGGER.error("Error parsing recipe from NBT: {}", pr.message());
                return null;
            }

            final MiniaturizationRecipe recipe = parseResult.result().orElse(null);
            if (recipe != null) {
                recipe.setId(recipeId);
            }

            CompactCrafting.LOGGER.debug("Finished recipe read: {}", recipeId);
            return recipe;
        }
        catch(Exception ex) {
            CompactCrafting.RECIPE_LOGGER.error("Error reading recipe information from network: {}", ex.getMessage());
            return null;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull MiniaturizationRecipe recipe) {
        CompactCrafting.LOGGER.debug("Sending recipe over network: {}", recipe.getRecipeIdentifier());
//        buffer.writeWithCodec(MiniaturizationRecipe.CODEC, recipe);
        // 使用CODEC将配方编码为NBT，然后写入缓冲区
        // 使用CODEC将配方编码为NBT，然后写入缓冲区
        DataResult<Tag> encodeResult = MiniaturizationRecipe.CODEC.encodeStart(NbtOps.INSTANCE, recipe);
        if (encodeResult.error().isPresent()) {
            CompactCrafting.RECIPE_LOGGER.error("Error encoding recipe to NBT: {}", encodeResult.error().get().message());
            return;
        }

        Tag tag = encodeResult.result().orElse(null);
        if (tag instanceof CompoundTag) {
            buffer.writeNbt((CompoundTag) tag);
        } else if (tag != null) {
            // 如果不是CompoundTag，创建一个新的CompoundTag来包装它
            CompoundTag nbt = new CompoundTag();
            nbt.put("recipe", tag);
            buffer.writeNbt(nbt);
        }
    }
}

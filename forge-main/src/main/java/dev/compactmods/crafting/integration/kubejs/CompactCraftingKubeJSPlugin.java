package dev.compactmods.crafting.integration.kubejs;

import dev.compactmods.crafting.core.CCMiniaturizationRecipes;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class CompactCraftingKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("CCLayerType", CCLayerType.class);
        event.add("CCBlockComponent", CCBlockComponent.class);
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(CCMiniaturizationRecipes.MINIATURIZATION_RECIPE_TYPE,MiniaturizationRecipeSchema.SCHEMA);
    }

}

/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.datagen.recipes

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.advancement.criterion.CriterionConditions
import net.minecraft.advancement.criterion.InventoryChangedCriterion
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import techreborn.datagen.recipes.machine.MachineRecipeJsonFactory
import techreborn.datagen.recipes.machine.blast_furnace.BlastFurnaceRecipeJsonFactory
import techreborn.datagen.recipes.machine.industrial_grinder.IndustrialGrinderRecipeJsonFactory
import techreborn.datagen.recipes.machine.industrial_sawmill.IndustrialSawmillRecipeJsonFactory
import techreborn.init.ModRecipes

import java.util.concurrent.CompletableFuture

abstract class TechRebornRecipesProvider extends FabricRecipeProvider {
	protected RecipeExporter exporter
	public Set<Identifier> exportedRecipes = []

	TechRebornRecipesProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
		super(output)
	}

	@Override
	final void generate(RecipeExporter exporter) {
		this.exporter = exporter
		generateRecipes()
	}

	abstract void generateRecipes()

	static Ingredient createIngredient(def input) {
		if (input instanceof Ingredient) {
			return input
		}
		if (input instanceof ItemConvertible) {
			return Ingredient.ofItems(input)
		} else if (input instanceof TagKey) {
			return Ingredient.fromTag(input)
		}

		throw new IllegalArgumentException()
	}

	static String getCriterionName(def input) {
		if (input instanceof ItemConvertible) {
			return hasItem(input)
		} else if (input instanceof TagKey) {
			return "has_tag_" + input.id().toUnderscoreSeparatedString()
		}

		throw new IllegalArgumentException()
	}

	static AdvancementCriterion<InventoryChangedCriterion.Conditions> getCriterionConditions(def input) {
		if (input instanceof ItemConvertible) {
			return conditionsFromItem(input)
		} else if (input instanceof TagKey) {
			return conditionsFromTag(input)
		}

		throw new IllegalArgumentException()
	}

	static String getInputPath(def input) {
		if (input instanceof ItemConvertible) {
			return getItemPath(input)
		} else if (input instanceof TagKey) {
			return input.id().toString().replace(":", "_")
		}

		throw new IllegalArgumentException()
	}

	static String getName(def input) {
		if (input instanceof ItemConvertible) {
			return getItemPath(input)
		} else if (input instanceof TagKey) {
			String name = input.id().toString()
			if (name.contains(":"))
				name = name.substring(name.indexOf(":")+1)
			return name
		}

		throw new IllegalArgumentException()
	}

	static String getNamePart1(def input) {
		String name
		if (input instanceof ItemConvertible) {
			name = getItemPath(input)
			return name.substring(0,name.indexOf("_"))
		} else if (input instanceof TagKey) {
			name = input.id().toString()
			if (name.contains(":"))
				name = name.substring(name.indexOf(":")+1)
			return name.substring(0,name.indexOf("_"))
		}

		throw new IllegalArgumentException()
	}

	static ItemStack stack(ItemConvertible itemConvertible, int count = 1) {
		return new ItemStack(itemConvertible, count)
	}

	// Todo refactor me out, used to help port json recipes
	static ItemStack stack(String id, int count = 1) {
		def item = Registries.ITEM.get(new Identifier(id))
		return new ItemStack(item, count)
	}

	// Todo refactor me out, used to help port json recipes
	static TagKey<Item> tag(String id, count = 1) {
		if (count != 1) {
			throw new UnsupportedOperationException()
		}

		return TagKey.of(RegistryKeys.ITEM, new Identifier(id))
	}

	def offerAlloySmelterRecipe(@DelegatesTo(value = MachineRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		MachineRecipeJsonFactory.create(ModRecipes.ALLOY_SMELTER, this, closure).offerTo(exporter)
	}

	def offerGrinderRecipe(@DelegatesTo(value = MachineRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		MachineRecipeJsonFactory.create(ModRecipes.GRINDER, this, closure).offerTo(exporter)
	}

	def offerCompressorRecipe(@DelegatesTo(value = MachineRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		MachineRecipeJsonFactory.create(ModRecipes.COMPRESSOR, this, closure).offerTo(exporter)
	}

	def offerExtractorRecipe(@DelegatesTo(value = MachineRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		MachineRecipeJsonFactory.create(ModRecipes.EXTRACTOR, this, closure).offerTo(exporter)
	}

	def offerChemicalReactorRecipe(@DelegatesTo(value = MachineRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		MachineRecipeJsonFactory.create(ModRecipes.CHEMICAL_REACTOR, this, closure).offerTo(exporter)
	}

	def offerAssemblingMachineRecipe(@DelegatesTo(value = MachineRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		MachineRecipeJsonFactory.create(ModRecipes.ASSEMBLING_MACHINE, this, closure).offerTo(exporter)
	}

	def offerCentrifugeRecipe(@DelegatesTo(value = MachineRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		MachineRecipeJsonFactory.create(ModRecipes.CENTRIFUGE, this, closure).offerTo(exporter)
	}

	def offerBlastFurnaceRecipe(@DelegatesTo(value = BlastFurnaceRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		BlastFurnaceRecipeJsonFactory.createBlastFurnace(this, closure).offerTo(exporter)
	}

	def offerIndustrialGrinderRecipe(@DelegatesTo(value = IndustrialGrinderRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		IndustrialGrinderRecipeJsonFactory.createIndustrialGrinder(this, closure).offerTo(exporter)
	}

	def offerIndustrialSawmillRecipe(@DelegatesTo(value = IndustrialSawmillRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		IndustrialSawmillRecipeJsonFactory.createIndustrialSawmill(this, closure).offerTo(exporter)
	}

	def offerImplosionCompressorRecipe(@DelegatesTo(value = MachineRecipeJsonFactory.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
		MachineRecipeJsonFactory.create(ModRecipes.IMPLOSION_COMPRESSOR, this, closure).offerTo(exporter)
	}

	@Override
	protected Identifier getRecipeIdentifier(Identifier identifier) {
		return new Identifier("techreborn", super.getRecipeIdentifier(identifier).path)
	}

	@Override
	public String getName() {
		return "Recipes / " + getClass().name
	}
}

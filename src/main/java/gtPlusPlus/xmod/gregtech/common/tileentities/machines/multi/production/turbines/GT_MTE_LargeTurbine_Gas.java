package gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.production.turbines;

import static gtPlusPlus.core.lib.CORE.RANDOM;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Recipe.GT_Recipe_Map;
import gregtech.api.util.GT_Utility;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings("deprecation")
public class GT_MTE_LargeTurbine_Gas extends GregtechMetaTileEntity_LargerTurbineBase {

    public GT_MTE_LargeTurbine_Gas(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MTE_LargeTurbine_Gas(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MTE_LargeTurbine_Gas(mName);
    }

    @Override
    public int getCasingMeta() {
        return 3;
    }

    @Override
    public byte getCasingTextureIndex() {
        return 58;
    }

    @Override
    protected boolean requiresOutputHatch() {
        return false;
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        return 4000;
    }

    public int getFuelValue(FluidStack aLiquid) {
        if (aLiquid == null) {
            return 0;
        }
        GT_Recipe tFuel = GT_Recipe_Map.sTurbineFuels.findFuel(aLiquid);
        if (tFuel != null) {
            return tFuel.mSpecialValue;
        }
        return 0;
    }

    @Override
    int fluidIntoPower(ArrayList<FluidStack> aFluids, int aOptFlow, int aBaseEff) {
        if (aFluids.size() >= 1) {
            int tEU = 0;
            int actualOptimalFlow = 0;

            FluidStack firstFuelType = new FluidStack(
                    aFluids.get(0),
                    0); // Identify a SINGLE type of fluid to process.  Doesn't matter which one. Ignore the rest!
            int fuelValue = getFuelValue(firstFuelType);
            // log("Fuel Value of "+aFluids.get(0).getLocalizedName()+" is "+fuelValue+"eu");
            if (aOptFlow < fuelValue) {
                // turbine too weak and/or fuel too powerful
                // at least consume 1L
                this.realOptFlow = 1;
                // wastes the extra fuel and generate aOptFlow directly
                depleteInput(new FluidStack(firstFuelType, 1));
                this.storedFluid += 1;
                return GT_Utility.safeInt((long) aOptFlow * (long) aBaseEff / 10000L);
            }

            actualOptimalFlow = GT_Utility.safeInt((long) aOptFlow / fuelValue);
            this.realOptFlow = actualOptimalFlow;

            int remainingFlow = GT_Utility.safeInt((long) (actualOptimalFlow
                    * 1.25f)); // Allowed to use up to 125% of optimal flow.  Variable required outside of loop for
            // multi-hatch scenarios.
            int flow = 0;
            int totalFlow = 0;

            storedFluid = 0;
            for (FluidStack aFluid : aFluids) {
                if (aFluid.isFluidEqual(firstFuelType)) {
                    flow = Math.min(
                            aFluid.amount,
                            remainingFlow); // try to use up to 125% of optimal flow w/o exceeding remainingFlow
                    depleteInput(new FluidStack(aFluid, flow)); // deplete that amount
                    this.storedFluid += aFluid.amount;
                    remainingFlow -= flow; // track amount we're allowed to continue depleting from hatches
                    totalFlow += flow; // track total input used
                }
            }
            if (totalFlow <= 0) return 0;
            tEU = GT_Utility.safeInt((long) totalFlow * fuelValue);

            // log("Total Flow: "+totalFlow);
            // log("Real Optimal Flow: "+actualOptimalFlow);
            // log("Flow: "+flow);
            // log("Remaining Flow: "+remainingFlow);

            if (totalFlow == actualOptimalFlow) {
                tEU = GT_Utility.safeInt((long) tEU * (long) aBaseEff / 10000L);
            } else {
                float efficiency = 1.0f - Math.abs((totalFlow - actualOptimalFlow) / (float) actualOptimalFlow);
                tEU *= efficiency;
                tEU = GT_Utility.safeInt((long) tEU * (long) aBaseEff / 10000L);
            }

            return tEU;
        }
        return 0;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return (RANDOM.nextInt(4) == 0) ? 0 : 1;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
    }

    @Override
    public String getCustomGUIResourceName() {
        return null;
    }

    @Override
    public String getMachineType() {
        return "Large Gas Turbine";
    }

    @Override
    protected String getTurbineType() {
        return "Gas";
    }

    @Override
    protected String getCasingName() {
        return "Reinforced Gas Turbine Casing";
    }

    @Override
    protected ITexture getTextureFrontFace() {
        return new GT_RenderedTexture(gregtech.api.enums.Textures.BlockIcons.LARGETURBINE_SS5);
    }

    @Override
    protected ITexture getTextureFrontFaceActive() {
        return new GT_RenderedTexture(gregtech.api.enums.Textures.BlockIcons.LARGETURBINE_SS_ACTIVE5);
    }
}

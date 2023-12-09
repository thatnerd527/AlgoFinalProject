package Server.Calculator;

import Material.Material;
import Material.MaterialComposite;

public class CalculatorFunctions {
    public static MaterialComposite getMaterialsForBuild(Material x, MaterialComposite current) {
        MaterialComposite result = new MaterialComposite();
        if (x.getComposite().materials().size() == 0) {
            result.addMaterial(x);
            return result;
        }
        x.getComposite().materials().forEach(subx -> {
            if (current.isPresentOr0(subx).quantity >= subx.quantity) {
                result.addMaterial(subx);
            }

            // quantityrequired = how much of this submaterial we need
            double quantityrequired = subx.quantity - current.isPresentOr0(subx).quantity;
            Material cloned = subx.clone();
            cloned.quantity = quantityrequired;
            getMaterialsForBuild(cloned, current).materials().forEach(result::addMaterial);
        });
        return result;
    }

    public static MaterialComposite getMaterialsForBuildShallow(Material x, MaterialComposite current) {
        MaterialComposite result = new MaterialComposite();
        if (x.getComposite().materials().size() == 0) {
            result.addMaterial(x);
            return result;
        }
        x.getComposite().materials().forEach(subx -> {
            if (current.isPresentOr0(subx).quantity >= subx.quantity) {
                result.addMaterial(subx);
            }

            // quantityrequired = how much of this submaterial we need
            double quantityrequired = subx.quantity - current.isPresentOr0(subx).quantity;
            Material cloned = subx.clone();
            cloned.quantity = quantityrequired;
            getMaterialsForBuild(cloned, current).materials().forEach(result::addMaterial);
        });
        return result;
    }
}

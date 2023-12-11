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
            subx.quantity = subx.quantity * x.quantity;
            if (current.isPresentOr0(subx).quantity >= subx.quantity) {
                result.addMaterial(subx);
                return;
            }

            // quantityrequired = how much of this submaterial we need and we dont have
            double quantityrequired = subx.quantity - current.isPresentOr0(subx).quantity;
            Material cloned = subx.clone();
            cloned.quantity = quantityrequired;

            // if we have some already, we can just add it to the list.
            if (quantityrequired < subx.quantity) {
                Material cloned2 = cloned.clone();
                cloned2.quantity = current.isPresentOr0(subx).quantity;
                result.addMaterial(cloned2);
            }
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
            subx.quantity = subx.quantity * x.quantity;
            if (current.isPresentOr0(subx).quantity >= subx.quantity) {
                result.addMaterial(subx);
                return;
            }

            // quantityrequired = how much of this submaterial we need
            double quantityrequired = subx.quantity - current.isPresentOr0(subx).quantity;
            Material cloned = subx.clone();
            cloned.quantity = quantityrequired;
            result.addMaterial(cloned);
        });
        return result;
    }
}
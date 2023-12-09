package Server.Calculator;

import Material.Material;
import Material.MaterialComposite;

public class CalculatorFunctions {
    public static MaterialComposite getMaterialsForBuild(Material x, MaterialComposite current) {
        MaterialComposite result = new MaterialComposite();
        if (x.getComposite().materials().size() == 0) {
            return x;
        }
        x.getComposite().materials().forEach(subx -> {
            if (current.isPresentOr0(subx).quantity >= subx.quantity) {
                
            }
        });
    }
}

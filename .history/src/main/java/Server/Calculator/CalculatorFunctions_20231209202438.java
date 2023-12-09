package Server.Calculator;

import Material.Material;
import Material.MaterialComposite;

public class CalculatorFunctions {
    public static MaterialComposite getMaterialsForBuild(Material x, MaterialComposite current) {
        MaterialComposite result = new MaterialComposite();
        x.getComposite().materials().forEach(subx -> {
            if (current.isPresentOr0(subx) >= )
        });
    }
}

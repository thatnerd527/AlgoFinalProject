package Server.Calculator;

import Material.Material;
import Material.MaterialComposite;

public class CalculatorFunctions {
        MaterialComposite getMaterialsForBuild(Material x, MaterialComposite current) {
        MaterialComposite materials = new MaterialComposite();
        materials.addMaterial(x);
        materials.addMaterials(x.materialsRequiredToBuild());
        return materials;
    }
}

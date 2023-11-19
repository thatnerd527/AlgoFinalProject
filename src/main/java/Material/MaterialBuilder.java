package Material;

import java.util.ArrayList;
import java.util.Date;

public class MaterialBuilder {
    private String name;
    private String description;
    private String differentiator;
    private ArrayList<String> tags = new ArrayList<>();
    private double valuePerQty;
    private double overrideValue;
    private double quantity;
    private Date lifespanStart = new Date(0);;
    private double lifespan;
    private MaterialComposite composite = new MaterialComposite();

    public MaterialBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MaterialBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public MaterialBuilder withDifferentiator(String differentiator) {
        this.differentiator = differentiator;
        return this;
    }

    public MaterialBuilder withTags(ArrayList<String> tags) {
        this.tags = tags;
        return this;
    }

    public MaterialBuilder withValuePerQty(double valuePerQty) {
        this.valuePerQty = valuePerQty;
        return this;
    }

    public MaterialBuilder withOverrideValue(double overrideValue) {
        this.overrideValue = overrideValue;
        return this;
    }

    public MaterialBuilder withQuantity(double quantity) {
        this.quantity = quantity;
        return this;
    }

    public MaterialBuilder withLifespanStart(Date lifespanStart) {
        this.lifespanStart = lifespanStart;
        return this;
    }

    public MaterialBuilder withLifespan(double lifespan) {
        this.lifespan = lifespan;
        return this;
    }

    public MaterialBuilder withComposite(MaterialComposite composite) {
        this.composite = composite;
        return this;
    }

    public Material build() {
        return new Material(name, description, differentiator, tags, valuePerQty, overrideValue, quantity,
                lifespanStart, lifespan, composite);
    }
}
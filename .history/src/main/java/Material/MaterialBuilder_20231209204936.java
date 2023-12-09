package Material;

import java.time.Instant;
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
    private Instant lifespanStart = Instant.now();
    private long lifespanInSeconds;
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

    public MaterialBuilder withLifespanStart(Instant lifespanStart) {
        this.lifespanStart = lifespanStart;
        return this;
    }

    public MaterialBuilder withLifespanSeconds(long lifespan) {
        this.lifespanInSeconds = lifespan;
        return this;
    }

    public MaterialBuilder withComposite(MaterialComposite composite) {
        this.composite = composite;
        return this;
    }

    public Material build() {
        return new Material(name, description, differentiator, tags, valuePerQty, overrideValue, quantity,
                lifespanStart, lifespanInSeconds, composite,"");
    }
}
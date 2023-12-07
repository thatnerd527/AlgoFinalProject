package Material;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Material implements Serializable {
    public String name;

    public String description;

    public String differentiator;

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<String> tags;

    public double valuePerQty;

    public double overrideValue;

    public double quantity;

    public Instant getLifespanStart() {
        return lifespanStart;
    }

    public void setLifespanStart(Instant lifespanStart) {
        this.lifespanStart = lifespanStart;
    }

    public Instant lifespanStart;

    public long lifespanInSeconds;

    public MaterialComposite getComposite() {
        return composite;
    }

    public void setComposite(MaterialComposite composite) {
        this.composite = composite;
    }

    public MaterialComposite composite;

    public Material(String name, String description, String differentiator, ArrayList<String> tags, double valuePerQty,
            double overrideValue, double quantity, Instant lifespanStart, long lifespan, MaterialComposite composite) {
        this.name = name;
        this.description = description;
        this.differentiator = differentiator;
        this.tags = tags;
        this.valuePerQty = valuePerQty;
        this.overrideValue = overrideValue;
        this.quantity = quantity;
        this.lifespanStart = lifespanStart;
        this.lifespanInSeconds = lifespan;
        this.composite = composite;
    }

    public Material() {
        this.lifespanStart = Instant.now();
        this.composite = new MaterialComposite();
        this.tags = new ArrayList<String>();
        this.differentiator = "";
        this.name = "";
        this.description = "";
        this.valuePerQty = 0;
        this.overrideValue = 0;
        this.quantity = 0;
        this.lifespanInSeconds = 0;
    }

    public Material(String name, String description, String differentiator, ArrayList<String> tags, double valuePerQty,
            double overrideValue, double quantity, Instant lifespanStart, long lifespan,
            ArrayList<Material> composite) {
        this.name = name;
        this.description = description;
        this.differentiator = differentiator;
        this.tags = tags;
        this.valuePerQty = valuePerQty;
        this.overrideValue = overrideValue;
        this.quantity = quantity;
        this.lifespanStart = lifespanStart;
        this.lifespanInSeconds = lifespan;
        this.composite = new MaterialComposite(composite);
    }

    public Integer MaterialID() {
        return (Double.toString(lifespanInSeconds) +
                Double.toString(valuePerQty) +
                Double.toString(overrideValue) +
                differentiator +
                lifespanStart.toString().hashCode() +
                composite.cumulativeID()).hashCode();
    }

    public Integer MaterialIDWithQuantity() {
        return (Double.toString(lifespanInSeconds) +
                Double.toString(valuePerQty) +
                Double.toString(overrideValue) +
                differentiator +
                quantity +
                lifespanStart.toString().hashCode() +
                getComposite().cumulativeID()).hashCode();
    }

    public Material clone() {
        ArrayList<String> clonedTags = new ArrayList<String>();
        for (String tag : tags) {
            clonedTags.add(String.valueOf(tag));
        }
        return new Material(String.valueOf(name), String.valueOf(description), String.valueOf(differentiator), tags,
                valuePerQty, overrideValue, quantity, Instant.parse(lifespanStart.toString()), lifespanInSeconds,
                getComposite());
    }

    public Material loadfrom(byte[] loadfrom) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream((InputStream) Stream.of(loadfrom));
            Material material = (Material) objectInputStream.readObject();
            return material;
        } catch (Exception e) {
            return null;

        }
    }

    public boolean isExpired() {
        Instant now = Instant.now();
        return now.isAfter(lifespanStart.plusSeconds(lifespanInSeconds));
    }

    public byte[] serialize() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<Material> getAllSubMaterials(boolean inclusive, boolean clone) {
        ArrayList<Material> submaterials = new ArrayList<>();
        if (inclusive) {
            if (clone) {
                submaterials.add(clone());
            } else {
                submaterials.add(this);
            }
        }
        getComposite().materials().forEach(m -> {
            if (clone) {
                submaterials.add(m.clone());
            } else {
                submaterials.add(m);
            }
            submaterials.addAll(m.getAllSubMaterials(false, clone));
        });
        return submaterials;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Material)) {
            return false;
        }

        Material c = (Material) o;

        return c.MaterialID() == MaterialID();
    }

}

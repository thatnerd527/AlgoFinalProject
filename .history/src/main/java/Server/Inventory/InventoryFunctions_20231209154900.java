package Server.Inventory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;

import Material.Material;
import Material.MaterialBuilder;
import Material.MaterialComposite;
import Server.WrappedReader;
import Server.WrappedWriter;
import UI.InputForm;
import UI.Menu;

public class InventoryFunctions {

    public static Instant DateEntry(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            try {
                HashMap<String, String> dateinput = new InputForm(wW, wR)
                        .withField("Date (2 digits)", true)
                        .withField("Month (2 digits)", true)
                        .withField("Year (4 digits)", true)
                        .withTitle("Date Entry")
                        .receiveInput();
                String stringDate = dateinput.get("Date (2 digits)") + "/"
                        + dateinput.get("Month (2 digits)") + "/"
                        + dateinput.get("Year (4 digits)");
                String pattern = "dd/MM/yyyy";
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(stringDate, dateFormatter)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant();
            } catch (Exception e) {
                wW.write("Invalid date format.\n");
                continue;
            }
        }
    }

    public static Material CreateMaterial(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            HashMap<String, String> result = new InputForm(wR, wW)
                    .withField("Name", true)
                    .withField("Description", true)
                    .withField("Differentiator (optional)", false)
                    .withField("Value per Qty", true)
                    .withField("Custom value (optional)", false)
                    .withTitle("Create / Edit Material: Basic Data")
                    .receiveInput();
            // #region Input validation
            try {
                if (Double.valueOf(result.get("Value per Qty")) <= 0) {
                    throw new Exception();
                }

            } catch (Exception e) {
                wW.write("Value per Qty has to be a valid positive number.");
                continue;
            }
            try {
                if (result.get("Custom value (optional)").length() > 0) {
                    if (Double.valueOf(result.get("Custom value (optional)")) <= 0) {
                        throw new Exception();
                    }
                }

            } catch (Exception e) {
                wW.write("Custom value has to be a valid positive number.");
                continue;
            }
            // #endregion

            MaterialBuilder builder = new MaterialBuilder()
                    .withName(result.get("Name"))
                    .withDescription(result.get("Description"))
                    .withDifferentiator(result.get("Differentiator (optional)"))
                    .withValuePerQty(Double.valueOf(result.get("Value per Qty")))
                    .withQuantity(0);
            if (result.get("Custom value (optional)").length() > 0) {
                builder = builder.withOverrideValue(Double.valueOf(result.get("Custom value (optional)")));
            }

            boolean hasLifespan = new Menu()
                    .withTitle("Does this item have a lifespan?")
                    .withChoice("Y", "Yes")
                    .withChoice("N", "No")
                    .makeASelection(wW, wR).equals("Y");

            if (hasLifespan) {
                boolean specifyStart = new Menu()
                        .withTitle("Specify the item's start lifespan (for calculation)?")
                        .withChoice("Y", "Yes")
                        .withChoice("N", "No (uses now)")
                        .makeASelection(wW, wR).equals("Y");

                Instant startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
                if (specifyStart) {
                    while (true) {
                        try {
                            HashMap<String, String> dateinput = new InputForm(wW, wR)
                                    .withField("Date (2 digits)", true)
                                    .withField("Month (2 digits)", true)
                                    .withField("Year (4 digits)", true)
                                    .withTitle("Specify the item's start lifespan (for calculation)")
                                    .receiveInput();
                            String stringDate = dateinput.get("Date (2 digits)") + "/"
                                    + dateinput.get("Month (2 digits)") + "/"
                                    + dateinput.get("Year (4 digits)");
                            String pattern = "dd/MM/yyyy";
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                            startLifespan = LocalDate.parse(stringDate, dateFormatter)
                                    .atStartOfDay(ZoneId.systemDefault()).toInstant();

                        } catch (Exception e) {
                            wW.write("Invalid date format.\n");
                            continue;
                        }
                    }
                }
                while (true) {
                    try {
                        HashMap<String, String> dateinput = new InputForm(wW, wR)
                                .withField("Date (2 digits)", true)
                                .withField("Month (2 digits)", true)
                                .withField("Year (4 digits)", true)
                                .withTitle("Specify the item's end lifespan")
                                .receiveInput();
                        String stringDate = dateinput.get("Date (2 digits)") + "/"
                                + dateinput.get("Month (2 digits)") + "/"
                                + dateinput.get("Year (4 digits)");
                        String pattern = "dd/MM/yyyy";
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);
                        Instant endLifespan = LocalDate.parse(stringDate, dateFormatter)
                                .atStartOfDay(ZoneId.systemDefault()).toInstant();
                        if (startLifespan.isAfter(endLifespan)) {
                            wW.write("Start date cannot be after end date.\n");
                            continue;
                        }
                        if (!specifyStart) {
                            startLifespan = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                                    .toInstant();
                        }
                        builder = builder.withLifespanStart(startLifespan)
                                .withLifespanSeconds(
                                        endLifespan.getEpochSecond() - startLifespan.getEpochSecond());
                        break;
                    } catch (Exception e) {
                        wW.write("Invalid date format.\n");
                        continue;
                    }
                }

            }

            boolean isComposite = new Menu()
                    .withTitle("Is this item a composite?")
                    .withChoice("Y", "Yes")
                    .withChoice("N", "No")
                    .makeASelection(wW, wR).equals("Y");
            if (isComposite) {
                MaterialComposite composite = new MaterialComposite();
                while (true) {
                    String choice = new Menu()
                            .withTitle("Material Composite adder\n| Amount of materials in composite: "
                                    + composite.getMaterials().size())
                            .withChoice("+", "Add material to composite")
                            .withChoice("-", "Remove material from composite")
                            .withChoice("F", "Finish")
                            .makeASelection(wW, wR);
                    switch (choice) {
                        case "+":
                            while (true) {
                                HashMap<String, String> result2 = new InputForm(wR, wW)
                                        .withField("Template ID", true)
                                        .withField("Amount (above 0)", true)
                                        .withTitle("Add existing template material to composite")
                                        .receiveInput();
                                try {
                                    if (Double.valueOf(result2.get("Amount (above 0)")) <= 0) {
                                        throw new Exception();
                                    }
                                } catch (Exception e) {
                                    wW.write("Amount has to be a valid positive number.");
                                    continue;
                                }
                                Optional<Material> getmat = Server.Server.templatematerials.materials()
                                        .stream()
                                        .filter(x -> result2.get("Template ID")
                                                .equals(x.MaterialID().toString()))
                                        .findFirst();
                                if (!getmat.isPresent()) {
                                    wW.write("Invalid template ID.\n");
                                    break;
                                }
                                getmat.get().quantity = Double.valueOf(result2.get("Amount (above 0)"));
                                composite.addMaterial(getmat.get());
                            }
                            continue;
                        case "-":
                            while (true) {
                                HashMap<String, String> result2 = new InputForm(wR, wW)
                                        .withField("Template ID", true)
                                        .withField("Amount (above 0)", true)
                                        .withTitle("Remove existing template material in composite")
                                        .receiveInput();
                                try {
                                    if (Double.valueOf(result2.get("Amount (above 0)")) <= 0) {
                                        throw new Exception();
                                    }
                                } catch (Exception e) {
                                    wW.write("Amount has to be a valid positive number.");
                                    continue;
                                }
                                Optional<Material> getmat = composite.materials()
                                        .stream()
                                        .filter(x -> result2.get("Template ID")
                                                .equals(x.MaterialID().toString()))
                                        .findFirst();
                                if (!getmat.isPresent()) {
                                    wW.write("Invalid template ID.\n");
                                    break;
                                }
                                getmat.get().quantity = Double.valueOf(result2.get("Amount (above 0)"));
                                composite.removeMaterial(getmat.get());
                            }
                            continue;
                        case "F":
                            builder = builder.withComposite(composite);
                            break;

                        default:
                            break;
                    }
                    break;

                }
            }

            wW.write("Material ID: " + builder.build().MaterialID() + "\n");
            return builder.build();
        }
    }

    public static Material CreateMaterialWithQuantity(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            HashMap<String, String> result = new InputForm(wR, wW)
                    .withField("Name", true)
                    .withField("Description", true)
                    .withField("Differentiator (optional)", false)
                    .withField("Value per Qty", true)
                    .withField("Quantity (above 0)", true)
                    .withField("Custom value (optional)", false)
                    .withTitle("Add item to stock by ID: Basic Data")
                    .receiveInput();
            // #region Input validation
            try {
                if (Double.valueOf(result.get("Value per Qty")) <= 0) {
                    throw new Exception();
                }
            } catch (Exception e) {
                wW.write("Value per Qty has to be a valid positive number.");
                continue;
            }
            try {
                if (Double.valueOf(result.get("Quantity (above 0)")) <= 0) {
                    throw new Exception();
                }

            } catch (Exception e) {
                wW.write("Quantity has to be a valid positive number.");
                continue;
            }
            try {
                if (result.get("Custom value (optional)").length() > 0) {
                    if (Double.valueOf(result.get("Custom value (optional)")) <= 0) {
                        throw new Exception();
                    }
                }

            } catch (Exception e) {
                wW.write("Custom value has to be a valid positive number.");
                continue;
            }
            // #endregion

            MaterialBuilder builder = new MaterialBuilder()
                    .withName(result.get("Name"))
                    .withDescription(result.get("Description"))
                    .withDifferentiator(result.get("Differentiator (optional)"))
                    .withValuePerQty(Double.valueOf(result.get("Value per Qty")))
                    .withQuantity(Double.valueOf(result.get("Quantity (above 0)")));
            if (result.get("Custom value (optional)").length() > 0) {
                builder = builder.withOverrideValue(Double.valueOf(result.get("Custom value (optional)")));
            }

            boolean hasLifespan = new Menu()
                    .withTitle("Does this item have a lifespan?")
                    .withChoice("Y", "Yes")
                    .withChoice("N", "No")
                    .makeASelection(wW, wR).equals("Y");

            if (hasLifespan) {
                boolean specifyStart = new Menu()
                        .withTitle("Specify the item's start lifespan?")
                        .withChoice("Y", "Yes")
                        .withChoice("N", "No (uses now)")
                        .makeASelection(wW, wR).equals("Yes");

                Instant startLifespan = Instant.now();
                if (specifyStart) {
                    startLifespan = DateEntry(wR, wW);
                }
                while (true) {

                    Instant endLifespan = DateEntry(wR, wW);
                    if (startLifespan.isAfter(endLifespan)) {
                        wW.write("Start date cannot be after end date.\n");
                        continue;
                    }
                    if (!specifyStart) {
                        startLifespan = Instant.now();
                    }
                    builder = builder.withLifespanStart(startLifespan)
                            .withLifespanSeconds(
                                    endLifespan.getEpochSecond() - startLifespan.getEpochSecond());
                    break;
                }

            }
            return builder.build();
        }
    }

    public static Material GetTemplateMaterial(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            HashMap<String, String> result = new InputForm(wR, wW)
                    .withField("Template ID", true)
                    .withTitle("Get template material by ID")
                    .receiveInput();
            Optional<Material> getmat = Server.Server.templatematerials.materials()
                    .stream()
                    .filter(x -> result.get("Template ID").equals(x.MaterialID().toString()))
                    .findFirst();
            if (!getmat.isPresent()) {
                wW.write("Invalid template ID.\n");
                return null;
            }
            return getmat.get();
        }
    }

    public static Material GetInStockMaterial(WrappedReader wR, WrappedWriter wW) {
        while (true) {
            HashMap<String, String> result = new InputForm(wR, wW)
                    .withField("Material ID", true)
                    .withTitle("Get in stock material by ID")
                    .receiveInput();
            Optional<Material> getmat = Server.Server.currentlystored.materials()
                    .stream()
                    .filter(x -> result.get("Material ID").equals(x.MaterialID().toString()))
                    .findFirst();
            if (!getmat.isPresent()) {
                wW.write("Invalid material ID.\n");
                return null;
            }
            return getmat.get();
        }
    }

}

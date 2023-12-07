package UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import Server.WrappedReader;
import Server.WrappedWriter;

public class InputForm {

    private WrappedReader wR;

    private WrappedWriter wW;

    private String title;

    private HashMap<String, HashMap<String, Boolean>> fields = new HashMap<>();

    public InputForm(WrappedReader wR, WrappedWriter wW) {
        this.wR = wR;
        this.wW = wW;

    }

    public InputForm(WrappedWriter wW, WrappedReader wR) {
        this.wR = wR;
        this.wW = wW;

    }

    public void addField(String field, boolean required) {
        fields.put(field, new HashMap<String, Boolean>() {
            {
                put("", required);
            }
        });
    }

    public InputForm withField(String field, Boolean required) {
        addField(field, required);
        return this;
    }

    public InputForm withTitle(String title) {
        this.title = title;
        return this;
    }

    public String buildResult() {
        StringBuilder built = new StringBuilder();
        int longest = Integer
                .parseInt(fields.keySet().stream().reduce("0",
                        (String currentlength, String el) -> ((Integer) Math
                                .max(("| " + el + " = " + fields.get(el)).length(), Integer.parseInt(currentlength)))
                                .toString()));

        built.append("| " + title + "\n");
        built.append(stringGenerator("=", longest) + "\n");
        fields.keySet().stream().forEach((String el) -> {
            StringBuilder keyname = new StringBuilder();
            fields.get(el).keySet().forEach(x -> {
                keyname.append(x);
            });
            built.append("| " + el + " = " + keyname.toString() + "\n");
        });
        built.append(stringGenerator("=", longest) + "\n");
        return built.toString();
    }

    public HashMap<String, String> receiveInput() {
        wW.write("| " + title + "\n");
        HashMap<String, String> result = new HashMap<>();
        Scanner scanner = new Scanner(wR);
        while (true) {
            fields.keySet().forEach(x -> {
                while (true) {
                    wW.write(x + ": \n");
                    String input = scanner.nextLine();
                    //System.out.println(input);
                    StringBuilder keyname = new StringBuilder();
                    fields.get(x).keySet().forEach(x2 -> {
                        keyname.append(x2);
                    });
                    if (fields.get(x).get(keyname.toString()) && input.length() < 1) {
                        wW.write("This field is required.\n");
                        continue;
                    } else {
                        fields.put(x, new HashMap<String, Boolean>() {
                            {
                                put(input, fields.get(x).get(keyname.toString()));
                            }
                        });
                        break;
                    }
                }
            });
            wW.write(buildResult());
            String choice = new Menu()
                    .withTitle("Input confirmation")
                    .withChoice("C", "Confirm input")
                    .withChoice("R", "Retry input")
                    .makeASelection(wW, wR);
            if (choice.equals("C")) {
                break;
            }
        }
        fields.forEach((String x, HashMap<String, Boolean> k) -> {
            StringBuilder value = new StringBuilder();
            k.keySet().forEach((x2) -> {
                value.append(x2);
            });
            result.put(x, value.toString());
        });
        return result;
    }

    public static String stringGenerator(String txt, Integer amount) {
        String result = "";
        for (int i = 0; i < amount; i++) {
            result += txt;
        }
        return result;
    }

}

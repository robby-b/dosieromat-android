package de.hrw.embeddedsystems.dosieromat;

import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.XMLFormatter;

public class RecipeXmlParser {
    private static final String ns = null;
    private XmlPullParser parser;

    public RecipeXmlParser(XmlPullParser parser) throws XmlPullParserException, IOException {
        this.parser = parser;
        this.parser.next();
        this.parser.next();
    }

    public List<Recipe> parseRecipeXml() throws XmlPullParserException, IOException {
        List<Recipe> recipes = new ArrayList<>();

        Log.d("RecipeList", "Step 1");

        parser.require(XmlPullParser.START_TAG, ns, "recipes");

        Log.d("RecipeList", "Step 2");


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();

            if(tagName.equals("recipe")) {
                recipes.add(readRecipe());
            }
            else {
                skip(parser);
            }
        }

        return recipes;
    }

    private Recipe readRecipe() throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "recipe");

        String name = null;
        int amount = 0;
        String unit = null;
        List<Ingredient> ingredients = null;

        while(parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();

            if (tagName.equals("name")) {
                name = readSimpleTag(parser, "name");
            }
            else if (tagName.equals("amount")) {
                amount = Integer.parseInt(readSimpleTag(parser, "amount"));
            }
            else if (tagName.equals("unit")) {
                unit = readSimpleTag(parser, "unit");
            }
            else if (tagName.equals("ingredients")) {
                ingredients = readIngredients(parser);
            }
            else {
                skip(parser);
            }
        }

        return new Recipe(name, amount, unit, ingredients);
    }

    private String readSimpleTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tagName);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tagName);
        return title;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private List<Ingredient> readIngredients(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Ingredient> ingredients = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "ingredients");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();

            if(tagName.equals("ingredient")) {
                ingredients.add(readIngredient(parser));
            }
            else {
                skip(parser);
            }
        }

        return ingredients;
    }

    private Ingredient readIngredient(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "ingredient");

        String name = null;
        int amount = 0;
        String unit = null;
        boolean isAutoDispensable = false;

        while(parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String tagName = parser.getName();

            if (tagName.equals("name")) {
                name = readSimpleTag(parser, "name");
            }
            else if (tagName.equals("amount")) {
                amount = Integer.parseInt(readSimpleTag(parser, "amount"));
            }
            else if (tagName.equals("unit")) {
                unit = readSimpleTag(parser, "unit");
            }
            else if (tagName.equals("isAutoDispensable")) {
                isAutoDispensable = Boolean.parseBoolean(readSimpleTag(parser, "isAutoDispensable"));
            }
            else {
                skip(parser);
            }
        }

        return new Ingredient(name, amount, unit, isAutoDispensable);
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}

package com.example.recipe;

import com.example.model.Ingredients;
import com.example.model.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Archi M
 */
@WebServlet(name = "FindRecipeServlet")
@MultipartConfig
public class FindRecipeServlet extends HttpServlet {

    public static final String DELIMITER = ",";

    public static final int DATE_TOKEN = 3;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final Status status = new Status();
        BufferedReader fileReader = null;

        try {

            final Part availableItemsPart = request.getPart("availableItemsFile");
            final Part recipesPart = request.getPart("recipesFile");
            final String availableItemsFileName = getFileName(availableItemsPart);
            final String recipesFileName = getFileName(recipesPart);
            ensureFileExtension(availableItemsFileName, "csv");
            ensureFileExtension(recipesFileName, "txt");
            String result = null;

            //read available items file
            fileReader = new BufferedReader(new InputStreamReader(availableItemsPart.getInputStream(), "UTF-8"));
            final List<Ingredients> availableItems = getAvailableItems(fileReader, status);


            fileReader = new BufferedReader(new InputStreamReader(recipesPart.getInputStream(), "UTF-8"));
            final Map<String, List<Ingredients>> recipesMap = getRecipes(fileReader);

            final Map<String, List<Ingredients>> recipesToPrepare = getPossibleRecipes(availableItems, recipesMap);
            if (recipesToPrepare.size() > 0) {
                result = getFinalResult(recipesToPrepare);
            }
            if (null != result && result.length() > 0) {
                request.setAttribute("result", result);
            } else {
                request.setAttribute("result", "Order Takeout");
            }

        } catch (FileNotFoundException e) {
            status.addMessage("Unable to read file, please make sure it is in correct format");
        } catch (IOException e) {
            status.addMessage("Unable to read file");
        } catch (JSONException e) {
            status.addMessage("Unable to read ingredients file, please make sure data is in correct format");
        } catch (ParseException e) {
            status.addMessage("Unable to read dates in file, make sure they are correct.");
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    //log error
                }
            }
            request.setAttribute("statusMsg", status);
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    private String getFinalResult(Map<String, List<Ingredients>> recipesToPrepare) {
        final Map<String, Double> finalMap = new HashMap<String, Double>();
        for (Map.Entry<String, List<Ingredients>> entry : recipesToPrepare.entrySet()) {
            final int numberOfItems = entry.getValue().size();
            double timeIngrd = 0;
            for (Ingredients ingrd : entry.getValue()) {
                timeIngrd += ingrd.getDate().getTime();
            }
            timeIngrd = timeIngrd / numberOfItems;
            finalMap.put(entry.getKey(), timeIngrd);
        }

        List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(finalMap.size());

        entries.addAll(finalMap.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(final Map.Entry<String, Double> entry1, final Map.Entry<String, Double> entry2) {
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });
        return entries.get(0).getKey();
    }

    private Map<String, List<Ingredients>> getPossibleRecipes(List<Ingredients> availableItems, Map<String, List<Ingredients>> recipesMap) {
        final Map<String, List<Ingredients>> recipesToPrepare = new HashMap<String, List<Ingredients>>();
        if (recipesMap.size() > 0 && availableItems.size() > 0) {
            Map<String, List<Ingredients>> updatedRecipe = checkRequiredItemsAvailable(availableItems, recipesMap);
            for (Map.Entry<String, List<Ingredients>> entry : updatedRecipe.entrySet()) {
                {
                    for (Ingredients availIngrd : availableItems) {
                        int flag = 0;
                        for (Ingredients ingrd : entry.getValue()) {
                            if (availIngrd.compareTo(ingrd) >= 0) {
                                ingrd.setDate(availIngrd.getDate());
                                flag = 1;
                            }
                        }
                        if (flag == 1) {
                            recipesToPrepare.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        }
        return recipesToPrepare;
    }

    private Map<String, List<Ingredients>> getRecipes(BufferedReader fileReader) throws JSONException, IOException {
        String jsonData = "";
        String line;
        while ((line = fileReader.readLine()) != null) {
            jsonData += line + "\n";
        }

        final JSONObject listOfRecipes = new JSONObject(jsonData);
        final JSONArray recipes = listOfRecipes.getJSONArray("recipes");
        final Map<String, List<Ingredients>> recipesMap = new HashMap<String, List<Ingredients>>();

        // iterate over list of recipes
        for (int j = 0; j < recipes.length(); j++) {
            final JSONObject recipe = recipes.getJSONObject(j);
            //get all ingredients list for recipe
            final JSONArray ingredients = recipe.getJSONArray("ingredients");
            final List<Ingredients> ingredientsList = new ArrayList<Ingredients>();
            for (int itr = 0; itr < ingredients.length(); itr++) {
                final JSONObject ingredient = ingredients.getJSONObject(itr);
                ingredientsList.add(new Ingredients(ingredient.getString("item"), ingredient.getString("amount"), ingredient.getString("unit")));
            }
            // prepare map of recipe name and ingredients
            recipesMap.put(recipe.getString("name"), ingredientsList);
        }
        //compare ingredients and display result.


        return recipesMap;
    }

    private List<Ingredients> getAvailableItems(BufferedReader fileReader, Status status) throws IOException, ParseException {
        String line;
        final List<Ingredients> availableItems = new ArrayList<Ingredients>();

        while (null != (line = fileReader.readLine())) {
            //read line
            final String[] tokens = line.split(DELIMITER);
            for (int i = 0; i < tokens.length; i++) {
                if (i == DATE_TOKEN) {
                    if (null != tokens[DATE_TOKEN]) {
                        //check for out dated items
                        final String[] dateTokens = tokens[DATE_TOKEN].split("/");
                        if (!(dateTokens[0].length() == 2 && dateTokens[1].length() == 2 && dateTokens[2].length() == 4)) {
                            status.addMessage("Unable to read date for item " + tokens[0]);
                        } else {
                            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                            final Date itemDate = formatter.parse(tokens[DATE_TOKEN]);
                            if (itemDate.compareTo(new Date()) < 0) {
                                //if out dated notify user to chuck them away
                                status.addMessage("Please trash item '" + tokens[0] + "' as it is    expired on " + tokens[3]);
                            } else {
                                // if not out dated then add them to available items list.
                                availableItems.add(new Ingredients(tokens[0], tokens[1], tokens[2], itemDate));
                            }
                        }
                    }
                }
            }

        }

        return availableItems;
    }

    private void ensureFileExtension(String fileName, String extension) throws FileNotFoundException {
        if (fileName != null) {
            final int pos = fileName.lastIndexOf('.');
            final String providedFileExtension = fileName.substring(pos + 1);
            if (!(providedFileExtension.equalsIgnoreCase(extension))) {
                throw new FileNotFoundException();
            }
        }
    }

    private Map<String, List<Ingredients>> checkRequiredItemsAvailable
            (List<Ingredients> availableItems, Map<String, List<Ingredients>> recipesMap) {
        final Map<String, List<Ingredients>> updatedRecipe = new HashMap<String, List<Ingredients>>();
        final List<String> available = new ArrayList<String>();
        for (Ingredients item : availableItems) {
            available.add(item.getItem().toLowerCase());
        }

        for (Map.Entry<String, List<Ingredients>> entry : recipesMap.entrySet()) {
            List<String> required = new ArrayList<String>();
            for (Ingredients ingr : entry.getValue()) {
                required.add(ingr.getItem().toLowerCase());
            }

            if (available.containsAll(required)) {
                updatedRecipe.put(entry.getKey(), entry.getValue());
            }

        }
        return updatedRecipe;
    }

    private String getFileName(final Part part) {
        if (part != null)
            for (String content : part.getHeader("content-disposition").split(";")) {
                if (content.trim().startsWith("filename")) {
                    return content.substring(
                            content.indexOf('=') + 1).trim().replace("\"", "");
                }
            }

        return null;
    }
}


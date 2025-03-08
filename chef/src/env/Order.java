import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Order {
    private List<String> ingredients;

    /**
     * Constructs an Order object from a list of ingredients.
     * 
     * @param ingredients The list of ingredients required for this order.
     */
    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Constructs an Order object from a JSONObject.
     * 
     * @param orderJson The JSONObject containing order data.
     * @throws JSONException If the JSON is malformed.
     */
    public Order(JSONObject orderJson) throws JSONException {
        this.ingredients = new ArrayList<>();
        JSONArray ingredientsArray = orderJson.getJSONArray("ingredients");
        
        for (int i = 0; i < ingredientsArray.length(); i++) {
            ingredients.add(ingredientsArray.getString(i));
        }
    }

    /**
     * Returns the list of ingredients required for this order.
     *
     * @return The list of ingredients.
     */
    public List<String> getIngredients() {
        return ingredients;
    }
    
    /**
     * Checks if this order contains the specified ingredient.
     *
     * @param ingredient The ingredient to check.
     * @return true if the order contains the ingredient, false otherwise.
     */
    public boolean containsIngredient(String ingredient) {
        return ingredients.contains(ingredient);
    }
    
    /**
     * Returns the count of a specific ingredient in this order.
     *
     * @param ingredient The ingredient to count.
     * @return The number of occurrences of the ingredient.
     */
    public int getIngredientCount(String ingredient) {
        int count = 0;
        for (String s : ingredients) {
            if (s.equals(ingredient)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Returns the total number of ingredients in this order.
     *
     * @return The total number of ingredients.
     */
    public int getTotalIngredientCount() {
        return ingredients.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Order[");
        for (int i = 0; i < ingredients.size(); i++) {
            sb.append(ingredients.get(i));
            if (i < ingredients.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

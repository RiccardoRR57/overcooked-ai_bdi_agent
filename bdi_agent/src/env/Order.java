import java.util.List;

public class Order {
    private final List<String> ingredients;

    /**
     * Constructs an Order object from a list of ingredients.
     * 
     * @param ingredients The list of ingredients required for this order.
     */
    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
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

import java.util.List;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;

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

    /**
     * Converts an order to a Jason literal for agent perception
     * 
     * @param isBonus Whether this order is a bonus order
     * @return A Jason literal representing the order
     * @throws ParseException if there is an error parsing the literal
     */
    public Literal getLiteral(boolean isBonus) throws ParseException {
        StringBuilder sb = new StringBuilder();
        if (isBonus) {
            sb.append("bonus_order(");
        } else {
            sb.append("order(");
        }
        for (int i = 0; i < ingredients.size(); i++) {
            sb.append(ingredients.get(i));
            if (i < ingredients.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return ASSyntax.parseLiteral(sb.toString());
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class representing a cooking pot in the Overcooked game
 */
public class Pot {
    private int x;  // X position
    private int y;  // Y position
    private int cookingTick;  // Timer for cooking
    private final List<Character> ingredients;  // Ingredients in pot

    /**
     * Creates a pot from string representation
     * 
     * @param potStr String containing pot state information
     */
    public Pot(String potStr) {
        this.ingredients = new ArrayList<>();
        this.cookingTick = -1;
        
        // Early return if no pot data
        if (potStr == null || potStr.isEmpty()) {
            this.x = 0;
            this.y = 0;
            return;
        }

        try {
            // Extract pot position
            Pattern posPattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\)");
            Matcher posMatcher = posPattern.matcher(potStr);
            if (posMatcher.find()) {
                this.x = Integer.parseInt(posMatcher.group(1));
                this.y = Integer.parseInt(posMatcher.group(2));
            } else {
                this.x = 0;
                this.y = 0;
            }

            // Extract ingredient list
            Pattern ingredPattern = Pattern.compile("Ingredients:\\s*\\[(.*?)\\]");
            Matcher ingredMatcher = ingredPattern.matcher(potStr);
            if (ingredMatcher.find()) {
                String ingredientsStr = ingredMatcher.group(1);
                Pattern ingredItemPattern = Pattern.compile("(\\w+)@");
                Matcher ingredItemMatcher = ingredItemPattern.matcher(ingredientsStr);
                while (ingredItemMatcher.find()) {
                    // Get the first letter of the ingredient
                    this.ingredients.add(ingredItemMatcher.group(1).charAt(0));
                }
            }

            // Extract cooking tick
            Pattern tickPattern = Pattern.compile("Cooking Tick:\\s*(-?\\d+)");
            Matcher tickMatcher = tickPattern.matcher(potStr);
            if (tickMatcher.find()) {
                this.cookingTick = Integer.parseInt(tickMatcher.group(1));
            }
        } catch (NumberFormatException e) {
            // Handle number parsing errors
            System.err.println("Error parsing numeric values: " + e.getMessage());
        } catch (PatternSyntaxException e) {
            // Handle regex pattern errors
            System.err.println("Error in regex pattern: " + e.getMessage());
        }
    }
    
    /**
     * Gets the pot's X position
     * 
     * @return X coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Gets the pot's Y position
     * 
     * @return Y coordinate
     */
    public int getY() {
        return y;
    }
    
    /**
     * Gets the pot's cooking tick counter
     * 
     * @return cooking tick (-1 if not cooking)
     */
    public int getCookingTick() {
        return cookingTick;
    }
    
    /**
     * Gets the pot's ingredients
     * 
     * @return list of ingredients as characters
     */
    public List<Character> getIngredients() {
        return new ArrayList<>(ingredients);
    }
    
    /**
     * Creates string representation of the pot
     * 
     * @return String pot state information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(x).append(", ").append(y).append(")");
        
        // Show ingredients
        sb.append(" contains [");
        if (ingredients.isEmpty()) {
            sb.append("empty");
        } else {
            for (int i = 0; i < ingredients.size(); i++) {
                char ingredient = ingredients.get(i);
                switch (ingredient) {
                    case 'o' -> sb.append("onion");
                    case 't' -> sb.append("tomato");
                    default -> sb.append("unknown");
                }
                
                if (i < ingredients.size() - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append("]");
        
        // Show cooking status
        if (cookingTick == -1) {
            sb.append(" not cooking");
        } else {
            sb.append(" cooking: ").append(cookingTick);
        }
        
        return sb.toString();
    }
}

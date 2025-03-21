import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing a player in the Overcooked game
 */
public class Player {
    private int x;       // X position
    private int y;       // Y position
    private int dx;      // X facing direction
    private int dy;      // Y facing direction
    private char holding;  // Currently held item

    /**
     * Creates a player from string representation
     * 
     * @param player String containing player state information
     */
    public Player(String player) {
        // Default initialization
        this.x = 0;
        this.y = 0;
        this.dx = 0;
        this.dy = 0;
        this.holding = 0;
        
        // Early return if no player data
        if (player == null || player.isEmpty()) {
            return;
        }
        
        // Pattern to match player position, facing direction and held object
        // Updated format to handle soup notation: (x, y) facing (dx, dy) holding {øøø✓
        Pattern pattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\)\\s*facing\\s*\\(([-]?\\d+),\\s*([-]?\\d+)\\)\\s*holding\\s*([^\\s]+)");
        Matcher matcher = pattern.matcher(player);
        
        // If pattern doesn't match, keep default values
        if (!matcher.find()) {
            return;
        }
        
        // Extract position and direction
        this.x = Integer.parseInt(matcher.group(1));  // Position X
        this.y = Integer.parseInt(matcher.group(2));  // Position Y
        this.dx = Integer.parseInt(matcher.group(3)); // Direction X
        this.dy = Integer.parseInt(matcher.group(4)); // Direction Y
        
        // Parse held object
        parseHeldObject(matcher.group(5));
    }
    
    /**
     * Helper method to parse the held object string
     * 
     * @param heldObject String representation of held object
     */
    private void parseHeldObject(String heldObject) {
        // Not holding anything
        if ("None".equals(heldObject)) {
            this.holding = 0;
            return;
        }
        // Determine object type from string
        if (heldObject.startsWith("onion")) {
            this.holding = 'o';  // Holding onion
        } else if (heldObject.startsWith("tomato")) {
            this.holding = 't';  // Holding tomato
        } else if (heldObject.startsWith("dish")) {
            this.holding = 'd';  // Holding dish
        } else if (heldObject.startsWith("{")) {
            // int onions = heldObject.length() - heldObject.replace("ø", "").length();
            // int tomatoes = heldObject.length() - heldObject.replace("†", "").length();
            this.holding = 's';  // Holding soup
        } else {
            this.holding = 0;    // Unknown object
        }
    }
    
    /**
     * Gets the player's X position
     * 
     * @return X coordinate
     */
    public int getX() {
        return x;
    }
    
    /**
     * Gets the player's Y position
     * 
     * @return Y coordinate
     */
    public int getY() {
        return y;
    }
    
    /**
     * Gets the player's X facing direction
     * 
     * @return X direction (-1, 0, or 1)
     */
    public int getDx() {
        return dx;
    }
    
    /**
     * Gets the player's Y facing direction
     * 
     * @return Y direction (-1, 0, or 1)
     */
    public int getDy() {
        return dy;
    }
    
    /**
     * Gets the item the player is holding
     * 
     * @return character code for held item
     */
    public char getHolding() {
        return holding;
    }
    
    /**
     * Creates string representation of the player
     * 
     * @return String player state information
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(x).append(", ").append(y).append(")");
        sb.append(" facing (").append(dx).append(", ").append(dy).append(")");
        
        // Show what player is holding
        sb.append(" holding ");
        switch (holding) {
            case 'o' -> sb.append("onion");
            case 't' -> sb.append("tomato");
            case 'd' -> sb.append("dish");
            case 's' -> sb.append("soup");
            default -> sb.append("nothing");
        }
        
        return sb.toString();
    }
}

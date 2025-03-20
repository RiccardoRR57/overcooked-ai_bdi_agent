import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Grid {
    private final int width;          // Grid width in cells
    private final int height;         // Grid height in cells
    private final char[][] grid;      // Static terrain representation
    private final char[][] objects;   // Dynamic game objects
    private Player player1;           // First player representation
    private Player player2;           // Second player representation
    private final List<Pot> pots;           // List of pots

    /**
     * Constructs a grid for the Overcooked AI game.
     *
     * @param width the width of the grid
     * @param height the height of the grid
     * @param layout a string representation of the grid layout
     */
    public Grid(int height, int width, String layout) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][width];
        this.objects = new char[height][width];
        this.pots = new ArrayList<>();
        
        if (layout.length() != width * height) {
            throw new IllegalArgumentException("Layout string length must match grid dimensions");
        }
        
        // Convert linear string to 2D grid
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                char cellChar = layout.charAt(index);
                grid[y][x] = cellChar;
            }
        }
    }

    /**
     * Gets the width of the grid.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the grid.
     *
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the cell type at the specified coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the cell type at (x, y)
     */
    public char getCellType(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return grid[y][x];
    }

    /**
     * Clears all objects from the grid
     */
    public void resetObjects() {
        // Reset object layer to empty
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                objects[y][x] = 0;
            }
        }
    }

    /**
     * Updates the player's state based on information from game engine
     * 
     * @param player String representation of player state
     * @param playerNum Player number (1 or 2)
     */
    public void setPlayer(String player, int playerNum) {
        switch (playerNum) {
            case 1 -> player1 = new Player(player);  // Update player 1
            case 2 -> player2 = new Player(player);  // Update player 2
            default -> throw new IllegalArgumentException("Invalid player number");
        }
    }

    /**
     * Places objects on the grid based on string representation
     * 
     * @param objectsString String containing object locations and types
     */
    public void setObjects(String objectsString) {
        // Clear existing objects and pots
        resetObjects();
        this.pots.clear();
        
        // Early return if no object data
        if (objectsString == null || objectsString.isEmpty()) {
            return;
        }
        
        // Remove outer brackets/braces
        objectsString = objectsString.replaceAll("^\\{|\\}$", "").trim();
        
        // Process soup objects first (they have special format with ingredients and cooking tick on separate lines)
        processSoupObjects(objectsString);
        
        // Process other standalone objects
        processStandardObjects(objectsString);
    }

    /**
     * Process soup objects with their ingredients and cooking ticks
     * 
     * @param objectsString Complete objects string
     */
    private void processSoupObjects(String objectsString) {
        // Pattern to match soup objects with positions
        Pattern soupPattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\):\\s*soup@\\(\\d+,\\s*\\d+\\)");
        Matcher soupMatcher = soupPattern.matcher(objectsString);
        
        while (soupMatcher.find()) {
            int potX = Integer.parseInt(soupMatcher.group(1));
            int potY = Integer.parseInt(soupMatcher.group(2));
            
            // Find ingredients list and cooking tick for this soup
            // We need to search ahead from the current position
            int soupStart = soupMatcher.start();
            
            // Extract a region of text after the soup position to find ingredients and cooking tick
            int searchEndIndex = Math.min(soupStart + 200, objectsString.length());
            String potRegion = objectsString.substring(soupStart, searchEndIndex);
            
            // Extract ingredients list
            Pattern ingredPattern = Pattern.compile("Ingredients:\\s*\\[(.*?)\\]");
            Matcher ingredMatcher = ingredPattern.matcher(potRegion);
            
            if (ingredMatcher.find()) {
                // Extract cooking tick
                Pattern tickPattern = Pattern.compile("Cooking Tick:\\s*(-?\\d+)");
                Matcher tickMatcher = tickPattern.matcher(potRegion);
                
                if (tickMatcher.find()) {
                    // Construct a pot string that the constructor can parse
                    String potStr = "(" + potX + ", " + potY + ") " +
                                    "Ingredients: [" + ingredMatcher.group(1) + "] " +
                                    "Cooking Tick: " + tickMatcher.group(1);
                    
                    // Create a new pot at this position with the pot string
                    Pot newPot = new Pot(potStr);
                    
                    // Add the new pot to the pots list
                    pots.add(newPot);
                    
                    // Mark pot location on the objects grid with 'P'
                    this.objects[potY][potX] = 'P';
                }
            }
        }
    }

    /**
     * Process standard objects (non-soup)
     * 
     * @param objectsString Complete objects string
     */
    private void processStandardObjects(String objectsString) {
        // Pattern for standard objects (format: (x, y): object@(x, y))
        Pattern objectPattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\):\\s*(\\w+)@\\(\\d+,\\s*\\d+\\)");
        Matcher objectMatcher = objectPattern.matcher(objectsString);
        
        while (objectMatcher.find()) {
            String objectType = objectMatcher.group(3).toLowerCase();
            
            // Skip soup objects as they're handled separately
            if (objectType.equals("soup")) {
                continue;
            }
            
            int x = Integer.parseInt(objectMatcher.group(1));
            int y = Integer.parseInt(objectMatcher.group(2));
            
            // Get first letter of object type as lowercase
            char objectChar = objectType.charAt(0);
            
            // Add object to grid (as lowercase letter)
            this.objects[y][x] = objectChar;
        }
    }

    /**
     * Creates string representation of the current grid state
     * 
     * @return String visualization of grid with players and objects
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Grid visualization
        sb.append("\nGrid state:\n");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (objects[y][x] != 0) {
                    sb.append(objects[y][x]);  // Show objects first
                } else if (player1 != null && player1.x == x && player1.y == y) {
                    sb.append('1');  // Show player 1
                } else if (player2 != null && player2.x == x && player2.y == y) {
                    sb.append('2');  // Show player 2
                } else{
                    sb.append(grid[y][x]);  // Show terrain
                }
            }
            sb.append("\n");
        }
        
        // Player state
        sb.append("\nPlayers:\n");
        if (player1 != null) {
            sb.append("Player 1: ").append(player1.toString()).append("\n");
        }
        if (player2 != null) {
            sb.append("Player 2: ").append(player2.toString()).append("\n");
        }
        
        // Pot state
        sb.append("\nPots:\n");
        if (pots.isEmpty()) {
            sb.append("No active pots\n");
        } else {
            for (int i = 0; i < pots.size(); i++) {
                sb.append("Pot ").append(i + 1).append(": ").append(pots.get(i).toString()).append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * Inner class representing a player in the game
     */
    private class Player {
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
            // Format: (x, y) facing (dx, dy) holding [object@(x, y) | None]
            Pattern pattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\)\\s*facing\\s*\\(([-]?\\d+),\\s*([-]?\\d+)\\)\\s*holding\\s*(\\w+@\\(\\d+,\\s*\\d+\\)|None)");
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
            } else {
                this.holding = 0;    // Unknown object
            }
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
                default -> sb.append("nothing");
            }
            
            return sb.toString();
        }
    }

    private class Pot {
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
                sb.append(" cooking: ").append(cookingTick).append(" ticks remaining");
            }
            
            return sb.toString();
        }
    }
}

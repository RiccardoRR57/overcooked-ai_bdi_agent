import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grid {
    private final int width;          // Grid width in cells
    private final int height;         // Grid height in cells
    private final char[][] grid;      // Static terrain representation
    private final char[][] objects;   // Dynamic game objects
    private Player player1;           // First player representation
    private Player player2;           // Second player representation

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
        // Remove outer brackets/braces
        objectsString = objectsString.replaceAll("^\\(\\{|\\}\\)$", "");
        
        // Pattern to match (x, y): object@(x, y)
        Pattern pattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\):\\s*(\\w+)@\\(\\d+,\\s*\\d+\\)");
        Matcher matcher = pattern.matcher(objectsString);
        
        while (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));  // Object x position
            int y = Integer.parseInt(matcher.group(2));  // Object y position
            String objectType = matcher.group(3).toLowerCase();  // Object type
            
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
        sb.append('\n');
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
    }
}

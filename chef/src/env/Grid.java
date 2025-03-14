import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Grid {
    private final int width;
    private final int height;
    private final char[][] grid;
    private final char[][] objects;
    private Player player1;
    private Player player2;

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

    public void resetObjects() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                objects[y][x] = 0;
            }
        }
    }

    public void setPlayer(String player, int playerNum) {
        switch (playerNum) {
            case 1 -> player1 = new Player(player);
            case 2 -> player2 = new Player(player);
            default -> throw new IllegalArgumentException("Invalid player number");
        }
    }

    public void setObjects(String objectsString) {
        // Remove outer brackets/braces
        objectsString = objectsString.replaceAll("^\\(\\{|\\}\\)$", "");
        
        // Pattern to match (x, y): object@(x, y)
        Pattern pattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\):\\s*(\\w+)@\\(\\d+,\\s*\\d+\\)");
        Matcher matcher = pattern.matcher(objectsString);
        
        while (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            String objectType = matcher.group(3).toLowerCase();
            
            // Get first letter of object type as lowercase
            char objectChar = objectType.charAt(0);
            
            // Add object to grid (as lowercase letter)
            this.objects[y][x] = objectChar;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (objects[y][x] != 0) {
                    sb.append(objects[y][x]);
                } else if (player1 != null && player1.x == x && player1.y == y) {
                    sb.append('1');
                } else if (player2 != null && player2.x == x && player2.y == y) {
                    sb.append('2');
                } else{
                    sb.append(grid[y][x]);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private class Player {
        private int x;
        private int y;
        private int dx;
        private int dy;
        private char holding;

        public Player(String player) {
            // Pattern to match player position, facing direction and held object
            // Format: (x, y) facing (dx, dy) holding [object@(x, y) | None]
            Pattern pattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\)\\s*facing\\s*\\(([-]?\\d+),\\s*([-]?\\d+)\\)\\s*holding\\s*(\\w+@\\(\\d+,\\s*\\d+\\)|None)");
            
            // Parse player 1
            if (player != null && !player.isEmpty()) {
                Matcher matcher = pattern.matcher(player);
                if (matcher.find()) {
                    this.x = Integer.parseInt(matcher.group(1));
                    this.y = Integer.parseInt(matcher.group(2));
                    this.dx = Integer.parseInt(matcher.group(3));
                    this.dy = Integer.parseInt(matcher.group(4));
                    
                    // Parse the held object
                    String heldObject = matcher.group(5);
                    if (heldObject.equals("None")) {
                        this.holding = 0; // Not holding anything
                    } else {
                        // Extract the object type from the string (format: objectType@(x,y))
                        if (heldObject.startsWith("onion")) {
                            this.holding = 'o';
                        } else if (heldObject.startsWith("tomato")) {
                            this.holding = 't';
                        } else if (heldObject.startsWith("dish")) {
                            this.holding = 'd';
                        } else {
                            // Default case - unknown object
                            this.holding = 0;
                        }
                    }
                }
            }
        }
    }
}

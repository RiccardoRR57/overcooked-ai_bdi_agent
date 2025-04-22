import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.parser.ParseException;

public class Grid {
    private final int width;          // Grid width in cells
    private final int height;         // Grid height in cells
    private final char[][] grid;      // Static terrain representation
    private final char[][] objects;   // Dynamic game objects
    private final Player[] players; // Array of player representations
    private final List<Pot> pots;     // List of pots
    private int timestep;
    private List<Order> bonusOrders;   // Special orders with extra points
    private List<Order> allOrders;    // All available orders
    private final boolean[] bdi;

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
        this.bdi = new boolean[2];
        this.players = new Player[2]; // Two players in the game
        this.allOrders = new ArrayList<>();
        this.bonusOrders = new ArrayList<>();
        
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
     * Sets the BDI agent status for the specified player ID.
     *
     * @param id the player ID (0 or 1)
     */
    public void setBdiAgent(int id) {
        if (id < 0 || id >= bdi.length) {
            throw new IllegalArgumentException("Invalid player ID");
        }
        bdi[id] = true;
    }

    /**
     * Checks if the specified player ID is a BDI agent.
     *
     * @param id the player ID (0 or 1)
     * @return true if the player is a BDI agent, false otherwise
     */
    public boolean isBdiAgent(int id) {
        if (id < 0 || id >= bdi.length) {
            throw new IllegalArgumentException("Invalid player ID");
        }
        return bdi[id];
    }

    /**
     * Gets the width of the grid as a Jason literal.
     *
     * @return a literal representing the width of the grid
     * @throws ParseException if there is an error parsing the literal
     */
    public Literal getWidthLiteral() throws ParseException {
        StringBuilder sb = new StringBuilder();
        sb.append("width(").append(width).append(")");
        return ASSyntax.parseLiteral(sb.toString());
    }

    /**
     * Gets the height of the grid as a Jason literal.
     *
     * @return a literal representing the height of the grid
     * @throws ParseException if there is an error parsing the literal
     */
    public Literal getHeightLiteral() throws ParseException {
        StringBuilder sb = new StringBuilder();
        sb.append("height(").append(height).append(")");
        return ASSyntax.parseLiteral(sb.toString());
    }

    /**
     * Gets the current timestep of the game as a Jason literal.
     *
     * @return a literal representing the current timestep
     * @throws ParseException if there is an error parsing the literal
     */
    public Literal getTimestepLiteral() throws ParseException {
        StringBuilder sb = new StringBuilder();
        sb.append("timestep(").append(timestep).append(")");
        return ASSyntax.parseLiteral(sb.toString());
    }

    /**
     * Gets all regular orders as Jason literals.
     *
     * @return a list of literals representing all available orders
     * @throws ParseException if there is an error parsing the literals
     */
    public List<Literal> getOrdersLiterals() throws ParseException {
        List<Literal> orders = new ArrayList<>();
        for (Order o : allOrders) {
            orders.add(o.getLiteral(false));
        }
        return orders;
    }

    /**
     * Gets all bonus orders as Jason literals.
     *
     * @return a list of literals representing all bonus orders
     * @throws ParseException if there is an error parsing the literals
     */
    public List<Literal> getBonusOrdersLiterals() throws ParseException {
        List<Literal> orders = new ArrayList<>();
        for (Order o : bonusOrders) {
            orders.add(o.getLiteral(true));
        }
        return orders;
    }

    /**
     * Converts the static grid cells to Jason literals.
     * Returns only non-empty cells (cells that have a counter, ingredient source, etc.).
     *
     * @return a list of literals representing all non-empty cells in the grid
     * @throws ParseException if there is an error parsing the literals
     */
    public List<Literal> getCellLiterals() throws ParseException {
        List<Literal> cells = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                StringBuilder sb = new StringBuilder();
                sb.append("cell(");
                switch (grid[y][x]) {
                    case 'X' -> 
                        sb.append("counter");
                    case 'O' -> // Onions
                        sb.append("onion");
                    case 'T' -> // Tomatoes
                        sb.append("tomato");
                    case 'P' -> // Pot
                        sb.append("pot");
                    case 'D' -> // Dishes
                        sb.append("dish");
                    case 'S' -> // Serving station
                        sb.append("serve");
                    default -> sb.append("empty");
                }
                sb.append(", ").append(x).append(", ").append(y).append(")");

                if(!sb.toString().contains("empty")) {
                    cells.add(ASSyntax.parseLiteral(sb.toString()));
                }
            }
        }
        return cells;
    }

    /**
     * Converts dynamic objects on the grid to Jason literals.
     * Only returns literals for non-empty objects.
     *
     * @return a list of literals representing objects currently on the grid
     * @throws ParseException if there is an error parsing the literals
     */
    public List<Literal> getObjectsLiterals() throws ParseException {
        List<Literal> objs = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char obj = objects[y][x];
                if (obj != 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("object(");
                    switch (obj) {
                        case 'o' -> 
                            sb.append("onion");
                        case 't' ->
                            sb.append("tomato");
                        case 'd' -> 
                            sb.append("dish");
                        default -> sb.append("empty");
                    }
                    sb.append(", ").append(x).append(", ").append(y).append(")");

                    if(!sb.toString().contains("empty")) {
                        objs.add(ASSyntax.parseLiteral(sb.toString()));
                    }
                }
            }
        }
        return objs;
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
     * @param playerId Player number (0 or 1)
     */
    public void setPlayer(String player, int playerId) {
        if (player == null || player.isEmpty()) {
            throw new IllegalArgumentException("Invalid player number");  // No player information provided
        }
        players[playerId] = new Player(player);  // Create a new player object
    }

    /**
     * Parses and sets the regular orders from string input
     * 
     * @param input String representation of orders
     */
    public void setOrders(String input) {
        allOrders = new ArrayList<>();
        List<List<String>> ingrList = parseIngredientString(input);  // Parse ingredients from string
        for (List<String> ingredients : ingrList) {
            Order o = new Order(ingredients);  // Create order for each ingredient list
            allOrders.add(o);                 // Add to orders collection
        }
    }

    /**
     * Parses and sets the bonus orders from string input
     * 
     * @param input String representation of bonus orders
     */
    public void setBonusOrders(String input) {
        bonusOrders = new ArrayList<>();
        List<List<String>> ingrList = parseIngredientString(input);
        for (List<String> ingredients : ingrList) {
            Order o = new Order(ingredients);
            bonusOrders.add(o);
        }
    }

    /**
     * Parses a string representation of ingredients into a list of ingredient lists
     * 
     * @param input String representation of ingredients in Python format
     * @return Parsed list of ingredient lists
     */
    private static List<List<String>> parseIngredientString(String input) {
        // Convert Python tuple format to JSON format
        // Example: From ((a, b), (c, d)) to [[a, b], [c, d]]
        input = input.replace('(', '[')
                     .replace(')', ']');

        // Convert Python string quotes to JSON string quotes
        // Example: From ['tomato'] to ["tomato"]
        input = input.replace("'", "\"");
        
        // Remove trailing commas in arrays which are invalid in JSON
        // Example: From [a, b,] to [a, b]
        input = input.replaceAll(",\\s*]", "]");

        // Parse using Jackson JSON library
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(input, new TypeReference<List<List<String>>>(){});
        } catch (JsonProcessingException ex) {
            return null;
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
        // Matches format like: (3, 2): soup@(0, 0)
        Pattern soupPattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\):\\s*soup@\\(\\d+,\\s*\\d+\\)");
        Matcher soupMatcher = soupPattern.matcher(objectsString);
        
        while (soupMatcher.find()) {
            // Extract coordinates where the pot is located
            int potX = Integer.parseInt(soupMatcher.group(1));
            int potY = Integer.parseInt(soupMatcher.group(2));
            
            // Find ingredients list and cooking tick for this soup
            // This information appears after the soup position in the string
            int soupStart = soupMatcher.start();
            
            // Extract a region of text after the soup position to find ingredients and cooking tick
            // Limit to 200 characters, which should be enough to contain all necessary info
            int searchEndIndex = Math.min(soupStart + 200, objectsString.length());
            String potRegion = objectsString.substring(soupStart, searchEndIndex);
            
            // Extract ingredients list with format: Ingredients: [tomato@(...), onion@(...)]
            Pattern ingredPattern = Pattern.compile("Ingredients:\\s*\\[(.*?)\\]");
            Matcher ingredMatcher = ingredPattern.matcher(potRegion);
            
            if (ingredMatcher.find()) {
                // Extract cooking tick with format: Cooking Tick: 3
                Pattern tickPattern = Pattern.compile("Cooking Tick:\\s*(-?\\d+)");
                Matcher tickMatcher = tickPattern.matcher(potRegion);
                
                if (tickMatcher.find()) {
                    // Construct a pot string in the format that Pot constructor expects
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
                } else if (players[0] != null && players[0].getX() == x && players[0].getY() == y) {
                    sb.append('1');  // Show player 1
                } else if (players[1] != null && players[1].getX() == x && players[1].getY() == y) {
                    sb.append('2');  // Show player 2
                } else{
                    sb.append(grid[y][x]);  // Show terrain
                }
            }
            sb.append("\n");
        }
        
        // Player state
        sb.append("\nPlayers:\n");
        if (players[0] != null) {
            sb.append("Player 0: ").append(players[0].toString()).append("\n");
        }
        if (players[1] != null) {
            sb.append("Player 1: ").append(players[1].toString()).append("\n");
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
     * Gets the first player's object
     *
     * @return the Player object
     */
    public Player getPlayer(int playerId) {
        if (playerId < 0 || playerId > 1) {
            throw new IllegalArgumentException("Invalid player number");
        }
        return players[playerId];
    }

    /**
     * Sets the current timestep of the game
     *
     * @param timestep current game timestep
     */
    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }
}

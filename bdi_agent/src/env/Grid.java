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
    private Player player1;           // First player representation
    private Player player2;           // Second player representation
    private final List<Pot> pots;     // List of pots
    private int timestep;
    private List<Order> bonusOrders;   // Special orders with extra points
    private List<Order> allOrders;    // All available orders

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
        input = input.replace('(', '[')
                     .replace(')', ']');

        // Convert Python string quotes to JSON string quotes
        input = input.replace("'", "\"");
        
        // Remove trailing commas in arrays which are invalid in JSON
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
                } else if (player1 != null && player1.getX() == x && player1.getY() == y) {
                    sb.append('1');  // Show player 1
                } else if (player2 != null && player2.getX() == x && player2.getY() == y) {
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

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }
}

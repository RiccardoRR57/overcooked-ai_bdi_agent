import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Structure;
import jason.asSyntax.parser.ParseException;
import jason.environment.Environment;
import py4j.GatewayServer;

public class Env extends Environment {

    private static final Logger logger = Logger.getLogger("chef."+Env.class.getName());
    private GatewayServer server;  // For Python-Java communication

    private Grid grid;             // Game world representation
    private List<Order> bonus_orders;  // Special orders with extra points
    private List<Order> all_orders;    // All available orders
    private int timestep;          // Current game tick counter

    
    /**
     * Called before the MAS execution with the args informed in .mas2j
     * Initializes the environment and starts the Python-Java gateway server
     * 
     * @param args Arguments passed from the .mas2j file
     */
    @Override
    public void init(String[] args) {
        super.init(args);
        try {
            addPercept(ASSyntax.parseLiteral("percept("+args[0]+")"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        server = new GatewayServer(this);
        server.start();
    }

    /**
     * Executes agent actions in the environment
     * 
     * @param agName The agent's name performing the action
     * @param action The structure representing the action to be executed
     * @return true if the action was executed successfully
     */
    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("executing: "+action+", but not implemented!");
        if (true) { // you may improve this condition
             informAgsEnvironmentChanged();
        }
        return true; // the action was executed with success
    }

    /**
     * Updates the environment state with new game information
     * 
     * @param player1 String representation of player 1's state
     * @param player2 String representation of player 2's state
     * @param objects String representation of objects in the environment
     * @param bonus_orders String representation of bonus orders
     * @param all_orders String representation of all available orders
     * @param timestep Current time step in the game
     */
    public void updateState(String player1, String player2, String objects, String bonus_orders, String all_orders, int timestep) {

        setBonusOrders(bonus_orders);  // Update special orders
        setOrders(all_orders);         // Update regular orders
        this.timestep=timestep;        // Update time
        grid.setObjects(objects);      // Update game objects
        grid.setPlayer(player1, 1);    // Update player 1 position
        grid.setPlayer(player2, 2);    // Update player 2 position

        logger.info(grid.toString());  // Log game state
    }

    /**
     * Returns a random action for the agent to perform
     * 
     * @return An integer representing a random action (0-5)
     */
    public int getAction() {
        logger.info("entrato nella funzione getaction");
        Random r = new Random();
        return r.nextInt(6);
    }

    /**
     * Resets the environment to a new state
     * 
     * @param height Grid height
     * @param width Grid width
     * @param terrain String representation of the terrain
     * @param bonus_orders String representation of bonus orders
     * @param all_orders String representation of all available orders
     */
    public void reset(int height, int width, String terrain, String bonus_orders, String all_orders) {
        this.grid = new Grid(height, width, terrain);  // Initialize new game world
        setBonusOrders(bonus_orders);                  // Set initial special orders
        setOrders(all_orders);                         // Set initial regular orders
    }

    /**
     * Called before the end of MAS execution
     * Cleans up resources before environment shutdown
     */
    @Override
    public void stop() {
        super.stop();
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
            logger.severe("Error parsing order string: ");
            logger.severe(ex.getMessage());
            return null;
        }
    }

    /**
     * Parses and sets the regular orders from string input
     * 
     * @param input String representation of orders
     */
    private void setOrders(String input) {
        all_orders = new ArrayList<>();
        List<List<String>> ingrList = parseIngredientString(input);  // Parse ingredients from string
        for (List<String> ingredients : ingrList) {
            Order o = new Order(ingredients);  // Create order for each ingredient list
            all_orders.add(o);                 // Add to orders collection
        }
    }
    
    /**
     * Parses and sets the bonus orders from string input
     * 
     * @param input String representation of bonus orders
     */
    private void setBonusOrders(String input) {
        bonus_orders = new ArrayList<>();
        List<List<String>> ingrList = parseIngredientString(input);
        for (List<String> ingredients : ingrList) {
            Order o = new Order(ingredients);
            bonus_orders.add(o);
        }
    }
}

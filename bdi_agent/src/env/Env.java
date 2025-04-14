import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.parser.ParseException;
import jason.environment.Environment;
import py4j.GatewayServer;

public class Env extends Environment {

    private static final Logger logger = Logger.getLogger("bdi_agent." + Env.class.getName());
    private GatewayServer server;  // For communication with overcooked server

    // Constants for action codes
    private static final int ACTION_NONE = 0;
    private static final int ACTION_NORTH = 1;
    private static final int ACTION_SOUTH = 2;
    private static final int ACTION_WEST = 3;
    private static final int ACTION_EAST = 4;
    private static final int ACTION_INTERACT = 5;

    private Grid grid;                 // Game world representation

    // private ArrayBlockingQueue<Integer> actionQueue = new ArrayBlockingQueue<>(1);
    int action = 0;

    /**
     * Called before the MAS execution with the args informed in .mas2j
     * Initializes the environment and starts the Python-Java gateway server
     *
     * @param args Arguments passed from the .mas2j file
     */
    @Override
    public void init(String[] args) {
        super.init(args);
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
        String actionName = action.getFunctor();
        switch (actionName) {
            case "north":
                this.action = ACTION_NORTH;
                break;
            case "south":
                this.action = ACTION_SOUTH;
                break;
            case "west":
                this.action = ACTION_WEST;
                break;
            case "east":
                this.action = ACTION_EAST;
                break;
            case "interact":
                this.action = ACTION_INTERACT;
                break;
            default:
                logger.warning("Unknown action: " + actionName);
                return false;
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

        grid.setBonusOrders(bonus_orders);  // Update special orders
        grid.setOrders(all_orders);         // Update regular orders
        grid.setTimestep(timestep);    // Update time
        grid.setObjects(objects);      // Update game objects
        grid.setPlayer(player1, 1);    // Update player 1 position
        grid.setPlayer(player2, 2);    // Update player 2 position

        updatePercepts();  // Update percepts for agents
        informAgsEnvironmentChanged();  // Inform agents of environment change
    }

    /**
     * Gets the current action chosen by the agent and resets it
     * 
     * @return The action code (1-5) or 0 if no action
     */
    public int getAction() {
        int ret = this.action;
        this.action = ACTION_NONE;
        return ret;
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
        // Initialize new game world with terrain layout
        this.grid = new Grid(height, width, terrain);
        
        // NOTE: The following lines are commented out because the orders
        // are set later in updateState() method instead
        grid.setBonusOrders(bonus_orders);  // Would set initial special orders
        grid.setOrders(all_orders);         // Would set initial regular orders
        
        try {
            // Clear all existing percepts to start fresh
            clearAllPercepts();
            
            // Add grid dimensions as percepts for the agent
            addPercept("bdi_agent", grid.getHeightLiteral());
            addPercept("bdi_agent", grid.getWidthLiteral());
            
            // Add static cell locations (counters, serving stations, etc.)
            for (Literal cell : grid.getCellLiterals()) {
                addPercept("bdi_agent", cell);
            }
            
            // Notify agents about changes to the environment
            informAgsEnvironmentChanged();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called before the end of MAS execution Cleans up resources before
     * environment shutdown
     */
    @Override
    public void stop() {
        super.stop();
    }

    /**
     * Updates the percepts for the agents.
     * Clears existing percepts and adds current game state information.
     */
    private void updatePercepts() {
        clearPercepts("bdi_agent");
        try {
            // Add grid dimensions as percepts for the agent
            addPercept("bdi_agent", grid.getHeightLiteral());
            addPercept("bdi_agent", grid.getWidthLiteral());
            
            // Add static cell locations (counters, serving stations, etc.)
            for (Literal cell : grid.getCellLiterals()) {
                addPercept("bdi_agent", cell);
            }

            for (Literal order : grid.getBonusOrdersLiterals()) {
                addPercept("bdi_agent", order);
            }
            for (Literal order : grid.getOrdersLiterals()) {
                addPercept("bdi_agent", order);
            }
            
            addPercept("bdi_agent", grid.getPlayer1().getLiteral(1));
            addPercept("bdi_agent", grid.getPlayer2().getLiteral(2));
            for (Literal obj : grid.getObjectsLiterals()) {
                addPercept("bdi_agent", obj);
            }
            addPercept("bdi_agent", grid.getTimestepLiteral());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

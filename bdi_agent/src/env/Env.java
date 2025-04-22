import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
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

    private final ArrayBlockingQueue<Integer> actionQueue0 = new ArrayBlockingQueue<>(1);
    private final ArrayBlockingQueue<Integer> actionQueue1 = new ArrayBlockingQueue<>(1);

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
        ArrayBlockingQueue<Integer> actionQueue;
        switch (agName) {
            case "player0" -> actionQueue = actionQueue0;
            case "player1" -> actionQueue = actionQueue1;
            default -> {
                logger.log(Level.WARNING, "Unknown agent: {0}", agName);
                return false;
            }
        }
        try {
            switch (actionName) {
                case "north" -> actionQueue.put(ACTION_NORTH);
                case "south" -> actionQueue.put(ACTION_SOUTH);
                case "west" -> actionQueue.put(ACTION_WEST);
                case "east" -> actionQueue.put(ACTION_EAST);
                case "interact" -> actionQueue.put(ACTION_INTERACT);
                default -> {
                    logger.log(Level.WARNING, "Unknown action: {0}", actionName);
                    return false;
                }
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error while executing action: {0}", e.getMessage());
            Thread.currentThread().interrupt();  // Restore interrupted status
            return false;
        }
        return true; // the action was executed with success
    }

    /**
     * Updates the environment state with new game information
     *
     * @param player0 String representation of player 0's state
     * @param player1 String representation of player 1's state
     * @param objects String representation of objects in the environment
     * @param bonus_orders String representation of bonus orders
     * @param all_orders String representation of all available orders
     * @param timestep Current time step in the game
     */
    public void updateState(String player0, String player1, String objects, String bonus_orders, String all_orders, int timestep) {

        grid.setBonusOrders(bonus_orders);  // Update special orders
        grid.setOrders(all_orders);         // Update regular orders
        grid.setTimestep(timestep);    // Update time
        grid.setObjects(objects);      // Update game objects
        grid.setPlayer(player0, 0);    // Update player 0 position
        grid.setPlayer(player1, 1);    // Update player 1 position

        if(grid.isBdiAgent(0)) {
            // Update percepts for player 0 (BDI agent)
            updatePercepts(0);
        }
        if(grid.isBdiAgent(1)) {
            // Update percepts for player 1 (BDI agent)
            updatePercepts(1);
        }
        informAgsEnvironmentChanged();  // Inform agents of environment change
    }

    /**
     * Gets the current action chosen by the agent and resets it
     * 
     * @return The action code (1-5) or 0 if no action
     */
    public int getAction(int id) throws InterruptedException {
        if (id == 0) {
            return 0;
            //return actionQueue0.take();  // Wait for an action to be available
        } else if (id == 1) {
            return actionQueue1.take();  // Wait for an action to be available
        }
        return ACTION_NONE;  // No action available
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
    public void reset(int height, int width, String terrain, String bonus_orders, String all_orders, int id) {

        // Initialize new game world with terrain layout
        this.grid = new Grid(height, width, terrain);
        this.grid.setBdiAgent(id);

        // NOTE: The following lines are commented out because the orders
        // are set later in updateState() method instead
        grid.setBonusOrders(bonus_orders);  // Would set initial special orders
        grid.setOrders(all_orders);         // Would set initial regular orders
        
        try {
            // Clear all existing percepts to start fresh
            clearAllPercepts();
            
            // Add grid dimensions as percepts for the agent
            addPercept(grid.getHeightLiteral());
            addPercept(grid.getWidthLiteral());
            
            // Add static cell locations (counters, serving stations, etc.)
            for (Literal cell : grid.getCellLiterals()) {
                addPercept(cell);
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
    private void updatePercepts(int playerId) {
        String agentName = "player" + playerId;
        int otherId = (playerId + 1) % 2; // Get the other player's ID
        clearPercepts(agentName);
        try {
            // Add grid dimensions as percepts for the agent
            addPercept(agentName, grid.getHeightLiteral());
            addPercept(agentName, grid.getWidthLiteral());
            
            // Add static cell locations (counters, serving stations, etc.)
            for (Literal cell : grid.getCellLiterals()) {
                addPercept(agentName, cell);
            }

            for (Literal order : grid.getBonusOrdersLiterals()) {
                addPercept(agentName, order);
            }
            for (Literal order : grid.getOrdersLiterals()) {
                addPercept(agentName, order);
            }
            
            addPercept(agentName, grid.getPlayer(playerId).getLiteral(false));
            addPercept(agentName, grid.getPlayer(otherId).getLiteral(true));

            for (Literal obj : grid.getObjectsLiterals()) {
                addPercept(agentName, obj);
            }
            addPercept(agentName, grid.getTimestepLiteral());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

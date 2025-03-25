
import java.util.logging.Logger;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.parser.ParseException;
import jason.environment.Environment;
import py4j.GatewayServer;

public class Env extends Environment {

    private static final Logger logger = Logger.getLogger("bdi_agent." + Env.class.getName());
    private GatewayServer server;  // For communication with overcooked server

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
                this.action = 1;
                // actionQueue.add(1);
                break;
            case "south":
                this.action = 2;
                // actionQueue.add(2);
                break;
            case "west":
                this.action = 3;
                // actionQueue.add(3);
                break;
            case "east":
                this.action = 4;
                // actionQueue.add(4);
                break;
            case "interact":
                this.action = 5;
                // actionQueue.add(5);
                break;
            default:
                logger.warning("Unknown action: " + actionName);
                return false;
        }

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

        grid.setBonusOrders(bonus_orders);  // Update special orders
        grid.setOrders(all_orders);         // Update regular orders
        grid.setTimestep(timestep);    // Update time
        grid.setObjects(objects);      // Update game objects
        grid.setPlayer(player1, 1);    // Update player 1 position
        grid.setPlayer(player2, 2);    // Update player 2 position

        updatePercepts();  // Update percepts for agents
        informAgsEnvironmentChanged();  // Inform agents of environment change
    }

    public int getAction() {
        int ret = this.action;
        this.action = 0;
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
        this.grid = new Grid(height, width, terrain);  // Initialize new game world
        //setBonusOrders(bonus_orders);                  // Set initial special orders
        //setOrders(all_orders);                         // Set initial regular orders
        try {
            clearAllPercepts();
            addPercept("bdi_agent", grid.getHeightLiteral());
            addPercept("bdi_agent", grid.getWidthLiteral());
            for (Literal cell : grid.getCellLiterals()) {
                addPercept("bdi_agent", cell);
            }
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
     * Updates the percepts for the agents
     */
    private void updatePercepts() {
        clearPercepts("bdi_agent");
        try {
            for (Literal order : grid.getBonusOrdersLiterals()) {
                addPercept("bdi_agent", order);
            }
            for (Literal order : grid.getOrdersLiterals()) {
                addPercept("bdi_agent", order);
            }
            addPercept("bdi_agent", grid.getTimestepLiteral());
            addPercept("bdi_agent", grid.getPlayer1().getLiteral(1));
            addPercept("bdi_agent", grid.getPlayer2().getLiteral(2));
            for (Literal obj : grid.getObjectsLiterals()) {
                addPercept("bdi_agent", obj);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

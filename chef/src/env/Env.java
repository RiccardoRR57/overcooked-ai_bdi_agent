
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
import jason.functions.time;
import py4j.GatewayServer;

public class Env extends Environment {

    private static final Logger logger = Logger.getLogger("chef."+Env.class.getName());
    private GatewayServer server;

    private String grid;
    private int grid_height;
    private int grid_width;
    private List<List<String>> bonus_orders;
    private List<List<String>> all_orders;
    private int timestep;

    
    /** Called before the MAS execution with the args informed in .mas2j */
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
        logger.info("Gateway Server Started at" + server.getAddress() + ":" + server.getListeningPort());
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("executing: "+action+", but not implemented!");
        if (true) { // you may improve this condition
             informAgsEnvironmentChanged();
        }
        return true; // the action was executed with success
    }

    public void updateState(String player1, String player2, String objects, String bonus_orders, String all_orders, int timestep) {
        
        logger.info("entrato nella funzione updatestate");

        this.bonus_orders=parseString(bonus_orders);
        this.all_orders=parseString(all_orders);
        this.timestep=timestep;

        logger.info(player1);
        logger.info(player2);
        logger.info(objects);
    }

    public int getAction() {
        logger.info("entrato nella funzione getaction");
        Random r = new Random();
        return r.nextInt(6);
    }

    public void reset(int height, int width, String terrain, String bonus_orders, String all_orders) {

        logger.info("entrato nella funzione reset");
        logger.info(String.valueOf(height));
        logger.info(String.valueOf(width));
        logger.info(terrain);
        logger.info(bonus_orders);
        logger.info(all_orders);

        this.grid_height = height;
        this.grid_width = width;
        this.grid = terrain;
        this.all_orders=parseString(all_orders);
        this.bonus_orders=parseString(bonus_orders);
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }

    public static List<List<String>> parseString(String input) {
        // 1. Convert parentheses to square brackets
        input = input.replace('(', '[')
                     .replace(')', ']');

        // 2. Convert single quotes to double quotes
        input = input.replace("'", "\"");

        // 3. Parse the resulting string as JSON
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(input, new TypeReference<List<List<String>>>(){});
        } catch (JsonProcessingException ex) {
            logger.severe("Error parsing order string: ");
            logger.severe(ex.getMessage());
            return null;
        }
    }
}

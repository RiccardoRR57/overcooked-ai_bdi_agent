import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Grid grid;
    private List<Order> bonus_orders;
    private List<Order> all_orders;
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

        setBonusOrders(bonus_orders);
        setOrders(all_orders);
        this.timestep=timestep;
        
        // Parse objects and update grid
        if (grid != null) {
            parseObjects(objects);
        }

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

        this.grid = new Grid(height, width, terrain);
        setBonusOrders(bonus_orders);
        setOrders(all_orders);
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }

    private static List<List<String>> parseIngredientString(String input) {
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

    private void setOrders(String input) {
        all_orders = new ArrayList<>();
        List<List<String>> ingrList = parseIngredientString(input);
        for (List<String> ingredients : ingrList) {
            Order o = new Order(ingredients);
            all_orders.add(o);
        }
    }
    
    private void setBonusOrders(String input) {
        bonus_orders = new ArrayList<>();
        List<List<String>> ingrList = parseIngredientString(input);
        for (List<String> ingredients : ingrList) {
            Order o = new Order(ingredients);
            bonus_orders.add(o);
        }
    }

    /**
     * Parse objects string and add them to the grid as lowercase letters
     * Example input: "({(8, 4): dish@(8, 4), (9, 4): tomato@(9, 4), (2, 0): tomato@(2, 0), (11, 2): onion@(11, 2), (3, 4): dish@(3, 4)}"
     */
    private void parseObjects(String input) {
        // Remove outer brackets/braces
        input = input.replaceAll("^\\(\\{|\\}\\)$", "");
        
        // Pattern to match (x, y): object@(x, y)
        Pattern pattern = Pattern.compile("\\((\\d+),\\s*(\\d+)\\):\\s*(\\w+)@\\(\\d+,\\s*\\d+\\)");
        Matcher matcher = pattern.matcher(input);
        
        while (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            String objectType = matcher.group(3).toLowerCase();
            
            // Get first letter of object type as lowercase
            char objectChar = objectType.charAt(0);
            
            // Add object to grid (as lowercase letter)
            grid.setObject(x, y, objectChar);
            logger.info("Added object " + objectType + " (" + objectChar + ") at position (" + x + ", " + y + ")");
        }
    }
}

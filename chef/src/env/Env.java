import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Structure;
import jason.asSyntax.parser.ParseException;
import jason.environment.Environment;
import py4j.GatewayServer;

public class Env extends Environment {

    private Logger logger = Logger.getLogger("chef."+Env.class.getName());
    private GatewayServer server;

    private List<Order> orders;
    private List<Order> bonus_orders;
    
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

    public void start(String layout) {
        logger.info("entrato nella funzione start");
        //logger.info(layout);
    }

    public void updateState(String player1, String player2, String objects, String bonus_orders, String all_orders, String timestep) {
        
        logger.info("entrato nella funzione updatestate");

        logger.info(player1);
        logger.info(player2);
        logger.info(objects);
        logger.info(bonus_orders);
        logger.info(all_orders);
        logger.info(timestep);
    }

    public int getAction() {
        logger.info("entrato nella funzione getaction");
        Random r = new Random();
        return r.nextInt(6);
    }

    /**
     * Converts a Python dictionary string into a JSONObject
     * 
     * @param pythonDictString string containing Python dictionary syntax
     * @return JSONObject containing the parsed data
     * @throws JSONException if the conversion or parsing fails
     */
    private JSONObject convertPythonDictToJSON(String pythonDictString) throws JSONException {
        try {
            // Convert Python dictionary syntax to JSON
            String content = pythonDictString.replaceAll("True", "true")
                                             .replaceAll("False", "false")
                                             .replaceAll("None", "null");
            
            // Handle triple-quoted strings and convert Python dict to JSON format
            StringBuilder jsonString = new StringBuilder();
            boolean inString = false;
            boolean inTripleString = false;
            char stringQuote = ' ';
            
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                
                // Handle triple quotes (""" or ''')
                if (!inString && !inTripleString && c == '"' && i + 2 < content.length() && 
                    content.charAt(i+1) == '"' && content.charAt(i+2) == '"') {
                    inTripleString = true;
                    stringQuote = '"';
                    jsonString.append('"'); // Start with a single double quote for JSON
                    i += 2; // Skip the next two quotes
                    continue;
                } else if (!inString && !inTripleString && c == '\'' && i + 2 < content.length() && 
                          content.charAt(i+1) == '\'' && content.charAt(i+2) == '\'') {
                    inTripleString = true;
                    stringQuote = '\'';
                    jsonString.append('"'); // Start with a single double quote for JSON
                    i += 2; // Skip the next two quotes
                    continue;
                }
                
                // Handle end of triple quotes
                if (inTripleString && c == stringQuote && i + 2 < content.length() && 
                    content.charAt(i+1) == stringQuote && content.charAt(i+2) == stringQuote) {
                    inTripleString = false;
                    jsonString.append('"'); // End with a single double quote for JSON
                    i += 2; // Skip the next two quotes
                    continue;
                }
                
                // Handle regular quotes
                if (!inTripleString && (c == '\'' || c == '"') && (i == 0 || content.charAt(i-1) != '\\')) {
                    if (!inString) {
                        inString = true;
                        stringQuote = c;
                        jsonString.append('"'); // Use double quotes for JSON
                    } else if (c == stringQuote) {
                        inString = false;
                        jsonString.append('"'); // Use double quotes for JSON
                    } else {
                        // Different quote inside a string
                        jsonString.append(c);
                    }
                    continue;
                }
                
                // Handle content within strings
                if (inString || inTripleString) {
                    if (c == '\n' || c == '\r') {
                        if (inTripleString) {
                            // For triple quotes, preserve newlines as escaped characters
                            if (c == '\n') jsonString.append("\\n");
                            // Skip carriage returns
                        } else {
                            // Regular strings shouldn't have unescaped newlines in JSON
                            jsonString.append("\\n");
                        }
                    } else if (c == '\\') {
                        // Handle escape sequences
                        if (i + 1 < content.length()) {
                            char next = content.charAt(i+1);
                            if (next == '"' || next == '\\' || next == '/') {
                                // Keep these escape sequences
                                jsonString.append('\\');
                                jsonString.append(next);
                                i++;
                            } else if (next == 'n') {
                                jsonString.append("\\n");
                                i++;
                            } else if (next == 'r') {
                                jsonString.append("\\r");
                                i++;
                            } else if (next == 't') {
                                jsonString.append("\\t");
                                i++;
                            } else if (next == 'b') {
                                jsonString.append("\\b");
                                i++;
                            } else if (next == 'f') {
                                jsonString.append("\\f");
                                i++;
                            } else {
                                // Unrecognized escape sequence, keep backslash
                                jsonString.append('\\');
                            }
                        } else {
                            // Backslash at the end of content
                            jsonString.append('\\');
                        }
                    } else if (c == '"' && inTripleString && stringQuote != '"') {
                        // Double quotes in triple single-quoted strings don't need escaping
                        jsonString.append(c);
                    } else if (c == '"') {
                        // Escape double quotes in strings for JSON
                        jsonString.append("\\\"");
                    } else {
                        jsonString.append(c);
                    }
                } else {
                    jsonString.append(c);
                }
            }
            
            String jsonContent = jsonString.toString();
            logger.fine("Converted Python dict to JSON: " + jsonContent);
            return new JSONObject(jsonContent);
            
        } catch (JSONException e) {
            logger.severe("Error parsing Python dictionary string: " + e.getMessage());
            throw e;
        }
    }

    public void reset(String layout) {
        try {
            String dictFilePath = "src/env/layouts/" + layout + ".layout";
            String content = new String(Files.readAllBytes(Paths.get(dictFilePath)));
            JSONObject layoutJson = convertPythonDictToJSON(content);
            if (layoutJson != null) {
                logger.info(layoutJson.toString());
                add_orders(layoutJson.getJSONArray("start_all_orders"));
                add_bonus_orders(layoutJson.getJSONArray("start_bonus_orders"));
            }
        } catch (Exception e) {
            logger.severe("Error in reset method");
            e.printStackTrace();
        }

        logger.info("entrato nella funzione reset");
        logger.info(layout);
    }

    private void add_orders(JSONArray orders) {
        for (int i = 0; i < orders.length(); i++) {
            this.orders.add(new Order(orders.getJSONObject(i)));
        }
    }

    private void add_bonus_orders(JSONArray orders) {
        for (int i = 0; i < orders.length(); i++) {
            this.bonus_orders.add(new Order(orders.getJSONObject(i)));
        }
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}

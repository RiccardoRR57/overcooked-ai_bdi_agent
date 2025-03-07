package example;

// Environment code for project chef

import java.util.Random;
import java.util.logging.Logger;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Structure;
import jason.asSyntax.parser.ParseException;
import jason.environment.Environment;
import py4j.GatewayServer;

public class Env extends Environment {

    private Logger logger = Logger.getLogger("chef."+Env.class.getName());
    private GatewayServer server;
    
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

    public void reset(String layout) {
        logger.info("entrato nella funzione reset");
        logger.info(layout);
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}

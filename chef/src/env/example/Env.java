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
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("executing: "+action+", but not implemented!");
        if (true) { // you may improve this condition
             informAgsEnvironmentChanged();
        }
        return true; // the action was executed with success
    }

    public int getAction(String state) {
        logger.info("entrato nella funzione getaction");
        logger.info(state);
        Random r = new Random();
        return r.nextInt(6);
    }

    public void reset() {
        logger.info("entrato nella funzione reset");
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}

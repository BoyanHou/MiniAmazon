package ece651.mini_amazon;

import ece651.mini_amazon.exceptions.IncorrectArgsException;
import ece651.mini_amazon.exceptions.utilsException.IntFormatException;
import ece651.mini_amazon.service.daemonService.CommService;
import ece651.mini_amazon.service.daemonService.DaemonService;
import ece651.mini_amazon.service.daemonService.SeqNumService;
import ece651.mini_amazon.utils.tools.Logger;
import ece651.mini_amazon.utils.tools.NumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ServerInitializer implements ApplicationRunner {
    boolean noUPS;

    @Autowired
    DaemonService daemonService;
    @Autowired
    CommService commService;
    @Autowired
    SeqNumService seqNumService;

    @Resource
    ApplicationArguments args;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String[] argStr = args.getSourceArgs();
        if (argStr.length != 7) {
            String usageStr = "usage: gradle run --args='<upsIP> <upsPort> <worldIP> <worldPort> <localBindIP> <localBindPort> use_with_ups'";
            Logger.logSys(usageStr);
            throw new IncorrectArgsException(usageStr);
        }

        String upsIP = argStr[0];
        int upsPort;
        try {
            upsPort = NumUtils.strToInt(argStr[1]);
        } catch (IntFormatException e) {
            Logger.logErr("Incorrect ups port format: " + e.getMsg());
            throw e;
        }
        String worldIP = argStr[2];
        int worldPort;
        try {
            worldPort = NumUtils.strToInt(argStr[3]);
        } catch (IntFormatException e) {
            Logger.logErr("Incorrect world port format: " + e.getMsg());
            throw e;
        }
        String localBindIP = argStr[4];
        int localBindPort;
        try {
            localBindPort = NumUtils.strToInt(argStr[5]);
        } catch (IntFormatException e) {
            Logger.logErr("Incorrect local port format: " + e.getMsg());
            throw e;
        }

        String use_with_ups = argStr[6];

        if (use_with_ups.equals("YES")) {
            this.noUPS = false;
        } else {
            this.noUPS = true;
        }


        Logger.logSys("==>running ServerInitializer...");
//        String str = "=====>acquired args: ";
//        str += "upsIP:" + upsIP + " upsPort:" + upsPort;
//        str += "worldIP:" + worldIP + " worldPort:" + worldPort;
//        Logger.logSys(str);

        this.seqNumService.init(250);

        // init commService's primitive field settings
        this.commService.init(upsIP, upsPort, worldIP, worldPort, localBindIP, localBindPort, 10);

        // init socket connection with world
        this.commService.initWorldConnection();

        if (!this.noUPS) {  // if we have a ups to connect to
            // init socket connection with ups
            this.commService.initUPSConnection();

            // run ups receiver daemon
            this.daemonService.runUpsReceiverDaemon();

            // send AMessages:AInitialWorldid to UPS to acquire worldID
            daemonService.getInitialWorldIDForCommService();

            // wait for notification of received worldID from UPS
            synchronized (this.commService.getNotifier()) {
                if (this.commService.getWorldID() == null) {  // if at this point the worldID field has not been set yet
                    Logger.logSys("======>Have not received worldID from UPS yet, entering wait loop...");
                    while(true) {
                        this.commService.getNotifier().wait(2000); // sleep for 2 seconds if no notification received
                        if (this.commService.getWorldID() != null) {
                            Logger.logSys("=====>Found worldID:" + this.commService.getWorldID() +" in CommService, jumping out of waiting loop!");
                            break;
                        } else {
                            Logger.logSys("=====>Still no worldID arrived, continue waiting!");
                        }
                    }
                } else {
                    String skipMsg = "=====>WorldID in CommService has already been set to ";
                    skipMsg +=  this.commService.getWorldID() + ", skip waiting!";
                    Logger.logSys(skipMsg);
                }
            }
        }


        // send AConnect to world, recv AConnected from world (!!DO NOT the world receiver daemon for this!!)
        this.commService.confirmWorldConnection();

        // run world receiver daemon
        this.daemonService.runWorldReceiverDaemon();

        Logger.logSys("==>Finished ServerInitializer!");
    }
}

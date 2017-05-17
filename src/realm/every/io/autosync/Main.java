package realm.every.io.autosync;

import java.nio.file.Paths;

import realm.every.io.autosync.actions.Action;
import realm.every.io.autosync.actions.AddDirAction;

public class Main {

    static final String DEFAULT_USER = "mfay";
    static final String DEFAULT_REMOTE = "engdev3";
    static final String DEFAULT_LOCAL_REPO = "~/repo/trunk/";
    static final String DEFAULT_REMOTE_REPO = "/scratch_b/%user%/svn/trunk/";

    static final int DEFAULT_SERVER_PORT = 6667; // devil's port + # deadly sins

    public static void main(String[] args) {
        // Expectation:
        // <command> <arguments ...>
        if (args.length < 1) {
            System.out.println("Invalid call.\n" + getUsageStr());
            System.exit(0);
        }

        // Check command
        Action action = null;
        String command = args[0].trim().toLowerCase();
        switch(command) {
        case "help":
            System.out.println(getUsageStr());
            System.exit(0);
        // start the background process
        case "start":
            Server server = new Server(
                    Paths.get(DEFAULT_LOCAL_REPO),
                    Paths.get(String.format(DEFAULT_REMOTE_REPO), DEFAULT_USER),
                    DEFAULT_REMOTE,
                    DEFAULT_SERVER_PORT);
            server.run();
            break;
        // add a directory to sync
        case "add":
            String dirs;
            if (args.length > 1) {
                dirs = args[1];
            } else {
                // default to pwd
                dirs = Paths.get("").toAbsolutePath().toString();
            }
            action = new AddDirAction(dirs);
            break;
        // save current settings/configuration to file that can be reused
        case "save":
            break;
        // load settings/configuration from saved file
        case "load":
            break;
        default:
            System.out.println("Invalid command.\n" + getUsageStr());
            System.exit(0);
        }

        if (action != null) {
            Client client = new Client(DEFAULT_SERVER_PORT, action);
            client.run();
        }
    }

    static String getUsageStr() {
        return null;
    }
}

package gitlet;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Aryan Amberkar and Liam Grunfeld
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains\
     *  Implement init, add, commit, checkout, and log by 7/17
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static gitlet.Repository GITLET_REPOSITORY; //do we need to write a repository persistence file?
    public static File REPOSITORY_FILE = Utils.join(GITLET_REPOSITORY.GITLET_DIR, "repository.txt");

    public static void main(String[] args) {
        boolean noCommand = true; //Boolean tracking if no command is run, set to False if a command is run
        //If REPOSITORY_FILE isn't null, set this instance's GITLET_REPOSITORY to the written repository
        if(REPOSITORY_FILE.exists()){
            GITLET_REPOSITORY = Utils.readObject(REPOSITORY_FILE, Repository.class);
        }
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        if((!firstArg.equals("init")) && GITLET_REPOSITORY == null){
            System.out.println("Not in an initialized Gitlet directory");
        } else {
            switch (firstArg) {
                case "init":
                    if (args.length > 1) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    if (GITLET_REPOSITORY == null) { //If gitlet repository isn't created yet
                        GITLET_REPOSITORY = new Repository(); //Creates new repository object
                    } else {
                        System.out.println("A Gitlet version-control system already exists in the current directory.");
                    }
                    break;
                case "add":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.add(args[1]);
                    break;
                case "rm":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.remove(args[1]);
                    break;
                case "commit":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    String message = args[1];
                    GITLET_REPOSITORY.makeCommit(message);
                    break;
                case "log":
                    if (args.length > 1) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.log();
                    break;
                case "global-log":
                    if (args.length > 1) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.globalLog();
                    break;
                case "find":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.find(args[1]);
                    break;
                case "status":
                    if (args.length > 1) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.status();
                    break;
                case "checkout":
                    noCommand = false;
                    if (args.length == 2) {
                        GITLET_REPOSITORY.checkoutBranch(args[1]);
                    } else if (args.length == 3 && args[1].equals("--")) {
                        GITLET_REPOSITORY.checkoutHead(args[2]);
                    } else if (args.length == 4 && args[2].equals("--")) {
                        GITLET_REPOSITORY.checkoutCommit(args[1], args[3]);
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    break;
                case "branch":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.runBranch(args[1]);
                    break;
                case "rm-branch":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.removeBranch(args[1]);
                    break;
                case "reset":
                    if (args.length != 2) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    noCommand = false;
                    GITLET_REPOSITORY.reset((args[1]));
                    break;
            }
            if(noCommand){
                System.out.println("No command with that name exists.");
                System.exit(0);
            } else{
                Utils.writeObject(REPOSITORY_FILE, GITLET_REPOSITORY);
            }
        }


    }
}

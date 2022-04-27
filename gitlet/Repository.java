package gitlet;

import com.sun.source.tree.Tree;
import org.w3c.dom.Node;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *
 *
 *  Repository sets all the persistence files for gitlet,
 *  keeps track of the head commit,
 *  edits the directory with add, remove, and commit
 *
 *  @author Aryan Amberkar and Liam Grunfeld
 */
public class Repository implements Serializable {

    /**
     * TODO: add instance variables here.
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** Creates a staging area object */
    private StagingArea stagingArea;
    /** A linked list (or tree?) of the commits in the repository */
    //private Tree commits = new Tree();
    /** The current head commit */
    private Commit head;
    private String headHash;
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    /** creates a staging area file */
    public static File STAGING = Utils.join(GITLET_DIR, "staging.txt");
    /** creates a directory for commits */
    public static final File COMMIT_DIR = Utils.join(GITLET_DIR, "commits");
    /** Creates a commits tree file */
    //public static File COMMITS =
    /** creates initial commit object */
    public final Commit initialCommit;
    /** name of the current branch */
    private String currBranch;
    /** map of branch names and their head commit */
    private HashMap<String, String> branches = new HashMap<String, String>();
    /** a temporary staging file that holds the staging file of the parent commit */
    private StagingArea parentStaging = new StagingArea();

    //Constructs repository object
    public Repository(){
        GITLET_DIR.mkdir(); //actually sets up GITLET_DIR
        COMMIT_DIR.mkdir();
        Utils.writeContents(STAGING, ""); //Actually creates staging.txt
        stagingArea = new StagingArea();
        currBranch = "master";
        initialCommit = makeCommit("initial commit", new Date(0)); //makes initial commit w/ message and date 0
        //branch("master");

    }

    /** Finds file from string and calls addFile in stagingArea*/
    public void add(String fileName){ //contents might be different
        File tempFile = Utils.join(CWD, fileName);
        if(tempFile.exists()) { //if the file exists,
            stagingArea.removeFromRemove(fileName);
            // if the current head commit has this file and the contents are the same as the contents as the file being added, just remove the file from staging area
            if(head.getBlobs().getNameToStringMap().containsKey(fileName) &&
                    Utils.readContentsAsString(tempFile).equals(head.getBlobs().getNameToStringMap().get(fileName))){
                stagingArea.removeFile(tempFile);
            } else {
                stagingArea.addFile(tempFile); //adds file to the staging area object
                stagingArea.removeFromRemove(fileName);
            }
        } else if(stagingArea.getFilesToRemove().containsKey(fileName)){
            stagingArea.removeFromRemove(fileName);
            Utils.writeContents(tempFile, head.getBlobs().getNameToStringMap().get(fileName));
        } else {
            System.out.println("File does not exist.");
        }
        saveStaging();
    }

    public void remove(String fileName){
        if(!stagingArea.getNameToFileMap().containsKey(fileName)
                && !head.getBlobs().getNameToFileMap().containsKey(fileName)){
            System.out.println("No reason to remove the file.");
        } else if(stagingArea.getNameToFileMap().containsKey(fileName)) {
            File fileToRemove = Utils.join(CWD, fileName);
            stagingArea.removeFile(fileToRemove);
        } else {
            File fileToRemove = Utils.join(CWD, fileName);
            //stagingArea.removeFile(fileToRemove);
            stagingArea.addToRemove(fileToRemove);
            if (head.getBlobsSet().contains(fileName)) {
                Utils.restrictedDelete(fileToRemove);
            }
        }

    }

    /** Creates commit object, passes through the current headhash as the parent, and passes in the staging area
     * Also calls setHead to update the head and headhash
     * Also calls saveCommit to save the commit persistently as a file
     * Clears staging at the end.*/
    public Commit makeCommit(String message){ //pass through staging area object?
        if (message.equals("")){
            System.out.println("Please enter a commit message.");
        } else if(stagingArea.getNameToFileMap().keySet().isEmpty() && stagingArea.getFilesToRemove().keySet().isEmpty()){
            System.out.println("No changes added to the commit");
        } else {
            Commit newCommit = new Commit(message, new Date(), headHash, stagingArea); //creates new commit
            commitHelper(newCommit);
            return newCommit;
        }
        return null;
    }

    public Commit makeCommit(String message, Date date){ //pass through staging area object?
        Commit newCommit = new Commit(message, date, headHash, stagingArea); //creates new commit
        commitHelper(newCommit);
        return newCommit;
    }

    public void commitHelper (Commit newCommit){
        String commitHash = Utils.sha1(Utils.serialize(newCommit)); //Gets sha-1 hashcode for commit object
        setHead(newCommit, commitHash);
        saveCommit(newCommit, commitHash);
        branch(currBranch); //changes the current branch's head to the new commit's hash which is set as headHash by saveCommit
        clearStaging();
    }

    public void setHead(Commit headCommit, String commitHash){
        head = headCommit;
        headHash = commitHash;//Utils.sha1(Utils.serialize(head));
    }

    public void saveCommit(Commit commit, String hash){
        File commitFile = Utils.join(COMMIT_DIR, hash); //creates commitFile in GITLET_DIR w/ sha1 as file name
        Utils.writeObject(commitFile, commit); //writes commit object to the file commitFile
    }

    public void saveStaging() {
        Utils.writeObject(STAGING, stagingArea); //writes staging area to STAGING file
    }

    public void clearStaging(){
        stagingArea.wipeStaging();
        Utils.writeContents(STAGING,"");
    }

    public void log(){
        String currHeadHash = headHash;
        File currFile = Utils.join(COMMIT_DIR, currHeadHash);
        Commit currCommit = Utils.readObject(currFile, Commit.class);
        printLog(currCommit, currHeadHash);
        while(!currCommit.parentIsNull()){ //change to hasParent?
            currHeadHash = currCommit.getParentHash();
            currFile = Utils.join(COMMIT_DIR, currHeadHash);
            currCommit = Utils.readObject(currFile, Commit.class);
            printLog(currCommit, currHeadHash);
        }
    }

    public void printLog(Commit commit, String hashcode){
        String lineHash = "commit " + hashcode;
        String commitTime = commit.getTime();
        String lineDate = "Date: " + commitTime;
        String lineMessage = commit.getMessage();
        System.out.println("===");
        System.out.println(lineHash);
        System.out.println(lineDate);
        System.out.println(lineMessage);
        System.out.println();
    }

    public void globalLog(){
        List<String> fileList = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String fileName : fileList){
            if (!fileName.equals("repository") && !fileName.equals("staging")) {
                File currFile = Utils.join(COMMIT_DIR, fileName);
                Commit currCommit = Utils.readObject(currFile, Commit.class);
                printLog(currCommit, fileName);
            }
        }
    }

    public void find(String commitMessage){
        Boolean found = false;
        List<String> commitsList = Utils.plainFilenamesIn(COMMIT_DIR);
        for(String commit : commitsList){
            File commitFile = Utils.join(COMMIT_DIR, commit);
            Commit currCommit = readObject(commitFile, Commit.class);
            if(currCommit.getMessage().equals(commitMessage)){
                found = true;
                System.out.println(commit);
            }
        }
        if(!found){
            System.out.println("Found no commit with that message");
        }
    }

    public void status(){
        printBranches();
        printStagedFiles();
        printRemovedFiles();
        printModifications();
        printUntracked();
    }

    public void printBranches(){
        System.out.println("=== Branches ===");
        String[] branchesArray = branches.keySet().toArray(new String[branches.size()]);
        Arrays.sort(branchesArray, String.CASE_INSENSITIVE_ORDER);
        for (String branch : branchesArray){
            if(branch.equals(currBranch)){
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();
    }
    public void printStagedFiles(){
        System.out.println("=== Staged Files ===");
        String[] stagedArray = (String[]) stagingArea.getNameToFileMap().keySet().toArray(new String[stagingArea.getNameToFileMap().size()]);
        Arrays.sort(stagedArray, String.CASE_INSENSITIVE_ORDER);
        for (String file : stagedArray){
            System.out.println(file);
        }
        System.out.println();
    }
    public void printRemovedFiles(){
        System.out.println("=== Removed Files ===");
        String[] removedArray = (String[]) stagingArea.getFilesToRemove().keySet().toArray(new String[stagingArea.getFilesToRemove().size()]);
        Arrays.sort(removedArray, String.CASE_INSENSITIVE_ORDER);
        for (String file : removedArray){
            System.out.println(file);
        }
        System.out.println();
    }
    public void printModifications(){
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
    }
    public void printUntracked(){
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void checkoutBranch(String branchName){
        if (!branches.containsKey(branchName)){
            System.out.println("No such branch exists");
        } else if(currBranch.equals(branchName)){
            System.out.println("No need to checkout the current branch.");
        } else {
            String newHeadHash = branches.get(branchName); //new head hash
            File newHeadFile = Utils.join(COMMIT_DIR, newHeadHash); //new head file
            Commit newHeadCommit = Utils.readObject(newHeadFile, Commit.class); //new head Commit
            if (checkUntracked(newHeadCommit)){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                branches.remove("tempBranch");
                System.exit(0);
            }
            Iterator commitBlobIterator = newHeadCommit.getBlobsIterNameToString(); //gets iterator of blobs map
            //iterates through the map of file name to contents and calls changeFile on each file name
            while (commitBlobIterator.hasNext()) {
                Map.Entry currElement = (Map.Entry) commitBlobIterator.next();
                String currFileName = (String) currElement.getKey();
                String newContents = (String) currElement.getValue();
                changeFile(newContents, currFileName);
            }
            deleteUntracked(newHeadCommit);
            currBranch = branchName;
            setHead(newHeadCommit, newHeadHash); //sets head to the head of the given branch, as stored in the branches map
            clearStaging(); //I think this should be here?
        }
    }

    public void deleteUntracked(Commit newCommit){ //deletes files tracked in the current commit but not the new commit
        Iterator headIterator = head.getBlobsIterNameToString(); //iterator of the files in the commit
        Set newCommitBlobSet = newCommit.getBlobsSet();
        while ((headIterator.hasNext())){
            Map.Entry currElement = (Map.Entry) headIterator.next();
            String currFileName = (String) currElement.getKey(); //name of each file
            if (!newCommitBlobSet.contains(currFileName)){
                Utils.restrictedDelete(currFileName);
            }
        }

    }

    /** checks if the CWD contains a file that is not being tracked and will be replaced */
    public boolean checkUntracked(Commit newCommit){
        File[] fileArray = CWD.listFiles(); //array of files in CWD
        for(File file : fileArray){
            if(head.getFile(file.getName()) == null){ //if current commit doesn't contain the file
                if(newCommit.getFile(file.getName()) != null){ // if new commit does contain the file
                    return true;
                }
            }

        }
        return false;
    }

    public void checkoutCommit(String commitID, String fileName){
        File commitFile = Utils.join(COMMIT_DIR, commitID); //creates reference to the file of the given commit
        if (!commitFile.exists()){
            File foundFile = findFromAbbrev(commitID);
            if(foundFile != null){
                commitFile = foundFile;
            } else {
                System.out.println("No commit with that id exists.");
                return;
            }
        }
        Commit currCommit = Utils.readObject(commitFile, Commit.class); //Creates actual commit object from file
        String tempString = currCommit.getFileString(fileName); //Creates tempString object containing string of blob in commit, will be null if does not exist
        changeFile(tempString, fileName); //Call to change file

    }
    public File findFromAbbrev(String commitID){
        File commitFile = null;
        List<String> commitList =  Utils.plainFilenamesIn(COMMIT_DIR);
        for (String hash : commitList){
            String shortHash = hash.substring(0, commitID.length());
            if(shortHash.equals(commitID)){
                commitFile = Utils.join(COMMIT_DIR, hash);
            }
        }
        return commitFile;
    }

    public void checkoutHead(String fileName){
        String tempString = head.getFileString(fileName);
        changeFile(tempString, fileName);
    }

    public void changeFile(String commitFileString, String fileName){
        if(commitFileString == null){ //if the string is null, the file does not exist
            System.out.println("File does not exist in that commit");
        } else {
            String fileString = commitFileString;
            File changedFile = Utils.join(CWD, fileName); //Creates reference to file in CWD
            Utils.writeContents(changedFile, fileString); //Writes string from the commit blob to the file
        }
    }

    public void runBranch(String branchName){
        if(branches.containsKey(branchName)){
            System.out.println("A branch with that name already exists.");
        } else {
            branch(branchName);
        }
    }

    public void branch(String branchName){
        branches.put(branchName, headHash); //adds new branch to map of branches and commits

    }

    public void removeBranch(String branchName){
        if(!branches.containsKey(branchName)){
            System.out.println("A branch with that name does not exist.");
        } else if(branchName.equals(currBranch)){
            System.out.println("Cannot remove the current branch.");
        } else {
            branches.remove(branchName);
        }
    }

    public void reset(String commitID){
        String branchHolder = "" + currBranch;
        if(!Utils.plainFilenamesIn(COMMIT_DIR).contains(commitID)){
            System.out.println("No commit with that id exists.");
        } else {
            branches.put("tempBranch", commitID);
            checkoutBranch("tempBranch");
            branches.remove("tempBranch");
            currBranch = branchHolder;
            branch(currBranch);
        }
        /**
        File commitFile = Utils.join(COMMIT_DIR, commitID);
        Commit tempCommit = readObject(commitFile, Commit.class);
        String[] commitBlobs = (String[]) tempCommit.getBlobsSet().toArray();
        for (String blob : commitBlobs){
            checkoutCommit(commitID, blob);
        }
        branches.put(currBranch, commitID); */
    }
}

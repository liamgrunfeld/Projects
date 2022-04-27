package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Aryan Amberkar and Liam Grunfeld

 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** The time of this Commit. */
    //private Date timeOfCommit;
    private String timeOfCommit;
    /** The hashcode of the commit */
    private String hashcode;
    /** A log object containing the log message */
    //private String log = new String();
    //private Log log = new Log();
    /** A mapping of text file names to blob objects */
    //private Map<String, Blob> blobs = new BlobMap<>();
    /** The parent commit of the commit */
    //private Commit parent;
    private String parentHash;
    /** A staging area object of the blob references that this commit will hold */
    private StagingArea blobs = new StagingArea();



    /* TODO: fill in the rest of this class. */

    public Commit(String message1, Date timeOfCommit1, String parentHash, StagingArea stagingArea) {
        message = message1;
        String timeString = setUpDate(timeOfCommit1);
        timeOfCommit = timeString;
        setParentHash(parentHash);
        importParentStaged();
        setBlobs(stagingArea);
    }

    /**
    public String getHash (Commit parent){
        String hash = Utils.sha1(Utils.serialize(parent));
        return hash;
    } */

    //Creates staging area object of references to blobs
    public void setBlobs(StagingArea stagingArea){
        Iterator stagingAddIterator = stagingArea.getNameToFileMap().entrySet().iterator();
        Iterator stagingRmIterator = stagingArea.getFilesToRemove().entrySet().iterator();
        while(stagingAddIterator.hasNext()){
            Map.Entry currElement = (Map.Entry) stagingAddIterator.next();
            File updatedFile = (File) currElement.getValue();
            blobs.addFile(updatedFile);
        }
        while(stagingRmIterator.hasNext()){
            Map.Entry currElement = (Map.Entry) stagingRmIterator.next();
            File fileToRemove = (File) currElement.getValue();
            blobs.removeFile(fileToRemove);
        }
        //StagingArea currStaging = stagingArea; //directly pass through stagArea object instead of reading from file?
        //blobs = currStaging;
    }

    public void importParentStaged(){ //problem is initial commit isn't adding anything to its blobs, so its null
        if (parentHash != null) {
            Commit parentCommit = getParentCommit();
            if (parentCommit.getBlobs() != null) {
            Iterator parentBlobsIterator = parentCommit.getBlobsIterNameToFile();
            while (parentBlobsIterator.hasNext()) {
                Map.Entry parentEntry = (Map.Entry) parentBlobsIterator.next();
                File fileToAdd = (File) parentEntry.getValue();
                blobs.addFile(fileToAdd);
            }
        }
        }
    }

    public File getParentFile(){
        return Utils.join(Repository.COMMIT_DIR, parentHash);
    }

    public Commit getParentCommit(){
        return Utils.readObject(getParentFile(), Commit.class);
    }

    public String setUpDate(Date commitDate){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z");
        String formattedDate = dateFormatter.format(commitDate);
        return formattedDate;
    }

    public void setParentHash(String parentHash){
        this.parentHash = parentHash;
    }

    public String getParentHash(){
        return parentHash;
    }

    public String getTime(){
        return timeOfCommit;
    }

    public String getMessage(){
        return message;
    }

    public boolean parentIsNull(){ //remember to check that parent is null for initial commit
        return (parentHash == null);
    }

    public File getFile(String fileName){
        File gotFile = blobs.getFileFromName(fileName);
        return gotFile;
    }

    public String getFileString(String fileName){
        String gotFileString = blobs.getStringFromName(fileName);
        return gotFileString;
    }

    public StagingArea getBlobs(){
        return blobs;
    }


    public Iterator getBlobsIterNameToString(){
        Iterator newIterator = blobs.getMapList(blobs.getNameToStringMap());
        return newIterator;
    }

    public Iterator getBlobsIterNameToFile(){
        Iterator newIterator = blobs.getMapList(blobs.getNameToFileMap());
        return newIterator;
    }

    public Set getBlobsSet(){
        return blobs.getKeySet(blobs.getNameToFileMap());
    }



}

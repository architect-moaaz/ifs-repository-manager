package io.intelliflow.helper;

import io.intelliflow.repomanager.model.FileInformation;

import java.util.HashMap;
import java.util.Map;

public class FileLocker {

    private static Map<String, Map<String, String>> lockerMap = new HashMap<String, Map<String, String>>();

    /*
        Add file to lockerMap locking the file
     */
    public static String lockFile(FileInformation fileInformation){
        if(searchForLock(fileInformation) == null) {
            if(lockerMap.containsKey(fileInformation.getWorkspaceName())) {
                Map<String, String> newMap = lockerMap.get(fileInformation.getWorkspaceName());
                //TODO: Change file id to user id/name or personindent
                newMap.put(innerKeyName(fileInformation), fileInformation.getUserId());
            } else {
                Map<String, String> newMap = new HashMap<String, String>();
                //TODO: Change file id to user id/name or personindent
                newMap.put(innerKeyName(fileInformation), fileInformation.getUserId());
                lockerMap.put(fileInformation.getWorkspaceName(), newMap);
            }
            return searchForLock(fileInformation);
        }
        return null;
    }

    /*
        Remove the file from lockerMap
     */
    public static String removeLockFile(FileInformation fileInformation) {
        String user = searchForLock(fileInformation);
        if(user != null && user.equals(fileInformation.getUserId())) {
            Map<String, String> newMap = lockerMap.get(fileInformation.getWorkspaceName());
            newMap.remove(innerKeyName(fileInformation));
            lockerMap.put(fileInformation.getWorkspaceName(), newMap);
            return "File is unlocked";
        } else if(user == null) {
            return "File is not locked";
        } else {
            return "Not Authorised to Unlock!";
        }
    }

    /*
        Search if the file lock exists for specific file in the map
        Returns the user's name in case exists else returns null
     */
    public static String searchForLock(FileInformation fileInformation) {
        if(lockerMap.containsKey(fileInformation.getWorkspaceName())) {
            Map<String, String> tempInnerMap = lockerMap.get(fileInformation.getWorkspaceName());
            if(tempInnerMap.containsKey(innerKeyName(fileInformation))) {
                return tempInnerMap.get(innerKeyName(fileInformation));
            }
        }
        return null;
    }

    /*
        Forms the inner hashmap key string with the values
     */
    public static String innerKeyName(FileInformation fileInformation) {
        return fileInformation.getMiniApp() + "_" + fileInformation.getFileName() + "_" + fileInformation.getFileType();
    }
}

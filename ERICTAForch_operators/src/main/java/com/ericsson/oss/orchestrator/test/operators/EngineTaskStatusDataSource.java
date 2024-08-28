package com.ericsson.oss.orchestrator.test.operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.taf.annotations.DataSource;

/**
 * Java class to hold the data-source that represents a run of the orchestrator engine. Contains the results of
 * the master status file such that it can serve as the source for a Data-Driven test to treat each task within
 * the orchestrator engine workflow as a separate test
 * @author xmcgama
 *
 */
public class EngineTaskStatusDataSource {
    
    private static List<Map<String,Object>> myList = new ArrayList<Map<String,Object>>();

    /**
     * Creats a new record for this taskId with the status of its run
     * @param taskId
     * @param status
     * @param duration
     * @param estimated
     */
    public static void addRecord(String taskId, String status, String duration, String estimated) {
        Map<String, Object> rec = new HashMap<String, Object>();
        rec.put(WfEngOperator.TASK_ID, taskId);
        rec.put(WfEngOperator.STATUS, status);
        rec.put(WfEngOperator.DURATION, duration);
        rec.put(WfEngOperator.ESTIMATED_DUR, estimated);
        rec.put(WfEngOperator.ERROR_MSGS, "");
        myList.add(rec);
    }
    
    /**
     * Appends this errorMsg to the errorMessages associated with this taskId
     * @param taskId
     * @param errorMsg
     */
    public static void addErrorMsg(String taskId, String errorMsg) {
        for (Map<String,Object> map : myList) {
            if (map.get(WfEngOperator.TASK_ID).equals(taskId)) {
                map.put(WfEngOperator.ERROR_MSGS, map.get(WfEngOperator.ERROR_MSGS) + "\n" + errorMsg);
                break;
            }
        }
    }
    
    /**
     * Empties the records, used at beginning of an engine run so that only have records for latest run within data-source.
     * This allows the re-using of classes between engine runs.
     */
    public static void empty() {
        myList.clear();
    }
    
    // Result should implement java.lang.Iterable<Map<String,Object>>
    @DataSource
    public static List<Map<String,Object>> dataSource() {
        
        //return myList
        return myList;
    }
}


package it.freedomotic.core;

/**
 *
 * @author Enrico
 */
public class SchedulingData {
    private long creation;
    private long start;
    private long end;
    private StringBuilder log = new StringBuilder();

    SchedulingData(long creation) {
        this.creation = creation;
    }

    /**
     * @return the creation
     */
    public long getCreation() {
        return creation;
    }

    /**
     * @param creation the creation to set
     */
    public void setCreation(long creation) {
        this.creation = creation;
    }

    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public long getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(long end) {
        this.end = end;
    }

    /**
     * @return the log
     */
    public StringBuilder getLog() {
        return log;
    }
}

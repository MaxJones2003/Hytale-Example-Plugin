package com.getfriedpig.golem.bytecode;

/**
 * Result of bytecode execution
 */
public class VMResult {
    public enum Status {
        RUNNING,      // Still executing
        COMPLETED,    // Finished successfully
        PAUSED,       // Hit a checkpoint, paused
        ERROR         // Error occurred
    }

    public Status status;
    public Object result;
    public String errorMessage;
    public String checkpointId;
    public ExecutionState state;

    public VMResult(Status status) {
        this.status = status;
    }

    public VMResult(Status status, Object result) {
        this.status = status;
        this.result = result;
    }

    public VMResult(Status status, ExecutionState state, String checkpointId) {
        this.status = status;
        this.state = state;
        this.checkpointId = checkpointId;
    }

    public static VMResult running() {
        return new VMResult(Status.RUNNING);
    }

    public static VMResult completed(Object result) {
        return new VMResult(Status.COMPLETED, result);
    }

    public static VMResult paused(ExecutionState state, String checkpointId) {
        return new VMResult(Status.PAUSED, state, checkpointId);
    }

    public static VMResult error(String message) {
        VMResult result = new VMResult(Status.ERROR);
        result.errorMessage = message;
        return result;
    }

    @Override
    public String toString() {
        return "VMResult{" +
                "status=" + status +
                (result != null ? ", result=" + result : "") +
                (errorMessage != null ? ", error='" + errorMessage + '\'' : "") +
                (checkpointId != null ? ", checkpoint='" + checkpointId + '\'' : "") +
                '}';
    }
}

package org.activiti.starters.test;

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.core.model.Task;

public class MockTaskEvent extends MockProcessEngineEvent {

    private Task task;

    public MockTaskEvent(Long timestamp, String eventType) {
        super(timestamp,
              eventType);
    }

    public static ProcessEngineEvent aTaskCreatedEvent(long timestamp,
                                                       Task task) {
        MockTaskEvent taskCreatedEvent = new MockTaskEvent(timestamp,
                                                           "TaskCreatedEvent");
        taskCreatedEvent.setTask(task);
        return taskCreatedEvent;
    }

    public static ProcessEngineEvent aTaskAssignedEvent(long timestamp,
                                                       Task task) {
        MockTaskEvent taskCreatedEvent = new MockTaskEvent(timestamp,
                                                           "TaskAssignedEvent");
        taskCreatedEvent.setTask(task);
        return taskCreatedEvent;
    }

    public static ProcessEngineEvent aTaskCompletedEvent(long timestamp,
                                                       Task task) {
        MockTaskEvent taskCreatedEvent = new MockTaskEvent(timestamp,
                                                           "TaskCompletedEvent");
        taskCreatedEvent.setTask(task);
        return taskCreatedEvent;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }
}

package com.compdog.util;

import java.util.HashMap;
import java.util.Map;

public class Task {
    private final Thread thread;
    private final Runnable function;
    private boolean running = false;

    public static Task currentTask(){
        return _taskList.get(Thread.currentThread().getId());
    }

    public Task(Runnable func) {
        this.function = func;
        this.thread = new Thread(() -> {
            try {
                this.function.run();
            } finally {
                running = false;
                _deinitializeTask(this);
            }
        });
        _initializeTask(this);
    }

    private static final Map<Long, Task> _taskList = new HashMap<>();
    private static void _initializeTask(Task task){
        _taskList.put(task.thread.getId(), task);
    }
    private static void _deinitializeTask(Task task){
        _taskList.remove(task.thread.getId());
    }

    public void Start(){
        running = true;
        this.thread.start();
    }

    public void TryStop(){
        this.thread.interrupt();
    }

    public static Task Start(Runnable func){
        Task t = new Task(func);
        t.Start();
        return t;
    }

    public boolean isRunning() {
        return running;
    }
}

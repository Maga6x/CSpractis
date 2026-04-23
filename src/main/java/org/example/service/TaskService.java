package org.example.service;

import org.example.entity.Task;
import org.example.entity.TaskStatus;
import org.example.repository.TaskRepository;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class TaskService {

    private final TaskRepository taskRepository;
    private final BlockingQueue<UUID> queue;

    public TaskService(TaskRepository taskRepository, BlockingQueue<UUID> queue) {
        this.taskRepository = taskRepository;
        this.queue          = queue;
    }

    public UUID createTask(String data, String type) {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setInputData(data);
        task.setInputType(type.toUpperCase());
        task.setStatus(TaskStatus.NEW);

        try {
            taskRepository.save(task);
            queue.put(task.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed", e);
        }

        return task.getId();
    }

    public Task getTaskById(UUID id) throws Exception {
        return taskRepository.findById(id);
    }
}

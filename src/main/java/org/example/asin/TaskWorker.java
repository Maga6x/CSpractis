package org.example.asin;

import org.example.entity.Task;
import org.example.entity.TaskStatus;
import org.example.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;


public class TaskWorker implements Runnable {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final XmlMapper    XML_MAPPER  = new XmlMapper();

    private final BlockingQueue<UUID> queue;
    private final TaskRepository      taskRepository;

    public TaskWorker(BlockingQueue<UUID> queue, TaskRepository taskRepository) {
        this.queue          = queue;
        this.taskRepository = taskRepository;
    }

    @Override
    public void run() {
        while (true) {
            try {
                UUID taskId = queue.take();
                process(taskId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Worker Interrupted, shutting down");
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void process(UUID taskId) {
        try {
            Task task = taskRepository.findById(taskId);
            if (task == null) {
                System.err.printf("Worker Task %s not found in DB%n", taskId);
                return;
            }

            taskRepository.updateStatus(taskId, TaskStatus.PROCESSING.name());
            System.out.printf("Worker task %s  type=%s%n", taskId, task.getInputType());

            String result = "JSON".equalsIgnoreCase(task.getInputType())
                    ? jsonToXml(task.getInputData())
                    : xmlToJson(task.getInputData());

            taskRepository.updateResult(taskId, result, TaskStatus.DONE.name());
            System.out.printf("[Worker] Done       task %s%n", taskId);

        } catch (Exception e) {
            System.err.printf("[Worker] Error on task %s: %s%n", taskId, e.getMessage());
            try {
                taskRepository.updateStatus(taskId, TaskStatus.ERROR.name());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String jsonToXml(String json) throws Exception {
        JsonNode node = JSON_MAPPER.readTree(json);
        return XML_MAPPER.writeValueAsString(node);
    }

    private static String xmlToJson(String xml) throws Exception {
        JsonNode node = XML_MAPPER.readTree(xml.getBytes());
        return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }
}

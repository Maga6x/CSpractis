package org.example.asin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.entity.Task;
import org.example.service.TaskService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Map;
import java.util.UUID;


public class TaskHandler implements HttpHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final TaskService taskService;

    public TaskHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();   // e.g. "/tasks" or "/tasks/uuid"

        try {
            if ("POST".equalsIgnoreCase(method) && path.matches("/tasks/?")) {
                handleCreate(exchange);
            } else if ("GET".equalsIgnoreCase(method) && path.matches("/tasks/[^/]+")) {
                handleGet(exchange, path);
            } else {
                sendJson(exchange, 404, Map.of("error", "Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", e.getMessage()));
        }
    }

    private void handleCreate(HttpExchange exchange) throws Exception {
        InputStream body = exchange.getRequestBody();
        Map<?, ?> req = MAPPER.readValue(body, Map.class);

        String type = (String) req.get("type");
        String data = (String) req.get("data");

        if (type == null || data == null) {
            sendJson(exchange, 400, Map.of("error", "Fields 'type' and 'data' are required"));
            return;
        }
        if (!type.equalsIgnoreCase("JSON") && !type.equalsIgnoreCase("XML")) {
            sendJson(exchange, 400, Map.of("error", "'type' must be JSON or XML"));
            return;
        }

        UUID id = taskService.createTask(data, type);
        sendJson(exchange, 202, Map.of(
                "id",      id.toString(),
                "status",  "NEW",
                "message", "Task accepted. Poll GET /tasks/" + id + " for result."
        ));
    }

    private void handleGet(HttpExchange exchange, String path) throws Exception {
        String uuidStr = path.substring(path.lastIndexOf('/') + 1);

        UUID id;
        try {
            id = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, Map.of("error", "Invalid UUID: " + uuidStr));
            return;
        }

        Task task = taskService.getTaskById(id);
        if (task == null) {
            sendJson(exchange, 404, Map.of("error", "Task not found: " + id));
            return;
        }

        sendJson(exchange, 200, Map.of(
                "id",         task.getId().toString(),
                "inputType",  task.getInputType(),
                "status",     task.getStatus().name(),
                "outputData", task.getOutputData() != null ? task.getOutputData() : ""
        ));
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.asin.TaskHandler;
import org.example.asin.TaskWorker;
import org.example.repository.TaskRepository;
import org.example.service.SchemaInitializer;
import org.example.service.TaskService;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args)throws Exception {

        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "postgres";

        Connection connection = DriverManager.getConnection(url, user, password);

        SchemaInitializer.init(connection);

        BlockingQueue<UUID> queue = new LinkedBlockingQueue<UUID>();

        TaskRepository taskRepository = new TaskRepository(connection);
        TaskService taskService = new TaskService(taskRepository, queue);

        int workersCount = 4;
        for (int i = 0; i < workersCount; i++) {
            Thread worker = new Thread(new TaskWorker(queue, taskRepository));
            worker.setDaemon(true);
            worker.start();
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", new TaskHandler(taskService));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("Сервер запущен 8080");
    }

}
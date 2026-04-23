package org.example.repository;

import org.example.entity.Task;
import org.example.entity.TaskStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class TaskRepository {

    private final Connection connection;

    public TaskRepository(Connection connection) {
        this.connection = connection;
    }

    public void save(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (id, input_data, input_type, output_data, status)" +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, task.getId());
            ps.setString(2, task.getInputData());
            ps.setString(3, task.getInputType());
            ps.setString(4, task.getOutputData());
            ps.setString(5, task.getStatus().name());
            ps.executeUpdate();
        }
    }

    public Task findById(UUID id) throws SQLException {

        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Task task = new Task();
                task.setId(id);
                task.setInputData(rs.getString("input_data"));
                task.setInputType(rs.getString("input_type"));
                task.setOutputData(rs.getString("output_data"));
                task.setStatus(TaskStatus.valueOf(rs.getString("status")));
                return task;
            }
        }
        return null;
    }

    public void updateStatus(UUID id, String status) throws SQLException {

        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, id);
            ps.executeUpdate();
        }
    }

    public void updateResult(UUID id, String result, String status) throws SQLException {

        String sql = "UPDATE tasks SET output_data = ?, status = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, result);
            ps.setString(2, status);
            ps.setObject(3, id);
            ps.executeUpdate();
        }
    }
}

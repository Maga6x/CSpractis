package org.example.service;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaInitializer {

    public static void init(Connection connection) throws Exception {

        String sql = "CREATE TABLE IF NOT EXISTS tasks (id UUID PRIMARY KEY," +
                "input_data TEXT NOT NULL, input_type VARCHAR(10) NOT NULL," +
                "output_data TEXT, status VARCHAR(20) NOT NULL)";

        Statement st = connection.createStatement();
        st.execute(sql);
        st.close();
    }
}

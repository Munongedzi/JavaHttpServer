package zw.co.simpleserver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sun.misc.IOUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.fasterxml.jackson.databind.ObjectMapper.*;

public class MyHttpHandler implements HttpHandler {
    static final Logger logger = Logger.getLogger(String.valueOf(MyHttpHandler.class));
    String url = "jdbc:mysql://localhost:3306/crud_application";
    String userName = "root";
    String psw = "Muno@123";

    public void handle(HttpExchange httpExchange) throws IOException {
        String requestParamValue = null;

        if ("GET".equals(httpExchange.getRequestMethod())) {


            requestParamValue = handleGetRequest(httpExchange);


        } else if ("POST".equals(httpExchange.getRequestMethod())) {

            requestParamValue = handlePostRequest(httpExchange);

        }

        handleResponse(httpExchange, requestParamValue);

    }


    private String handlePostRequest(HttpExchange httpExchange) throws IOException {
        InputStream inputStream = httpExchange.getRequestBody();
        byte[] result = IOUtils.readAllBytes(inputStream);
        ObjectMapper mapper = new ObjectMapper();
        String json = new String(result, StandardCharsets.UTF_8);
        User user = mapper.readValue(json, User.class);
        EntityManagerFactory entitymanagerfactory = Persistence.createEntityManagerFactory("User");

        EntityManager entitymanager = entitymanagerfactory.createEntityManager();
        entitymanager.getTransaction().begin();


        entitymanager.persist(user);
        entitymanager.getTransaction().commit();

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, userName, psw);
            Statement preparedStmt = con.createStatement();

            String sql = "SELECT * FROM User where email ='" + user.getEmail() + "';";
            ResultSet rs = preparedStmt.executeQuery(sql);
            rs.next();
            user.setUserid(rs.getLong("userid"));
            con.close();
            String userString = mapper.writeValueAsString(user);
            return userString;
        } catch (Exception e) {
            logger.severe("got exception");

        }
        entitymanager.close();
        entitymanagerfactory.close();
        logger.info(user.getAddress());
//

        return "{" + "error\":\"error adding user\"\n" + "}";

    }

    private Map<String, Object> readValue(String json, TypeReference<Map<String, Object>
            > mapTypeReference) {
        return null;
    }


    private String handleGetRequest(HttpExchange httpExchange) throws IOException {
//get users from db
        List<User> users = new ArrayList<>();
        try {


            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, userName, psw);

            String selectSql = "SELECT * from user";
            Statement st = con.createStatement();
            ResultSet resultSet = st.executeQuery(selectSql);

            while (resultSet.next()) {
                User user = new User();

                user.setUsername(resultSet.getString("Username"));
                user.setFirstname(resultSet.getString("Firstname"));
                user.setSurname(resultSet.getString("Surname"));
                user.setEmail(resultSet.getString("Email"));
                user.setDateOfBirth(resultSet.getString("DateOfBirth"));
                user.setPhoneNumber(resultSet.getString("PhoneNumber"));
                user.setAddress(resultSet.getString("Address"));
                user.setPassword(resultSet.getString("Password"));
                user.setUserid(resultSet.getLong("UserId"));
                users.add(user);

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception");
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(out, users);

        final byte[] data = out.toByteArray();
        String result = new String(data);
        return result;
    }

    private void handleResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append(requestParamValue);
        String htmlResponse = htmlBuilder.toString();
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.getResponseHeaders().add("Status", "200");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST");
        httpExchange.sendResponseHeaders(200, htmlResponse.length());

        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();

    }

}

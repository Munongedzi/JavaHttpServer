package zw.co.simpleserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.logging.Logger;

public class MyHttpHandler implements HttpHandler {
    static final Logger logger = Logger.getLogger(String.valueOf(MyHttpHandler.class));

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
//


        String requestParamValue = null;

        if("GET".equals(httpExchange.getRequestMethod())) {

            try {
                requestParamValue = handleGetRequest(httpExchange);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

        }else if("POST".equals(httpExchange.getRequestMethod())) {

            requestParamValue = handlePostRequest(httpExchange);

        }

        handleResponse(httpExchange,requestParamValue);

    }



    private String handlePostRequest(HttpExchange httpExchange) throws IOException {
        InputStream inputStream= httpExchange.getRequestBody();
        String json = "{\"username\":\"firstname\":\"surname\":\"email\":\"dateOfBirth\":\"Phone number\":\"Address\":\"password\"}";
        user user = objectMapper.readValue(json,user.class);
        byte[] result = IOUtils.readAllBytes(inputStream);
        String s = new String(result, StandardCharsets.UTF_8);
        System.out.println(s);
        return "this is post request";

    }
    private String handleGetRequest(HttpExchange httpExchange) throws SQLException, IOException {
//get users from db
        try {
            String url = "jdbc:mysql://localhost:3306/crud_application";
            String userName = "root";
            String psw = "Muno@123";
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, userName, psw);

            String selectSql = "SELECT * from user";
            Statement st = con.createStatement();
            ResultSet	resultSet = st.executeQuery(selectSql);

            while (resultSet.next()) {
                logger.info(resultSet.getString(1) + " " + resultSet.getString(2) + " " + resultSet.getString(3)
                        + " " + resultSet.getString(4) + " " + resultSet.getString(5) + " "
                        + resultSet.getString(6) + " " + resultSet.getString(7) + " " + resultSet.getString(8));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.severe("Exception");
        }
        return "this is get request";
    }

    private void handleResponse(HttpExchange httpExchange,  String requestParamValue)  throws  IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html>")
                .append("<body>")
                .append("<h1>")
                .append("Hello ")
                .append(requestParamValue)
                .append("</h1>")
                .append("</body>");
        String htmlResponse = htmlBuilder.toString();
        httpExchange.sendResponseHeaders(200, 10000L);
        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();

    }

}

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title><%= request.getAttribute("titre") %></title>
</head>
<body>
    <h2><%= request.getAttribute("titre") %></h2>
    <ul>
        <li>Nom : <%= request.getAttribute("titre") %></li>
        <li>Valeur : <%= request.getAttribute("employes") %></li>
        <li>Anarana : <%= request.getAttribute("anarana") %></li>
    </ul>
</body>
</html
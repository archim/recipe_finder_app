<%--
  Created by IntelliJ IDEA.
  User: rajan_000
  Date: 11-Mar-15
  Time: 9:50 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Find Recipe</title>
</head>
<body>

<c:if test="${!empty statusMsg}">
    <c:forEach var="status" items="${statusMsg.messages}">
        <span style="color: red"><c:out value="${status}"/></span> <br/>
    </c:forEach>
</c:if>
<c:choose>
    <c:when test="${!empty result}">
        <c:out value="${result}"/></span>
    </c:when>
    <c:otherwise>
        <form action="FindRecipeServlet" method="POST" enctype="multipart/form-data">
            <span><h3>Enter available items (in CSV format) :</h3>
            <input type="file" name="availableItemsFile"/></span> <br/><br/>

            <span> <h3>Enter recipes (in TXT format) :</h3>
            <input type="file" name="recipesFile"/></span><br/><br/><br/>

            <input type="submit" value="Find Recipe"/>
        </form>
    </c:otherwise>
</c:choose>
</body>
</html>

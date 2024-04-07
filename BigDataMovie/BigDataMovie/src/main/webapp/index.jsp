<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Search Movie</title>
</head>
<body>
<h2>Search Movie</h2>
<form action="/search" method="GET">
    <label for="movieId">Movie ID:</label>
    <input type="text" id="movieId" name="movieId">
    <button type="submit">Search</button>
</form>
<br>
<c:if test="${not empty movie}">
    <p><strong>Movie ID:</strong> ${movie.movieId}</p>
    <p><strong>Title:</strong> ${movie.title}</p>
    <p><strong>Genres:</strong> ${movie.genres}</p>
</c:if>
<c:if test="${not empty message}">
    <p>${message}</p>
</c:if>
</body>
</html>

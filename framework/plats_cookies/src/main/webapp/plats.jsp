<%@ page import="java.util.List" %>
<%@ page import="model.Plat" %>
<%
    List<Plat> plats = (List<Plat>) request.getAttribute("plats");
%>

<h2>Liste des plats</h2>

<div id="plats">
    <%
    for (Plat p : plats) {
    %>
        <div class="plat">
            <h3><%= p.getName() %> - <%= p.getPrix() %> €</h3>
            <img src="<%= p.getImage() %>" width="150"><br>
            <button class="like-btn" data-name="<%= p.getName() %>">👍 Like</button>
        </div>
    <%
    }
    %>
</div>

<script>
// Charger les likes depuis les cookies
document.addEventListener("DOMContentLoaded", () => {
    const cookies = document.cookie.split(";").reduce((acc, c) => {
        const [k, v] = c.trim().split("=");
        acc[k] = v;
        return acc;
    }, {});

    document.querySelectorAll(".like-btn").forEach(btn => {
        const name = btn.dataset.name;
        if (cookies[name] === "liked") btn.style.backgroundColor = "lightgreen";

        btn.addEventListener("click", () => {
            if (cookies[name] !== "liked") {
                document.cookie = `${name}=liked; path=/; max-age=31536000`;
                btn.style.backgroundColor = "lightgreen";
            }
        });
    });
});
</script>

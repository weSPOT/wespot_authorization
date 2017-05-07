<%@ page import="net.wespot.oauth2.provider.AccountService" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="net.wespot.utils.Utils" %>
<%@ page import="net.wespot.utils.DbUtils" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <title>Create account</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0">

    <link rel="stylesheet" href="css/Login.css">
    <link rel="stylesheet" href="css/Account.css">
</head>
<body>

<figure id="background"></figure>

<%
    String username = request.getParameter("username");
    String firstname = request.getParameter("firstname");
    String lastname = request.getParameter("lastname");
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    String passwordagain = request.getParameter("passwordagain");

    List<String> incorrectFields = new ArrayList<String>();

    if (username != null && "".equals(username.trim())) incorrectFields.add("username");
    if (firstname != null && "".equals(firstname.trim())) incorrectFields.add("firstname");
    if (lastname != null && "".equals(lastname.trim())) incorrectFields.add("lastname");
    if (email != null && "".equals(email.trim())) incorrectFields.add("email");
    if (password != null && "".equals(password.trim())) incorrectFields.add("password");
    if (passwordagain != null && "".equals(passwordagain.trim())) incorrectFields.add("passwordagain");

    boolean nameExists = false;
    if (username != null && !"".equals(username.trim()) && DbUtils.getAccount(username) != null) {
        nameExists = true;
    }

    if (email != null) {
        if (!Utils.isValidEmail(email) && !incorrectFields.contains("email")) {
            incorrectFields.add("email");
        }
    }

    boolean passwordsDoNotMatch = false;
    if (password != null && passwordagain != null) {
        passwordsDoNotMatch = !password.equals(passwordagain);
    }

    // Also show form when there was no input (when all the fields were null)
    boolean showForm = false;
    if (username == null && email == null && password == null && passwordagain == null && firstname == null && lastname == null) {
        showForm = true;
    }

    if (incorrectFields.size() > 0 || passwordsDoNotMatch || nameExists || showForm) {
%>

<section id="content" data-role="content">
    <h1 class="title">weSPOT account registration</h1>
    <form action="Account.jsp" id="check-user" method="post">
        <fieldset>
            <input id="username" name="username" placeholder="Username" type="text" value="<%= (username != null) ? username : "" %>">
            <%
                if (nameExists) {
            %>
            <label id="incorrectUsername" for="username" class="error">Username taken, choose another username</label>
            <%
                }

                if (incorrectFields.contains("username")) {
            %>
            <label id="incorrectUsername2" for="username" class="error">Provide a non empty value here</label>
            <%
                }
            %>
        </fieldset>
        <fieldset>
            <input id="firstname" name="firstname" placeholder="First name" type="text" value="<%= (firstname != null) ? firstname : "" %>">
            <%
                if (incorrectFields.contains("firstname")) {
            %>
            <label id="incorrectFirstName" for="firstname" class="error">Provide a non empty value here</label>
            <%
                }
            %>
        </fieldset>
        <fieldset>
            <input id="lastname" name="lastname" placeholder="Last name" type="text" value="<%= (lastname != null) ? lastname : "" %>">
            <%
                if (incorrectFields.contains("lastname")) {
            %>
            <label id="incorrectLastName" for="lastname" class="error">Provide a non empty value here</label>
            <%
                }
            %>
        </fieldset>
        <fieldset>
            <input id="email" name="email" placeholder="E-mail" type="text" value="<%= (email != null) ? email : "" %>">
            <%
                if (incorrectFields.contains("email")) {
            %>
            <label id="incorrectEmail" for="email" class="error">Incorrect E-mail</label>
            <%
                }
            %>
        </fieldset>
        <fieldset>
            <input id="password" name="password" placeholder="Password" type="password">
            <%
                if (passwordsDoNotMatch) {
            %>
            <label id="incorrectPassword" for="password" class="error">Passwords do not match</label>
            <%
                }

                if (incorrectFields.contains("password")) {
            %>
            <label id="incorrectPassword" for="password" class="error">Provide a non empty value here</label>
            <%
                }
            %>
        </fieldset>
        <fieldset>
            <input id="passwordagain" name="passwordagain" placeholder="Repeat password" type="password">
            <%
                if (passwordsDoNotMatch) {
            %>
            <label id="incorrectPasswordAgain" for="passwordagain" class="error">Passwords do not match</label>
            <%
                }

                if (incorrectFields.contains("passwordagain")) {
            %>
            <label id="incorrectPasswordAgain" for="passwordagain" class="error">Provide a non empty value here</label>
            <%
                }
            %>
        </fieldset>
        <input class="submit-btn" type="submit" name="login" value="Create account">
    </form>
</section>

<%
    } else {
        new AccountService().createAccountStatic(username, password, firstname, lastname, email);
%>

<section id="content" data-role="content">
    <h1 class="title">Your account has been created</h1>
</div>
<%
    }
%>
</body>
</html>

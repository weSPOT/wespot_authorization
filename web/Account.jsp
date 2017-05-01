<%@ page import="net.wespot.oauth2.provider.AccountService" %>
<%@ page import="net.wespot.oauth2.provider.EmailValidator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>Create account</title>
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0"/>
    <script src="jquery-1.9.1.min.js"></script>
    <link rel="stylesheet" href="Login.css">
</head>
<body>

<figure id="background"></figure>

<%
    List<String> fields = new ArrayList<String>();
    fields.add("username");
    fields.add("firstname");
    fields.add("lastname");
    fields.add("email");
    fields.add("password");
    fields.add("passwordagain");
    HashMap<String, String> requestFields = new HashMap<String, String>();
    for (int i = 0; i < fields.size(); i++) {
      requestFields.put(fields.get(i), request.getParameter(fields.get(i)));
    }
    List<String> incorrectFields = new ArrayList<String>();
    for (int i = 0; i < fields.size(); i++) {
      String currentField = requestFields.get(fields.get(i));
      if (currentField != null && "".equals(currentField.trim())) {
        incorrectFields.add(fields.get(i));
      }
    }
    // Fields that need custom verification
    String username = requestFields.get("username");
    boolean nameExists = false;
    if (username != null && !"".equals(username) && new AccountService().accountExists(username).contains("true")) {
        nameExists = true;
    }

    String email = requestFields.get("email");
    if (email != null) {
        if (!new EmailValidator().validate(email) && !incorrectFields.contains("email")) {
            incorrectFields.add("email");
        }
    }

    String password = requestFields.get("password");
    String passwordagain = requestFields.get("passwordagain");
    boolean passwordsDoNotMatch = false;
    if (password != null && passwordagain != null) {
        passwordsDoNotMatch = !password.equals(passwordagain);
    }

    String firstname = requestFields.get("firstname");
    String lastname = requestFields.get("lastname");

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

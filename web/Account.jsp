<%@ page import="net.wespot.oauth2.provider.AccountService" %>
<%@ page import="net.wespot.oauth2.provider.EmailValidator" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>Create account</title>
    <meta name="viewport" content="width=device-width, height=device-height, initial-scale=1.0"/>
    <link rel="stylesheet" href="jquery.mobile-1.4.0.min.css" />
    <script src="jquery-1.9.1.min.js"></script>
    <script src="jquery.mobile-1.4.0.min.js"></script>
    <style type="text/css">
        #check-user
        {
            background: #fff;
            opacity:0.9;
            font-size: 12px;
            line-height: 25px;
        }

        #check-user { width: 90%; }
        label.error {
            float: left;
            color: red;
            width: 90%;
            vertical-align: top;
            font-weight:bold
        }
    </style>
</head>
<body>
<%
    String username = request.getParameter("username");
    String firstname = request.getParameter("firstname");
    String lastname = request.getParameter("lastname");
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    String passwordagain = request.getParameter("passwordagain");
    boolean input = false;
    boolean nameExists =false;
    boolean pageHasErros = true;
    if (username != null || firstname !=null ||lastname !=null ||email!=null) {
        pageHasErros= false;
        if (username !=null && !"".equals(username.trim())&& new AccountService().accountExists(username).contains("true")) {
            nameExists = true;
            pageHasErros = true;
        }

    }
    boolean incorrectUsername= false;
    if (username != null && "".equals(username.trim())) {
        incorrectUsername = true;
        pageHasErros = true;
    }

    boolean incorrectFirstName= false;
    if (firstname != null && "".equals(firstname.trim())) {
        incorrectFirstName = true;
        pageHasErros = true;
    }
    boolean incorrectLastName= false;
    if (lastname != null && "".equals(lastname.trim())) {
        incorrectLastName = true;
        pageHasErros = true;
    }
    boolean incorrectEmail= false;
    if (email != null) {
//        String emailreg = "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
        if (!new EmailValidator().validate(email)) {

            incorrectEmail = true;
            pageHasErros = true;
        }

    }
    boolean passwordsDoNotMatch = false;
    if (password !=null && passwordagain!=null) {
        passwordsDoNotMatch = !password.equals(passwordagain);
        if (passwordsDoNotMatch)pageHasErros = true;
    }

    boolean incorrectPassword= false;
    if (password != null && "".equals(password.trim())) {
        incorrectPassword =true;
        pageHasErros = true;
    }

    boolean incorrectPasswordAgain= false;
    if (passwordagain != null && "".equals(passwordagain.trim())) {
        incorrectPasswordAgain =true;
        pageHasErros = true;
    }

    if (pageHasErros) {
%>



<div data-role="page" id="first" class="wrapper" data-theme="b" style="background-size: cover;background-image: url('/images/login_background.jpg')">
    <div data-role="content"  id="content">
        <h1> weSPOT account registration</h1>
        <form action="Account.jsp" id="check-user" class="ui-body ui-body-a ui-corner-all" method="post">
            <fieldset>
                <label for="username">Choose a username:</label>
                <div data-role="fieldcontain">
                    <input type="text" value="<%=(username!=null)?username:""%>" name="username" id="username" placeholder="username"/>
                    <%
                        if (nameExists) {
                    %>
                    <label id="incorrectUsername" for="username" class="error">Name exists, choose another username</label>
                    <%
                        }
                    %>
                    <%
                        if (incorrectUsername) {
                    %>
                    <label id="incorrectUsername2" for="username" class="error">Provide a non empty value here</label>
                    <%
                        }
                    %>
                </div>

                <label for="firstname">First Name:</label>
                <div data-role="fieldcontain">
                    <input type="text" value="<%=(firstname!=null)?firstname:""%>" name="firstname" id="firstname" placeholder="firstname"/>
                    <%
                        if (incorrectFirstName) {
                    %>
                    <label id="incorrectFirstName" for="firstname" class="error">Provide a non empty value here</label>
                    <%
                        }
                    %>
                </div>



                <label for="lastname">Last Name:</label>
                <div data-role="fieldcontain">
                    <input type="text" value="<%=(lastname!=null)?lastname:""%>" name="lastname" id="lastname" placeholder="lastname"/>
                    <%
                        if (incorrectLastName) {
                    %>
                    <label id="incorrectLastName" for="lastname" class="error">Provide a non empty value here</label>
                    <%
                        }
                    %>
                </div>


                <label for="email">E-mail:</label>
                <div data-role="fieldcontain">
                    <input type="text" value="<%=(email!=null)?email:""%>" name="email" id="email" placeholder="E-mail"/>
                    <%
                        if (incorrectEmail) {
                    %>
                    <label id="incorrectEmail" for="email" class="error">Incorrect E-mail</label>
                    <%
                        }
                    %>
                </div>

                <label for="password">Password:</label>
                <div data-role="fieldcontain">
                    <input type="password" value="" name="password" id="password" placeholder="Password"/>
                    <%
                        if (passwordsDoNotMatch) {
                    %>
                    <label id="incorrectPassword" for="password" class="error">Passwords do not match.</label>
                    <%
                        }
                    %>
                    <%
                        if (incorrectPassword) {
                    %>
                    <label id="incorrectPassword" for="password" class="error">Provide a non empty value here</label>
                    <%
                        }
                    %>
                </div>

                <label for="email">Retype password:</label>
                <div data-role="fieldcontain">
                    <input type="password" value="" name="passwordagain" id="passwordagain" placeholder="Password"/>
                    <%
                        if (passwordsDoNotMatch) {
                    %>
                    <label id="incorrectPasswordAgain" for="passwordagain" class="error">Passwords do not match.</label>
                    <%
                        }
                    %>
                    <%
                        if (incorrectPasswordAgain) {
                    %>
                    <label id="incorrectPasswordAgain" for="passwordagain" class="error">Provide a non empty value here</label>
                    <%
                        }
                    %>
                </div>



                <input type="submit" data-theme="b" name="Login"  value="Create account">
            </fieldset>
        </form>
    </div>
</div>

<%
    } else {
        new AccountService().createAccountStatic(username, password, firstname, lastname, email);
%>

<div data-role="page" id="first" class="wrapper" data-theme="b" style="background-size: cover;background-image: url('/images/login_background.jpg')">
    <div data-role="content"  id="content">

        <h1>your account has been created</h1>
    </div>
</div>
<%
    }
%>
</body>
</html>
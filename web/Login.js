var $usernameIncorrect = $('#incorrect-username');
var $incorrectPassword = $('#incorrect-password');

if (window.location.href.indexOf('incorrectUsername') != -1) {
  $usernameIncorrect.show();
} else {
  $usernameIncorrect.hide();
}

if (window.location.href.indexOf('incorrectPassword') != -1) {
  $incorrectPassword.show();
} else {
  $incorrectPassword.hide();
}

$('#check-user').submit(function(e) {
    e.preventDefault();
    submitCredentials();
});

function submitCredentials() {
    var username = $('#username').val();
    var selectedSchool = $('#school-select option:selected').val();
    if (selectedSchool != 0){
        username = selectedSchool + '_' + username;
    }
    $.ajax({
        type: 'post',
        async: 'false',
        dataType: 'json',
        data: JSON.stringify(
                {
                    username: username,
                    password: $('#password').val()
                }
        ),
        contentType: 'application/json; charset=utf-8',
        url: 'https://rk02.net/fakewespotauth.php',
        success: function(data) {
            if (data.error) {
                if (data.error == 'username does not exist ') {
                    $usernameIncorrect.show();
                    $usernameIncorrect.siblings('input').addClass('error');
                    $incorrectPassword.hide();
                    $incorrectPassword.siblings('input').removeClass('error');
                } else if (data.error == 'password incorrect') {
                    $incorrectPassword.show();
                    $incorrectPassword.siblings('input').addClass('error');
                    $usernameIncorrect.hide();
                    $usernameIncorrect.siblings('input').removeClass('error');
                }
            } else {
                $usernameIncorrect.hide();
                $usernameIncorrect.siblings('input').removeClass('error');
                $incorrectPassword.hide();
                $incorrectPassword.siblings('input').removeClass('error');

                var params = getQueryParams();
                if (!params.redirect_uri) {
                    alert('Login successful');
                } else {
                    window.location.href = window.location.origin + '/oauth/auth?' +
                      'redirect_uri=' + params.redirect_uri +
                      '&client_id=' + params.client_id +
                      '&response_type=' + params.response_type +
                      '&scope=' + params.scope;
                }
            }
        },
        error: function(request, error) {
            alert('Network error has occurred please try again!');
        }
    });
}

function getQueryParams() {
    var qs = document.location.search;
    qs = qs.split('+').join(' ');

    var params = {};
    var tokens;
    var regex = /[?&]?([^=]+)=([^&]*)/g;

    while (tokens = regex.exec(qs)) {
        params[decodeURIComponent(tokens[1])]
                = decodeURIComponent(tokens[2]);
    }

    return params;
}

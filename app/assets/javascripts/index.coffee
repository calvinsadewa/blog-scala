$ ->
  $.get "/persons", (persons) ->
    $.each persons, (index, person) ->
      name = $("<div>").addClass("name").text person.name
      email = $("<div>").addClass("email").text person.email
      $("#persons").append $("<li>").append(name).append(email)
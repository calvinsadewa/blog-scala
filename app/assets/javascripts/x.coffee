$ ->
  $.get "/blogs", (blogs) ->
    $.each blogs, (index, blog) ->
      title = $("<div>").addClass("title").text blog.title
      content = $("<div>").addClass("content").text blog.content
      date = $("<div>").addClass("date").text blog.date
      delete_button = $("<form>")      $("#blogs").append $("<li>").append(title).append(content).append(date).append(delete_button)


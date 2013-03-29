$ ->
  $("#delete-post").click (e) ->
    postTitle = $(this).attr("data-post-title")
    $("#post-delete-form").submit() if confirm("Are you sure to delete post #{postTitle}?")


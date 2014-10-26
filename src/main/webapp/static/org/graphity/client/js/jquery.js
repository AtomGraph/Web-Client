$(document).ready(function()
{

    $(".btn-delete").on("click", function() // prompt on DELETE
    {        
        return confirm('Are you sure?');
    });

    $(".btn-remove").on("click", function()
    {        
        return $(this).parent().parent().remove();
    });

    $(".btn-add").on("click", function()
    {
        var clone = $(this).parent().parent().clone(true, true);
        var uuid = "uuid" + generateUUID();
        var input = clone.find("input[name='ou'],input[name='ob'],input[name='ol']");
        input.attr("id", uuid);
        input.val("");
        clone.find("label").attr("for", uuid);
        return $(this).parent().parent().after(clone);
    });

});
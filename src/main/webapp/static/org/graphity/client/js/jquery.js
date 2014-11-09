$(document).ready(function()
{

    $(".navbar-form").on("submit", function()
    {
        var uriOrLabel = $(this).find("input[name=uri]").val();
        if (uriOrLabel.indexOf("http://") === -1 && uriOrLabel.indexOf("https://") === -1)
        {
            $(this).attr("action", "resources/labelled");
            $(this).find("input[name=uri]").attr("name", "label");
        }
        
        return true;
    });
    
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
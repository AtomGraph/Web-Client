var onRemoveButtonClick = function()
{        
    return $(this).parent().parent().parent().remove();
};

var onDropdownClick = function()
{
    $(this).toggleClass("open");

    return true;
};
    
$(document).ready(function()
{

    $(".navbar-form").on("submit", function()
    {
        var labelInput = $(this).find("input[name=label]");
        if (labelInput.length) // check whether label input exists
        {
            var uriOrLabel = labelInput.val();
            if (uriOrLabel.indexOf("http://") === 0 || uriOrLabel.indexOf("https://") === 0)
            {
                $(this).attr("action", "");
                $(this).find("input[name=label]").attr("name", "uri");
            }
        }
        
        return true;
    });
    
    $(".btn-delete").on("click", function() // prompt on DELETE
    {        
        return confirm('Are you sure?');
    });

    $(".btn-remove").on("click", onRemoveButtonClick);

    $(".btn-group:has(.btn.dropdown-toggle)").on("click", onDropdownClick);

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
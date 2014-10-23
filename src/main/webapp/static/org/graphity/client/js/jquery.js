$(document).ready(function()
{

    $(".remove-statement").on("click", function()
    {        
        return $(this).parent().parent().remove();
    });
    
});
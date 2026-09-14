/* Browser glue for the stylesheets' markup: statement add/remove in edit forms,
   delete confirmation, and URI-form normalization. Dependency-free. */

document.addEventListener("submit", function(event)
{
    var form = event.target.closest(".uri-form");
    if (form === null) return;

    // a URI pasted into a label-typeahead field navigates directly instead of searching
    var labelInput = form.querySelector("input[name=label]");
    if (labelInput !== null && /^https?:\/\//.test(labelInput.value))
    {
        form.setAttribute("action", "");
        labelInput.setAttribute("name", "uri");
    }
});

document.addEventListener("click", function(event)
{
    var deleteBtn = event.target.closest(".btn-delete");
    if (deleteBtn !== null)
    {
        if (!confirm(deleteBtn.dataset.confirm || "Are you sure?")) event.preventDefault();
        return;
    }

    var removeBtn = event.target.closest(".btn-remove-property");
    if (removeBtn !== null)
    {
        removeBtn.closest(".statement").remove();
        return;
    }

    var addBtn = event.target.closest(".btn-add");
    if (addBtn !== null)
    {
        var statement = addBtn.closest(".statement");
        var clone = statement.cloneNode(true);
        var uuid = "uuid" + crypto.randomUUID();
        clone.querySelectorAll("input[name=ou], input[name=ob], input[name=ol]").forEach(function(input)
        {
            input.id = uuid;
            input.value = "";
        });
        var label = clone.querySelector("label");
        if (label !== null) label.setAttribute("for", uuid);
        statement.after(clone);
    }
});

$(window).on("load", function () {
    $("#mode_select").change(function (event) {
        if($("#mode_select").val()==="s"){
            let select = document.createElement("select");
            select.className = "form-control";
            select.id = "status_select";
            select.name = "search";
            let option1 = document.createElement("option");
            option1.defaultSelected = true;
            option1.value = "false";
            option1.text = "Неактивеный";
            let option2 = document.createElement("option");
            option2.value = "true";
            option2.text = "Активный";
            select.appendChild(option1)
            select.appendChild(option2)
            $("#query_input").replaceWith(select);
        }
        if( ($("#status_select").length>0) && ($("#mode_select").val() !== "s") ){
            let input = document.createElement("input")
            input.id = "query_input";
            input.type = "text";
            input.name = "search";
            input.className="form-control";
            input.required = "true";
            input.setAttribute("aria-lable", "Text input with segmented select and label");
            $("#status_select").replaceWith(input);
        }
    })
})
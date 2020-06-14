$('#inputGroupFile01').on("change", function() {
    if($("#inputGroupFile01").val()) {
        $("#clear01").addClass("red-clear").html("Очистить");
        let files = document.getElementById("inputGroupFile01").files;
        let filenames = [];

        if (files.length > 1) {
            filenames.push("Всего файлов (" + files.length + ")");
        } else {
            for (let i in files) {
                if (files.hasOwnProperty(i)) {
                    filenames.push(files[i].name);
                }
            }
        }
        $(this)
            .next(".custom-file-label")
            .html(filenames.join(","));
    }
});

$(document).on('click', '#clear01', function (event) {
    if($("#inputGroupFile01").val()) {
        $(this).removeClass("red-clear").html("Файлы");
        $("#inputGroupFile01").val("");
        $("#label01").html("При необходимости, прикрепите файлы");
    }
})

$(document).ready(function() {
    let timerId = setInterval(function () {


        if (!$("#inputGroupFile01").val()) {
            $("#clear01").removeClass("red-clear").html("Файлы");
            $("#label01").html("При необходимости, прикрепите файлы");
        }



    }, 500);
});
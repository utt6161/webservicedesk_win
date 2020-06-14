$(window).on("load", function () {
    $("#role").on("change", function () {
        let val = $("#role").val();
        let id = $("#user_id").val();
        let token = $("meta[name='_csrf']").attr("content");
        if (val == "2") {
            $.ajax({
                method:"POST",
                url:"/users/change/id/"+id,
                headers: {
                    "X-CSRF-TOKEN":token,
                },
                data:{
                    role:val,
                }
            })
        } else if (val == "3") {
            $.ajax({
                method:"POST",
                url:"/users/change/id/"+id,
                headers: {
                    "X-CSRF-TOKEN":token,
                },
                data:{
                    role:val,
                }
            })
        }
    })
});
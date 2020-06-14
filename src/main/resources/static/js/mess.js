'use strict';
let username;
let id;
let div_moved = false;
let token;
let getT;
let jr_me;
let r_me;
let your_last_message;
let rec_last_message;
let closed;
$(window).on("load", function () {
    let message_area = document.getElementById("message_area");

    let send_button = document.getElementById("send_button");
    let messageInput = document.getElementById("messageInput")
    let stompClient;
    stompClient = null;
    let stompNotfClient = null;
    username = document.getElementById("login").value;
    id = document.getElementById("requestid").value;
    token = $("meta[name='_csrf']").attr("content");
    if (!stompClient)
        console.log("calling the connection func");
        connect();

    getT = Cookies.get("token");
    function connect() {
        if (username) {
            let socket = new SockJS('https://localhost:8443/ws'); // window.location.host
            stompClient = Stomp.over(socket);
            stompClient.connect({
                "X-CSRF-TOKEN":token,
            }, onConnected, onError);
            stompClient.debug = true;
        }
    }

    function onError(error) {
        $("#modal-message")[0].innerText = error;
        $('#exampleModalCenter').modal('toggle');
    }

    send_button.onclick = function (event) {
        sendMessage(event);
    }

    function onConnected() {
        stompClient.subscribe('/topic/chat/' + id, onMessageReceived);
        stompClient.subscribe('/topic/closed/' + id, onClosedReceived);
    }

    function onClosedReceived(payload){
        let is = JSON.parse(payload.body);
        if(is.closed==="0"){
            $("#modal-message")[0].innerText = "Заявка ожидает обработки";
            $('#exampleModalCenter').modal('toggle');
            $("#status_select :nth-child(2)").attr("selected","selected");
            $("#text_status").html(" <i class='fas fa-cogs'></i> Ожидает обработки")
            $("#move_div").removeAttr("disabled")
            $("#messageInput").removeAttr("disabled")
            $("#send_button").removeAttr("disabled")
        } else if(is.closed==="1"){
            $("#modal-message")[0].innerText = "Заявка была закрыта";
            $('#exampleModalCenter').modal('toggle');
            $("#status_select :nth-child(2)").attr("selected","selected");
            $("#text_status").html(" <i class='fas fa-check'></i> Завершено")
            if(div_moved) {
                $("#move_div")[0].click();
            }
            $("#move_div").attr("disabled","disabled")
            $("#messageInput").attr("disabled","disabled")
            $("#send_button").attr("disabled","disabled")
        } else if(is.closed==="2"){
            $("#modal-message")[0].innerText = "Заявка принята в обработку";
            $('#exampleModalCenter').modal('toggle');
            $("#text_status").html(" <i class='far fa-clock'></i> В обработке")
            $("#status_select :nth-child(1)").attr("selected","selected");
            $("#move_div").removeAttr("disabled")
            $("#messageInput").removeAttr("disabled")
            $("#send_button").removeAttr("disabled")
        }
    }

    function onMessageReceived(payload) {
        r_me = JSON.parse(payload.body);
        jr_me = JSON.parse(r_me.paths)
        console.log(r_me);
        console.log(jr_me);
        let div11 = document.createElement('div');
        let div12 = document.createElement('div');
        let div13 = document.createElement('div');
        let div14 = document.createElement('div');
        let div21 = document.createElement('div');
        let div22 = document.createElement('div');
        let div23 = document.createElement('div');
        let span11 = document.createElement('span');
        let span12 = document.createElement('span');

        let span21 = document.createElement('span');
        let span22 = document.createElement('span');


        let p1 = document.createElement('p');
        let p2 = document.createElement('p');
        span11.className="time_date";
        span12.className="time_date";
        span21.className="time_date";
        span22.className="time_date";
        if(r_me.login != username){
            div11.className="incoming_msg";
            div12.className="received_msg";
            div13.className="received_withd_msg";
            div14.className="received_withd_msg_inner"
            div11.appendChild(div12);
            div12.appendChild(div13);
            div13.appendChild(span11);
            span11.innerText = r_me.fullname + ": " + r_me.userpost;
            div13.appendChild(div14);
            if(r_me.message_data.length>0){
                p1.innerHTML = r_me.message_data
                div14.appendChild(p1);
            }
            div13.appendChild(span12);
            span12.innerText = r_me.timestamps;
            message_area.appendChild(div11);
            let ul = document.createElement('ul');
            ul.className = "list-group list-group-flush";
            let i = 0;
            if(r_me.paths !== null){
                // div13.removeChild(span11);
                let jpaths = JSON.parse(r_me.paths)
                for(const key in jpaths){
                    let li = document.createElement("li");
                    li.className = "list-group-item incoming_attach-rec";
                    let a = document.createElement("a");
                    let span13 = document.createElement("span");
                    span13.className = "my-auto";
                    let span14 = document.createElement("span");
                    span14.className="fas fa-file fa-2x";
                    a.setAttribute("target","_blank");
                    a.setAttribute("rel","nofollow noopener");
                    a.setAttribute("href", "https://" + window.location.host + "/"  + jpaths[key][0]["url"]);
                    span13.innerText = jpaths[key][1]["filename"];
                    a.className = "d-flex justify-content-between";
                    if(jpaths[key][2]["image"]){
                        span14.className = "far fa-file-image fa-2x";
                        a.setAttribute("image","true");
                        a.setAttribute("id",generateKey(10));
                    } else if(jpaths[key][3]["audio"]){
                        span14.className = "far fa-file-audio fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                    } else if(jpaths[key][4]["video"]){
                        span14.className = "far fa-file-video fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                    } else if(jpaths[key][5]["pdf"]){
                        span14.className = "far fa-file-pdf fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                    } else if(jpaths[key][6]["excel"]){
                        span14.className = "far fa-file-excel fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                    } else if(jpaths[key][7]["word"]){
                        span14.className = "far fa-file-word fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                    } else if(jpaths[key][8]["txt"]){
                        span14.className = "far fa-file-alt fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                    } else if(jpaths[key][9]["archive"]){
                        span14.className = "far fa-file-archive fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                    }
                    a.appendChild(span13);
                    a.appendChild(span14);
                    li.appendChild(a);
                    ul.appendChild(li);
                }

                div14.appendChild(ul);

            }

            scrollToBottom();
        }
        else{
            div21.className="outgoing_msg";
            div22.className="sent_msg";
            div23.className="sent_msg_inner";

            div21.appendChild(div22);
            div22.appendChild(span21);
            span21.innerText = r_me.fullname + ": " + r_me.userpost;
            div22.appendChild(div23);
            if(r_me.message_data.length>0){
                p2.innerHTML = r_me.message_data
                div23.appendChild(p2);
            }
            div22.appendChild(span22);
            span22.innerText = r_me.timestamps;
            message_area.appendChild(div21);
            let ul = document.createElement('ul');

            let i = 0;
            ul.className = "list-group";
            if(r_me.paths !== null) {
                // div22.removeChild(span21);
                let jpaths = JSON.parse(r_me.paths)
                for (const key in jpaths) {
                    let li = document.createElement("li");
                    li.className = "list-group-item incoming_attach";
                    let a = document.createElement("a");
                    let span23 = document.createElement("span");
                    span23.className = "my-auto";
                    let span24 = document.createElement("span");
                    span24.className = "fas fa-file fa-2x";
                    a.setAttribute("target","_blank");
                    a.setAttribute("rel","nofollow noopener");
                    a.setAttribute("href", "https://" + window.location.host + "/" + jpaths[key][0]["url"]);
                    span23.innerText = jpaths[key][1]["filename"];
                    a.className = "d-flex justify-content-between";
                    if (jpaths[key][2]["image"]) {
                        span24.className = "far fa-file-image fa-2x";
                        a.setAttribute("image", "true");
                        a.setAttribute("id", generateKey(10));
                    } else if (jpaths[key][3]["audio"]) {
                        span24.className = "far fa-file-audio fa-2x";
                    } else if (jpaths[key][4]["video"]) {
                        span24.className = "far fa-file-video fa-2x";
                    } else if (jpaths[key][5]["pdf"]) {
                        span24.className = "far fa-file-pdf fa-2x";
                    } else if (jpaths[key][6]["excel"]) {
                        span24.className = "far fa-file-excel fa-2x";
                    } else if (jpaths[key][7]["word"]) {
                        span24.className = "far fa-file-word fa-2x";
                    } else if (jpaths[key][8]["txt"]) {
                        span24.className = "far fa-file-alt fa-2x";
                    } else if (jpaths[key][9]["archive"]) {
                        span24.className = "far fa-file-archive fa-2x";
                    }
                    a.appendChild(span23);
                    a.appendChild(span24);
                    li.appendChild(a);
                    ul.appendChild(li);
                }
                div23.appendChild(ul);
            } else {

            }

            scrollToBottom();
        }


    }

    function sendMessage(event) {
        console.log("sendmessage was called");
        event.preventDefault();
        send_button.setAttribute("disabled","disabled");
        $("#send_button_icon")[0].className = "fas fa-spinner fa-spin fa-2x";
        let messageContent = messageInput.value.trim();
        if (stompClient) {

            // default implementation

            // let files = $('#inputGroupFile02');
            // let images = $('#inputGroupFile01');

            // if (files.val()  ||
            //     images.val()) {
            //
            //     let form_data = new FormData($("#messageSendForm")[0]);
            //     form_data.append("requestid", document.getElementById("requestid").value);
            //     form_data.append("fullname", document.getElementById("fullname").value);
            //     form_data.append("userpost", document.getElementById("userpost").value);
            //     form_data.append("message_data", document.getElementById("messageInput").value);
            //     form_data.append("login", document.getElementById("login").value);
            //     form_data.append("token",getT);
            //
            //     $.ajax({
            //         url: "/send_message",
            //         type: "POST",
            //         headers: {"X-CSRF-TOKEN": token},
            //         data: form_data,
            //         enctype: 'multipart/form-data',
            //         processData: false,
            //         contentType: false,
            //         cache: false,
            //         success: function(data, textStatus) {
            //             console.log("message with attachments was sent successfully")
            //         },
            //         statusCode: {
            //             400: function(jqXHR, textStatus, errorThrown) {
            //                 $("#modal-message")[0].innerText = textStatus;
            //                 $('#exampleModalCenter').modal('toggle');
            //             }
            //         },
            //         error: function(){
            //             $("#modal-message")[0].innerText = "Произошла ошибка при отправке данных.";
            //             $('#exampleModalCenter').modal('toggle');
            //         },
            //         complete:function () {
            //             send_button.removeAttribute("disabled");
            //             $("#send_button_icon")[0].className = "fa fa-paper-plane fa-2x";
            //         }
            //     });
            //
            //     $('#clear01').removeClass("red-clear").html("Изображения");
            //     $("#inputGroupFile01").val("");
            //     $("#label01").html("Выберите изображения");
            //
            //     $('#clear02').removeClass("red-clear").html("Файлы");
            //     $("#inputGroupFile02").val("");
            //     $("#label02").html("Выберите файлы");
            //
            //     $("#move_div")[0].click();
            //     messageInput.value = '';
            //     console.log("inputs clear")
            if(messageContent){
                let chatMessage = {
                    requestid:document.getElementById("requestid").value,
                    fullname:document.getElementById("fullname").value,
                    userpost:document.getElementById("userpost").value,
                    message_data:document.getElementById("messageInput").value,
                    login:document.getElementById("login").value,
                };
                console.log("sending the message...");
                stompClient.send("/app/send." + id, {}, JSON.stringify(chatMessage));
                messageInput.value = '';

            }
            if($('#drag-and-drop-zone').dmUploader().data().dmUploader.queue.length>0) {
                $('#drag-and-drop-zone').dmUploader('start');
            }
        }
        if(div_moved) {
            $("#move_div")[0].click();
        }
        send_button.removeAttribute("disabled");
        $("#send_button_icon")[0].className = "fa fa-paper-plane fa-2x";

    }




    $.ajax({
        method:"GET",
        url:"/get_messages",
        data: 'requestid='+id+"&token="+token,
    }).then(function(data) {
        preloadAll(data);
    });


    function preloadAll(messages){
        for(let j = 0; j < messages.length; j++){
            console.log(messages[j]);
            let div11 = document.createElement('div');
            let div12 = document.createElement('div');
            let div13 = document.createElement('div');
            let div14 = document.createElement('div');
            let div21 = document.createElement('div');
            let div22 = document.createElement('div');
            let div23 = document.createElement('div');
            let span11 = document.createElement('span');
            let span12 = document.createElement('span');

            let span21 = document.createElement('span');
            let span22 = document.createElement('span');


            let p1 = document.createElement('p');
            let p2 = document.createElement('p');
            span11.className="time_date";
            span12.className="time_date";
            span21.className="time_date";
            span22.className="time_date";
            if(messages[j].login != username){
                console.log("user isnt me");
                div11.className="incoming_msg";
                div12.className="received_msg";
                div13.className="received_withd_msg";
                div14.className="received_withd_msg_inner"
                div11.appendChild(div12);
                div12.appendChild(div13);
                div13.appendChild(span11);
                span11.innerText = messages[j].fullname + ": " + messages[j].userpost;
                div13.appendChild(div14);
                if(messages[j].message_data.length>0){
                    console.log(messages[j].message_data);
                    p1.innerHTML = messages[j].message_data
                    div14.appendChild(p1);
                }
                div13.appendChild(span12);
                span12.innerText = messages[j].timestamps;
                message_area.appendChild(div11);
                let ul = document.createElement('ul');
                ul.className = "list-group list-group-flush";
                let i = 0;
                if(messages[j].paths !== null){
                    let jpaths = JSON.parse(messages[j].paths);
                    for(const key in jpaths){
                        let li = document.createElement("li");
                        li.className = "list-group-item incoming_attach-rec";
                        let a = document.createElement("a");
                        let span13 = document.createElement("span");
                        span13.className = "my-auto";
                        let span14 = document.createElement("span");
                        span14.className="fas fa-file fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                        a.setAttribute("href", "https://" + window.location.host + "/"  + jpaths[key][0]["url"]);
                        span13.innerText = jpaths[key][1]["filename"];
                        a.className = "d-flex justify-content-between";
                        if(jpaths[key][2]["image"]){
                            span14.className = "far fa-file-image fa-2x";
                            a.setAttribute("image","true");
                            a.setAttribute("id",generateKey(10));
                        } else if(jpaths[key][3]["audio"]){
                            span14.className = "far fa-file-audio fa-2x";
                            a.setAttribute("target","_blank");
                            a.setAttribute("rel","nofollow noopener");
                        } else if(jpaths[key][4]["video"]){
                            span14.className = "far fa-file-video fa-2x";
                            a.setAttribute("target","_blank");
                            a.setAttribute("rel","nofollow noopener");
                        } else if(jpaths[key][5]["pdf"]){
                            span14.className = "far fa-file-pdf fa-2x";
                            a.setAttribute("target","_blank");
                            a.setAttribute("rel","nofollow noopener");
                        } else if(jpaths[key][6]["excel"]){
                            span14.className = "far fa-file-excel fa-2x";
                            a.setAttribute("target","_blank");
                            a.setAttribute("rel","nofollow noopener");
                        } else if(jpaths[key][7]["word"]){
                            span14.className = "far fa-file-word fa-2x";
                            a.setAttribute("target","_blank");
                            a.setAttribute("rel","nofollow noopener");
                        } else if(jpaths[key][8]["txt"]){
                            span14.className = "far fa-file-alt fa-2x";
                            a.setAttribute("target","_blank");
                            a.setAttribute("rel","nofollow noopener");
                        } else if(jpaths[key][9]["archive"]){
                            span14.className = "far fa-file-archive fa-2x";
                            a.setAttribute("target","_blank");
                            a.setAttribute("rel","nofollow noopener");
                        }
                        a.appendChild(span13);
                        a.appendChild(span14);
                        li.appendChild(a);
                        ul.appendChild(li);
                    }

                    div14.appendChild(ul);

                }

            }
            else{
                console.log("user is me");
                div21.className="outgoing_msg";
                div22.className="sent_msg";
                div23.className="sent_msg_inner";

                div21.appendChild(div22);
                div22.appendChild(span21);
                span21.innerText = messages[j].fullname + ": " + messages[j].userpost;
                div22.appendChild(div23);
                if(messages[j].message_data.length>0){
                    p2.innerHTML = messages[j].message_data
                    div23.appendChild(p2);
                }
                div22.appendChild(span22);
                span22.innerText = messages[j].timestamps;send_button
                message_area.appendChild(div21);
                let ul = document.createElement('ul');

                let i = 0;
                ul.className = "list-group";
                if(messages[j].paths !== null) {
                    // div22.removeChild(span21);
                    let jpaths = JSON.parse(messages[j].paths)
                    for (const key in jpaths) {
                        let li = document.createElement("li");
                        li.className = "list-group-item incoming_attach";
                        let a = document.createElement("a");
                        let span23 = document.createElement("span");
                        span23.className = "my-auto";
                        let span24 = document.createElement("span");
                        span24.className = "fas fa-file fa-2x";
                        a.setAttribute("target","_blank");
                        a.setAttribute("rel","nofollow noopener");
                        a.setAttribute("href", "https://" + window.location.host + "/" + jpaths[key][0]["url"]);
                        span23.innerText = jpaths[key][1]["filename"];
                        a.className = "d-flex justify-content-between";
                        if (jpaths[key][2]["image"]) {
                            span24.className = "far fa-file-image fa-2x";
                            a.setAttribute("image", "true");
                            a.setAttribute("id", generateKey(10));
                        } else if (jpaths[key][3]["audio"]) {
                            span24.className = "far fa-file-audio fa-2x";
                        } else if (jpaths[key][4]["video"]) {
                            span24.className = "far fa-file-video fa-2x";
                        } else if (jpaths[key][5]["pdf"]) {
                            span24.className = "far fa-file-pdf fa-2x";
                        } else if (jpaths[key][6]["excel"]) {
                            span24.className = "far fa-file-excel fa-2x";
                        } else if (jpaths[key][7]["word"]) {
                            span24.className = "far fa-file-word fa-2x";
                        } else if (jpaths[key][8]["txt"]) {
                            span24.className = "far fa-file-alt fa-2x";
                        } else if (jpaths[key][9]["archive"]) {
                            span24.className = "far fa-file-archive fa-2x";
                        }
                        a.appendChild(span23);
                        a.appendChild(span24);
                        li.appendChild(a);
                        ul.appendChild(li);
                    }

                    div23.appendChild(ul);

                }

            }
        }
    }


});

function dec2hex (dec) {
    return ('0' + dec.toString(16)).substr(-2)
}

function generateKey (len) {
    let arr = new Uint8Array((len || 40) / 2)
    window.crypto.getRandomValues(arr)
    return Array.from(arr, dec2hex).join('')
}

$("#move_div").click(function () {
    if(div_moved){
        document.getElementById('movable').style.top = 454+'px';
        document.getElementById('movable').style.zIndex="-1";
        document.getElementById('movable').style.display = "none";
        div_moved = false;
        console.log("true moved");
    }else{
        document.getElementById('movable').style.top = '-'+454+'px';
        document.getElementById('movable').style.zIndex="2";
        document.getElementById('movable').style.display = "block";
        div_moved = true;
        console.log("false moved");
    }});

$("#status_select").on("change",function () {
    let val = $("#status_select").val();
    $("#status_select").attr("disabled","disabled");
    if(val == "0"){
        open_request();
    } else if(val=="1"){
        close_request();
    } else if(val=="2"){
        working_request();
    }
})

function close_request(){
    $.ajax({
        method:"POST",
        url:"/request_close/"+id,
        headers: {
            "X-CSRF-TOKEN":token,
        }
    }).then(function () {
        $("#status_select").removeAttr("disabled");
    })
}

function open_request(){
    $.ajax({
        method:"POST",
        url:"/request_open/"+id,
        headers: {
            "X-CSRF-TOKEN":token,
        }
    }).then(function () {
        $("#status_select").removeAttr("disabled");
    })
}

function working_request(){
    $.ajax({
        method:"POST",
        url:"/request_work/"+id,
        headers: {
            "X-CSRF-TOKEN":token,
        }
    }).then(function () {
        $("#status_select").removeAttr("disabled");
    })
}

function scrollToBottom(){
    console.log("Scrolling to bottom");
    $('html,body').animate({scrollTop: document.body.scrollHeight}, 0.1);
}

$(document).ready(function() {

    let timerId = setInterval(function () {
        $("a[image='true']").each(function () {
            let id = $(this).attr("id");
            $("#" + id).magnificPopup({
                type: "image"
            })
        })

    }, 500);
});



$(function(){
  /*
   * For the sake keeping the code clean and the examples simple this file
   * contains only the plugin configuration & callbacks.
   * 
   * UI functions ui_* can be located in: demo-ui.js
   */
  $('#drag-and-drop-zone').dmUploader({ //
    url: "https://localhost:8443/send_message",
    method: "POST",
    queue: true,
    auto: false,
    extraData: function(){
      var paramObj = {};
      $.each($('#drop-area').closest('form').serializeArray(), function(_, kv) {
        paramObj[kv.name] = kv.value;
      });
      return{
        "files": paramObj,
        "requestid": document.getElementById("requestid").value,
        "fullname": document.getElementById("fullname").value,
        "userpost": document.getElementById("userpost").value,
        "login": document.getElementById("login").value,
        "token": Cookies.get("token"),
      }
    },
    headers: {
      'X-CSRF-TOKEN': $("meta[name='_csrf']").attr("content"),
    },
    fieldName: "files",
    maxFileSize: 52428800,
    onDragEnter: function(){
      // Happens when dragging something over the DnD area
      this.addClass('active');
    },
    onDragLeave: function(){
      // Happens when dragging something OUT of the DnD area
      this.removeClass('active');
    },
    onInit: function(){
      $("#clear_queue").click(function () {
        $('.media').remove();
        $('#files').find('li.empty').fadeIn();
        $("#clear_queue").addClass("disabled").attr("disabled","disabled")
        var myUploader = $('#drag-and-drop-zone').dmUploader().data();
        myUploader.dmUploader.queue = [];
      })

    },
    onComplete: function(){
      // All files in the queue are processed (success or error)
      $("#clear_queue").addClass("disabled").attr("disabled","disabled")
      var myUploader = $('#drag-and-drop-zone').dmUploader().data();
      myUploader.dmUploader.queue = [];
      $('.media').remove();
      $('#files').find('li.empty').fadeIn();
      $("#send_button")[0].removeAttribute("disabled");
      $("#send_button_icon")[0].className = "fa fa-paper-plane fa-2x";
      $("#messageInput")[0].value = ""

    },
    onNewFile: function(id, file){
      // When a new file is added using the file selector or the DnD area
      $("#clear_queue").removeClass("disabled").removeAttr("disabled");
      ui_multi_add_file(id, file);
    },
    onBeforeUpload: function(id){
      // about tho start uploading a file
      ui_multi_update_file_status(id, 'uploading', 'Uploading...');
      ui_multi_update_file_progress(id, 0, '', true);
    },
    onUploadCanceled: function(id) {
      // Happens when a file is directly canceled by the user.
      ui_multi_update_file_status(id, 'warning', 'Canceled by User');
      ui_multi_update_file_progress(id, 0, 'warning', false);
    },
    onUploadProgress: function(id, percent){
      // Updating file progress
      ui_multi_update_file_progress(id, percent);
    },
    onUploadSuccess: function(id, data){
      // A file was successfully uploaded
      ui_multi_update_file_status(id, 'success', 'Загрузка завершена');
      ui_multi_update_file_progress(id, 100, 'success', false);
    },
    onUploadError: function(id, xhr, status, message){
      console.log("id: " + id + "; status: " + status + "; message: " + message + "; xhr: " + xhr.responseText );
      ui_multi_update_file_status(id, 'danger', message);
      ui_multi_update_file_progress(id, 0, 'danger', false);  
    },
    onFallbackMode: function(){
      // When the browser doesn't support this plugin :(

    },
    onFileSizeError: function(file){

      $("#modal-message")[0].innerText = "Недопустимый вес вложений (файл больше 50 МБ!)";
      $('#exampleModalCenter').modal('toggle');

    },
  });
});
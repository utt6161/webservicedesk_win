  /*
   * Some helper functions to work with our UI and keep our code cleaner
   */

function removeFileElement(e){
  console.log(e.data.id);
  e.preventDefault();
  var data = $('#drag-and-drop-zone').dmUploader().data().dmUploader.queue;
  if (data.length==1){
    $('.media').remove();
    $('#files').find('li.empty').fadeIn();
    $("#clear_queue").addClass("disabled").attr("disabled","disabled")
    $('#drag-and-drop-zone').dmUploader().data().dmUploader.queue = [];
  } else{
      $("#uploaderFile"+e.data.id).remove();
      for(let i = 0; i < data.length; i++){
        if(data[i]["id"]==e.data.id){
          data.splice(i,1);
          break;
        }

      }

  }
}

// Creates a new file and add it to our list

function ui_multi_add_file(id, file)
{
  var template = $('#files-template').text();
  template = template.replace('%%filename%%', file.name);
  template = template.replace('%%remove%%', "data-attach-id="+id)

  template = $(template);
  template.prop('id', 'uploaderFile' + id);
  template.data('file-id', id);

  $('#files').find('li.empty').fadeOut(); // remove the 'no files yet'
  $('#files').prepend(template);
  console.log("file added");
  $("button[data-attach-id="+id+"]").click({"id":id}, removeFileElement);
}

// Changes the status messages on our list
function ui_multi_update_file_status(id, status, message)
{

  $('#uploaderFile' + id).find('span').html(message).prop('class', 'status text-' + status);
}

// Updates a file progress, depending on the parameters it may animate it or change the color.
function ui_multi_update_file_progress(id, percent, color, active)
{
  color = (typeof color === 'undefined' ? false : color);
  active = (typeof active === 'undefined' ? true : active);

  var bar = $('#uploaderFile' + id).find('div.progress-bar');

  bar.width(percent + '%').attr('aria-valuenow', percent);
  bar.toggleClass('progress-bar-striped progress-bar-animated', active);

  if (percent === 0){
    bar.html('');
  } else {
    bar.html(percent + '%');
  }

  if (color !== false){
    bar.removeClass('bg-success bg-info bg-warning bg-danger');
    bar.addClass('bg-' + color);
  }
}
$(document).ready( function() {
    $.get('/scheduler/jobs?tme='+((new Date()).getTime()), function(data) {
        $(data.data).each(function(i,v) {
            $(".trows").append(getJobRow(v))
        });
    });

    var getJobRow = function(v){
        var a = getButtonStatus(v.jobStatus)
        var template = `<tr>
                        <td><a id="his-${v.jobName}" class="history" href="#">${v.jobName}</a></td>
                        <td>${getDisplayDate(v.scheduleTime)}</td>
                        <td>${getDisplayDate(v.lastFiredTime)}</td>
                        <td>${getDisplayDate(v.nextFireTime)}</td>
                        <td>${v.jobStatus}</td>
                        <td><button type="button" id="btn1-${v.jobName}" class="btn btn-primary btn-sm run ${a.brun}">Run Now</button>
                            <button type="button" id="btn2-${v.jobName}" class="btn btn-primary btn-sm stop ${a.bstop}">Stop</button>
                            <button type="button" id="btn3-${v.jobName}" class="btn btn-primary btn-sm pause ${a.bpause}">Pause</button>
                            <button type="button" id="btn4-${v.jobName}" class="btn btn-primary btn-sm resume ${a.bresume}">Resume</button>
                            <button type="button" id="btn5-${v.jobName}" class="btn btn-primary btn-sm edit ${a.bedit}">Edit</button>
                            <button type="button" id="btn6-${v.jobName}" class="btn btn-primary btn-sm delete ${a.bdelete}">Delete</button>
                        </td>
                    </tr>`
        return template
    }

    var getButtonStatus = function(stat){
        var res = {}
        if(stat=="SCHEDULED"){
            res["bstop"] ="disabled"
            res["bresume"] ="disabled"
        }
        if(stat=="RUNNING"){
            res["brun"] ="disabled"
            res["bpause"] ="disabled"
            res["bresume"] ="disabled"
            res["bedit"] ="disabled"
            res["bdelete"] ="disabled"
        }
        if(stat=="PAUSED"){
            res["bpause"] ="disabled"
            res["bstop"] ="disabled"
        }
        return res
    }

});

$(".btnSave").click(function() {
    $.post( "/scheduler/update", { jobName: $.trim($("#inputJobname").val()), jobScheduleTime: getISODate($("#inputJobSchTime").val()), cronExpression: $("#inputCronExpr").val() })
    .done(function( data ) {
        if(data.statusCode == 200) {
            $(".modal-panel").hide()
            location.reload()
        }
    });
});

$(".btnSchedule").click(function() {
    $.post( "/scheduler/schedule", { jobName: $.trim($("#inputJobname").val()), jobScheduleTime: getISODate($("#inputJobSchTime").val()), cronExpression: $("#inputCronExpr").val(), className: $("#inputClassName").val()  })
      .done(function( data ) {
        if(data.statusCode == 200) {
            $(".modal-panel").hide()
            location.reload()
        }
        else {
            msg = ""
            if(data.statusCode == 501) msg = "Job with same name already exists!";
            if(data.statusCode == 504) msg = "Invalid job class!";
            if(data.statusCode == 503) msg = "Invalid cron Expression!";
            $(".error").text(msg)
            $(".error").show()
        }
    });
});

$( ".btnNewJob" ).click(function() {
  $("#inputJobname").val('');
  $("#inputJobSchTime").val('');
  $("#inputCronExpr").val('');
  $("#inputClassName").val('');
  $(".modal-panel").show()
  $(".btnSave").hide()
  $(".btnSchedule").show()
  $(".error").hide()
  $("#itemHistory").hide()
});

$( ".close" ).click(function() {
  $(".modal-panel").hide()
});

$('body').on('click', '.history', function() {
    $(".hrows").html('')
    var btn = $(this).attr('id').split('-')
    $.get("/scheduler/jobs/history/"+btn[1]+"?count=10")
    .done(function( data ) {
        if(data.statusCode == 200) {
            $(data.data).each(function(i,v) {
                if(i<10) {
                    var row = `<tr><td>${getDisplayDate(v.dateTime)}</td><td>${v.duration}</td><td>${v.status}</td><td>${getDisplayMessage(v.message)}</td></tr>`
                    $(".hrows").append(row)
                }
            });
            $("#itemHistory").show()
        }
    });
});

$('body').on('click', '.btn-primary', function() {
  if($(this).attr('id')){
    var btn = $(this).attr('id').split('-')
    switch(btn[0]){
        case 'btn1':
           takeJobAction(btn[1],'start');
           break;
        case 'btn2':
           takeJobAction(btn[1],'stop');
           break;
        case 'btn3':
           takeJobAction(btn[1],'pause');
           break;
        case 'btn4':
           takeJobAction(btn[1],'resume');
           break;
        case 'btn5':
           editJob(btn[1]);
           break;
        case 'btn6':
           takeJobAction(btn[1],'delete');
           break;
    }
  }
});

var takeJobAction = function(jName,action){
    $.get('/scheduler/'+action+'?jobName='+jName+'&tme='+((new Date()).getTime()), function(data) {
        if(data.statusCode == 200) {
            location.reload()
        }
    });
}


var editJob = function(jName){
    $.get('/scheduler/jobs/'+jName+'?tme='+((new Date()).getTime()), function(data) {
        $(data.data).each(function(i,v) {
          $("#inputJobname").val(v.jobName);
          $("#inputJobSchTime").val(getDisplayDate(v.scheduleTime));
          $("#inputCronExpr").val(v.cronExpr);
          $("#inputClassName").val(v.className);
          $("#inputJobname").attr('readonly', true);
          $("#inputClassName").attr('readonly', true);
          $(".modal-panel").show()
          $(".btnSchedule").hide()
          $(".btnSave").show()
          $(".error").hide()
          $("#itemHistory").hide();
        });
    });
}

var getDisplayDate = function(ts){
    if(ts==null) return ""
    else return ts.replace("T"," ").substring(0,ts.indexOf("."))
}

var getDisplayMessage = function(ts){
    if(ts==null) return ""
    else return ts
}

var getISODate = function(ts){
    return $.trim(ts).replace(" ","T") +".000-0500"
}

var rec_flg = true;
var rev_flg = true;
var rec_count = 0;
var decision_string = "";

output_recognition = Speech_Func();

$('#revision_button').on('click', function() {
    if (rev_flg) {
        $('#output_contents').css('height', '70%');
        $('#revision_textarea').show();
        $(this).text('REV EXECUTION');
        $('#revision_textarea').val(decision_string);
        $("#delete_button").attr('disabled', true);
        $("#rec_button").attr('disabled', true);
        $('#Button_Fields').css('opacity', '0.3');
        rev_flg = false;
    } else {
        $('#output_contents').css('height', '90%');
        $('#revision_textarea').hide();
        decision_string = $('#revision_textarea').val();
        $('#output_contents').text(decision_string);
        $(this).text('REV');
        $("#delete_button").attr('disabled', false);
        $("#rec_button").attr('disabled', false);
        $('#Button_Fields').css('opacity', '1');
        rev_flg = true;
    }
});

$('#rec_button').on('click', function() {
    if (rec_flg) {
        output_recognition.start();
        $('#delete_button').hide();
        $(this).text('STOP');
        $("#revision_button").attr('disabled', true);
        $('body').css('background-color', '#FCE7EA');
        $('#revision_button').css('opacity', '0.3');
        rec_flg = false;
    } else {
        output_recognition.stop();
        $('#delete_button').show();
        $(this).text('REC');
        rec_count = 0;
        $("#revision_button").attr('disabled', false);
        $('body').css('background-color', '#FFF');
        $('#revision_button').css('opacity', '1');
        rec_flg = true;
    }
});

$('#delete_button').on('click', function() {
    $('#output_contents').text('');
    decision_string = "";
});

window.addEventListener('touchmove', function(event) {
    event.preventDefault();
});

function Speech_Func() {
    SpeechRecognition = webkitSpeechRecognition || SpeechRecognition;

    if ('SpeechRecognition' in window) {
        console.log("対応");
    } else {
        console.log("不対応");
    }

    const recognition = new SpeechRecognition();
    recognition.lang = 'ja-JP';
    recognition.interimResults = true;
    recognition.continuous = true;

    recognition.onresult = (event) => {
        $('#output_contents').text(decision_string + event.results[rec_count][0].transcript);
        //console.log(event);
        console.log(event.results[rec_count][0].transcript);
        console.log(event.results[rec_count].isFinal);
        //console.log(rec_count);
        if (event.results[rec_count].isFinal) {
            decision_string += event.results[rec_count][0].transcript;
            rec_count++;
        }
    }

    return recognition;
}
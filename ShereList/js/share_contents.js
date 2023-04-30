// Firebase諸々の設定
const firebaseConfig = {
    apiKey: "AIzaSyBDKc6xsFIOz_InKhkblf_QxySPl8MW6bE",
    authDomain: "ysharelist.firebaseapp.com",
    projectId: "ysharelist",
    storageBucket: "ysharelist.appspot.com",
    messagingSenderId: "507583824941",
    appId: "1:507583824941:web:462d1c0c7b444ac394a4a4",
    measurementId: "G-WMTM2YT79V"
};

firebase.initializeApp(firebaseConfig);
var db = firebase.firestore();

var gb = document.getElementById("goodbt");
var bb = document.getElementById("badbt");
var gbt = document.getElementById("GBtext");
//var bgbody = document.getElementsByClassName("tops");
var url = document.getElementById("top_url")
var u = document.getElementById("URL");
var input_comment = document.getElementById("in_kome");
var error_text = document.getElementById("e_mes");
var comment_text = document.getElementById("commentcount");
var ul = document.getElementById("ul_kome");

var query = location.search;
var id = query.split('=');
var postRef;
var value_v;
var butpush = false;
var nowGB = 0; //0:None 1:Good 2:Bad

// 初期実行
$(function() {
    ContentsView();
});

// Firebaseからのデータ取得＆データ表示
function ContentsView() {
    (async() => {
        url.href = "https://www.youtube.com/playlist?list=" + String(id[1]);
        u.href = "https://www.youtube.com/playlist?list=" + String(id[1]);
        u.textContent = "https://www.youtube.com/playlist?list=" + String(id[1]);
        postRef = db.collection('PlayList_Data').doc(id[1]);
        var postDoc = await postRef.get();

        var value_title = postDoc.get('title'); // タイトル
        var title = document.getElementById('s_title');
        title.innerHTML = value_title;
        var value_overview = postDoc.get('overview'); // 概要
        var p_overview = document.getElementById('gaiyou');
        p_overview.innerHTML = value_overview;
        var value_listdata = postDoc.get('pdata'); // 再生リストデータ
        value_v = postDoc.get('value'); // 評価
        var value_comment = Comment_Space(postDoc.get('comment')); // コメント

        for (var i = 0; i < value_listdata.length; i++) { // 再生リスト表示

            var samune = document.getElementById('samune');
            samune.src = value_listdata[0].thmurl;

            var ul1 = document.getElementById('list');
            var li1 = document.createElement('li');

            var l_img = document.createElement('img');
            l_img.src = value_listdata[i].thmurl;
            l_img.id = "img_cont";
            li1.appendChild(l_img);

            var l_title = document.createElement('p');
            l_title.innerHTML = value_listdata[i].title;
            li1.appendChild(l_title);

            ul1.appendChild(li1);

        }

        comment_text.textContent = value_comment.length + "件のコメント";

        CommentView(value_comment);
    })()
}

// コメント表示
function CommentView(vc) {
    var p_value = document.getElementById('p_value');
    p_value.innerHTML = Math.round(PercentCalc(value_v) * 100) + "%";
    for (var i = 0; i < vc.length; i++) {

        var ul2 = document.getElementById('ul_kome');
        var li2 = document.createElement('li');

        var p_kome = document.createElement('p');
        p_kome.innerHTML = vc[i];

        li2.appendChild(p_kome);
        ul2.appendChild(li2);

    }
}

// 評価値計算
function PercentCalc(v) {
    var vtemp = v.split(",");
    if (isNaN(Number(vtemp[0]) / (Number(vtemp[0]) + Number(vtemp[1])))) { return 0; }
    return Number(vtemp[0]) / (Number(vtemp[0]) + Number(vtemp[1]));
}

// Goodボタン処理
function GoodBt() {
    if (nowGB == 0 || nowGB == 2) {
        (async() => {
            var gv = value_v.split(',');
            try {
                gbt.textContent = "Good!";
                gbt.style.color = "#006AB6";
                //bgbody[0].style.backgroundColor = "rgb(0, 106, 182, 0.05)"
                if (nowGB == 0) {
                    nowGB = 1;
                    value_v = (Number(gv[0]) + 1) + "," + gv[1];
                    await postRef.update({
                        value: String(Number(gv[0]) + 1) + ',' + String(Number(gv[1]))
                    })
                } else {
                    nowGB = 1;
                    value_v = (Number(gv[0]) + 1) + "," + (Number(gv[1]) - 1);
                    await postRef.update({
                        value: String(Number(gv[0]) + 1) + ',' + String(Number(gv[1]) - 1)
                    })
                }
            } catch {}
        })()
    }
}

// Badボタン処理
function BadBt() {
    if (nowGB == 0 || nowGB == 1) {
        (async() => {
            var gv = value_v.split(',');
            try {
                gbt.textContent = "Bad...";
                gbt.style.color = "#C32539";
                //bgbody[0].style.backgroundColor = "rgb(195, 37, 57, 0.05)"
                if (nowGB == 0) {
                    nowGB = 2;
                    value_v = gv[0] + "," + (Number(gv[1]) + 1);
                    await postRef.update({
                        value: String(Number(gv[0])) + ',' + String(Number(gv[1]) + 1)
                    })
                } else {
                    nowGB = 2;
                    value_v = (Number(gv[0]) - 1) + "," + (Number(gv[1]) + 1);
                    await postRef.update({
                        value: String(Number(gv[0]) - 1) + ',' + String(Number(gv[1]) + 1)
                    })
                }
            } catch {}
        })()
    }
}

// 入力コメント1送信処理
function CommentSend() {
    if (input_comment.value != "") {
        error_text.textContent = "";
        (async() => {
            var postRef = db.collection('PlayList_Data').doc(id[1]);
            var postDoc = await postRef.get();

            var commentArray = Comment_Space(postDoc.get('comment'));
            for (var i = 0; i < commentArray.length; i++) {
                ul.lastElementChild.remove();
            }

            commentArray.push(input_comment.value);
            CommentView(commentArray);
            comment_text.textContent = commentArray.length + "件のコメント";

            input_comment.value = "";

            await postRef.update({
                comment: firebase.firestore.FieldValue.arrayUnion(...commentArray)
            })
        })()
    } else {
        error_text.textContent = "ERROR : 文字が入力されていません。";
    }
}

// コメント重複削除処理
function Comment_Space(carray) {
    var array = [];
    for (var i = 0; i < carray.length; i++) {
        if (carray[i] != "") {
            array.push(carray[i]);
        }
    }
    return array;
}
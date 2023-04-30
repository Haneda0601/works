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

var apikey = 'AIzaSyC0pnoqIJJxOBR0rhX8-HlnYSS-DCMKiKU';
var inputflg = false;
var finishflg = false;
var ValuePercent = {};

var input = document.getElementById("search-bar");
var inputN = document.getElementsByName("search-bar");
var h1_1 = document.getElementById("ran-h1");
var gifimg = document.getElementsByClassName("loadgif");
var rsearch = document.getElementById("ran-search");
var btn = document.getElementById('exe_botan');

firebase.initializeApp(firebaseConfig);

var db = firebase.firestore();

// 初期処理
$(function() {
    MakeRankingList();
});

// 検索欄に文字が入力される度に実行
input.addEventListener("input", function() {
    if (input.value) {
        inputflg = true;
    } else {
        inputflg = false;
    }
});

// Enterキー押下時、送信処理が実行する
window.document.onkeydown = function(event) {
    if (event.key === 'Enter' && inputflg) {
        TagSearch();
    }
}

// ランキングデータ取得処理
function MakeRankingList() {
    (async() => {
        var postRefR = db.collection('Ranking_Data').doc('Data');
        var postDocR = await postRefR.get();

        ValuePercent = postDocR.get('RankingArray');

        DataOutput();
    })()
}

// 動画リストデータ表示処理
function DataOutput() {
    (async() => {
        //並び変えたデータを順に出力
        for (var s = 0; s < Object.keys(ValuePercent).length; s++) {
            if (s >= 10) { break; }
            var postRef = db.collection('PlayList_Data').doc(ValuePercent[String(s)][0]);
            var postDoc = await postRef.get();

            var li = document.createElement('li');
            var ul = document.getElementById("search-r");

            var a_rink = document.createElement('a');
            a_rink.href = 'https://ysharelist.web.app/share_contents.html?id=' + ValuePercent[String(s)][0];

            var img_s = document.createElement('img');
            img_s.src = postDoc.get('pdata')[0].thmurl;
            a_rink.appendChild(img_s);

            var div1 = document.createElement('div');
            div1.classList.add('detail');

            var div2 = document.createElement('div');
            div2.classList.add('titel_div');

            var p1 = document.createElement('p');
            p1.classList.add('title');
            p1.innerHTML = postDoc.get('title');
            div2.appendChild(p1);
            div1.appendChild(div2);

            var div3 = document.createElement('div');
            div3.classList.add('hyouka');

            var imag_g = document.createElement('img');
            imag_g.classList.add('like_img');
            imag_g.src = 'img/like-solid-24.png';
            div3.appendChild(imag_g);

            var p2 = document.createElement('p');
            p2.classList.add('evaluation');
            p2.innerHTML = Math.round((ValuePercent[String(s)][1] * 100)) + '%';
            div3.appendChild(p2);
            div1.appendChild(div3);

            a_rink.appendChild(div1);

            li.appendChild(a_rink);
            ul.appendChild(li);
            gifimg[0].style.display = "none";
        }
        finishflg = true;
        $('input:text').attr('placeholder', 'データ読込み完了');

        window.setTimeout(function() {
            $('input:text').attr('placeholder', '検索');
        }, 1000);
    })()
}

// 評価値計算処理
function PercentCalc(v) {
    var vtemp = v.split(",");
    if (isNaN(Number(vtemp[0]) / (Number(vtemp[0]) + Number(vtemp[1])))) { return 0; }
    return Number(vtemp[0]) / (Number(vtemp[0]) + Number(vtemp[1]));
}

// Tag検索処理
function TagSearch() {
    if (finishflg) {
        finishflg = false;
        var SearchValue = [];
        (async() => {
            var tref = await db.collection('PlayList_Tags').get();
            // 検索された内容に登録されているタグがあるか否か検索
            for (var i = 0; i < tref.size; i++) {
                var reg = new RegExp(String(tref.docs.map(postDoc => postDoc.id)[i]));
                if (String(input.value).match(reg)) {
                    SearchValue.push(tref.docs.map(postDoc => postDoc.id)[i]);
                }
            }

            gifimg[0].style.display = "block";
            rsearch.style.display = "block";

            switch (SearchValue.length) {
                case 0: // 検索結果0HIT

                    Ul_remove();

                    rsearch.textContent = "検索結果:無し"
                    h1_1.style.display = "block";

                    DataOutput();
                    break;
                case 1: // 検索結果1HIT

                    Ul_remove();

                    var tagref = db.collection('PlayList_Tags').doc(SearchValue[0]);
                    var tagdoc = await tagref.get();
                    rsearch.textContent = "検索結果:" + String(tagdoc.get('listcount')) + "件";
                    for (var n = 0; n < tagdoc.get('listcount'); n++) {
                        SearchResultView(tagdoc.get('listid')[n]);
                    }
                    finishflg = true;
                    break;
                case 2: // 検索結果2HIT

                    Ul_remove();

                    var SearchIdArray = [];
                    var count = 0;
                    for (var s = 0; s < SearchValue.length; s++) {
                        var tagref = db.collection('PlayList_Tags').doc(SearchValue[s]);
                        var tagdoc = await tagref.get();
                        SearchIdArray.push(tagdoc.get('listid'));
                    }
                    for (var x = 0; x < SearchIdArray[0].length; x++) {
                        for (var y = 0; y < SearchIdArray[1].length; y++) {
                            if (SearchIdArray[0][x] == SearchIdArray[1][y]) {
                                SearchResultView(SearchIdArray[0][x]);
                                count++;
                            }
                        }
                    }
                    rsearch.textContent = "検索結果:" + String(count) + "件";
                    finishflg = true;
                    break;
                default: // 検索結果3HIT以上
                    rsearch.textContent = "検索結果:無し"
                    h1_1.style.display = "block";
                    MakeRankingList();
                    break;
            }
        })()
    } else {
        alert("データが読み込まれるまで少々お待ちください。");
    }
}

// 表示されている再生リストのデータを非表示に
function Ul_remove() {
    h1_1.style.display = "block";

    if (h1_1.style.display == "block") {
        h1_1.style.display = "none";
    }

    var ul = document.getElementById('search-r');
    var ul_count = ul.children.length;

    for (var i = 0; i < ul_count; i++) {
        ul.lastElementChild.remove();
    }
}

// 検索結果を表示
function SearchResultView(id) {
    (async() => {
        var pRef = db.collection('PlayList_Data').doc(id);
        var pDoc = await pRef.get();

        var li = document.createElement('li');
        var ul = document.getElementById("search-r");

        var a_rink = document.createElement('a');
        a_rink.href = 'https://ysharelist.web.app/share_contents.html?id=' + id;

        var img_s = document.createElement('img');
        img_s.src = pDoc.get('pdata')[0].thmurl;
        a_rink.appendChild(img_s);

        var div1 = document.createElement('div');
        div1.classList.add('detail');

        var div2 = document.createElement('div');
        div2.classList.add('titel_div');

        var p1 = document.createElement('p');
        p1.classList.add('title');
        var text_title = document.createTextNode(pDoc.get('title'));
        p1.appendChild(text_title);
        div2.appendChild(p1);
        div1.appendChild(div2);

        var div3 = document.createElement('div');
        div3.classList.add('hyouka');

        var imag_g = document.createElement('img');
        imag_g.classList.add('like_img');
        imag_g.src = 'img/like-solid-24.png';
        div3.appendChild(imag_g);

        var p2 = document.createElement('p');
        p2.classList.add('evaluation');
        var text_hyouka = document.createTextNode(String(Math.round((PercentCalc(pDoc.get('value')) * 100))) + '%');
        p2.appendChild(text_hyouka);
        div3.appendChild(p2);
        div1.appendChild(div3);

        a_rink.appendChild(div1);

        li.appendChild(a_rink);
        ul.appendChild(li);
        gifimg[0].style.display = "none";
    })()
}
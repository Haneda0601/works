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

firebase.initializeApp(firebaseConfig);
var db = firebase.firestore();

var idx = 0;
var listid = '';
var videoid = '';
var pagetoken = '';
var results = [];
var tempurls = [];
var ValuePercent = [];
var tags = [''];
var flg = false;
var urlflg = false;
var idexit = false;

var value_url = document.getElementsByClassName("input_url");
var value_title = document.getElementsByClassName("input_title");
var value_ov = document.getElementsByClassName("input_overview");
var post_but = document.getElementsByClassName("postbut");
var error_text = document.getElementsByClassName("error");
var error2_text = document.getElementsByClassName("error2");

// 初期実行
$(function() {
    (async() => {
        // FireBaseからデータを取得
        var querySnapshot = await db.collection('PlayList_Data').get();
        for (var i = 0; i < querySnapshot.size; i++) {
            var postRef = db.collection('PlayList_Data').doc(querySnapshot.docs.map(postDoc => postDoc.id)[i])
            var postDoc = await postRef.get();
            var tempArray = [querySnapshot.docs.map(postDoc => postDoc.id)[i], PercentCalc(postDoc.get('value'))];
            ValuePercent.push(tempArray);
        }

        // 評価値を降順に並び替え
        ValuePercent.sort(function(a, b) { return (b[1] - a[1]); });

        var obj = {};
        for (let i = 0; i < ValuePercent.length; i++) {
            obj[i] = ValuePercent[i];
        }

        // Firebase側へデータをセット
        await db.collection('Ranking_Data').doc('Data').set({
            RankingArray: obj
        })
    })()
});

// 評価値計算
function PercentCalc(v) {
    var vtemp = v.split(",");
    if (isNaN(Number(vtemp[0]) / (Number(vtemp[0]) + Number(vtemp[1])))) { return 0; }
    return Number(vtemp[0]) / (Number(vtemp[0]) + Number(vtemp[1]));
}

// input_urlの入力が終了する度に実行
value_url[0].addEventListener("change", function() {
    pagetoken = '';
    idx = 0;
    flg = false;

    var temp = value_url[0].value.split('https://youtube.com/playlist?list=');
    listid = temp[1];
    if (listid == undefined) {
        var temp = value_url[0].value.split('https://www.youtube.com/playlist?list=');
        listid = temp[1];
    }
    Get_Api_Data();
});

// input_titleの入力が終了する度に実行
value_title[0].addEventListener("change", function() {
    if (!value_title[0].value) {
        error2_text[0].textContent = "ERROR : タイトルが入力されていません。"
    } else {
        error2_text[0].textContent = ""
    }
});

// URLを生成する関数
function Generate_Api_URL(token, lid) {
    var url = 'https://www.googleapis.com/youtube/v3/playlistItems';
    url += '?part=snippet';
    url += '&maxResults=50';
    if (pagetoken !== '') {
        url += '&pageToken=' + token;
    }
    url += '&playlistId=' + lid;
    url += '&key=' + apikey;
    return url;
}

// データを取得する関数
function Get_Api_Data() {
    var getData = $.ajax({
        url: Generate_Api_URL(pagetoken, listid),
        dataType: 'json'
    }).done(function() {
        urlflg = true;
        error_text[0].textContent = "";
    }).fail(function() {
        urlflg = false;
        error_text[0].textContent = "ERROR : 入力形式が正しくありません。";
    });

    // データが取得出来たら
    getData.then(
        function(data) {
            pagetoken = data.nextPageToken;

            if (!flg) {
                results = [];
                flg = true;
            }

            // 取得したデータを整形して結果用の配列に入れる
            for (var i = 0; i < data.items.length; i++) {
                try {
                    results.push({
                        "title": data.items[i].snippet.title,
                        "id": data.items[i].snippet.resourceId.videoId,
                        "thmurl": data.items[i].snippet.thumbnails.medium.url
                    });
                } catch {
                    results.push({
                        "title": "非公開または限定公開の為表示できません",
                        "id": "NoID",
                        "thmurl": "img/errorthm.png"
                    });
                }
            }
            if (pagetoken !== undefined) {
                // 次のデータがある場合は再度データ取得を実行
                Get_Api_Data();
            } else {
                // データの取得が完了した場合はconsoleに結果表示
                (async() => {
                    var idData = await db.collection('PlayList_Data').get();
                    idexit = false;
                    error_text[0].textContent = "";
                    if (!ExitDataId(idData, listid)) {
                        idexit = true;
                        error_text[0].textContent = "ERROR : その再生リストは既に存在しています。";
                    }
                })()
            }
        },
        function(error) {
            console.log(error);
        }
    );
}

// 入力されたデータをFireBaseに送信
function PostValue() {
    post_but[0].disabled = true;
    if (!ErrorCheck()) {
        Tags_Array();
        // 入力されたデータを全てFirebaseへ送信
        (async() => {
            try {
                await db.collection('PlayList_Data').doc(listid).set({
                    title: value_title[0].value,
                    overview: value_ov[0].value,
                    tags: firebase.firestore.FieldValue.arrayUnion(...tags),
                    value: '0,0',
                    comment: firebase.firestore.FieldValue.arrayUnion(''),
                    pdata: firebase.firestore.FieldValue.arrayUnion(...results)
                })

                if (tags[0] != '') {
                    var TagsData = await db.collection('PlayList_Tags').get();
                    for (var i = 0; i < tags.length; i++) {
                        if (ExitDataTag(TagsData, i)) {
                            // Tagが存在していない
                            await db.collection('PlayList_Tags').doc(tags[i]).set({
                                listcount: 1,
                                listid: firebase.firestore.FieldValue.arrayUnion(listid)
                            })
                        } else {
                            // Tagが存在している
                            var ref = db.collection('PlayList_Tags').doc(tags[i]);
                            var doc = await ref.get();
                            var tagstemp = doc.get('listid');
                            tagstemp.push(listid);
                            await db.collection('PlayList_Tags').doc(tags[i]).set({
                                listcount: doc.get('listcount') + 1,
                                listid: firebase.firestore.FieldValue.arrayUnion(...tagstemp)
                            })
                        }
                    }
                }
                document.location.reload();
                alert("「" + value_title[0].value + "」\nが登録されました。");
            } catch (err) {
                console.log(`Error: ${JSON.stringify(err)}`);
            }
        })()
    } else {
        // Error:必須入力欄に正しく入力されていない処理
        post_but[0].disabled = false;
    }
}

// TagがFirebaseに存在するか否か
function ExitDataTag(Tags, i) {
    for (var s = 0; s < Tags.size; s++) { if (Tags.docs.map(postDoc => postDoc.id)[s] == tags[i]) { return false } }
    return true;
}

// IdがFirebaseに存在するか否か
function ExitDataId(ids, id) {
    for (var s = 0; s < ids.size; s++) { if (ids.docs.map(postDoc => postDoc.id)[s] == id) { return false } }
    return true;
}

//不正な値が入力されていないかをチェック
function ErrorCheck() {
    if (!urlflg) { return true }
    if (idexit) { return true }
    if (!value_title[0].value) { return true; }
    return false;
}

//タグを配列に入れる
function Tags_Array() {
    for (var i = 0; i < 5; i++) {
        if (document.getElementById('inputform_' + i) != null) {
            if (document.getElementById('inputform_' + i).value != undefined) {
                tags[i] = document.getElementById('inputform_' + i).value;
            }
        }
    }
}
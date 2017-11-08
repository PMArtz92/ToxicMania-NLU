const https = require('https');
const   _ = require('lodash');
const mysql = require('mysql');
const config = require('../config/conf');
const connection = mysql.createConnection(
    config.database
);




function getFireBaseIdOfSession(Se_Id) {
    console.log(Se_Id);
    let final_result  = [];
    var queryPromoise = new Promise(function(resolve, reject) {
        let sql = 'SELECT `Firebase_Id` FROM `Session_user` WHERE `Se_Id` = ?;';
        connection.query(sql,[Se_Id],function(err, rows, fields) {
            if (err) {
                reject(err);
            }else{
                _.forEach(rows,(value) => {
                    final_result.push(value.Firebase_Id);
                });

                resolve(final_result);
            }
        });
    });
    return queryPromoise;
}

module.exports.triggerFireBase = function(Se_Id,body){
    const options = {
        hostname: 'fcm.googleapis.com',
        port: 443,
        path: '/fcm/send',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'key=AAAAlqNA37o:APA91bGdY8EmZQn4WJx6jidvZB6YnVtApaBlOPtvov8IOVy9zydo0giCgZdMvaWunNU5vMdfokCDg64VgIzmlIAL61n09iD_WYJwA0QP-jKiwT41rEcgoODL0hJKi3HFknX1ceVDJo96'
        }
    };

    let main  ={};

    let firebaseIds  = getFireBaseIdOfSession(Se_Id);

    firebaseIds.then(function(results){
        console.log(results);
        //var temp = {};
        //temp.body = body;

        //main.notification = temp;
        main.registration_ids = results;
        main.data =  body;

        main.data.appID = "com.toxicmania.toxicmania";

        const req = https.request(options, (res) => {
            let data = [];
            res.on('data', (chunk) => {
                data.push(chunk);
            }).on('end', () => {
                console.log(data);
            });
        });
        req.on('error', (e) => {
            console.error(e);
        });

        console.log(main);
        req.write(JSON.stringify(main));
        req.end();

    }).catch(function(err) {
        console.log("Failed:", err);
    });
}



// {
//     "notification":{
//     "body":{
//         "event":"start",
//             "status":"gameStarted",
//             "playerCount":"3"
//     }
// },
//     "data":{
//     "id":3
// },
//     "registration_ids":["fvt-Rf4UB2A:APA91bE_eABJ8DeVl0l3zGIGd3JkWd0IU_OPDxNTACeuYX8qspR8Jkkb5L9QwuGDSRoFlnenfIROKQdTJ7n7FchWx99L3-k3nmb8InuHJ0Mj1RSkd2AG4Xk-4dvrQ2vME96bUxn8B90_"]
// }












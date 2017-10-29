var https = require('https');
const   _ = require('lodash');
const wtf = require("wtf_wikipedia");
const mysql = require('mysql');
const shortid = require('shortid');
const config = require('../config/conf');

const firebaseHelper = require('./firebase_helperfunction');


var connection = mysql.createConnection(
    config.database
);


module.exports.getGameSummary = function(req, res, next) {
  let Se_Id = req.query.Se_Id;
  console.log(Se_Id);
  let sql = 'SELECT `Ready`,`Summary` FROM `Session` WHERE `Se_Id` = ?;';
  connection.query(sql,[Se_Id],function(err, rows, fields) {
    if (err) return next(err);

    try {
        let ready = rows[0].Ready;
        let Summary = rows[0].Summary;
        if (ready == 1){
          res.send({"success":true,"ready":true,"summary":JSON.parse(Summary)});
        }else{
          res.send({"success":true,"ready":false});
        }
    }
    catch(er){
      return next(er);
    }
    console.log(rows);
  });
};

module.exports.getisgameStart = function(req, res, next) {
  var Se_Id = req.query.Se_Id;
  console.log(Se_Id);
  var sql = 'SELECT `Start` FROM `Session` WHERE `Se_Id` = ?;';
  connection.query(sql,[Se_Id],function(err, rows, fields) {
    if (err) return next(err);

    try {
        var status = rows[0].Start;
        res.send({"success":true,"start":status});
    }
    catch(er){
      return next(er);
    }

    console.log(rows);
  });
};

// module.exports.getisgameStart = function(req, res, next) {
//   var Se_Id = req.query.Se_Id;
//   console.log(Se_Id);
//   var sql = 'SELECT `Start` FROM `Session` WHERE `Se_Id` = ?;';
//   connection.query(sql,[Se_Id],function(err, rows, fields) {
//     if (err) return next(err);
//
//     try {
//         var status = rows[0].Start;
//         res.send({"success":true,"start":status});
//     }
//     catch(er){
//       return next(er);
//     }
//
//     console.log(rows);
//   });
// };

// "{"U_Id":"1459","U_Name":"Pasan ","U_Img":"pasan.jpg"}"
module.exports.postConnect = function(req, res, next) {
  var Se_Id = req.body.Se_Id;

  var U_Obj = req.body.U_Obj;
  var U_Id = U_Obj.U_Id;
  var U_Name = U_Obj.U_Name;
  var U_Img = U_Obj.U_Img;
  var Firebase_Id = U_Obj.Firebase_Id;


  var sql = 'SELECT `Player_Count` FROM `Session` WHERE `Se_Id` = ?';
  connection.query(sql,[Se_Id],function(err, rows, fields) {
    if (err) return next(err);

    try{
        var Player_Count = rows[0].Player_Count;
        if (Player_Count >= 10){
            res.send({"success":false,"msg":"user limt exceed"});
        }else{
            var sql2 = 'INSERT IGNORE INTO `Session_user` (`Se_Id`, `U_Id`, `U_Name`, `U_Img`, `Mark`, `Firebase_Id`) VALUES (?, ?, ?, ?,?,?);';
            connection.query(sql2,[Se_Id,U_Id,U_Name,U_Img,0,Firebase_Id],function(err, rows, fields) {
                console.log(rows.affectedRows);
                if (err) return next(err);
                if (rows.affectedRows > 0){
                    var sql3 = 'UPDATE`Session` SET `Player_Count` = `Player_Count` + 1 WHERE `Se_Id` = ?;';
                    connection.query(sql3,[Se_Id],function(err, rows, fields) {
                        if (err) return next(err);

                        // need firebase trigger
                        let body  = {};
                        body.event = 'start';
                        body.status = 'ready';
                        body.session_Id = Se_Id;
                        body.playerCount = Player_Count + 1;
                        firebaseHelper.triggerFireBase(Se_Id,body);
                        res.send({"success":true});
                    });
                }else{
                    res.send({"success":true});
                }
            });
        }

    } catch(er){
        res.send({"success":false,"msg":"invalid game id"});
    }
  });
};

module.exports.postNewGame = function(req, res, next) {
  // var U_id = req.body.U_Id;
  var Se_Id = shortid.generate();

  var U_Obj = req.body.U_Obj;
  var U_Id = U_Obj.U_Id;
  var U_Name = U_Obj.U_Name;
  var U_Img = U_Obj.U_Img;
  var Firebase_Id = U_Obj.Firebase_Id;


  var all_subPromise = [];
  var time = 150; // 30 * 5s
  const timeConstant = 0.2;

  var new_question_query = new Promise(function(resolve, reject) {
    var sql = 'SELECT `Q_Id`,`Text`,`Length` FROM `question` WHERE `Length` > 3  ORDER BY `Used_Count`,`Length` LIMIT 20';
    connection.query(sql,function(err, rows, fields) {
      if(!err) {
          resolve(rows);
      } else {
          reject();
      }
    });
  });

  var known_question_query = new Promise(function(resolve, reject) {
    var sql2 = 'SELECT `Q_Id`,`Text`,`Length` FROM `known_questions` WHERE `Length` > 3 ORDER BY `Used_Count`,`Length` LIMIT 10';
    connection.query(sql2,function(err, rows, fields) {
      if(!err) {
          resolve(rows);
      } else {
          reject();
      }
    });
  });


  Promise.all([new_question_query, known_question_query])
      .then(function (results) {
          var new_question = results[0];
          var known_question = results[1];

          _.forEach(known_question, (value) => {
            time += value.Length * timeConstant ;
            var sql = 'INSERT INTO `Session_questions` (`Se_Id`, `Q_Id`, `Text`) VALUES (?, ?, ?);';
            var subPromise = new Promise(function (resolve, reject) {
              connection.query(sql,[Se_Id,value.Q_Id,value.Text], function(err, rows, fields) {
                if (err) reject(err); // need to put transaction
                // console.log('The result is: ', rows);
                resolve(rows);
                });
              });
              all_subPromise.push(subPromise);
            });

          _.forEach(new_question, (value2) => {
            time += value2.Length * timeConstant ;
            var subPromise2 = new Promise(function (resolve, reject) {
              var sql2 = 'INSERT INTO `Session_questions` (`Se_Id`, `Q_Id`, `Text`) VALUES (?, ?, ?);';
              connection.query(sql2,[Se_Id,value2.Q_Id,value2.Text], function(err, rows, fields) {
                if (err) reject(err); // need to put transaction
                // console.log('The result is: ', rows);
                resolve(rows);
              });
            });
            all_subPromise.push(subPromise2);
          });
          /////////////////
          // console.log("!@#!@#");
          // console.log(all_subPromise);
          Promise.all(all_subPromise)
              .then(function (results) {
                console.log(all_subPromise);
                time = time * 1000; // convert to miliseconds
                // INSERT INTO `Session` (`Se_Id`, `Player_Count`, `Time`, `Ready`, `Summary`, `Owner`) VALUES ('123', '0', '123', '0', '', '213213213');
                var sql2 = 'INSERT INTO `Session` (`Se_Id`, `Player_Count`,`Respond_Count`,`Time`, `Ready`, `Summary`, `Owner`) VALUES (?,?,?,?,?,?,?);';
                connection.query(sql2,[Se_Id,1,0,time,0,0,U_Id], function(err, rows, fields) {
                  if (err) next(err); // need to put transaction
                  console.log('The result is: ', rows);
                  // insert admin user to game
                  var sql2 = 'INSERT INTO `Session_user` (`Se_Id`, `U_Id`, `U_Name`, `U_Img`, `Mark`, `Firebase_Id`) VALUES (?, ?, ?, ?,?,?);';
                  connection.query(sql2,[Se_Id,U_Id,U_Name,U_Img,0,Firebase_Id],function(err, rows, fields) {
                    if (err) return next(err);
                    res.send({"success":true,"Se_id":Se_Id});
                  });
                });
              }).catch(function (err) {
                  console.log("Failed:", err);
                  return next(err);
              });

          /////////////////

        })
        .catch(function (err) {
            console.log("Failed:", err);
            return next(err);
        });

};

module.exports.postStartGame = function(req, res, next) {
  var Se_Id = req.body.Se_Id;
  var U_id = req.body.U_Id;
  var Check = req.body.Check;
  console.log(Se_Id);
  console.log(U_id);

  var sql = 'SELECT `Player_Count`,`Time` FROM `Session` WHERE `Se_Id` = ?';
  connection.query(sql,[Se_Id],function(err, rows, fields) {
    if (err) return next(err);
    try {
      var Player_Count = rows[0].Player_Count;  //Todo try catch
      if (Player_Count >= 2){
        if (Check == 1){
          // only check game can start
          res.send({"success":true,"status":1});
        }else{
          let sql = 'UPDATE `Session` SET `Start` = 1 WHERE `Se_Id` = ?';
          connection.query(sql,[Se_Id],function(err, rows, fields) {
            if (err) return next(err);
            gameCountDown(Se_Id);
            // need firebase trigger
              // need freebase trigger
              let body  = {};
              body.event = 'start';
              body.status = 'gameStarted';
              body.playerCount = Player_Count;
              body.session_Id = Se_Id;
              firebaseHelper.triggerFireBase(Se_Id,body);

            res.send({"success":true,"status":1});
            // start the game actually
            // need to start server side countdown
          });
        }
      }else{
        res.send({"success":true,"status":0});
      }  
    }
    catch(er){
      return next(er);
    }
  });
};


function gameCountDown(Se_Id,time){
  setTimeout(function(){
    console.log(Se_Id);
    console.log("Call get summary function after specific time");
    makeSummary(Se_Id);
  }, time);
}


// copy of this function have in answer - controller page
function makeSummary(Se_Id) {
  console.log(Se_Id);
  var sql = 'SELECT * FROM `Session_user` WHERE `Se_Id` = ?;';
  connection.query(sql,[Se_Id],function(err, rows, fields) {
    if (err) {
      console.log(err);
      return err;
    }
    let result = rows;
    result.sort(function(a, b) {
        var nameA = parseInt(a.Mark);
        var nameB = parseInt(b.Mark);
        if (nameA < nameB) {
          return 1;
        }
        if (nameA > nameB) {
          return -1;
        }
        return 0;
      });
    let sql = 'UPDATE `Session` SET `Ready`= ? , `Summary`  = ? WHERE `Se_Id` = ?;';
    connection.query(sql,[1,JSON.stringify(result),Se_Id],function(err, rows, fields) {
      if (err) {
        console.log(err);
        return err;
      }
      // need freebase trigger
      let body  = {};
      body.event = 'finish';
      body.players = JSON.stringify(result);
      body.session_Id = Se_Id;
      firebaseHelper.triggerFireBase(Se_Id,body);
      console.log("Complete");
    });
  });
}

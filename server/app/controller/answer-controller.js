const mysql = require('mysql');
const config = require('../config/conf');
const   _ = require('lodash');


var connection = mysql.createConnection(
    config.database
);

module.exports.postAnswer = function(req, res, next) {

  var U_Id = req.body.U_Id;
  var Q_Id = req.body.Q_Id;
  var Readable = req.body.Readable;
  var Toxicity = req.body.Toxicity;
  var Skipped = req.body.Skipped;
  var type = Q_Id.split('#A#-')[0];
  var sql  = '';
  if (type == 'A'){
    let sql = 'INSERT INTO `user_answer` (`U_Id`, `Q_Id`, `Readable`, `Toxicity`, `Posted`,`Skipped`) VALUES (?,?,?,?,?,?)';
  }else if (type == 'B') {
    let sql =  'INSERT INTO `user_known_answer` (`U_Id`, `Q_Id`, `Readable`, `Toxicity`, `Posted`,`Skipped`) VALUES (?,?,?,?,?,?)';
  }else{
    return next("Invalid Question Id");
  }
  connection.query(sql,[U_Id,Q_Id,Readable,Toxicity,0], function(err, rows, fields) {
    if (err) return next(err);
    res.send(rows);
    });
};


module.exports.postAnswerMock = function(req, res, next) {
  res.send(req.body);
};



module.exports.postAnswerMultiplay = function(req, res, next) {
    // u_id, answers, mult_session must come in the api call

    //var answers = JSON.parse(req.body.answers);
    //var mult_session = JSON.parse(req.body.mult_session);
    var answers = req.body.answers;
    var mult_session = req.body.mult_session;
    var u_id = req.body.u_id;
    var mode = 'multi';
    var promises = [];

    answers.questions.forEach(function (element) {
        var x = saveUserAnswer(u_id, element, mode);
        promises.push(x);
    });

    // Return true if exit condition is met
    let exit = checkExit(mult_session);
    if (exit == true){
        // Call chamath's method here
        makeSummary(mult_session);
    }

    Promise.all(promises)
        .then(function (results) {
            connection.beginTransaction(function (error) {
                if (error) {console.log(error);}

                // Return updated marks from session_user
                let sql = 'SELECT * FROM Session_user WHERE U_Id = ?';
                connection.query(sql,[u_id], function(err, rows, fields) {
                    if (err) {
                        return connection.rollback(function () {
                            console.log(err);
                        });
                    }
                    connection.commit(function (error) {
                        if (error){
                            return connection.rollback(function () {
                                console.log(error);
                            })
                        }
                    });
                    res.send({'result': rows});
                });
            });
        })
        .catch(function (err) {
            console.log("Failed:", err);
            return next(err);
        });
};


module.exports.postAnswerSingleplay = function(req, res, next) {

    // u_id, answers must come in the api call
    //console.log(req.body.answers);
    //console.log(req.body.u_id);
    //var answers = JSON.parse(req.body.answers);
    var answers = req.body.answers;
    var u_id = req.body.u_id;
    var mode = 'single';
    var promises = [];

    answers.forEach(function (element) {
        var x = saveUserAnswer(u_id, element, mode);
        promises.push(x);
    });

    Promise.all(promises)
        .then(function (results) {
            connection.beginTransaction(function (error) {
                if (error) {console.log(error);}

                // Return the updated marks results for the user
                let sql = 'SELECT * FROM user WHERE U_Id = ?';
                connection.query(sql,[u_id], function(err, rows, fields) {
                    if (err){
                        return connection.rollback(function () {
                            console.log(err);
                        })
                    }
                    connection.commit(function (error) {
                        if (error){
                            return connection.rollback(function () {
                                console.log(error);
                            })
                        }
                    });
                    res.send({'result': rows});
                });

            });
        })
        .catch(function (err) {
            console.log("Failed:", err);
            return next(err);
        });

};

function saveUserAnswer(u_id, ans, mode){
  // TODO:
  // save Users answer to relevent table : answer, knownanswer (based on Q_Id)
  // Update users marks accodingly.
  // Increment usage count of question

    var q_id = ans.id;
    var readability = ans.readability;
    var toxicity = ans.toxicity;
    var skipped = ans.skipped;
    var marks = 0;
    var weight = 10;

    var type = q_id.split('#A#-')[0];

    var saveAnswer = new Promise(function(resolve, reject) {
        if (type == 'A'){

            connection.beginTransaction(function (error) {
                if (error) {
                    reject();
                    console.log(error);
                }
                // Add new record to the user_answer table
                let sql = 'INSERT INTO `user_answer` (`U_Id`, `Q_Id`, `Readable`, `Toxicity`, `Posted`, `Skipped`) VALUES (?,?,?,?,?,?)';
                connection.query(sql,[u_id,q_id,readability,toxicity,0, skipped], function(err, rows, fields) {
                    if (err){
                        return connection.rollback(function () {
                            reject();
                            console.log(err);
                        })
                    }
                    else{
                        if (skipped == 1){
                            connection.commit(function (error) {
                                if (error){
                                    return connection.rollback(function () {
                                        reject();
                                        console.log(error);
                                    })
                                }
                                resolve('Complete');

                            });
                        }
                        else{
                            // Increment question counter at question table
                            let sql = 'UPDATE question SET Used_Count = Used_Count + 1 WHERE Q_Id = ?';

                            connection.query(sql,[q_id], function(err, rows, fields) {
                                if (err){
                                    return connection.rollback(function () {
                                        reject();
                                        console.log(err);
                                    })
                                }
                                connection.commit(function (error) {
                                    if (error){
                                        return connection.rollback(function () {
                                            reject();
                                            console.log(error);
                                        })
                                    }
                                    resolve('Complete');
                                });
                            });
                        }
                    }
                });

            });


        }else if (type == 'B') {

            connection.beginTransaction(function (error) {
                if (error) {
                    reject();
                    console.log(error);
                }

                // Add new record to the user_known_answer table
                let sql =  'INSERT INTO `user_known_answer` (`U_Id`, `Q_Id`, `Readable`, `Toxicity`, `Posted`, `Skipped`) VALUES (?,?,?,?,?,?)';
                connection.query(sql,[u_id,q_id,readability,toxicity,0, skipped], function(err, rows, fields) {
                    if (err){
                        return connection.rollback(function () {
                            reject();
                            console.log(err);
                        })
                    }
                    else {
                        if (skipped == 1){
                            connection.commit(function (error) {
                                if (error){
                                    return connection.rollback(function () {
                                        reject();
                                        console.log(error);
                                    })
                                }
                                resolve('Complete');
                            });
                        }
                        else{
                            // Increment question counter at known_questions table
                            let sql = 'UPDATE known_questions SET Used_Count = Used_Count + 1 WHERE Q_Id = ?';

                            connection.query(sql,[q_id], function(err, rows, fields) {
                                if (err){
                                    return connection.rollback(function () {
                                        reject();
                                        console.log(err);
                                    })
                                }
                                else {
                                    // Get Readable and Toxicity data from known_questions table
                                    let sql = 'SELECT Readable, Toxicity FROM known_questions WHERE Q_Id = ?';
                                    connection.query(sql,[q_id], function(err, rows, fields) {
                                        if (err){
                                            return connection.rollback(function () {
                                                reject();
                                                console.log(err);
                                            })
                                        }
                                        else {
                                            let readable = JSON.parse(rows[0].Readable);

                                            // Check if user has set readability to false in a readable answer
                                            if (readability == false){
                                                if (readable.yes == 1){
                                                    marks = marks - 0.5;
                                                }
                                            }

                                            else {
                                                let toxic = JSON.parse(rows[0].Toxicity);
                                                marks = marks + toxic[toxicity];
                                            }

                                            marks = marks * weight;

                                            if (mode == 'single'){

                                                // Update marks in the user table for single player games
                                                let sql = 'UPDATE user SET Mark = Mark + ? WHERE U_Id = ?';
                                                connection.query(sql,[marks, u_id], function(err, rows, fields) {
                                                    if (err){
                                                        return connection.rollback(function () {
                                                            reject();
                                                            console.log(err);
                                                        })
                                                    }
                                                    connection.commit(function (error) {
                                                        if (error){
                                                            return connection.rollback(function () {
                                                                reject();
                                                                console.log(error);
                                                            })
                                                        }
                                                        resolve('Complete');
                                                    });
                                                });
                                            }

                                            else{

                                                // Update marks in the session_user table for multiplayer games
                                                let sql = 'UPDATE Session_user SET Mark = Mark + ? WHERE U_Id = ?';
                                                connection.query(sql,[marks, u_id], function(err, rows, fields) {
                                                    if (err){
                                                        return connection.rollback(function () {
                                                            reject();
                                                            console.log(err);
                                                        })
                                                    }
                                                    connection.commit(function (error) {
                                                        if (error){
                                                            return connection.rollback(function () {
                                                                reject();
                                                                console.log(error);
                                                            })
                                                        }
                                                        resolve('Complete');
                                                    });
                                                });
                                            }


                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            });

        }
    });
    return saveAnswer;

}

function checkExit(session_id){

    // Increment respond_count in session table
    let sql = 'UPDATE Session SET Respond_Count = Respond_Count + 1 WHERE Se_Id = ?';
    connection.query(sql,[session_id], function(err, rows, fields) {
        if (err) {
            console.log(err);
        }
        else {
            console.log('count updated');
        }
    });

    let sql2 = 'SELECT Player_Count, Respond_Count FROM Session WHERE Se_Id = ?';
    connection.query(sql2,[session_id], function(err, rows, fields) {
        if (err) {
            console.log(err);
        }
        else {
            let p_count = rows[0].Player_Count;
            let r_count = rows[0].Respond_Count;

            if (p_count == r_count){
                return true;
            }
            else{
                return false;
            }
        }
    });
}


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
      console.log("Complete");
      // need firebase trigger
    });
  });
}



module.exports.leaderBoard = function(req, res, next) {
    // let U_Id = '108351526010011789853';
    let U_Id = req.query.u_id;
    let user_mark_query = new Promise(function(resolve, reject) {
        let sql1 = 'SELECT  * FROM `user`  ORDER BY `Mark` DESC;';
        connection.query(sql1,function(err, rows, fields) {
            if(!err) {
                resolve(rows);
            } else {
                reject();
            }
        });
    });
    let user_answer_query = new Promise(function(resolve, reject) {
        let sql2 = 'SELECT `U_Id`,COUNT(`Q_Id`) FROM `user_answer` GROUP BY `U_Id`;';
        connection.query(sql2,function(err, rows, fields) {
            if(!err) {
                resolve(rows);
            } else {
                reject();
            }
        });
    });
    let user_known_question_query = new Promise(function(resolve, reject) {
        let sql3 = 'SELECT `U_Id`,COUNT(`Q_Id`) FROM `user_known_answer` GROUP BY `U_Id`;';
        connection.query(sql3,function(err, rows, fields) {
            if(!err) {
                resolve(rows);
            } else {
                reject();
            }
        });
    });

    Promise.all([user_mark_query,user_answer_query,user_known_question_query]).then(function (results) {
        let user_mark = results[0];
        let user_answer_count = results[1];
        let user_known_answer_count = results[2];

        let final = [];
        let current_user = '';

        let user_answer_count_dist = {};
        let user_known_answer_count_dist = {};

        _.forEach(user_answer_count,(value) => {
            user_answer_count_dist[value.U_Id] = value['COUNT(`Q_Id`)'];
        });

        _.forEach(user_known_answer_count,(value) => {
            user_known_answer_count_dist[value.U_Id] = value['COUNT(`Q_Id`)'];
        });

        let final_count = 0;

        _.forEach(user_mark,(value) => {
            final_count += 1;
            if (final_count <= 10){
                let user_answer_count = user_answer_count_dist[value.U_Id];
                let user_known_answer_count = user_known_answer_count_dist[value.U_Id];
                if (!user_answer_count){
                    user_answer_count = 0;
                }
                if (!user_known_answer_count){
                    user_known_answer_count = 0;
                }
                let total_count  = user_answer_count + user_known_answer_count ;
                let temp = value;
                temp.question_count = total_count;
                temp.rank = final_count;
                final.push(temp);
            }

            if (value.U_Id === U_Id){
                let user_answer_count = user_answer_count_dist[value.U_Id];
                let user_known_answer_count = user_known_answer_count_dist[value.U_Id];
                if (!user_answer_count){
                    user_answer_count = 0;
                }
                if (!user_known_answer_count){
                    user_known_answer_count = 0;
                }
                let total_count  = user_answer_count + user_known_answer_count ;
                let temp = value;
                temp.question_count = total_count;
                temp.rank = final_count;
                current_user = temp;
                // console.log("final");
                // console.log(current_user);
            }

        });

        res.send({"success":true,"leader_board":final,"user":current_user});
    }).catch(function (err) {
        console.log("Failed:", err);
        return next(err);
    });

};
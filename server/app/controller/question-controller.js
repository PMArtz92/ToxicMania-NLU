var https = require('https');
const   _ = require('lodash');
const wtf = require("wtf_wikipedia");
const mysql = require('mysql');
const config = require('../config/conf');
//var connection = mysql.createConnection({
//  host     : 'localhost',
//  user     : 'root',
//  password : '',
//  database : 'toxicmania'
//});


var connection = mysql.createConnection(
    config.database
);

module.exports.getNextQuestion = function(req, res, next) {
  connection.query('SELECT * from admin_details', function(err, rows, fields) {
    if (!err){
      console.log('The solution is: ', rows);
      res.send(rows);
    }
    else
      console.log('Error while performing Query.');
      if (err) return next(err);
    });
};



module.exports.getSessionQuestions = function(req, res, next) {
  var Se_Id = req.query.Se_Id;
  console.log(Se_Id);
  var sql = 'SELECT * FROM `Session_questions` WHERE `Se_Id` = ?;';
  connection.query(sql,[Se_Id],function(err, rows, fields) {
    if (err) return next(err);
    console.log(rows);
    var result = rows;
    result.sort(function(a, b) {
        var nameA = a.Text.toUpperCase();
        var nameB = b.Text.toUpperCase();
        if (nameA < nameB) {
          return -1;
        }
        if (nameA > nameB) {
          return 1;
        }
        return 0;
      });
    var respond = {'result':result};
    res.send(respond);
  });

  // TODO:
  // req - Se_Id
  // select session question from session_question table
  // return {"result" : return above query result"};
  // res.send("getSessionQuestions");
};

// SELECT `Q_Id`,`Text` FROM `question` WHERE `Q_Id` NOT IN (SELECT `Q_Id` FROM `user_answer` WHERE `U_Id` = '123')    ORDER BY `length`,`Used_Count` LIMIT 10

module.exports.getNextTenQuestion = function(req, res, next) {
    // var U_Id = req.params.user;
    var U_Id = req.query.user;
    var Reputation = req.query.reputation;
    console.log(U_Id);
    console.log(Reputation);
    // var sql = 'SELECT `question`.`Q_Id`,`question`.`Text` FROM `question` LEFT JOIN user_answer ON `question`.`Q_Id`  = `user_answer`.`Q_Id` LEFT JOIN user_skip ON `question`.`Q_Id`  = `user_skip`.`Q_Id` WHERE user_answer.U_Id IS NULL AND user_skip.U_Id IS NULL AND `question`.`length` > 40 ORDER BY `question`.`length` , `question`.`Used_Count` LIMIT 20;';

    var new_question_query = new Promise(function(resolve, reject) {
      var sql = 'SELECT `Q_Id`,`Text` FROM `question` WHERE `Q_Id` NOT IN (SELECT `Q_Id` FROM `user_answer` WHERE `U_Id` = ?) AND `Length` > 3 ORDER BY `Used_Count`,`Length` LIMIT 50';
      connection.query(sql,[U_Id],function(err, rows, fields) {
        if(!err) {
            resolve(rows);
        } else {
            reject();
        }
        // if (err) return next(err);
        // // console.log('The solution is: ', rows);
        // var respond = {'result':rows};
        // // res.send(respond);
      });
    });

    var known_question_query = new Promise(function(resolve, reject) {
      var sql2 = 'SELECT `Q_Id`,`Text` FROM `known_questions` WHERE `Q_Id` NOT IN (SELECT `Q_Id` FROM `user_known_answer` WHERE `U_Id` = ?) AND `Length` > 3  ORDER BY `Used_Count`,`Length` LIMIT 30';
      connection.query(sql2,[U_Id],function(err, rows, fields) {
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
            var new_and_known_question = [];
            // console.log(new_question,known_question);
            var known_count = mixValue(Reputation);
            var new_count = 50 - known_count;
            if (known_count >= known_question.length){
              known_count = known_question.length;
            }
            if (new_count >= new_question.length){
              new_count = new_question.length;
            }

            new_and_known_question =  _.concat(
              _.slice(new_question,0,new_count),
              _.slice(known_question,0,known_count)
            );

            // console.log(known_question);

            new_and_known_question.sort(function(a, b) {
                var nameA = a.Text.toUpperCase();
                var nameB = b.Text.toUpperCase();
                if (nameA < nameB) {
                  return -1;
                }
                if (nameA > nameB) {
                  return 1;
                }
                return 0;
              });

            var respond = {'result':new_and_known_question};

            res.send(respond);
          })
          .catch(function (err) {
              console.log("Failed:", err);
              return next(err);
          });
};

function mixValue(reputation){
  if (reputation <= 100){
    return 20;
  }else if (reputation <= 150) {
    return 15;
  }else if (reputation <= 400) {
    return 12;
  }else if (reputation <= 1000) {
    return 10;
  }else if (reputation <= 1500) {
    return 7;
  }else{
    return 5;
  }
}

// // OK
// module.exports.postSkipQuestion = function(req, res, next) {
//     var Q_Id = req.body.Q_Id;
//     var U_Id = req.body.U_Id;
//
//     var sql = 'INSERT IGNORE INTO `user_skip` (`U_Id`, `Q_Id`) VALUES (?, ?);';
//     connection.query(sql,[U_Id,Q_Id], function(err, rows, fields) {
//       if (err) return next(err);
//       // console.log('The solution is: ',rows,fields);
//       res.send(rows);
//     });
// };


// OK
module.exports.feafetchQuestions = function() {
  console.log("feafetchQuestions");
    // https://crowd9api-dot-wikidetox.appspot.com/client_jobs/wp_v1_x10k_rmf60/to_answer_questions

  const options = {
    hostname: 'crowd9api-dot-wikidetox.appspot.com',
    port: 443,
    path: '/client_jobs/wp_v1_x10k_rmf60/to_answer_questions',
    method: 'GET'
  };
  const req = https.request(options, (res) => {
    // console.log('statusCode:', res.statusCode);
    // console.log('headers:', res.headers);
   var data = [];
   res.on('data', (chunk) => {
       data.push(chunk);
   }).on('end', () => {
       var buffer = Buffer.concat(data);
       var parsedBuffer = JSON.parse(buffer);
    // console.log(parsedBuffer);
       console.log("parseDone");

      _.forEach(parsedBuffer, (value) => {
           //console.log(value.question_id);
          //var temp  = JSON.parse(value.question);
          var plainText = wtf.plaintext(value.question.revision_text);
          //console.log(plainText);
          var Q_id  = "A#A#-" + value.question_id;
          var lengthOfplainText = plainText.length;
          // console.log(Q_id);
          // console.log(lengthOfplainText);
          // console.log(plainText);
          if (lengthOfplainText < 500){
              var sql = 'INSERT IGNORE INTO `question` (`Q_Id`, `Text`, `Used_Count`, `Length`) VALUES (?, ?, ?, ?);';
              connection.query(sql,[Q_id,plainText,0,lengthOfplainText], function(err, rows, fields) {
                  if (!err){
                      console.log('The solution is: ', rows);
                  }
                  else
                      console.log('Error while performing Query.');
                  // if (err) return next(err);
                  if (err) {
                      console.log(err);
                      return 0;
                  }

              });
          }
        });
   });
  });
  req.on('error', (e) => {
    console.error(e);
  });
  req.end();
};

//

// OK

module.exports.feafetchKnownQuestions = function() {
  console.log("feafetchQuestions");
  // const options = {
  //   hostname: 'crowd9api-dot-wikidetox.appspot.com',
  //   port: 443,
  //   path: '/client_jobs/wp_x2000_rmf60/training_questions',
  //   method: 'GET'
  // };
  //   https://crowd9api-dot-wikidetox.appspot.com/client_jobs/wp_v1_x10k_rmf60/training_questions
    const options = {
      hostname: 'crowd9api-dot-wikidetox.appspot.com',
      port: 443,
      path: '/client_jobs/wp_v1_x10k_rmf60/training_questions',
      method: 'GET'
    };

  const req = https.request(options, (res) => {
    // console.log('statusCode:', res.statusCode);
    // console.log('headers:', res.headers);
   var data = [];
   res.on('data', (chunk) => {
       data.push(chunk);
   }).on('end', () => {
       var buffer = Buffer.concat(data);
       var parsedBuffer = JSON.parse(buffer);
      _.forEach(parsedBuffer, (value) => {
          // console.log(value.question_id);
          // console.log(value);
          //var temp  = JSON.parse(value.question);
          var plainText = wtf.plaintext(value.question.revision_text);
          var Q_id  = "B#A#-" + value.question_id;
          var lengthOfplainText = plainText.length;
          // console.log(lengthOfplainText);
          //var acceptedAnswers = JSON.parse(value.question.accepted_answers);
          var acceptedAnswers = value.accepted_answers;
          //console.log(acceptedAnswers);
          var readableAndInEnglish = JSON.stringify(acceptedAnswers.readableAndInEnglish.enum) ;
          var toxicity = JSON.stringify(acceptedAnswers.toxic.enum);

          // console.log(acceptedAnswers);
          // console.log(acceptedAnswers.readableAndInEnglish.enum);
          // console.log(acceptedAnswers.toxicity.enum);
// INSERT INTO `known_questions` (`Q_Id`, `Text`, `Readable`, `Toxicity`, `Used_Count`) VALUES (?, ?, ?, ?, ?);
          if (lengthOfplainText < 500){
              var sql = 'INSERT IGNORE INTO `known_questions` (`Q_Id`, `Text`, `Readable`, `Toxicity`, `Used_Count`,`Length`) VALUES (?, ?, ?, ?, ?,?);';
              connection.query(sql,[Q_id,plainText,readableAndInEnglish,toxicity,0,lengthOfplainText], function(err, rows, fields) {
                  if (!err){
                      console.log('The solution is: ', rows);
                  }
                  else
                      console.log('Error while performing Query.');
              });
          }
        });
   });
  });
  req.on('error', (e) => {
    console.error(e);
  });
  req.end();
};


//

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

module.exports.getUser = function(req, res, next) {
    var U_Id = req.query.U_Id;
    console.log(req.query);
    console.log(U_Id);
    var sql = "SELECT * FROM `user` WHERE `U_Id` = ?";
    connection.query(sql,[U_Id],function(err, rows, fields) {
      if (err) return next(err);
      try {
        res.send({"result":rows[0]});
      }
      catch(er){
        return next(er);
      }
    });
};


module.exports.postnewUser = function(req, res, next) {
    // var U_Obj = JSON.parse(req.body.U_Obj);
    let U_Obj = req.body.U_Obj;
    let U_Id = U_Obj.U_Id;
    let U_Name = U_Obj.U_Name;
    let U_Img = U_Obj.U_Img;
    let U_Email = U_Obj.U_Email ;

    // INSERT INTO `user` (`U_Id`, `Email`, `Weight`, `Level`, `Mark`, `User_Name`, `U_Img`) VALUES (?,?,?,?,?,?,?);
    let sql = "INSERT IGNORE INTO `user` (`U_Id`, `Email`, `Weight`, `Level`, `Mark`, `User_Name`, `U_Img`) VALUES (?,?,?,?,?,?,?);";
    connection.query(sql,[U_Id,U_Email,0,0,0,U_Name,U_Img],function(err, rows, fields) {
      if (err) return next(err);
        let sql = "SELECT * FROM `user` WHERE `U_Id` = ?";
        connection.query(sql,[U_Id],function(err, rows, fields) {
            if (err) return next(err);
            try {
                console.log("_____");
                res.send({"success":true,"result":rows[0]});
            }
            catch(er){
                return next(er);
            }
        });
    });
};

module.exports.updateUser = function(req, res, next) {
    console.log(req.body);
    let U_Id = req.body.U_Id;
    let Weight = req.body.Weight;
    let Level = req.body.Level;
    let Mark = req.body.Mark;
    let User_Name = req.body.U_Name;
    let sql = "UPDATE `user` SET `Weight` = ? , `Level` = ?, `Mark` = ?, `User_Name` = ? WHERE `user`.`U_Id` = ?;";
  connection.query(sql,[Weight,Level,Mark,User_Name,U_Id],function(err, rows, fields) {
    if (err) return next(err);
    res.send(rows);
  });
};


// module.exports.signUp = function(req, res, next) {
//     var U_Id = req.params.query;
//     var sql = "SELECT * FROM `user` WHERE `U_Id` = ?";
//     connection.query(sql,[U_Id],function(err, rows, fields) {
//       if (err) return next(err);
//       res.send(rows);
//     });
// };

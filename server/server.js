var express = require('express'),
    path = require('path'),
    bodyParser = require('body-parser'),
    answer = require('./app/api/answer-api'),
    question = require('./app/api/question-api'),
    multiplay = require('./app/api/multiplay-api.js'),
    questionController = require('./app/controller/question-controller.js'),
    user = require('./app/api/user-api'),
    app = express(),
    mysql = require('mysql'),
    jwt = require('express-jwt'),
    config = require('./app/config/conf'),
    morgan = require('morgan'),
    fs = require('fs'),
    port = 3001,
    passport = require('passport'),
    https = require('https');

//////
//
//

var connection = mysql.createConnection(
    config.database
);


var dbConnect = new Promise(function(resolve, reject) {
     connection.connect(function(err){
     if(!err) {
         console.log("Database is connected ... nn");
         resolve();
     } else {
         console.log("Error connecting database ... nn");
         reject();
     }
   });
});


// call backend function after database connected
dbConnect.then(function(results){
     // questionController.feafetchQuestions();
     // questionController.feafetchKnownQuestions();
}).catch(function(err) {
    console.log("Failed:", err);
});

////



// create a write stream (in append mode)
var accessLogStream = fs.createWriteStream(path.join(__dirname, 'access.log'), {
    flags: 'a'
});

// setup the logger
app.use(morgan('common', {
    stream: accessLogStream
}));

// logger stdout
app.use(morgan('dev'));



//Set Static Folder
app.use(express.static(path.join(__dirname, '../client')));

// app.use('/assets', express.static('../client/assets'));
// app.use('/uploads', express.static('assets/uploads'));
require('./app/config/passport')(passport); // pass passport for configuration

//Cookie and session
var cookieParser = require('cookie-parser');
var session = require('express-session');
app.use(session({
    secret: config.secret,
    resave: false,
    saveUninitialized: true,
}));

app.use(cookieParser());
app.use(passport.initialize());
app.use(passport.session());


//Body-parser
app.use(bodyParser.json({
    extended: true,
    parameterLimit: 10000,
    limit: 1024 * 1024 * 10
}));
//for parsing application/json
app.use(bodyParser.urlencoded({
    extended: true,
    parameterLimit: 10000,
    limit: 1024 * 1024 * 10
}));

// routes ======================================================================
// Load our routes and pass in our app and fully configured passport
require('./app/api/auth.js')(app, passport);

// app.use('/schedule', schedule);

app.use('/answer', answer);
app.use('/question', question);
app.use('/user', user);
app.use('/multiplay', multiplay);


app.get("/",function(req,res){
connection.query('SELECT * from admin_details', function(err, rows, fields) {
// connection.end();
  if (!err){
    console.log('The solution is: ', rows);
    res.send(rows);
  }
  else
    console.log('Error while performing Query.');
  });
});



// error handlers
// Catch unauthorised errors
app.use(function (err, req, res, next) {
    if (err.name === 'UnauthorizedError') {
        res.status(401);
        res.json({
            success: false,
            msg:err.message,
            error: err
        });
        console.log("Log - Unauthorized Error");
    } else {
        console.log("Log - Unhandled");
        console.log("message" + err.name + ": " + err.message);
        res.json({
            success: false,
            msg:err.message,
            error: err
        });
    }
});

app.listen(port, function () {
    console.log('Server started on port : ' + port);
});



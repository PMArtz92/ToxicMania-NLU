var User = require('../model/user');
var jwt = require('jsonwebtoken');
var config = require('../config/conf');

var jwt = require('express-jwt');
var config = require('../config/conf');
var auth = jwt({
    secret: config.secret,
    userProperty: 'payload'
});


module.exports = function (app, passport) {
    // process the login form  -  //OK
    app.post('/login', function (req, res, next) {
        console.log("auth.login");
        User.findOne({
            username: req.body.username
        }, function (err, user) {
            if (err) return next(err);
            if (!user) {
                // res.status(401).send({success: false, msg: 'Authentication failed. User not found.'});
                res.json({
                    success: false,
                    msg: 'Authentication failed. User not found.'
                });
            } else {
                // check if password matches
                user.comparePassword(req.body.password, function (err, isMatch) {
                    if (err) return next(err);
                    if (isMatch && !err) {
                        // if user is found and password is right create a token
                        // var token = jwt.sign(user, config.secret);
                        var token = user.generateJwt();
                        // return the information including token as JSON
                        req.login(user, function (err) {
                            if (err) return next(err);
                            res.json({
                                success: true,
                                token: 'Bearer ' + token
                            });
                        });
                        // res.json({success: true, token: 'JWT ' + token ,id:user.id, path: './views/student'});
                    } else {
                        // res.status(401).send({success: false, msg: 'Authentication failed. Wrong password.'});
                        res.json({
                            success: false,
                            msg: 'Authentication failed.'
                        });
                    }
                });
            }
        });
    });

    // handle logout - Todo
    app.post("/logout", function (req, res, next) {
        req.logOut();
        res.send({
            success: true,
            msg: 'Loged out.',
            path: '../../index.html'
        });
    });


    // remove send
    app.get("/loggedin", function (req, res, next) {
        if (req.isAuthenticated()) {
            res.send({
                success: true,
                msg: req.user._id
            });
        } else {
            // res.status(401).send({success: false, msg: 'user not loged.'});
            res.send({
                success: false,
                msg: 'user not logged.'
            });
        }
    });

    // OK
    app.post('/signup', auth ,function (req, res, next) {
        if (!req.body.username || !req.body.password || !req.body.usertype) {
            res.json({
                success: false,
                msg: 'Please provide your username and password.'
            });
        } else {
            //Check if username exists
            User.findOne({username: req.body.username}, function (err, user) {
                if (err) throw err;
                if (!user) {
                    var newUser = new User({
                        username: req.body.username,
                        password: req.body.password,
                        usertype: req.body.usertype,
                        isfirst: 1
                    });
                    // save the user
                    newUser.save(function (err, user1) {
                        if (err) throw err;
                        res.json({
                            success: true,
                            msg: 'Successfully created new user.',
                            data: user1
                        });
                    });

                } else {
                    return res.json({
                        success: false,
                        msg: 'Username already exists.',
                        data: {username: req.body.username}
                    });
                }
            });
        }
    });

    //ok
    app.post('/changepass',auth ,function (req, res, next) {
        if (!req.body.username || !req.body.oldpassword || !req.body.password) {
            res.json({
                success: false,
                msg: 'Please provide your username, old password & new password.'
            });
        } else {
            User.findOne({username: req.body.username}, function (err, user) {
                if (err) throw err;
                if (!user) {
                    res.json({
                        success: false,
                        msg: 'Authentication failed. User not found.'
                    });

                } else {
                    // check if password matches
                    user.comparePassword(req.body.oldpassword, function (err, isMatch) {
                        if (isMatch && !err) {
                            user.password = req.body.password;
                            user.save(function (err) {
                                if (err) {
                                    return res.json({
                                        success: false,
                                        msg: 'Something went wrong. Please try again'
                                    });
                                }
                                res.json({
                                    success: true,
                                    msg: 'Password updated successfully'
                                });
                            });
                        } else {
                            // res.status(401).send({success: false, msg: 'Authentication failed. Wrong password.'});
                            res.json({
                                success: false,
                                msg: 'Authentication failed. Wrong password.'
                            });
                        }
                    });
                }
            });
        }
    });

};

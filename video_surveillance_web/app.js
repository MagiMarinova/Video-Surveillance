var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var session = require('express-session');
var logger = require('morgan');
var mysql = require('mysql');
var mustacheExpress = require('mustache-express');

var app = express();

// app.use(session)

var con = mysql.createConnection({
    host : 'localhost',
    user : 'pi',
    password : 'raspberry',
    database : 'login'
});

con.connect();
// view engine setup
app.engine('html', mustacheExpress());
app.set('view engine', 'html');
app.set('views',path.join(__dirname, 'views'));


app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());


app.use(session({
    key: 'user_sid',
    secret: 'somerandonstuffs',
    resave: false,
    saveUninitialized: false,
    cookie: {
        expires: 600000
    }
}));


app.route('/login')
    .post(function(req, res){
        console.log(req.cookies);
        var loginQuery = 'select * from users where username = \'' + req.body.username +
            '\' and password = \'' + req.body.password + '\';';
        con.query(loginQuery, function (error, result) {
            console.log(JSON.stringify(result));
            if (JSON.stringify(result) == "[]") {
                res.redirect('/');
            } else {
                var cookie = req.cookies.user;
                if (cookie === undefined) {
                    var randomNumber=Math.random().toString();
                    randomNumber=randomNumber.substring(2,randomNumber.length);
                    res.cookie('user',randomNumber, { maxAge: 4200000, httpOnly: true });
                }
                res.redirect('/stream');
            }
        });
});

app.route('/androidLogin')
    .post(function(req, res){
        var loginQuery = 'select * from users where username = \'' + req.body.username +
            '\' and password = \'' + req.body.password + '\';';
        con.query(loginQuery, function (error, result) {
            if (error) throw error;
            console.log(result);
	    if (JSON.stringify(result) != "[]") {
                res.send({
                    isSuccessful: true
                });
            } else {
                res.send({
                    isSuccessful: false
                });
            }
        });
    });

app.get('/stream', (req,res) => {
    if(req.cookies.user)
        res.render('stream');
    else
        res.redirect('/');
});

app.get('/', (req,res) => {
    res.render('index');
});

app.use(function(req, res, next) {
  next(createError(404));
});

module.exports = app;

console.log("Node is running");
